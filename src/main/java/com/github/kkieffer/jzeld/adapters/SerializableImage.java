
package com.github.kkieffer.jzeld.adapters;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author kkieffer
 */
@XmlRootElement(name = "SerializableImage")
public class SerializableImage implements Serializable {
    
    /**
     * Make an exact copy of the BufferedImage
     * @param i source image
     * @return copy
     */
    public static BufferedImage copyImage(BufferedImage i) {
        if (i == null)
            return null;
        ColorModel cm = i.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = i.copyData(i.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    /**
     * Resize the image, scaling it to fit the specified dimensions
     * @param original the original image 
     * @param newWidth the new width
     * @param newHeight the new height
     * @param newType the new image type
     * @return the rescaled image
     */
    public static BufferedImage resizeImage(Image original, int newWidth, int newHeight, int newType) {
        return resizeImage(original, newWidth, newHeight, newType, 0, 0, newWidth, newHeight);
    }
        
    /**
     * Resize the image, scaling it to fit the specified dimensions, and the provided offset
     * @param original the original image 
     * @param newWidth the new image width bounds
     * @param newHeight the new image height bounds
     * @param newType the new image type
     * @param x the x offset where the old image is copied, from the left
     * @param y the y offset where the old image is copied, from the top
     * @param w the scaled width of the new image
     * @param h the scaled height of the new image
     * @return the rescaled image
     */
    public static BufferedImage resizeImage(Image original, int newWidth, int newHeight, int newType, int x, int y, int w, int h) {
        
        if (newType == 0) {
            
            PixelGrabber pg = new PixelGrabber(original, 0, 0, 1, 1, false);  //grab the first pixel
            try { 
                if (!pg.grabPixels())
                throw new RuntimeException("Unable to get pixel from image");
            } catch (InterruptedException ex) {
                throw new RuntimeException("Interrupted while getting pixel from image");
            }
            boolean hasAlpha = pg.getColorModel().hasAlpha();  //see if the pixel has alpha channel
            
            newType = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        }
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, newType);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, x, y, w, h, null);
        g.dispose();
        return resizedImage;
    }
    
    
    transient private Image image; //marked transient for Serializable - custom read/write object will restore it from bytes

    
    public SerializableImage() {}
    
    public SerializableImage(Image i) {
        if (i != null)
            image = copyImage((BufferedImage)i);
    }
    
    public SerializableImage(SerializableImage i) {
        this.image = copyImage((BufferedImage)i.image);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeBoolean(image != null);
        if (image != null)
            ImageIO.write((BufferedImage)image, "png", out); 
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        boolean imageExists = in.readBoolean();
        if (imageExists)
            image = ImageIO.read(in);
    }
    
    
    public Image getImage() { 
        return image; 
    }

    @XmlElement (name = "Image")
    public void setImage(Image i) {
        this.image = i;
    } 
    
    public Image getImageCopy() {
        return copyImage((BufferedImage)image);
    }
    
}
