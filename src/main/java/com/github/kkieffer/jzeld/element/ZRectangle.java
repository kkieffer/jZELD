
package com.github.kkieffer.jzeld.element;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A ZRectangle is an object that defines a rectilinear shape with a border (that has color and thickness) and an interior color.  The 
 * rectangle's top left corner is set within the bounds box such the top left corner of the rectangle is at the object's position.  If rotated,
 * the rotation occurs about the center of the rectangle.
 * 
 * If the rectangle's height or width is negative, the the rectangle is drawn to the width or height of the panel.
 * 
 * The rectangle's border is drawn centered on the bounds of the rectangle, no matter how thick.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZRectangle")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZRectangle extends ZAbstractShape {

    
    
    protected ZRectangle() {}
    
    /**
     * Create a ZRectangle
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the object in units, or -1 for unlimited width
     * @param height the height of the object in units, or -1 for unlimited height
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove if the object can be moved by the mouse drag
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     */
    public ZRectangle(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor);        
    }
    
    protected ZRectangle(ZRectangle src) {
        super(src);
    }
    
    @Override
    public boolean supportsFlip() {
        return false;
    }

    
    @Override
    public ZRectangle copyOf() {
        return new ZRectangle(this);
    }
    
    
    @Override
    protected Shape getAbstractShape() {
        Rectangle2D r = getBounds2D();
        return new Rectangle2D.Double(0, 0, r.getWidth(), r.getHeight());
    }
    
    @Override
    protected void fillShape(Graphics2D g, int unitSize, int width, int height) {
        g.fillRect(0, 0, width, height);
    }
    
    @Override
    protected void drawShape(Graphics2D g, int unitSize, int width, int height) {
        g.drawRect(0, 0, width, height);
    }
    

}
