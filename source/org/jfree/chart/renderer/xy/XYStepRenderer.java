/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -------------------
 * XYStepRenderer.java
 * -------------------
 * (C) Copyright 2002-2008, by Roger Studner and Contributors.
 *
 * Original Author:  Roger Studner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Matthias Rose;
 *                   Gerald Struck (fix for bug 1569094);
 *                   Ulrich Voigt (patch 1874890);
 *                   Martin Hoeller (contribution to patch 1874890);
 *
 * Changes
 * -------
 * 13-May-2002 : Version 1, contributed by Roger Studner (DG);
 * 25-Jun-2002 : Updated import statements (DG);
 * 22-Jul-2002 : Added check for null data items (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem() method signature (DG);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 28-Oct-2003 : Added tooltips, code contributed by Matthias Rose 
 *               (RFE 824857) (DG);
 * 10-Feb-2004 : Removed working line (use line from state object instead) (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState.  Renamed 
 *               XYToolTipGenerator --> XYItemLabelGenerator (DG);
 * 19-Jan-2005 : Now accesses only primitives from dataset (DG);
 * 15-Mar-2005 : Fix silly bug in drawItem() method (DG);
 * 19-Sep-2005 : Extend XYLineAndShapeRenderer (fixes legend shapes), added 
 *               support for series visibility, and use getDefaultEntityRadius()
 *               for entity hotspot size (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 15-Jun-2006 : Added basic support for item labels (DG);
 * 11-Oct-2006 : Fixed rendering with horizontal orientation (see bug 1569094),
 *               thanks to Gerald Struck (DG);
 * 06-Feb-2007 : Fixed bug 1086307, crosshairs with multiple axes (DG);
 * 14-Feb-2008 : Applied patch 1874890 by Ulrich Voigt (with contribution from
 *               Martin Hoeller) (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

/**
 * Line/Step item renderer for an {@link XYPlot}.  This class draws lines 
 * between data points, only allowing horizontal or vertical lines (steps).
 */
public class XYStepRenderer extends XYLineAndShapeRenderer 
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -8918141928884796108L;

    /** 
     * The factor (from 0.0 to 1.0) that determines the position of the 
     * step.  
     *
     * @since 1.0.10. 
     */
    private double stepPoint = 1.0d;

    /**
     * Constructs a new renderer with no tooltip or URL generation.
     */
    public XYStepRenderer() {
        this(null, null);
    }

    /**
     * Constructs a new renderer with the specified tool tip and URL 
     * generators.
     *
     * @param toolTipGenerator  the item label generator (<code>null</code> 
     *     permitted).
     * @param urlGenerator  the URL generator (<code>null</code> permitted).
     */
    public XYStepRenderer(XYToolTipGenerator toolTipGenerator,
                          XYURLGenerator urlGenerator) {
        super();
        setBaseToolTipGenerator(toolTipGenerator);
        setURLGenerator(urlGenerator);
        setShapesVisible(false);
    }

    /**
     * Returns the fraction of the domain position between two points on which
     * the step is drawn.  The default is 1.0d, which means the step is drawn 
     * at the domain position of the second`point. If the stepPoint is 0.5d the
     * step is drawn at half between the two points.
     * 
     * @return The fraction of the domain position between two points where the
     *         step is drawn.
     *         
     * @see #setStepPoint(double)
     * 
     * @since 1.0.10
     */
    public double getStepPoint() {
        return this.stepPoint;
    }
    
    /**
     * Sets the step point and sends a {@link RendererChangeEvent} to all 
     * registered listeners.
     * 
     * @param stepPoint  the step point (in the range 0.0 to 1.0)
     * 
     * @see #getStepPoint()
     * 
     * @since 1.0.10
     */
    public void setStepPoint(double stepPoint) {
        if (stepPoint < 0.0d || stepPoint > 1.0d) {
            throw new IllegalArgumentException(
            		"Requires stepPoint in [0.0;1.0]");
        }
        this.stepPoint = stepPoint;
        fireChangeEvent();
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color 
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the vertical axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot 
     *                        (<code>null</code> permitted).
     * @param pass  the pass index (ignored here).
     */
    public void drawItem(Graphics2D g2, 
                         XYItemRendererState state,
                         Rectangle2D dataArea, 
                         PlotRenderingInfo info,
                         XYPlot plot, 
                         ValueAxis domainAxis, 
                         ValueAxis rangeAxis,
                         XYDataset dataset, 
                         int series, 
                         int item,
                         CrosshairState crosshairState, 
                         int pass) {

        // do nothing if item is not visible
        if (!getItemVisible(series, item)) {
            return;   
        }

        PlotOrientation orientation = plot.getOrientation();
        
        Paint seriesPaint = getItemPaint(series, item);
        Stroke seriesStroke = getItemStroke(series, item);
        g2.setPaint(seriesPaint);
        g2.setStroke(seriesStroke);

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = (Double.isNaN(y1) ? Double.NaN 
        		: rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation));

        if (item > 0) {
            // get the previous data point...
            double x0 = dataset.getXValue(series, item - 1);
            double y0 = dataset.getYValue(series, item - 1);
            double transX0 = domainAxis.valueToJava2D(x0, dataArea, 
            		xAxisLocation);
            double transY0 = (Double.isNaN(y0) ? Double.NaN 
            		: rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation));

            if (orientation == PlotOrientation.HORIZONTAL) {
                if (transY0 == transY1) { 
                	// this represents the situation
                    // for drawing a horizontal bar.
                    drawLine(g2, state.workingLine, transY0, transX0, transY1, 
                    		transX1);
                }
                else {  //this handles the need to perform a 'step'.

                    // calculate the step point
                    double transXs = transX0 + (getStepPoint() 
                    		* (transX1 - transX0));
                    drawLine(g2, state.workingLine, transY0, transX0, transY0, 
                    		transXs);
                    drawLine(g2, state.workingLine, transY0, transXs, transY1, 
                    		transXs);
                    drawLine(g2, state.workingLine, transY1, transXs, transY1, 
                    		transX1);
                }
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                if (transY0 == transY1) { // this represents the situation 
                                          // for drawing a horizontal bar.
                    drawLine(g2, state.workingLine, transX0, transY0, transX1, 
                    		transY1);
                }
                else {  //this handles the need to perform a 'step'.
                    // calculate the step point
                    double transXs = transX0 + (getStepPoint() 
                    		* (transX1 - transX0));
                    drawLine(g2, state.workingLine, transX0, transY0, transXs, 
                    		transY0);
                    drawLine(g2, state.workingLine, transXs, transY0, transXs, 
                    		transY1);
                    drawLine(g2, state.workingLine, transXs, transY1, transX1, 
                    		transY1);
                }
            }

        }

        // draw the item label if there is one...
        if (isItemLabelVisible(series, item)) {
            double xx = transX1;
            double yy = transY1;
            if (orientation == PlotOrientation.HORIZONTAL) {
                xx = transY1;
                yy = transX1;
            }          
            drawItemLabel(g2, orientation, dataset, series, item, xx, yy, 
                    (y1 < 0.0));
        }

        int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
        int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
        updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex, 
                rangeAxisIndex, transX1, transY1, orientation);
        
        // collect entity and tool tip information...
        if (state.getInfo() != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                int r = getDefaultEntityRadius();
                Shape shape = orientation == PlotOrientation.VERTICAL
                    ? new Rectangle2D.Double(transX1 - r, transY1 - r, 2 * r, 
                            2 * r)
                    : new Rectangle2D.Double(transY1 - r, transX1 - r, 2 * r, 
                            2 * r);           
                if (shape != null) {
                    String tip = null;
                    XYToolTipGenerator generator 
                        = getToolTipGenerator(series, item);
                    if (generator != null) {
                        tip = generator.generateToolTip(dataset, series, item);
                    }
                    String url = null;
                    if (getURLGenerator() != null) {
                        url = getURLGenerator().generateURL(dataset, series, 
                                item);
                    }
                    XYItemEntity entity = new XYItemEntity(shape, dataset, 
                            series, item, tip, url);
                    entities.add(entity);
                }
            }
        }
    }

    /**
     * A utility method that draws a line but only if none of the coordinates
     * are NaN values.
     * 
     * @param g2  the graphics target.
     * @param line  the line object.
     * @param x0  the x-coordinate for the starting point of the line.
     * @param y0  the y-coordinate for the starting point of the line.
     * @param x1  the x-coordinate for the ending point of the line.
     * @param y1  the y-coordinate for the ending point of the line.
     */
    private void drawLine(Graphics2D g2, Line2D line, double x0, double y0, 
    		double x1, double y1) {
        if (Double.isNaN(x0) || Double.isNaN(x1) || Double.isNaN(y0) 
        		|| Double.isNaN(y1)) {
            return;
        }
        line.setLine(x0, y0, x1, y1);
        g2.draw(line);
    }    
    
    /**
     * Tests this renderer for equality with an arbitrary object.
     * 
     * @param obj  the object (<code>null</code> permitted).
     * 
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYLineAndShapeRenderer)) {
            return false;
        }		
        XYStepRenderer that = (XYStepRenderer) obj;
        if (this.stepPoint != that.stepPoint) {
        	return false;
        }
		return super.equals(obj);
	}
  
    /**
     * Returns a hash code for this instance.
     * 
     * @return A hash code.
     */
    public int hashCode() {
        return HashUtilities.hashCode(super.hashCode(), this.stepPoint);
    }

    /**
     * Returns a clone of the renderer.
     * 
     * @return A clone.
     * 
     * @throws CloneNotSupportedException  if the renderer cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
