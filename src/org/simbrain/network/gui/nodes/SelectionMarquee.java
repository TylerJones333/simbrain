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
package org.simbrain.network.gui.nodes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

import org.simbrain.util.SimbrainUtils;
import org.simbrain.util.StrokeUtils;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * Selection marquee node.
 */
public final class SelectionMarquee
    extends PPath {

    /** Default paint. */
    private static final Paint DEFAULT_PAINT = Color.WHITE;

    /** Default stroke. */
    private static final Stroke DEFAULT_STROKE = SimbrainUtils.isMacOSX() ? new BasicStroke(1.0f) : new PFixedWidthStroke(1.0f);

    /** Color of selection marquee. */
    private static Color marqueeColor = Color.yellow;

    /** Default interior transparency. */
    private static final float DEFAULT_TRANSPARENCY = 0.6f;


    /**
     * Create a new selection marquee at the specified point
     * (<code>x</code>, <code>y</code>).
     *
     * @param x x
     * @param y y
     */
    public SelectionMarquee(final float x, final float y) {
        super();

        setPathToRectangle(x, y, 0.0f, 0.0f);

        setPaint(DEFAULT_PAINT);
        setStroke(DEFAULT_STROKE);
        setStrokePaint(marqueeColor);
        setTransparency(DEFAULT_TRANSPARENCY);
    }


    /** {@inheritDoc} */
    protected void paint(final PPaintContext paintContext) {
        Paint p = getPaint();
        Stroke stroke = getStroke();
        Paint strokePaint = getStrokePaint();
        GeneralPath path = getPathReference();
        Graphics2D g2 = paintContext.getGraphics();

        if (p != null) {
            g2.setPaint(p);
            g2.fill(path);
        }

        if (stroke != null && strokePaint != null) {
            g2.setPaint(strokePaint);
            g2.setStroke(StrokeUtils.prepareStroke(stroke, paintContext));
            g2.draw(path);
        }
    }

    /**
     * @return Returns the marqueeColor.
     */
    public static Color getMarqueeColor() {
        return marqueeColor;
    }


    /**
     * @param marqueeColor The marqueeColor to set.
     */
    public static void setMarqueeColor(final Color marqueeColor) {
        SelectionMarquee.marqueeColor = marqueeColor;
    }
}