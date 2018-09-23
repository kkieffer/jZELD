
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.adapters.JAXBAdapter;
import com.github.kkieffer.jzeld.adapters.SerializableImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlRootElement(name = "PaintAttributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaintAttributes implements Serializable {

    public enum RadiusRelative {WIDTH, HEIGHT, LONGEST, SHORTEST};
    
    private enum PaintType {LINEAR, RADIAL, PATTERN};
    
    private PaintType type;
    private CycleMethod cycleMethod;
    private RadiusRelative radiusRelative;
    
    private Point2D.Float start;
    private Point2D.Float finish;
    private Point2D.Float center;
    private Point2D.Float focus;

    private float radius;
    private float[] dist;
   
    @XmlJavaTypeAdapter(JAXBAdapter.ColorAdapter.class)
    private Color[] colors;
    
    SerializableImage patternImage;
    private float imgWidth;
    private float imgHeight;
    
    private PaintAttributes() {}
    
    public PaintAttributes(PaintAttributes src) {
        
        type = src.type;
        
        switch (type) {
            case LINEAR:   
                start = new Point2D.Float((float)src.start.getX(), (float)src.start.getY());
                finish = new Point2D.Float((float)src.finish.getX(), (float)src.finish.getY());
                break;
            case RADIAL:       
                center = new Point2D.Float((float)src.center.getX(), (float)src.center.getY());
                focus = new Point2D.Float((float)src.focus.getX(), (float)src.focus.getY());
                radius = src.radius;
                radiusRelative = src.radiusRelative;
                break;
            case PATTERN:
                patternImage = new SerializableImage(src.patternImage);
                imgWidth = src.imgWidth;
                imgHeight = src.imgHeight;
                break;
        }
            
        if (type != PaintType.PATTERN) {
            dist = src.dist.clone();
            colors = src.colors.clone();
            cycleMethod = src.cycleMethod;
        }
    }
    
    public void applyPaintAttribute(Graphics2D g2d, double width, double height, double unitSize, boolean flipHoriz, boolean flipVert) {
        
        float w = (float)width;
        float h = (float)height;
        
        Paint p = null;
        switch (type) {
            case LINEAR:
                
                Point2D.Float theStart = new Point2D.Float(flipHoriz ? 1.0f-start.x : start.x, flipVert ? 1.0f-start.y : start.y);
                Point2D.Float theEnd = new Point2D.Float(flipHoriz ? 1.0f-finish.x : finish.x, flipVert ? 1.0f-finish.y : finish.y);
             
                p = new LinearGradientPaint(w * theStart.x, h * theStart.y, w * theEnd.x, h * theEnd.y, dist, colors, cycleMethod);
                break;
            case RADIAL:
                                
                float radiusVal = 0.0f;
                switch (radiusRelative) {
                    case WIDTH:
                        radiusVal = w * radius;
                        break;
                    case HEIGHT:
                        radiusVal = h * radius;
                        break;
                    case LONGEST:
                        radiusVal = radius * (float)(width > height ? width : height);
                        break;
                    case SHORTEST:
                        radiusVal = radius * (float)(width < height ? width : height);
                        break;
                }
                
                Point2D.Float theCenter = new Point2D.Float(flipHoriz ? 1.0f-center.x : center.x, flipVert ? 1.0f-center.y : center.y);
                Point2D.Float theFocus = new Point2D.Float(flipHoriz ? 1.0f-focus.x : focus.x, flipVert ? 1.0f-focus.y : focus.y);

                
                p = new RadialGradientPaint(w * theCenter.x, h * theCenter.y, radiusVal, w * theFocus.x, h * theFocus.y, dist, colors, cycleMethod);
                break;

            case PATTERN:
                                
                p = new TexturePaint((BufferedImage)patternImage.getImage(), new Rectangle2D.Double(0, 0, imgWidth * unitSize * (flipHoriz ? -1 : 1), imgHeight * unitSize * (flipVert ? -1 : 1)));
                break;
                
        }
        g2d.setPaint(p);
    }
    
    
    /**
     * Create a linear gradient paint attribute set
     * @param start the starting point, relative to the element bounds (x and y range from 0 to 1.0)
     * @param finish the finish point, relative to the element bounds (x and y range from 0 to 1.0)
     * @param dist the color distribution points
     * @param colors the colors at the distribution points
     * @param cycle how to paint outside start and finish
     * @return 
     */
    public static PaintAttributes createLinearPaintAttribute(Point2D start, Point2D finish, float[] dist, Color[] colors, CycleMethod cycle) {
        
        PaintAttributes a = new PaintAttributes();
        
        a.type = PaintType.LINEAR;
        
        a.start = new Point2D.Float((float)start.getX(), (float)start.getY());
        a.finish = new Point2D.Float((float)finish.getX(), (float)finish.getY());
        
        a.dist = dist.clone();
        a.colors = colors.clone();
        a.cycleMethod = cycle;
        return a;
    }
    
    /**
     * Create a radial gradient paint attribute set
     * @param center the starting point, relative to the element bounds (x and y range from 0 to 1.0, 0.5,0.5 is centered in shape)
     * @param focus the focus point, relative to the element bounds (x and y range from 0 to 1.0).  If focus is null, the focus is the center
     * @param radius the radius to which the endpoint lies, relative to the bounds specified by the rr parameter
     * @param rr determines how to compute the radius, if WIDTH, the radius is relative to the width (0 to 1.0), likewise for height.  If LONGEST, radius is relative to the longer side.  Likewise for SHORTEST.
     * @param dist the color distribution points
     * @param colors the colors at the distribution points
     * @param cycle how to paint outside start and finish
     * @return 
     */
    public static PaintAttributes createRadialPaintAttribute(Point2D center, Point2D focus, float radius, RadiusRelative rr, float[] dist, Color[] colors, CycleMethod cycle) {
        
        PaintAttributes a = new PaintAttributes();
        
        a.type = PaintType.RADIAL;
        
        a.center = new Point2D.Float((float)center.getX(), (float)center.getY());
        if (focus != null)
            a.focus = new Point2D.Float((float)focus.getX(), (float)focus.getY());
        else
            a.focus = a.center;
            
        a.radius = radius;
        a.radiusRelative = rr;
        a.dist = dist.clone();
        a.colors = colors.clone();
        a.cycleMethod = cycle;
        return a;
    }
    
    /**
     * Create a texture pattern paint attribute set
     * @param img the image to use for the pattern, which is replicated everywhere on the element
     * @param width the width to scale the image, in units
     * @param height the height to scale the image, in units
     * @return 
     */
    public static PaintAttributes createTexturePaintAttribute(BufferedImage img, float width, float height) {
        
        PaintAttributes a = new PaintAttributes();
        
        a.type = PaintType.PATTERN;
        a.patternImage = new SerializableImage(img);
        a.imgWidth = width;
        a.imgHeight = height;
        
        return a;
    }
    
}
