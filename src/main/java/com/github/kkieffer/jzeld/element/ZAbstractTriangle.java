
package com.github.kkieffer.jzeld.element;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A ZAbstractTriangle is a right or isoceles triangle with a border (that has color and thickness) and an interior color.  The 
 * right triangle's top left corner is set within the bounds box such the top left corner of the triangle is at the object's position.  If rotated,
 * the rotation occurs about the center of the triangle.  The isoceles triangle has the point with the non-unique angle at the top center.
 * 
 * If the triangle's height or width is negative, the the rectangle is drawn to the width or height of the panel.
 * 
 * The triangle's border is drawn inside the bounds of the rectangle, no matter how thick.
 * 
 * A subclass could override this by providing a custom paint method for different triangle types.  The Type can then be NULL.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZAbstractTriangle")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ZAbstractTriangle extends ZRectangle {

    public enum Type {RIGHT, ISOCELES}
    
    protected Type type;
    
    protected ZAbstractTriangle() {}
    
    protected ZAbstractTriangle(Type t, double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, int borderWidth, Color borderColor, Float[] dashPattern, Color fillColor) {
        super(x, y, width, height, rotation, canSelect, canResize, borderWidth, borderColor, dashPattern, fillColor);
        type = t;
    }
    
    protected ZAbstractTriangle(ZAbstractTriangle copy) {
        super(copy);
        this.type = copy.type;
    }
    
    
    @Override
    public boolean supportsFlip() {
        return true;
    }
    
    @Override
    public void paint(Graphics2D g, int unitSize, int width, int height) {

        if (type == null)
            throw new RuntimeException("Custom triangles must override paint");

        
        int x = 0;
        int x2 = width;
        if (flipHoriz) {
            x = width;
            x2 = 0;                  
        }
        
        int y=0;
        int y2=height;
        if (flipVert) {
            y = height;
            y2 = 0;
        }
        
        int xc;  
        if (type == Type.ISOCELES)
            xc = (int)Math.round((double)width/2.0);
        else 
            xc = x;  //for right triangle

        
        Polygon triangle = new Polygon(new int[]{xc, x, x2}, new int[]{y, y2, y2}, 3);
        
       if (backgroundColor != null) {
            g.setColor(backgroundColor);
            g.fill(triangle);
       }

       if (borderThickness != 0) {
            g.setColor(borderColor);
            g.setStroke(new BasicStroke(borderThickness)); 
            g.draw(triangle);
       }
       
    }
    

    
}
