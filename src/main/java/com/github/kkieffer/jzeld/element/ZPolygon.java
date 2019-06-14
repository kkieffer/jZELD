package com.github.kkieffer.jzeld.element;


import com.github.kkieffer.jzeld.adapters.ShapeAdapter;
import com.github.kkieffer.jzeld.attributes.Clippable;
import static com.github.kkieffer.jzeld.element.ZShape.setClip;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A ZPolygon is a abstract closed polygon
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZPolygon")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ZPolygon extends ZAbstractShape implements Clippable {
    
    transient private Shape shape;
    transient protected Shape scaledClip;
    
    @XmlJavaTypeAdapter(ShapeAdapter.class)
    private Shape clippingShape = null; 

    
    protected ZPolygon(){}
    

    public ZPolygon(ZPolygon src, boolean forNew) {
        super(src, forNew);
         if (src.clippingShape != null) {
            this.clippingShape = ShapeAdapter.copyOf(src.clippingShape);
        }
    }
    
    public ZPolygon(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, StrokeStyle borderStyle) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor, borderStyle);
    }
    
    @Override
    public boolean supportsFlip() {
        return true;
    }
    
     @Override
    protected void setSize(double w, double h, double minSize, double scale) {
        
        if (w <= 0)
            w = minSize;  //don't go to zero
        if (h <= 0)
            h = minSize; //don't go to zero
        
        Rectangle2D bounds = this.getBounds2D(scale);
        double scaleX = w / bounds.getWidth(); 
        double scaleY = h / bounds.getHeight(); 
  
        //Scale the clip
        if (clippingShape != null) {
            AffineTransform scaleInstance = AffineTransform.getScaleInstance(scaleX, scaleY);
            clippingShape = scaleInstance.createTransformedShape(clippingShape);
        }
            
        super.setSize(w, h, minSize, scale);
    }
    
    
    @Override
    public boolean hasClip() {
        return clippingShape != null;
    }
    
    /**
     * Returns the clipping shape for this shape. The clipping shape has been offset to this shape's origin
     * @return 
     */
    @Override
    public Shape getClippingShape() {
        return clippingShape;
    }
    
        
    /**
     * Set the clipping shape for this shape. The clip shape must be in canvas units, but in absolute position on the canvas
     * @param s the clip shape to apply, null to remove clip
     */
    @Override
    public void setClippingShape(Shape s) {
        if (s != null) {
            Rectangle2D b = this.getBounds2D();
            AffineTransform a = AffineTransform.getTranslateInstance(-b.getX(), -b.getY());  //translate to this shape's origin
            clippingShape = a.createTransformedShape(s);
        }
        else
            clippingShape = null;
    }
    
    @Override
    protected void fillShape(Graphics2D g, double unitSize, double width, double height) {
        Shape origClip = setClip(g, scaledClip);
        g.fill(shape);
        g.setClip(origClip);
    }
    
    @Override
    protected void drawShape(Graphics2D g, double unitSize, double width, double height) {
        Shape origClip = setClip(g, scaledClip);
        g.draw(shape);
        g.setClip(origClip);
    }
    
    
    protected abstract Shape getPolygon(double width, double height, double scale);
    
    private Shape getTransformedPolygon(double width, double height, double scale) {
        
        Shape polygon = getPolygon(width, height, scale);
        
        if (flipHoriz || flipVert) {
            AffineTransform scaleInstance = AffineTransform.getScaleInstance(flipHoriz ? -1.0 : 1.0, flipVert ? -1.0 : 1.0);  //scaling negative creates a mirror image the other direction
            polygon = scaleInstance.createTransformedShape(polygon);
            scaledClip = scaleInstance.createTransformedShape(scaledClip);
            
            AffineTransform translateInstance = AffineTransform.getTranslateInstance(flipHoriz ? width : 0, flipVert ? height : 0);  //move back to where it was
            polygon = translateInstance.createTransformedShape(polygon);
            scaledClip = translateInstance.createTransformedShape(scaledClip);
        }
        return polygon;
    }
    
     
    @Override
    protected Shape getAbstractShape() {
        Rectangle2D r = getBounds2D();
        return getTransformedPolygon(r.getWidth(), r.getHeight(), 1.0);
    }

    
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {
 
        if (clippingShape != null) {
            AffineTransform scaleInstance = AffineTransform.getScaleInstance(unitSize, unitSize);
            scaledClip = scaleInstance.createTransformedShape(clippingShape);
        }
        else
            scaledClip = null;
        
        shape = getTransformedPolygon(width, height, unitSize);  //and also transform the scaled clip
   
        Shape origClip = setClip(g, scaledClip);
        
        super.paint(g, unitSize, width, height);
        
        g.setClip(origClip);

    }

   
}
