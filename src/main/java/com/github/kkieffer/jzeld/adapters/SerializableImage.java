
package com.github.kkieffer.jzeld.adapters;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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
    
    
    public static BufferedImage copyImage(BufferedImage i) {
        if (i == null)
            return null;
        ColorModel cm = i.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = i.copyData(i.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    
    transient private Image image; //marked transient for Serializable - custom read/write object will restore it from bytes

    
    public SerializableImage() {}
    
    public SerializableImage(Image i) {
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
