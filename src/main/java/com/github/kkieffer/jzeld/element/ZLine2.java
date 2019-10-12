
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
 * This is a revision of ZLine.
 * 
 * In the ZLine2 class the line is positioned at 0,0 to width, 0  (top edge) and is not sizable in height. This 
 * provides better referencing as the position coordinates are always aligned with the end of the line and scaling the line only scales in
 * the width dimension.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZLine2")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZLine2 extends ZLine {

    
    protected ZLine2() {}

    /**
     * Create a ZLine2
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
    public ZLine2(double x, double y, double width, double rotation, boolean canSelect, boolean canResize, boolean canMove, float lineThickness, Color lineColor, Float[] dashPattern, ZElement.StrokeStyle borderStyle) {
        super(x, y, width, rotation, canSelect, canResize, canMove, lineThickness, lineColor, dashPattern, borderStyle);    
    }
    
    public ZLine2(ZLine2 copy, boolean forNew) {
        super(copy, forNew);
    }
    
    @Override
    public ZLine2 copyOf(boolean forNew) {
        return new ZLine2(this, forNew);
    }
    
   

    @Override
    protected void setSize(double w, double h, double minSize, double scale) {
        super.setSize(w, MIN_SHAPE_DIMENSION*scale, minSize, scale);  //override any changes in height
    }
   

    @Override
    public boolean selectAsShape() {  //allow select outside of shape
        return false;
    }
    

    @Override
    protected Shape getPolygon(double width, double height, double scale) {
        Rectangle2D bounds2D = getBounds2D(scale);
        
        return new Line2D.Double(0, 0, bounds2D.getWidth(), 0);
        
    }

      
}
