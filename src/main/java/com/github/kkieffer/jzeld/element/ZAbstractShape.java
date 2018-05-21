
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.JAXBAdapter.ColorAdapter;
import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Graphics2D;
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
    
    protected int borderThickness;
    
    protected Float[] dashPattern = null;
    
    
    protected ZAbstractShape(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, int borderWidth, Color borderColor, Float[] dashPattern, Color fillColor) {
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
     * Change the rectangle attributes
     * @param outlineWidth unit width of the border, use zero for no border
     * @param outlineColor color of the border, which can be null for a transparent border
     * @param dashPattern the dash pattern for the border, if null, solid line
     * @param fillColor color of the rectangle area, which can be null for transparent 

     */
    @Override
    public void setAttributes(int outlineWidth, Color outlineColor, Float[] dashPattern, Color fillColor) {
    
        setFillColor(fillColor);
        setOutlineWidth(outlineWidth);
        setDashPattern(dashPattern);
        setOutlineColor(outlineColor);
        hasChanges = true;
    }
    
    @Override
    public void setOutlineWidth(int width) {
        borderThickness = width;
        hasChanges = true;
    }
    
    @Override
    public void setDashPattern(Float[] dashPattern) {
        this.dashPattern = dashPattern == null ? null : (Float[])Arrays.copyOf(dashPattern, dashPattern.length);
        hasChanges = true;
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
    
    
    protected abstract void fillShape(Graphics2D g, int unitSize, int width, int height);
    protected abstract void drawShape(Graphics2D g, int unitSize, int width, int height);
    
    
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
