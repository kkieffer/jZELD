
package com.github.kkieffer.jzeld.element;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A ZCircle is an object that defines a circular shape with a border (that has color and thickness) and an interior color.  The ZCircle is a 
 * subset of a ZOval, with constant radius about the center.  The radius is equal to the smaller of the width or height.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZCircle")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZCircle extends ZOval {

    protected ZCircle() {}
    
    /**
     * Create a ZCircle
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the object in units, or -1 for unlimited width
     * @param height the height of the object in units, or -1 for unlimited height
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove if the object can be moved 
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     */
    public ZCircle(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor);
    }
    
    public ZCircle(ZCircle copy, boolean forNew) {
        super(copy, forNew);
    }
    
    private static double radius(double w, double h) {
        return w < h ? w : h;
    }
    
    @Override
    public ZCircle copyOf(boolean forNew) {
        return new ZCircle(this, forNew);
    }
    
    @Override
    protected String getShapeSummary() {       
        return "An circle.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "";     
    }
    
    @Override
    protected Shape getPolygon(double width, double height) {
        double r = radius(width, height);
        return new Ellipse2D.Double(0, 0, r, r);
    }
    
    @Override
    protected Shape getAbstractShape() {
        Rectangle2D b = getBounds2D();
        double r = radius(b.getWidth(), b.getHeight());
        return new Ellipse2D.Double(0, 0, r, r);
    }
 
    
}
