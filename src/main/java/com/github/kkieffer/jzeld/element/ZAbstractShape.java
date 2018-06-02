
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.JAXBAdapter.ColorAdapter;
import com.github.kkieffer.jzeld.ZCanvas.Merge;
import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * An abstract shape, that has an outline and a fill color
 * @author kkieffer
 */
@XmlRootElement(name = "ZAbstractShape")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ZAbstractShape extends ZElement {
    
    
    @XmlJavaTypeAdapter(ColorAdapter.class)
    protected Color borderColor;

    @XmlJavaTypeAdapter(ColorAdapter.class)
    protected Color backgroundColor;
    
    protected float borderThickness;
    
    protected Float[] dashPattern = null;
    
   
    protected ZAbstractShape(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor) {
        super(x, y, width, height, rotation, canSelect, canResize);
        setAttributes(borderWidth, borderColor, dashPattern, fillColor);
        hasChanges = false;
    }
    
    protected ZAbstractShape(ZAbstractShape src) {
        super(src);
        setAttributes(src.borderThickness, src.borderColor, src.dashPattern, src.backgroundColor);
        hasChanges = false;
    }
    
    protected ZAbstractShape() {}
    
    /**
     * Change the attributes
     * @param outlineWidth unit width of the border, use zero for no border
     * @param outlineColor color of the border, which can be null for a transparent border
     * @param dashPattern the dash pattern for the border, if null, solid line
     * @param fillColor color of the rectangle area, which can be null for transparent 

     */
    @Override
    public void setAttributes(float outlineWidth, Color outlineColor, Float[] dashPattern, Color fillColor) {
    
        setFillColor(fillColor);
        setOutlineWidth(outlineWidth);
        setDashPattern(dashPattern);
        setOutlineColor(outlineColor);
        hasChanges = true;
    }
    
    @Override
    public void setOutlineWidth(float width) {
        borderThickness = width;
        hasChanges = true;
    }
    
    @Override
    public float getOutlineWidth() {
        return borderThickness;
    }
    
    @Override
    public void setDashPattern(Float[] dashPattern) {
        this.dashPattern = dashPattern == null ? null : (Float[])Arrays.copyOf(dashPattern, dashPattern.length);
        hasChanges = true;
    }
    
    @Override
    public Float[] getDashPattern() {
        if (dashPattern == null)
            return null;
        else
            return (Float[])Arrays.copyOf(dashPattern, dashPattern.length);
    }
    
    @Override
    public void setOutlineColor(Color outlineColor) {
        this.borderColor = outlineColor;
        hasChanges = true;
    }
    
    @Override
    public void setFillColor(Color fillColor) {
        backgroundColor = fillColor;
        hasChanges = true;
    }
    
    @Override
    public Color getOutlineColor() {
        return borderColor;
    }
    
    @Override
    public Color getFillColor() {
        return backgroundColor;
    }
    
    @Override
    public boolean hasOutline() {
        return true;
    }

    @Override
    public boolean hasFill() {
        return true;
    }

    @Override
    public boolean hasDash() {
        return true;
    }
    
    protected abstract Shape getAbstractShape();
    protected abstract void fillShape(Graphics2D g, int unitSize, int width, int height);
    protected abstract void drawShape(Graphics2D g, int unitSize, int width, int height);
    
    /**
     * Retrieves the abstract shape and then transforms it according to the rotation and position where it lies on the canvas
     * @return 
     */
    private Shape getShape() {
        
        Shape s = getAbstractShape();  //gets the abstract shape (placed at 0,0)
        Rectangle2D bounds = getBounds2D();
        
        //Move to its center
        AffineTransform ctr = AffineTransform.getTranslateInstance(-bounds.getWidth()/2, -bounds.getHeight()/2);
        s = ctr.createTransformedShape(s);
        
        //Rotate it
        AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(this.getRotation()));
        s = rotate.createTransformedShape(s);
        
        //Move to its position
        AffineTransform pos = AffineTransform.getTranslateInstance(bounds.getWidth()/2 + bounds.getX(), bounds.getHeight()/2  + bounds.getY());
        s = pos.createTransformedShape(s);
        
        return s;
    }
    
    
    
    /**
     * Merge this shape with the provided ZAbstractShape
     * @param operation the merge operation
     * @param zShape the shape to merge with
     * @return true if a merge occurred, false if the provided ZAbstractShape does not support being merged
     */
    public final Shape mergeShape(Merge operation, ZAbstractShape zShape) {
        
        Shape mergeShape = getShape();
        
        Shape from = zShape.getShape();
        if (from == null)
            return null;
        
        Area a = new Area(mergeShape);
        
        switch (operation) {
            case Join:
                a.add(new Area(from));
                break;
            case Subtract:
                a.subtract(new Area(from));
                break;
            case Intersect:
                a.intersect(new Area(from));
                break;
            case Exclusive_Join:
                a.exclusiveOr(new Area(from));
                break;
        }
        
                
        Rectangle2D bounds = a.getBounds2D();  //find the new position of the joined object (may move, especially with Intersect)

        //Move back to zero position reference
        AffineTransform pos = AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY());
        mergeShape = pos.createTransformedShape(a);
   
        return mergeShape;
    }
    
    
    @Override
    public void paint(Graphics2D g, int unitSize, int width, int height) {

       if (backgroundColor != null) {
            g.setColor(backgroundColor);
            fillShape(g, unitSize, width, height);
       }


       if (borderThickness != 0) {
           
            if (dashPattern == null)
                g.setStroke(new BasicStroke(borderThickness));
            else {
                float[] d = new float[dashPattern.length];
                for (int i=0; i<dashPattern.length; i++)
                    d[i] = dashPattern[i] * unitSize + borderThickness * .75f;
                    

                g.setStroke(new BasicStroke(borderThickness, CAP_SQUARE, JOIN_MITER, 10.0f, d, 0.0f));
            }
       
 
            g.setColor(borderColor);
            drawShape(g, unitSize, width, height);
       }
       
    }
    
    
}
