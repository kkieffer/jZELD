
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.draw.BoundaryDraw;
import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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
 * The triangle's border is drawn centered on the bounds of the rectangle, no matter how thick.
 * 
 * A subclass could override this by providing a custom paint method for different triangle types.  The Type can then be NULL.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZAbstractTriangle")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ZAbstractTriangle extends ZPolygon {

    public enum TriType {RIGHT, ISOCELES}
    
    protected TriType type;
       
    protected ZAbstractTriangle() {}
    
    protected ZAbstractTriangle(TriType t, double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor);
        type = t;
    }
    
    protected ZAbstractTriangle(ZAbstractTriangle copy, boolean forNew) {
        super(copy, forNew);
        this.type = copy.type;
    }
    
    
    @Override
    protected String getShapeSummary() {       
        return "A 3-sided polygon.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "";     
    }
    
    
    @Override
    public boolean supportsFlip() {
        return true;
    }
    

    
    @Override
    protected Path2D getPath2D(double width, double height) {
           
        if (type == null)
            throw new RuntimeException("Custom triangles must override getPath2D()");

        ArrayList<Point2D> points = new ArrayList<>(3);
        
        double x = 0;
        double x2 = width;
        if (flipHoriz) {
            x = width;
            x2 = 0;                  
        }
        
        double y=0;
        double y2=height;
        if (flipVert) {
            y = height;
            y2 = 0;
        }
        
        double xc;  
        if (type == TriType.ISOCELES)
            xc = width/2.0;
        else 
            xc = x;  //for right triangle

        points.add(new Point2D.Double(xc, y));
        points.add(new Point2D.Double(x, y2));
        points.add(new Point2D.Double(x2, y2));
        
        
        return BoundaryDraw.pathFromPoints(points, true);
    }
    
 
}
