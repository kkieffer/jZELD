
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.adapters.JAXBAdapter.ColorAdapter;
import com.github.kkieffer.jzeld.ZCanvas.CombineOperation;
import com.jhlabs.image.ShadowFilter;
import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
    protected PaintAttributes paintAttr = null;    
    protected CustomStroke customStroke = null;
       
    protected ShadowAttributes shadowAttributes = null;
    
    private transient BufferedImage shadowImage = null;
    
    protected ZAbstractShape(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove);
        setAttributes(borderWidth, borderColor, dashPattern, fillColor);
        shadowImage = null;
    }
    
    protected ZAbstractShape(ZAbstractShape src, boolean forNew) {
        super(src, forNew);
        setAttributes(src.borderThickness, src.borderColor, src.dashPattern, src.backgroundColor);
        paintAttr = src.paintAttr == null ? null : new PaintAttributes(src.paintAttr);
        shadowAttributes = src.shadowAttributes == null ? null : new ShadowAttributes(src.shadowAttributes);
        customStroke = src.customStroke == null ? null : src.customStroke.copyOf();
        shadowImage = null;
    }
    
    protected ZAbstractShape() {}
    
    
    
    protected abstract String getShapeSummary();
    protected abstract String getShapeDescription();
    
    
    @Override
    public String getHtmlHelp() {
        String className = this.getClass().getSimpleName();
        return "<b>" + className + ": " + getShapeSummary() + "</b><br><br>" + getShapeDescription() + "<br><br>" + 
                "Right-click on this element to set its attributes: line color and width, line dash pattern, and fill color.<br><br>" + super.getHtmlHelp();
    }
    
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
        changed();
    }
    
    @Override
    public void setOutlineWidth(float width) {
        borderThickness = width;
        changed();
    }
    
    @Override
    public float getOutlineWidth() {
        return borderThickness;
    }
    
    @Override
    public void setDashPattern(Float[] dashPattern) {
        this.dashPattern = dashPattern == null ? null : (Float[])Arrays.copyOf(dashPattern, dashPattern.length);
        changed();
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
        changed();
    }
    
    @Override
    public void setFillColor(Color fillColor) {
        backgroundColor = fillColor;
        changed();
    }
    
    @Override
    public void removeFill() {
        backgroundColor = null;
        paintAttr = null;
        changed();

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
    
    public void setCustomStroke(CustomStroke s) {
        customStroke = s;
        changed();
    }
    
    public CustomStroke getCustomStroke() {
        return customStroke;
    }
    
    /**
     * Set the paint attributes for the element.  The paint attributes (linear, radial, or texture) are applied over the shape's fill color.
     * @param p the paint attributes.  To remove, use null
     */
    public void setPaintAttributes(PaintAttributes p) {
        paintAttr = p;
        changed();
    }
    
    
    public PaintAttributes getPaintAttributes() {
        return paintAttr;
    }
    
    
    public ShadowAttributes getShadowAttributes() {
        return shadowAttributes;
    }
    
    /**
     * When a subclass modifies itself such that the shadow requires redraw, calling this will force a 
     * recompute of the shadow image on next repaint.
     */
    @Override
    public void changed() {
        shadowImage = null;
        super.changed();
    }
    
    public void setShadowAttributes(ShadowAttributes s) {
        shadowAttributes = s;
        changed();
    }
    
    
    @Override
    public void flipHorizontal() {
        super.flipHorizontal();
        changed();
    }
    
    @Override
    public void flipVertical() {
        super.flipVertical();
        changed();
    }
    
    protected abstract Shape getAbstractShape();
    protected abstract void fillShape(Graphics2D g, double unitSize, double width, double height);
    protected abstract void drawShape(Graphics2D g, double unitSize, double width, double height);
    
    /**
     * Retrieves the abstract shape and then transforms it according to the rotation and position where it lies on the canvas
     * @return 
     */
    protected Shape getShape() {
        
        Shape s = getAbstractShape();  //gets the abstract shape (placed at 0,0)
        Rectangle2D bounds = getBounds2D();
        
        //Move to its center
        AffineTransform ctr = AffineTransform.getTranslateInstance(-bounds.getWidth()/2, -bounds.getHeight()/2);
        s = ctr.createTransformedShape(s);
        
        //Rotate it
        AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(this.getRotation()));
        s = rotate.createTransformedShape(s);
        
        //Shear it
        AffineTransform shear = AffineTransform.getShearInstance(this.getShearX(), this.getShearY());
        s = shear.createTransformedShape(s);
        
        //Move to its position
        AffineTransform pos = AffineTransform.getTranslateInstance(bounds.getWidth()/2 + bounds.getX(), bounds.getHeight()/2  + bounds.getY());
        s = pos.createTransformedShape(s);
        
        return s;
    }
    
    
    
    /**
     * Combine this shape with the provided list of ZAbstractShapes. The 
     * @param operation the merge operation
     * @param shapes the elements whose shapes are combined
     * @return the combined shape
     */
    public final Shape combineWith(CombineOperation operation, ArrayList<ZAbstractShape> shapes) {
        
        Shape refShape = getShape();  //reference shape
        Area a = new Area(refShape);
        
        for (ZAbstractShape zShape : shapes) {
            
            Shape from = zShape.getShape();

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
        }
        
               
        return a;
    }
    
    @Override
    protected void setSize(double w, double h, double minSize, double scale) {
        super.setSize(w, h, minSize, scale);
        changed();
    }
    
    
    protected double getShadowMargin(double scale) {
        if (shadowAttributes == null)
            return 0;
        
        double margin = 2*shadowAttributes.getRadius() + getOutlineWidth()/2;  //increase image to support the additional size on the edge from kernel and shape outline
        return margin * scale / 72.0;
    }
    
    @Override
    public Rectangle2D getMarginBounds(double scale) {
                   
        //If the element has a shadow, determine the rectangle that will hold the shape and shadow based on the shadow size & position,
        //otherwise, just add half the line width for the margins
        
        Rectangle2D bounds = getBounds2D(scale);
        double ow = (getOutlineWidth()/2.0)/72.0 * scale;  //half the line width
        
        if (shadowImage != null) {           
            double margin = getShadowMargin(scale);
            
            double shadW = bounds.getWidth() * shadowAttributes.getSizeRatio();
            double shadH = bounds.getHeight() * shadowAttributes.getSizeRatio();

            double shadowPosX = shadowAttributes.getXOffset()*scale;
            double shadowPosY = shadowAttributes.getYOffset()*scale;
            
            double shadowLeft = shadowPosX - margin;
            double shadowTop = shadowPosY - margin;
            double shadowRight = shadowPosX + shadW + margin;
            double shadowBottom = shadowPosY + shadH + margin;

            double leftMargin = shadowLeft < -ow ? shadowLeft : -ow;
            double topMargin = shadowTop < -ow ? shadowTop : -ow;
            
            return new Rectangle2D.Double(leftMargin, topMargin,
                                          shadowRight > bounds.getWidth() + ow ? shadowRight - leftMargin : bounds.getWidth() + ow - leftMargin,
                                          shadowBottom > bounds.getHeight() + ow ? shadowBottom - topMargin : bounds.getHeight() + ow - topMargin);
                    
        }
        else {
            return new Rectangle2D.Double(-ow, -ow, bounds.getWidth() + ow, bounds.getHeight() + ow);
        }
        
    }

    
    /**
     * Draw and fill the element's shape on a new image in black, then from the shadow parameters, create a shadow filter and create the shadow image.
     * The shadow shape's size is the width and height, Note that the size is increased by a margin to account for the blur and line width
     * @param unitSize
     * @param width the width of the shape
     * @param height the height of the shape
     */
    protected void createShadow(double unitSize, double width, double height) {
        
        ShadowFilter shadow = shadowAttributes.createFilter();
       
        double margin = getShadowMargin(unitSize);

        //Create Buffered Image
        BufferedImage bi = new BufferedImage((int)Math.ceil(width+margin), (int)Math.ceil(height+margin), BufferedImage.TYPE_INT_ARGB);
        Graphics2D imgGraphics = bi.createGraphics();
        imgGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        imgGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        imgGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        imgGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                      
        imgGraphics.setColor(Color.BLACK);
        imgGraphics.translate(margin/2, margin/2);  //center the shadowed image
        if (backgroundColor != null || paintAttr != null)
            fillShape(imgGraphics, unitSize, width, height);

        if (customStroke != null || (borderThickness > 0 && borderColor != null))
            drawShape(imgGraphics, unitSize, width, height);
              
        imgGraphics.dispose();
        
        //Filter the image to create the shadow
        shadowImage = shadow.filter(bi, null);
        
    }
    
    
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {

        if (!isVisible())
            return;
        
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
        //If the element has a shadow, create the shadow image (if needed), and place it at the desired offset
        if (shadowAttributes != null && shadowAttributes.isEnabled()) {
            if (shadowImage == null) 
                createShadow(unitSize, width, height);
            
            double margin = getShadowMargin(unitSize);
   
            int shadW = (int)(shadowImage.getWidth() * shadowAttributes.getSizeRatio());
            int shadH = (int)(shadowImage.getHeight() * shadowAttributes.getSizeRatio());
                
            g.drawImage(shadowImage, (int)(shadowAttributes.getXOffset()*unitSize - margin/2), (int)(shadowAttributes.getYOffset()*unitSize - margin/2), shadW, shadH, null);
        }

        if (backgroundColor != null) {
            g.setColor(backgroundColor);
            fillShape(g, unitSize, width, height);
        }
        if (paintAttr != null) {
            paintAttr.applyPaintAttribute(g, width, height, unitSize, flipHoriz, flipVert);
            fillShape(g, unitSize, width, height);
        }

       if (customStroke != null) {
           g.setStroke(customStroke);
           g.setColor(customStroke.getColor());
           drawShape(g, unitSize, width, height);
       } 
        
       if (borderThickness != 0 && borderColor != null) {  //use built-in Basic Stroke
           
            if (dashPattern == null || dashPattern.length == 0)
                g.setStroke(new BasicStroke(borderThickness));
            else {
                float[] d = new float[dashPattern.length];
                for (int i=0; i<dashPattern.length; i++)
                    d[i] = dashPattern[i] * (float)unitSize + borderThickness * .75f;
                    

                g.setStroke(new BasicStroke(borderThickness, CAP_SQUARE, JOIN_MITER, 10.0f, d, 0.0f));
            }
       
 
            g.setColor(borderColor);
            drawShape(g, unitSize, width, height);
       }
              
    }

    
    
}
