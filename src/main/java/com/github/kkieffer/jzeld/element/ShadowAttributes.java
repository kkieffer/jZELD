
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.adapters.JAXBAdapter;
import com.jhlabs.image.ShadowFilter;
import java.awt.Color;
import java.awt.Image;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Class that holds shadow attributes for ZAbstractShapes.
 * @author kkieffer
 */
@XmlRootElement(name = "ShadowAttributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class ShadowAttributes implements Serializable {

    public interface ShadowInterface {
        
        public ShadowAttributes getShadowAttributes();
        public void setShadowAttributes(ShadowAttributes s);    
        public void changed();
        public Image getShadowImage();
        
        default void setEnabled(boolean e) {
            getShadowAttributes().setEnabled(e);
            changed();
        }
    
        default void setColor(Color c) {
            getShadowAttributes().setColor(c);
            changed();
        }

        default void setRadius(int r) {
            getShadowAttributes().setRadius(r);     
            changed();
        }
    
        default void setOpacity(float o) {
            getShadowAttributes().setOpacity(o);    
            changed();
        }
    
        default void setXOffset(double x) {
            getShadowAttributes().setXOffset(x);    
            changed();
        }

        default void setYOffset(double y) {
            getShadowAttributes().setYOffset(y);    
            changed();
        }

        default void setSizeRatio(double s) {
            getShadowAttributes().setSizeRatio(s);    
            changed();
        }
  
    }
    
    
    private boolean enabled; //display the shadow, on or off
    
    private float opacity; //0.0 to 1.0
        
    @XmlJavaTypeAdapter(JAXBAdapter.ColorAdapter.class)
    private Color color;  //shadow color
    
    private int radius;  //amount of blur, 0 is hard, higher numbers are soft
    
    private double xOffset;  //offset in units from the shape horizontally
    
    private double yOffset;  //offset in units from the shape vertically
    
    private double size; //ratio of the shadow size to the shape size (1.0 = same size)
    
    /**
     * Create default attributes
     */
    public ShadowAttributes() {
        setColor(Color.BLACK);
        radius = 5;
	opacity = 0.5f;
        xOffset = 0.1f;
        yOffset = 0.1f;
        size = 1.0;
        enabled = true;
    }
    
    public ShadowAttributes(ShadowAttributes src) {
        opacity = src.opacity;
        color = src.color;
        radius = src.radius;
        xOffset = src.xOffset;
        yOffset = src.yOffset;   
        size = src.size;
        enabled = src.enabled;
    }
    
    public ShadowAttributes(float opacity, Color color, int radius, double xOffset, double yOffset) {
        this.opacity = opacity;
        this.color = color;
        this.radius = radius;
        this.xOffset = xOffset;
        this.yOffset = yOffset;  
        this.size = 1.0f;
        this.enabled = true;
    }
    
    /**
     * Create a filter based on the attributes 
     * @return a JHLabs ShadowFilter
     */
    public ShadowFilter createFilter() {
        ShadowFilter filter = new ShadowFilter();
        filter.setShadowOnly(true);
        filter.setAddMargins(false);
        filter.setDistance(0);  //will be positioned by drawing the shadow in relation to the object
        filter.setShadowColor(color.getRGB());
        filter.setOpacity(opacity);
        filter.setRadius(radius);
        return filter;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean e) {
        enabled = e;
    }
    
    public Color getColor() {
        return color;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public float getOpacity() {
        return opacity;
    }
    
    public double getXOffset() {
        return xOffset;
    }
    
    public double getYOffset() {
        return yOffset;
    }
    
    
    public void setColor(Color c) {
        color = new Color(c.getRGB() | 0xFF000000);  //keep alpha opaque
    }
    
    public void setRadius(int r) {
        radius = r;
    }
    
    public void setOpacity(float o) {
        opacity = o;
    }
    
    public void setXOffset(double x) {
        xOffset = x;
    }
    
    public void setYOffset(double y) {
        yOffset = y;
    }
    
    public void setSizeRatio(double s) {
        size = s;
    }
     
    public double getSizeRatio() {
        return size;
    }
    
    
    
}
