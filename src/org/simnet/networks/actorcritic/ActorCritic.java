/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005 Jeff Yoshimi <www.jeffyoshimi.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.simnet.networks.actorcritic;

import org.simnet.connections.AllToAll;
import org.simnet.interfaces.Network;
import org.simnet.interfaces.Neuron;
import org.simnet.interfaces.RootNetwork;
import org.simnet.interfaces.Synapse;
import org.simnet.layouts.Layout;
import org.simnet.networks.Backprop;
import org.simnet.networks.StandardNetwork;
import org.simnet.neurons.ClampedNeuron;
import org.simnet.neurons.LinearNeuron;
import org.simnet.synapses.SimpleSynapse;
import org.simnet.util.ConnectNets;

/**
 * <b>ActorCritic</b>. Implements Temporal Difference learning. This network
 * consists of two components - an 'Adaptive Critic' that learns to predict the
 * goodness of various states of the world and an 'Actor' that learns to take
 * actions that lead the network towards rewarding states.
 */
public class ActorCritic extends Network {
    /** Number of state neuron. */
    private int stateUnits = 2;

    /** Number of possible actions. */
    private int actorUnits = 2;

    /** Flag indicating whether the network should be trained or not. */
    private boolean train = true;

    /** Flag to indicate if the absorbing reward condition is true or not. */
    private boolean absorbReward = true;

    /** Simbrain representation of state. */
    private StandardNetwork state = null;

    /** Simbrain representation of actions. */
    private StandardNetwork actions = null;

    /** Simbrain representation of critic. */
    private StandardNetwork critic = null;

    /** Buffers to hold the last activation state of the network. */
    private double[] lastState = null;

    /** Buffers to hold the last activation state of the network. */
    private double[] lastActions = null;

    /** Buffers to hold the last activation state of the network. */
    private double[] lastCritic = null;

    /** Actor learning rate. */
    private double actorLearningRate = 1;

    /** Critic learning rate. */
    private double criticLearningRate = 1;

    /** Reward discount factor. */
    private double gamma = 1;

    /** Exploration policy. */
    ExplorationPolicy explorationPolicy = new RandomExplorationPolicy();

    /**
     * Default constructor.
     */
    public ActorCritic() {
        super();
    }

    /**
     * Creates a new actor-critic network.
     *
     * @param stateUnits
     *            Number of state neurons
     * @param actorUnits
     *            Number of actor neurons
     * @param layout
     *            the way to layout the network
     */
    public ActorCritic(final RootNetwork root, final int stateUnits,
            final int actorUnits, final Layout layout) {
        super();
        setRootNetwork(root);
        setParentNetwork(root);

        this.stateUnits = stateUnits;
        this.actorUnits = actorUnits;
        init();
        createNeurons();
        layout.layoutNeurons(this);
        createConnections();
    }

    /**
     * Create layers
     */
    private void init() {
        this.lastState = new double[stateUnits];
        this.lastActions = new double[actorUnits];
        this.lastCritic = new double[2];
        for(int i=0;i<stateUnits;i++)
            this.lastState[i] = 0;
        for(int i=0;i<actorUnits;i++)
            this.lastActions[i] = 0;
        this.lastCritic[0] = 0;
        this.lastCritic[1] = 0;
    }

    /**
     *  Create neurons 
    */
    private void createNeurons() {

        state = new StandardNetwork(this.getRootNetwork());
        actions = new StandardNetwork(this.getRootNetwork());
        critic = new StandardNetwork(this.getRootNetwork());
        state.setParentNetwork(this);
        actions.setParentNetwork(this);
        critic.setParentNetwork(this);

	// add neurons to the layers
        for (int i = 0; i < this.stateUnits; i++) {
            this.state.addNeuron(new LinearNeuron());
        }
        for (int i = 0; i < this.actorUnits; i++) {
            this.actions.addNeuron(new LinearNeuron());
        }
        for (int i = 0; i < 2; i++) {
            this.critic.addNeuron(new LinearNeuron());
        }
        addNetwork(state);
        addNetwork(actions);
        addNetwork(critic);
    }

    /**
     * Create connections
     */
    private void createConnections() {
        if (stateUnits == 0) {
            return;
        }
        // create the connections between states and critic
        for (Neuron s : state.getFlatNeuronList()) {
            SimpleSynapse w = new SimpleSynapse(s, critic.getNeuron(0));
            w.setLowerBound(10);
            w.setUpperBound(-10);
            w.setStrength(0);
            this.addWeight(w);
        }

        // create the connections between states and actions
        AllToAll connector = new AllToAll(this, state.getFlatNeuronList(),
        actions.getFlatNeuronList());
        connector.connectNeurons();

        for (int i = 0; i < getFlatSynapseList().size(); i++) {
            ((Synapse) getFlatSynapseList().get(i)).setUpperBound(10);
            ((Synapse) getFlatSynapseList().get(i)).setLowerBound(-10);
            ((Synapse) getFlatSynapseList().get(i)).randomize();
        }

        for (int i = 0; i < getFlatNeuronList().size(); i++) {
            ((Neuron) getFlatNeuronList().get(i)).setUpperBound(1);
            ((Neuron) getFlatNeuronList().get(i)).setLowerBound(0);
            ((Neuron) getFlatNeuronList().get(i)).setIncrement(1);
        }

    }

    /**
     * Randomize the network.
     */
    public void randomize() {
        if (this.getNetworkList().size() == 0) {
            return;
        }

        for (int i = 0; i < getFlatSynapseList().size(); i++) {
            ((Synapse) getFlatSynapseList().get(i)).randomize();
        }
    }

    /**
     * Update network.
     */
    public void update() {
        // First update all the state neurons
        for (int i = 0; i < state.getNeuronCount(); i++) {
            this.lastState[i] = state.getNeuron(i).getActivation();
            state.getNeuron(i).update();
            state.getNeuron(i).setActivation(state.getNeuron(i).getBuffer());
        }
        // now find the action
        double[] a = new double[actions.getNeuronCount()];
        for (int i = 0; i < actions.getNeuronCount(); i++) {
            this.lastActions[i] = actions.getNeuron(i).getActivation();
            actions.getNeuron(i).update();
            a[i] = actions.getNeuron(i).getBuffer();
        }
        this.explorationPolicy.selectAction(a);
        for (int i = 0; i < actions.getNeuronCount(); i++) {
            actions.getNeuron(i).setActivation(a[i]);
        }
        // now the critic
        for (int i = 0; i < critic.getNeuronCount(); i++) {
            this.lastCritic[i] = critic.getNeuron(i).getActivation();
            critic.getNeuron(i).update();
            critic.getNeuron(i).setActivation(critic.getNeuron(i).getBuffer());
        }
        if (this.train) {
            updateWeights();
        }

    }

    /**
     * Update the network weights
     */
    private void updateWeights() {
        double delta = this.gamma * this.critic.getNeuron(0).getActivation()
                + this.lastCritic[1] - this.lastCritic[0];
        
        if(delta < 0){
            System.out.print("negative delta");
        }
        int i;
        // update critic weights
        for (i = 0; i < this.stateUnits; i++) {
            this.getWeight(this.state.getNeuron(i), this.critic.getNeuron(0)).setStrength(
                    this.getWeight(this.state.getNeuron(i), this.critic.getNeuron(0)).getStrength() + this.criticLearningRate
                            * this.lastState[i] * delta);
            this.getWeight(i).checkBounds();
        }
        // update actor weights
        for (int k = 0; k < this.stateUnits; k++, i++) {
            for (int j = 0; j < this.actorUnits; j++) {
                this.getWeight(this.state.getNeuron(k), this.actions.getNeuron(j)).setStrength(
                        this.getWeight(this.state.getNeuron(k), this.actions.getNeuron(j)).getStrength()
                                + this.actorLearningRate * this.lastState[k]
                                * delta * this.lastActions[j]);
            }
            this.getWeight(i).checkBounds();
        }
    }

    /**
     * Reset the network: resets all activations, absorbs any pending rewards
     */
    public void reset() {
        for (int i = 0; i < state.getNeuronCount(); i++) {
            state.getNeuron(i).setInputValue(0);
        }
        critic.getNeuron(1).setInputValue(0);
        update();
    }

    /**
     * Used by duplicate().
     */
    public void duplicateLayers() {
        state = (StandardNetwork) this.getNetwork(0);
        actions = (StandardNetwork) this.getNetwork(1);
        critic = (StandardNetwork) this.getNetwork(2);
    }

    /**
     * Duplicate the network.
     */
    public Network duplicate() {
        ActorCritic ac = new ActorCritic();
        ac = (ActorCritic) super.duplicate(ac);
        ac.setAbsorbReward(this.isAbsorbReward());
        ac.setActorLearningRate(this.getActorLearningRate());
        ac.setCriticLearningRate(this.getCriticLearningRate());
        ac.setExplorationPolicy(this.getExplorationPolicy());
        ac.setRewardDiscountFactor(this.getRewardDiscountFactor());
        ac.setActorUnits(this.getActorUnits());
        ac.setStateUnits(this.getStateUnits());
        ac.duplicateLayers();
        return ac;

    }

    /**
     * Perform intialization required after opening saved networks.
     */
    public void initCastor() {
        super.initCastor();
        init();
        state = (StandardNetwork) this.getNetworkList().get(0);
        actions = (StandardNetwork) this.getNetworkList().get(1);
        critic = (StandardNetwork) this.getNetworkList().get(2);
        state.setParentNetwork(this);
        actions.setParentNetwork(this);
        critic.setParentNetwork(this);        
    }

    public int getCurrentAction() {
        for (int i = 0; i < this.actorUnits; i++) {
            if (this.actions.getNeuron(i).getActivation() > 0)
                return i;
        }
        return -1;
    }

    /**
     * @return the actorLearningRate
     */
    public double getActorLearningRate() {
        return actorLearningRate;
    }

    /**
     * @param actorLearningRate
     *            to set
     */
    public void setActorLearningRate(final double actorLearningRate) {
        this.actorLearningRate = actorLearningRate;
    }

    /**
     * @return the criticLearningRate
     */
    public double getCriticLearningRate() {
        return criticLearningRate;
    }

    /**
     * @param criticLearningRate
     *            to set
     */
    public void setCriticLearningRate(final double criticLearningRate) {
        this.criticLearningRate = criticLearningRate;
    }

    /**
     * @return the reward discount factor
     */
    public double getRewardDiscountFactor() {
        return gamma;
    }

    /**
     * @param gamma
     *            the reward discount factor to set
     */
    public void setRewardDiscountFactor(final double rewardDiscountFactor) {
        this.gamma = rewardDiscountFactor;
    }

    /**
     * @return the number of stateUnits
     */
    public int getStateUnits() {
        return stateUnits;
    }

    /**
     * @param stateUnits
     *            to set
     */
    public void setStateUnits(final int stateUnits) {
        this.stateUnits = stateUnits;
    }

    /**
     * @return the number of actorUnits
     */
    public int getActorUnits() {
        return actorUnits;
    }

    /**
     * @param actorUnits
     *            to set
     */
    public void setActorUnits(final int actorUnits) {
        this.actorUnits = actorUnits;
    }

    /**
     * @return true if network is being trained
     */
    public boolean isTrain() {
        return train;
    }

    /**
     * @param train
     *            to set
     */
    public void setTrain(final boolean train) {
        this.train = train;
    }

    /**
     * @return true if absorbing reward conditiong is set
     */
    public boolean isAbsorbReward() {
        return absorbReward;
    }

    /**
     * @param absorbReward
     *            to set
     */
    public void setAbsorbReward(final boolean absorbReward) {
        this.absorbReward = absorbReward;
    }

    public ExplorationPolicy getExplorationPolicy() {
        return explorationPolicy;
    }

    public void setExplorationPolicy(final ExplorationPolicy explorationPolicy) {
        this.explorationPolicy = explorationPolicy;
    }

}