
package com.github.kkieffer.jzeld.adapters;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Simple class to scale the image to the desired width and height in order to properly render high resolution icons
 * The paint method is overridden to generate the high quality image.  For the superclass, provide a scaled down version of the 
 * image.  This is needed for example, when an icon is greyed out when disabled.
 * @author kkieffer
 */
public class HighResImageIcon extends ImageIcon {

    private final Image image;
    private final int width, height;
    private final BufferedImage scaledImage;  //used for superclass getImage()
    
    /**
     * Create the Icon using the supplied image.  The image is scaled to the desired dimensions
     * @param i image
     * @param w desired width
     * @param h desired height
     */
    public HighResImageIcon(Image i, int w, int h) {
        image = i;
        width = w;
        height = h;
        loadImage(image);  //wait for load
        scaledImage = SerializableImage.resizeImage(i, width, height, 0, 0, 0, width, height);
        
    }
    
    public HighResImageIcon(URL imageLocation, int w, int h) {
        this(Toolkit.getDefaultToolkit().getImage(imageLocation), w, h);
    }
    
    
    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(image, x, y, (int)width, (int)height, null);
    }

    @Override
    public Image getImage() {
        return scaledImage;
    }
    
    
    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
    
    
    
}
