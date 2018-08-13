
package com.github.kkieffer.jzeld.element;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A ZShape is an element that defines an arbitrary shape with a border (that has color and thickness) and an interior color.  The bounds box
 * is set to completely contain the shape bounds. 
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZShape")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZShape extends ZAbstractShape {

    private static class ShapeAdapter extends XmlAdapter<String, Shape> {

        @Override
        public String marshal(final Shape s) throws Exception {
            
            Path2D.Double path = new Path2D.Double(s);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(path);
            out.flush();
            out.close();
            byte[] bA = bos.toByteArray();
            return DatatypeConverter.printHexBinary(bA);
            
        }

        @Override
        public Shape unmarshal(String v) throws Exception {
                        
            byte[] decodedHex = DatatypeConverter.parseHexBinary(v);

            // Make an input stream from the byte array and read a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(decodedHex));
            return (Shape)in.readObject();
        }

     }
    
    
    @XmlJavaTypeAdapter(ShapeAdapter.class)
    protected Shape shape;  //holds the original, unaltered shape
    
    transient private Shape scaledShape;  //holds a resized version of the shape for painting
    
    protected ZShape() {}
    
    /**
     * Create a shape
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param s the shape from which to draw
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove if the object can be moved 
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     * @param pA paint attributes
     */
    public ZShape(double x, double y, Shape s, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, PaintAttributes pA) {
        super(x, y, s.getBounds2D().getWidth(), s.getBounds2D().getHeight(), rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor);        
        this.shape = s;
        this.paintAttr = pA;
    }
    
    protected ZShape(ZShape src, boolean forNew) {
        super(src, forNew);
        
        try {
            //Make a copy of the shape
            ShapeAdapter a = new ShapeAdapter();
            this.shape = a.unmarshal(a.marshal(src.shape));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    

    @Override
    public ZShape copyOf(boolean forNew) {
        return new ZShape(this, forNew);
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
        ZShape copy = copyOf(false);
        return copy.shape;
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
        hasChanges = true;
    }
    

    @Override
    protected void fillShape(Graphics2D g, double unitSize, double width, double height) {
        g.fill(scaledShape);
        hasChanges = true;
    }

   
    
    @Override
    protected void drawShape(Graphics2D g, double unitSize, double width, double height) {        
        g.draw(scaledShape);
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
        AffineTransform translateInstance = AffineTransform.getTranslateInstance(bounds.getWidth(), 0);  //move back to where it was
        shape = translateInstance.createTransformedShape(shape);
        super.flipHorizontal();
    }
    
    @Override
    public void flipVertical() {
        Rectangle2D bounds = getBounds2D();
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(1.0, -1.0);  //scaling negative creates a mirror image the other direction
        shape = scaleInstance.createTransformedShape(shape);
        AffineTransform translateInstance = AffineTransform.getTranslateInstance(0, bounds.getHeight());  //move back to where it was
        shape = translateInstance.createTransformedShape(shape);
        super.flipVertical();
    }
    
    
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(unitSize, unitSize);
        scaledShape = scaleInstance.createTransformedShape(shape);
        
        super.paint(g, unitSize, width, height);

    }

    
}
