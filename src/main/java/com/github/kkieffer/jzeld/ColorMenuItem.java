
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.element.ZElement;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;

/**
 * Color Menu Item is a Menu item with a color wheel icon, that when selected brings up a color chooser panel.  It can be used for
 * fill color or line color.  When the menu item is selected, it sets the fill or line color for the selected element on the canvas.
 * 
 * @author kkieffer
 */
public class ColorMenuItem extends JMenuItem {
     
    static enum Type {LINE, FILL}
    
    private static final ImageIcon colorIcon;
    
    static {
        InputStream i = ColorMenuItem.class.getClassLoader().getResourceAsStream("color16x16.png");
        try {
            BufferedImage image = ImageIO.read(i);
            colorIcon = new ImageIcon(image);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    

    public ColorMenuItem(String text, ZCanvas canvas, Type type) {
        super(text);
        setIcon(colorIcon);

        
        addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                ZElement[] selectedElements = canvas.getSelectedElements();
                if (selectedElements.length > 0) {
                    
                    Color oldColor = type == Type.FILL ? selectedElements[0].getFillColor() : selectedElements[0].getOutlineColor();
                    
                    Color newColor = JColorChooser.showDialog(canvas, "Select " + type + " Color", oldColor);
                    if (newColor == null)
                        return;
                    
                    if (type == Type.FILL)
                        canvas.setFillColor(newColor);
                    else
                        canvas.setOutlineColor(newColor);
        

                }
            }
        });
    }

    
        
        
}
