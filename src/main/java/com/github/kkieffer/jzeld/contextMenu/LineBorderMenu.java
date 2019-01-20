
package com.github.kkieffer.jzeld.contextMenu;

import com.github.kkieffer.jzeld.ZCanvas;
import static com.github.kkieffer.jzeld.ZCanvas.errorIcon;
import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.ComponentOrientation;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

/**
 * LineBorderMenu class provides a JMenu with two sub menu items for line weight and line dash pattern.  When an menu item is selected,
 * it calls the method to set the element line weight or dash in the canvas on whatever the selected element is.
 * 
 * @author kkieffer
 */
public class LineBorderMenu extends JMenu {

   public enum Type {WEIGHT, DASH}
    
    public static final ImageIcon lineIcon = new ImageIcon(ZCanvas.class.getResource("/line.png")); 

    
    private static final float[] WIDTHS = {0.0f, 0.25f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f, 6.0f, 8.0f, -1};  //-1 is custom
    private static final Float[][] DASHES = {new Float[0],
                                        new Float[]{.025f, .025f},
                                        new Float[]{.025f, .05f},
                                        new Float[]{.05f, .05f},
                                        new Float[]{.1f, .1f},
                                        new Float[]{.1f, .1f, .05f, .1f},
                                        new Float[]{.2f, .2f, .05f, .2f},
                                        new Float[]{.05f, .2f},
                                        new Float[]{.2f, .1f},
                                        new Float[]{.2f, .3f},
                                        new Float[]{.3f, .1f}
        
    };
    
    private static class WeightMenuItem extends AbstractContextMenuItem {
        
        
        public WeightMenuItem(ZCanvas c, GraphicsConfiguration gC, float thickStep) {
            
            super("", c);
            createMenuGraphics();

            DecimalFormat fmt = new DecimalFormat("0.0");
            
            if (thickStep >= 0) {
                g.setStroke(new BasicStroke(thickStep));
                String label = fmt.format(thickStep);
                setText(label);            
                g.drawLine(10, 8, 90, 8);                    
            }
            else 
                setText("Set...");
            
            g.dispose();

            this.setIcon(new ImageIcon(bufferedImage));
        
            addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (canvas == null)
                        return;
                    
                    if (thickStep < 0) {
                        //Prompt for custom size
                        String rc = (String)JOptionPane.showInputDialog(canvas.getParent(), "Set Custom Line Width", "Modify Line Width", JOptionPane.QUESTION_MESSAGE, lineIcon, (Object[])null, "");
                        if (rc != null) {
                            try {
                                float f = Float.parseFloat(rc);
                                if (f < 0)
                                    throw new NumberFormatException("Value cannot be negative");
                                 canvas.setOutlineWidth(f); 
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(canvas.getParent(), "Invalid value: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, errorIcon);
                            }
                        }
                    }
                    else {
                        canvas.setOutlineWidth(thickStep);
                        lineWidthChanged(thickStep);
                    }
                }
            });
        }
        
    }
    
   
    
    private static class DashMenuItem extends AbstractContextMenuItem {
        
        
        public DashMenuItem(ZCanvas c, GraphicsConfiguration gC, Float[] dash) {
            
            super("", c);

            createMenuGraphics();

            if (dash.length == 0) 
                g.setStroke(new BasicStroke(1));
            else {
                
                float[] d = new float[dash.length];
                for (int i=0; i<dash.length; i++)
                    d[i] = dash[i] * 72;
                 
                g.setStroke(new BasicStroke(1, CAP_SQUARE, JOIN_MITER, 10.0f, d, 0.0f));
            }
            
            g.drawLine(0, 8, 100, 8);
            g.dispose();

            this.setIcon(new ImageIcon(bufferedImage));
            
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
    
    
    
    private final ArrayList<AbstractContextMenuItem> menuItemList = new ArrayList<>();
    
  
    public LineBorderMenu(String text, ZCanvas c, Type type) {
        super(text);

        GraphicsConfiguration gC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            
        switch (type) {
        
            case WEIGHT: //Solid lines
                for (float i : WIDTHS) {
                    WeightMenuItem weightMenuItem = new WeightMenuItem(c, gC, i);
                    weightMenuItem.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                    menuItemList.add(weightMenuItem);
                    this.add(weightMenuItem);   
                }
                break;
            
            case DASH: //Dashed lines
                for (Float[] d : DASHES) {
                    DashMenuItem dashMenuItem = new DashMenuItem(c, gC, d);
                    menuItemList.add(dashMenuItem);
                    this.add(dashMenuItem);   
                }
                break;
                
            default:
                throw new RuntimeException("Unhandled Line Border Type");
        }
                    
    }
    
    public void addListener(ContextMenuListener l) {
        for (AbstractContextMenuItem m : menuItemList)
            m.addListener(l);
    }
    
    public void removeListener(ContextMenuListener l) {
        for (AbstractContextMenuItem m : menuItemList)
            m.removeListener(l);
    }
    
    public void setCanvas(ZCanvas c) {
        for (AbstractContextMenuItem m : menuItemList)
            m.setCanvas(c);
    }
    
}
