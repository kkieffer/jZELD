
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.adapters.ShapeAdapter;
import com.github.kkieffer.jzeld.attributes.CustomStroke;
import com.github.kkieffer.jzeld.attributes.PaintAttributes;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A ZShape is an element that defines an arbitrary shape with a border (that has color and thickness) and an interior color.  The bounds box
 * is set to completely contain the shape bounds. 
 * 
 * These features of ZShape are added for compatibility with SVG:
 * 
 * A ZShape can have a clipping shape that excludes areas of the ZShape that doesn't intersect the clipping
 * area. An optional setting will scale the border thickness appropriately with the area of the shape.  
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZShape")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZShape extends ZAbstractShape {


    /**
     * Intersect the current graphics clip with this clip, if any, and return the current graphics clip
     * @param g current graphics context
     * @param newClip new clip to merge
     * @return the original clip 
     */
    public static Shape setClip(Graphics2D g, Shape newClip) {
        Shape currClip = g.getClip();
        if (currClip == null) {
            g.setClip(newClip);
            return null;
        }
        
        if (newClip != null) {
            Area currAreaClip = new Area(currClip);
            currAreaClip.intersect(new Area(newClip));
            g.setClip(currAreaClip);
        }
        return currClip;
    }
    
    
    
    public static final double MIN_SHAPE_DIMENSION = 0.2;
    
    @XmlJavaTypeAdapter(ShapeAdapter.class)
    protected Shape shape;  //holds the original, unaltered shape
    
    @XmlJavaTypeAdapter(ShapeAdapter.class)
    private Shape clippingShape = null; 
    
    private boolean scaleBorderWithShape = false;
   
    
    transient protected Shape scaledShape;  //holds a resized version of the shape for painting
    transient protected Shape scaledClip;  //holds a resized version of the clipping shape for painting
    
    
    /**
     * Creates a new ZShape from the provided Shape, with the same attributes as the reference 
     * @param ref the reference element, from which attributes are taken
     * @param s the shape to use (scaled to unit coordinates)
     * @return a new ZShape 
     */
    public static ZShape createFromReference(ZAbstractShape ref, Shape s) {
              
        Rectangle2D bounds = s.getBounds2D();  //find the position of the shape
  
        //Move back to zero position reference
        AffineTransform pos = AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY());
        s = pos.createTransformedShape(s);
        
    
        return new ZShape(bounds.getX(), bounds.getY(), s, 0.0, true, true, true, ref.getOutlineWidth(), ref.getOutlineColor(), ref.getDashPattern(), ref.getFillColor(), ref.getPaintAttributes(), ref.getStrokeAttributes(), ref.getCustomStroke(), ref.getOutlineStyle());                
    }
    
    
    
    protected ZShape() {}
    
    /**
     * Create a shape
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param s the shape from which to draw (scaled to unit coordinates)
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove if the object can be moved 
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     * @param pA paint attributes
     * @param sA stroke paint attributes
     * @param cS any custom stroke
     * @param borderStyle the border style
     */
    public ZShape(double x, double y, Shape s, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, PaintAttributes pA, PaintAttributes sA, CustomStroke cS, StrokeStyle borderStyle) {
        super(x, y, s.getBounds2D().getWidth() == 0 ? MIN_SHAPE_DIMENSION : s.getBounds2D().getWidth(), 
                    s.getBounds2D().getHeight() == 0 ? MIN_SHAPE_DIMENSION : s.getBounds2D().getHeight(), 
                    rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor, borderStyle);        
        this.shape = s;
        this.paintAttr = pA;
        this.strokeAttr = sA;
        this.customStroke = cS;        
    }
    
    protected ZShape(ZShape src, boolean forNew) {
        super(src, forNew);
        
        this.scaleBorderWithShape = src.scaleBorderWithShape;
        try {
            //Make a copy of the shape
            ShapeAdapter a = new ShapeAdapter();
            this.shape = a.unmarshal(a.marshal(src.shape));
            if (src.clippingShape != null)
                this.clippingShape = a.unmarshal(a.marshal(src.clippingShape));
  
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    

    @Override
    public ZShape copyOf(boolean forNew) {
        return new ZShape(this, forNew);
    }
    
    protected void setShape(Shape s) {
        this.shape = s;
        super.setSize(s.getBounds2D().getWidth(), s.getBounds2D().getHeight(), MIN_SHAPE_DIMENSION, 1.0);
    }
    
    
     @Override
    protected String getShapeSummary() {       
        return "A general shape created by drawing or merging other elements.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "";     
    }
    
    
    @Override
    protected Shape getAbstractShape() {
        //Make a copy of the shape
        ShapeAdapter a = new ShapeAdapter();
        try {
            return a.unmarshal(a.marshal(shape));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Returns the clipping shape for this shape. The clipping shape has been offset to this shape's origin
     * @return 
     */
    public Shape getClippingShape() {
        return clippingShape;
    }
    
    /**
     * Set the clipping shape for this shape. The clip shape must be in canvas units, but in absolute position on the canvas
     * @param s the clip shape to apply, null to remove clip
     */
    public void setClippingShape(Shape s) {
        if (s != null) {
            Rectangle2D b = this.getBounds2D();
            AffineTransform a = AffineTransform.getTranslateInstance(-b.getX(), -b.getY());  //translate to this shape's origin
            clippingShape = a.createTransformedShape(s);
        }
        else
            clippingShape = null;
    }
    
    public void scaleBorderWithShape(boolean enable) {
        scaleBorderWithShape = enable;
    }

    public boolean scaleBorderWithShape() {
        return scaleBorderWithShape;
    }

    
    /**
     * Resize the object, new width and height in pixels
     * @param w width in pixels
     * @param h height in pixels
     * @param minSize the minimum size, in pixels
     * @param scale scale factor
     */
    @Override
    public void setSize(double w, double h, double minSize, double scale) {
   
        //Hold onto the old values
        Rectangle2D r = getBounds2D(scale);
        double oldWidth = r.getWidth();
        double oldHeight = r.getHeight();

        //Compute the new size, which may limit the minimum
        super.setSize(w, h, minSize, scale);
        
        //Get the new size and calculate the ratio of new to old
        r = getBounds2D(scale);

        double widthRatio = r.getWidth() / oldWidth;
        double heightRatio = r.getHeight() / oldHeight;

        //Scale the shape by the ratio
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(widthRatio, heightRatio);
        shape = scaleInstance.createTransformedShape(shape);
        clippingShape = scaleInstance.createTransformedShape(clippingShape);
        
        if (scaleBorderWithShape && borderThickness != 0) {
            double borderScale = (double)Math.sqrt(widthRatio * heightRatio);
            borderThickness *= borderScale;
            
        }
        
    }
    

    @Override
    protected void fillShape(Graphics2D g, double unitSize, double width, double height) {     
        Shape origClip = setClip(g, scaledClip);
        if (scaledShape != null)
            g.fill(scaledShape);
        g.setClip(origClip);
    }

   
    
    @Override
    protected void drawShape(Graphics2D g, double unitSize, double width, double height) { 
        Shape origClip = setClip(g, scaledClip);
        if (scaledShape != null)
            g.draw(scaledShape);
        g.setClip(origClip);
    }

    @Override
    public boolean supportsFlip() {
        return true;
    }
    
    @Override
    public boolean supportsEdit() {
        return false;
    };
    
    @Override
    public void flipHorizontal() {
        Rectangle2D bounds = getBounds2D();
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(-1.0, 1.0);  //scaling negative creates a mirror image the other direction
        shape = scaleInstance.createTransformedShape(shape);
        clippingShape = scaleInstance.createTransformedShape(clippingShape);

        AffineTransform translateInstance = AffineTransform.getTranslateInstance(bounds.getWidth(), 0);  //move back to where it was
        shape = translateInstance.createTransformedShape(shape);
        clippingShape = translateInstance.createTransformedShape(clippingShape);

        super.flipHorizontal();
    }
    
    @Override
    public void flipVertical() {
        Rectangle2D bounds = getBounds2D();
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(1.0, -1.0);  //scaling negative creates a mirror image the other direction
        shape = scaleInstance.createTransformedShape(shape);
        clippingShape = scaleInstance.createTransformedShape(clippingShape);
          
        AffineTransform translateInstance = AffineTransform.getTranslateInstance(0, bounds.getHeight());  //move back to where it was
        shape = translateInstance.createTransformedShape(shape);
        clippingShape = translateInstance.createTransformedShape(clippingShape);
    
        super.flipVertical();
    }
    
    
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(unitSize, unitSize);
        scaledShape = scaleInstance.createTransformedShape(shape);
        scaledClip = scaleInstance.createTransformedShape(clippingShape);
        
        super.paint(g, unitSize, width, height);

    }

    
}
