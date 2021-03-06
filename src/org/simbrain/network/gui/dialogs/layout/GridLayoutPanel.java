/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
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
package org.simbrain.network.gui.dialogs.layout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.simbrain.network.layouts.GridLayout;

/**
 * <b>GridLayoutPanel</b> allows the user to define the layout of a network.
 */
public class GridLayoutPanel extends AbstractLayoutPanel {

    /** Spacing field. */
    private JTextField tfNumColumns = new JTextField();

    /** Spacing field. */
    private JTextField tfHSpacing = new JTextField();

    /** Vertical spacing field. */
    private JTextField tfVSpacing = new JTextField();

    /** Manual spacing field. */
    private JCheckBox manuallySetNumColumns = new JCheckBox();

    /**
     * Default constructor.
     * @param layout
     */
    public GridLayoutPanel(GridLayout layout) {
        this.layout = layout;
        fillFieldValues();
        this.addItem("Horizontal Spacing:", tfHSpacing);
        this.addItem("Vertical Spacing:", tfVSpacing);
        this.addItem("Manual Columns:", manuallySetNumColumns);
        this.addItem("Number of columns:", tfNumColumns);
        manuallySetNumColumns.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                enableDisableSpacingFields();
            }
        });
    }

    /**
     * Enable or disable spacing fields depending on whether the layout is in
     * manual columns mode.
     */
    private void enableDisableSpacingFields() {
        if (manuallySetNumColumns.isSelected()) {
            tfNumColumns.setEnabled(true);
        } else {
            tfNumColumns.setEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitChanges() {
        ((GridLayout) layout).setNumColumns(Integer.parseInt(tfNumColumns
                .getText()));
        ((GridLayout) layout).setHSpacing(Double.parseDouble(tfHSpacing
                .getText()));
        ((GridLayout) layout).setVSpacing(Double.parseDouble(tfVSpacing
                .getText()));
        ((GridLayout) layout).setManualColumns(manuallySetNumColumns
                .isSelected());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fillFieldValues() {
        tfNumColumns.setText(Integer.toString(((GridLayout) layout)
                .getNumColumns()));
        tfHSpacing
                .setText(Double.toString(((GridLayout) layout).getHSpacing()));
        tfVSpacing
                .setText(Double.toString(((GridLayout) layout).getVSpacing()));
        manuallySetNumColumns.setSelected(((GridLayout) layout)
                .isManualColumns());
        enableDisableSpacingFields();
    }

}
