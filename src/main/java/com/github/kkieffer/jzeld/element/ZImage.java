
package com.github.kkieffer.jzeld.element;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
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
    
    protected static BufferedImage copyImage(BufferedImage i) {
        if (i == null)
            return null;
        ColorModel cm = i.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = i.copyData(i.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    
    protected Image image;

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
     * @param img the image painted on this element, if null, it is not painted
     */
    public ZImage(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove,  float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, BufferedImage img) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor);
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Width and height must be positive value");

        image = img;
    }
    
    public ZImage(ZImage copy) {
        super(copy);
        this.flipHoriz = copy.flipHoriz;
        this.flipVert = copy.flipVert;
        
        //Copy image
        this.image = copyImage((BufferedImage)copy.image);
    }
    
    @Override
    public ZImage copyOf() {
        return new ZImage(this);
    }
    
    /**
     * Set the image to use
     * @param i 
     */
    public void setImage(BufferedImage i) {
        image = i;
        hasChanges = true;
    }
    
    /**
     * Retrieves a copy of the image from this element.  
     * @return a copy of the image
     */
    public Image getImage() {
        return copyImage((BufferedImage)image);
    }
    

     @Override
    public void setFillColor(Color fillColor) {
        backgroundColor = null;
        hasChanges = true;
    }
    
    
    @Override
    public boolean supportsFlip() {
        return true;
    }
    
    @Override
    public void setAttributes(float outlineWidth, Color outlineColor, Float[] dashPattern, Color fillColor) {
        super.setAttributes(outlineWidth, outlineColor, dashPattern, fillColor);
        backgroundColor = null;
        hasChanges = true;
    }
    
    
    @Override
    public boolean hasFill() {
        return false;     
    }

  
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
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(img, x, y, width, height, null);
    }
    
    
    @Override
    public void paint(Graphics2D g, int unitSize, int width, int height) {
     
        if (image != null) {
            int x = flipHoriz ? width : 0;
            int w = flipHoriz ? -width : width;
            int y = flipVert ? height : 0;
            int h = flipVert ? -height : height;
            
            paintImage(g, image, x, y, w, h);
        }
        super.paint(g, unitSize, width, height);       
    }
    
    
}
