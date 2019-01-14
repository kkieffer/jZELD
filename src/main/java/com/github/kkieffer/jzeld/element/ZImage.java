
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.adapters.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * A ZImage is similar to a ZRectangle, but the rectangle is filled with a loaded Image.  
 * The image is scaled to fit the size of the rectangle.  The ZImage does not support a fill color.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZImage")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZImage extends ZRectangle {
    
    protected static final Color FILL_COLOR = new Color(255, 255, 255, 0);  //translucent white, default fill unless set explicitly
    
    protected SerializableImage image;
    
    protected ZImage() {}
    
    /**
     * Create a ZImage
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the object in units
     * @param height the height of the object in units
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove if the object can be moved by the mouse drag
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the dash pattern of the border, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent. Any transparent image pixels will have this color
     * @param borderStyle style of the border
     * @param img the image painted on this element, if null, it is not painted
     */
    public ZImage(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove,  float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, StrokeStyle borderStyle, BufferedImage img) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, FILL_COLOR, borderStyle);
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Width and height must be positive value");
        
        
        //Create the serializable image from a resized image.  The image doesn't actually change size but we switch to ARGB or RGB types because some
        //other types don't always print when printing the image to a printer.
        if (img != null) {
            if (img.getColorModel().hasAlpha())
                image = new SerializableImage(SerializableImage.resizeImage(img, img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB));
            else
                image = new SerializableImage(SerializableImage.resizeImage(img, img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB));
        } else
            image = new SerializableImage();
    }
    
    public ZImage(ZImage copy, boolean forNew) {
        super(copy, forNew);
        this.flipHoriz = copy.flipHoriz;
        this.flipVert = copy.flipVert;
        
        //Copy image
        this.image = new SerializableImage(copy.image);
    }
    
    @Override
    public ZImage copyOf(boolean forNew) {
        return new ZImage(this, forNew);
    }
    
    @Override
    protected String getShapeSummary() {       
        return "An image.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "";
    }
    
    /**
     * Set the image to use
     * @param i 
     */
    public void setImage(BufferedImage i) {
        image.setImage(i);
        changed();
    }
    
    /**
     * Retrieves a copy of the image from this element.  
     * @return a copy of the image
     */
    public Image getImage() {
        return image.getImageCopy();
    }
    

    
    @Override
    public boolean supportsEdit() {
        return false;
    };
  
    /**
     * Draw the image on the graphics. This allows subclass overriding to modify the image or change rendering
     * @param g graphics to draw on
     * @param img the loaded image
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the image width
     * @param height the image height
     */
    protected void paintImage(Graphics2D g, Image img, final int x, final int y, final int width, final int height) {      
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(img, x, y, width, height, null);
    }
    
    
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {
             
        if (!isVisible())
            return;

        super.paint(g, unitSize, width, height);       
        
        if (image != null) {
            int x = flipHoriz ? (int)width : 0;
            int w = flipHoriz ? (int)-width : (int)width;
            int y = flipVert ? (int)height : 0;
            int h = flipVert ? (int)-height : (int)height;

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getOpacity()));
            paintImage(g, image.getImage(), x, y, w, h);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
    
    
}
