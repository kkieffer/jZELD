
package com.github.kkieffer.jzeld.element;

import static com.github.kkieffer.jzeld.element.ZShape.MIN_SHAPE_DIMENSION;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A ZLine is an object that defines line that has color and thickness. The line's
 * top left corer is set within the bounds box such the top left corner of the rectangle is at the object's position.  If rotated,
 * the rotation occurs about the center of the line.
 * 
 * If the line's width is negative, the the line is drawn to the bounds of the panel.
 * 
 * The ZLine does not support a fill color.
 * 
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZLine")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZLine extends ZPolygon {

  
    protected ZLine() {}

    /**
     * Create a ZLine
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the line in units, or -1 for unlimited width
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove if the object can be moved by mouse drag
     * @param lineThickness unit width of the border, use zero for no border
     * @param lineColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern
     */
    public ZLine(double x, double y, double width, double rotation, boolean canSelect, boolean canResize, boolean canMove, float lineThickness, Color lineColor, Float[] dashPattern, StrokeStyle borderStyle) {
        super(x, y, width, MIN_SHAPE_DIMENSION, rotation, canSelect, canResize, canMove, lineThickness, lineColor, dashPattern, null, borderStyle);                     

        if (lineColor == null)
            throw new IllegalArgumentException("Line color cannot be null");
        
        if (lineThickness <= 0)
            throw new IllegalArgumentException("Line thickness must be positive");
  
    }
    
    public ZLine(ZLine copy, boolean forNew) {
        super(copy, forNew);
    }
    
    @Override
    public ZLine copyOf(boolean forNew) {
        return new ZLine(this, forNew);
    }
    
    @Override
    protected String getShapeSummary() {       
        return "A straight line.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "";     
    }

    
    @Override
    public boolean hasFill() {
        return false;
    }
   
    
    @Override
    public boolean supportsFlip() {
        return false;
    }
    
    @Override
    public boolean supportsEdit() {
        return false;
    };

    

    @Override
    protected Shape getPolygon(double width, double height, double scale) {
        Rectangle2D bounds2D = getBounds2D(scale);
        return new Line2D.Double(0, bounds2D.getHeight()/2, bounds2D.getWidth(), bounds2D.getHeight()/2);
    }

      
}
