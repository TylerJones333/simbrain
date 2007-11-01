package org.simbrain.world.gameworld2d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import org.simbrain.workspace.Consumer;
import org.simbrain.workspace.Coupling;
import org.simbrain.workspace.Producer;
import org.simbrain.workspace.gui.DesktopComponent;

public class GameWorld2DDesktopComponent extends DesktopComponent<GameWorld2DComponent> {

    private static final long serialVersionUID = 1L;
    
    /** Reference to the wrapped game world object. */
    private GameWorld2D world;

    /**
     * Construct a new world panel.  Set up the toolbars.  Create an  instance of a world object.
     * @param ws the workspace associated with this frame
     */
    public GameWorld2DDesktopComponent(GameWorld2DComponent component) {
        super(component);
        this.setLayout(new BorderLayout());
        world = new GameWorld2D();
        world.initEngineApplet(30,30,10,10,null,null,null);
        world.setPreferredSize(new Dimension(450,400));
        getContentPane().add("Center", world);
    }

    @Override
    public void postAddInit() {
        world.init();
    }

    /**
     * @return Returns the world.
     */
    public GameWorld2D getWorld() {
        return world;
    }

    /**
     * @param world The world to set.
     */
    public void setWorld(GameWorld2D world) {
        this.world = world;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public String getFileExtension() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void save(File saveFile) {
        // TODO Auto-generated method stub
        
    }

    public List<Consumer> getConsumers() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Coupling> getCouplings() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Producer> getProducers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void open(File openFile) {
        // TODO Auto-generated method stub
    }


    @Override
    public void updateComponent() {
        world.stop();
        world.player.snapToGrid();
        world.start();
    }

//
//    public CouplingContainer getCouplingContainer() {
//        return this;
//    }


}