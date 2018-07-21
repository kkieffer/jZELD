
package com.github.kkieffer.jzeld.contextMenu;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.ImageIcon;
import javax.swing.JMenu;

/**
 * LineBorderMenu class provides a JMenu with two sub menu items for line weight and line dash pattern.  When an menu item is selected,
 * it calls the method to set the element line weight or dash in the canvas on whatever the selected element is.
 * 
 * @author kkieffer
 */
public class LineBorderMenu extends JMenu {

   public enum Type {WEIGHT, DASH}
    
    
    
    private static final float[] WIDTHS = {0.0f, 0.25f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f, 6.0f, 8.0f};
    private static final Float[][] DASHES = {new Float[0],
                                        new Float[]{.05f, .05f},
                                        new Float[]{.1f, .1f},
                                        new Float[]{.1f, .1f, .05f, .1f},
                                        new Float[]{.2f, .2f, .05f, .2f},
                                        new Float[]{.05f, .2f},
                                        new Float[]{.2f, .1f},
                                        new Float[]{.3f, .1f}
        
    };
    
    private static class WeightMenuItem extends AbstractContextMenu {
        
        
        public WeightMenuItem(ZCanvas c, GraphicsConfiguration gC, float thickStep) {
            
            super(c);
            DecimalFormat fmt = new DecimalFormat("0.0");
            BufferedImage bimg = gC.createCompatibleImage(100, 16, Transparency.BITMASK);
            Graphics2D g = (Graphics2D)bimg.getGraphics();
            
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(thickStep));
            String label = fmt.format(thickStep);
            int strW = g.getFontMetrics().stringWidth(label) + 10;
            g.drawString(label, 0, 12);
            if (thickStep > 0.0)
                g.drawLine(strW, 8, 100, 8);
            

            this.setIcon(new ImageIcon(bimg));
                        
            
            addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (canvas == null)
                        return;
                    canvas.setOutlineWidth(thickStep);
                    lineWidthChanged(thickStep);
                }
            });
        }
        
    }
    
   
    
    private static class DashMenuItem extends AbstractContextMenu {
        
        
        public DashMenuItem(ZCanvas c, GraphicsConfiguration gC, Float[] dash) {
            
            super(c);

            BufferedImage bimg = gC.createCompatibleImage(100, 16, Transparency.BITMASK);
            Graphics2D g = (Graphics2D)bimg.getGraphics();
            g.setColor(Color.BLACK);
            
            if (dash.length == 0) 
                g.setStroke(new BasicStroke(1));
            else {
                
                float[] d = new float[dash.length];
                for (int i=0; i<dash.length; i++)
                    d[i] = dash[i] * 72;
                 
                g.setStroke(new BasicStroke(1, CAP_SQUARE, JOIN_MITER, 10.0f, d, 0.0f));
            }
            
            g.drawLine(0, 8, 100, 8);
            this.setIcon(new ImageIcon(bimg));
            
            addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {  
                    if (canvas == null)
                        return;
                    
                    lineDashChanged(dash);
                    canvas.setDashPattern(dash);

                }
            });
        }
        
    }
    
    
    
    private AbstractContextMenu menuItem;
    
  
    public LineBorderMenu(String text, ZCanvas c, Type type) {
        super(text);

        GraphicsConfiguration gC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            
        switch (type) {
        
            case WEIGHT: //Solid lines
                for (float i : WIDTHS) {
                    menuItem = new WeightMenuItem(c, gC, i);
                    this.add(menuItem);   
                }
                break;
            
            case DASH: //Dashed lines
                for (Float[] d : DASHES) {
                    menuItem = new DashMenuItem(c, gC, d);
                    this.add(menuItem);   
                }
                break;
                
            default:
                throw new RuntimeException("Unhandled Line Border Type");
        }
                    
    }
    
    public void addListener(ContextMenuListener l) {
        menuItem.addListener(l);
    }
    
    public void removeListener(ContextMenuListener l) {
        menuItem.removeListener(l);
    }
    
    public void setCanvas(ZCanvas c) {
        menuItem.setCanvas(c);
    }
    
}
