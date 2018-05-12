
package com.github.kkieffer.jzeld;

import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * LineBorderMenu class provides a JMenu with two sub menu items for line weight and line dash pattern.  When an menu item is selected,
 * it calls the method to set the element line weight or dash in the canvas on whatever the selected element is.
 * 
 * @author kkieffer
 */
public class LineBorderMenu extends JMenu {
    
    public enum Type {WEIGHT, DASH}
    
    
    
    private static final int[] WIDTHS = {0, 1, 2, 4, 6, 8};
    private static final Float[][] DASHES = {new Float[0],
                                        new Float[]{.05f, .05f},
                                        new Float[]{.1f, .1f},
                                        new Float[]{.1f, .1f, .05f, .1f},
                                        new Float[]{.2f, .2f, .05f, .2f},
                                        new Float[]{.05f, .2f},
                                        new Float[]{.2f, .1f},
                                        new Float[]{.3f, .1f}
        
    };
    
    private class WeightMenuItem extends JMenuItem {
        
        
        public WeightMenuItem(GraphicsConfiguration gC, int thickStep) {
            
            super();

            BufferedImage bimg = gC.createCompatibleImage(50, 16, Transparency.BITMASK);
            Graphics2D g = (Graphics2D)bimg.getGraphics();
            g.setColor(Color.BLACK);
            if (thickStep > 0) {
                g.setStroke(new BasicStroke(thickStep));
                g.drawLine(0, 8, 50, 8);
            }
            else {
                g.setStroke(new BasicStroke(1));
                g.drawString("Empty", 0, 12);
            }

            this.setIcon(new ImageIcon(bimg));
                        
            
            addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    canvas.setOutlineWidth(thickStep);
                }
            });
        }
        
    }
    
   
    
    private class DashMenuItem extends JMenuItem {
        
        
        public DashMenuItem(GraphicsConfiguration gC, Float[] dash) {
            
            super();

            BufferedImage bimg = gC.createCompatibleImage(100, 16, Transparency.BITMASK);
            Graphics2D g = (Graphics2D)bimg.getGraphics();
            g.setColor(Color.BLACK);
            
            if (dash.length == 0) 
                g.setStroke(new BasicStroke(1));
            else {
                
                float[] d = new float[dash.length];
                for (int i=0; i<dash.length; i++)
                    d[i] = dash[i] * (float)canvas.getScale();
                 
                g.setStroke(new BasicStroke(1, CAP_SQUARE, JOIN_MITER, 10.0f, d, 0.0f));
            }
            
            g.drawLine(0, 8, 100, 8);
            this.setIcon(new ImageIcon(bimg));
            
            addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {  
                    
                    canvas.setDashPattern(dash);

                }
            });
        }
        
    }
    
    
    
    private final ZCanvas canvas;
    
    LineBorderMenu(String text, ZCanvas canvas, Type type) {
        super(text);
        this.canvas = canvas;

        GraphicsConfiguration gC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            
        switch (type) {
        
            case WEIGHT: //Solid lines
                for (int i : WIDTHS) {
                    WeightMenuItem b = new WeightMenuItem(gC, i);
                    this.add(b);   
                }
                break;
            
            case DASH: //Dashed lines
                for (Float[] d : DASHES) {
                    DashMenuItem b = new DashMenuItem(gC, d);
                    this.add(b);   
                }
                break;
        }
                    
    }
    
    
}
