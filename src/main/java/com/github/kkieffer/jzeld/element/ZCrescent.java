
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.adapters.DialogUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A ZArc is an object that defines a arc (pie) shape with a border (that has color and thickness) and an interior color.  The arc is  
 * circumscribed by the bounds rectangle. The bounds rectangle top left corner is at the object's position.  If rotated,
 * the rotation occurs about the center of the arc.
 *  
 * @author kkieffer
 */
@XmlRootElement(name = "ZCrescent")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZCrescent extends ZRectangle {

    public static final ImageIcon crescentIcon = new ImageIcon(ZCanvas.class.getResource("/crescent.png")); 
    
    private double amount;

    transient private CrescentDialog dialog;
    
    protected ZCrescent() {}
    
    /**
     * Create a ZCrescent
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the object in units, or -1 for unlimited width
     * @param height the height of the object in units, or -1 for unlimited height
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove if the object can be moved by the mouse drag
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     * @param borderStyle
     * @param amount amount to show.  0.0 = none, 1.0 = half moon, 2.0 = full moon
     */
    public ZCrescent(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, StrokeStyle borderStyle, double amount) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor, borderStyle);
        this.amount = amount;
    }
    
    public ZCrescent(ZCrescent copy, boolean forNew) {
        super(copy, forNew);
        this.amount = copy.amount;
    }
    
    @Override
    protected String getShapeSummary() {       
        return "An arc removed from a circle (a moon).";
    }
    
    @Override
    protected String getShapeDescription() {
        return "Double click on the crescent to set the amount to show. The amount ranges from 0.0 to 2.0, with 1.0 showing a full hemisphere, and 2.0 showing a sphere.";   
    }
    
    @Override
    public ZRectangle copyOf(boolean forNew) {
        return new ZCrescent(this, forNew);
    }
    
    @Override
    protected Shape getPolygon(double width, double height, double scale) {
        Arc2D hemi = new Arc2D.Double(0, 0, width, height, -90, -180, Arc2D.CHORD); //hemisphere
        Area a = new Area(hemi);
         
        
        if (amount <= 1.0) {
            double xOffset = (width/2) * amount;
            Arc2D sub = new Arc2D.Double(xOffset+.000001, 0, 2*((width/2)-xOffset), height, -90, -180, Arc2D.CHORD); 
            a.subtract(new Area(sub));
        }
        else {
            double xOffset = width/2 * (2.0 - amount);
            Arc2D add = new Arc2D.Double(xOffset-.000001, 0, 2*((width/2)-xOffset), height, -90, 180, Arc2D.CHORD); 
            a.add(new Area(add));          
        }
               
        
        return a;
    }
    
    
   
    public void setAmount(double amount) {
        if (amount <= 0 || amount > 2.0)
            return;
            
        this.amount = amount;
        changed();
    }

    
    public double getAmount() {
        return amount;
    }

  
    
     @Override
    public boolean supportsEdit() {
        return true;
    };

    @Override
    public boolean selectedForEdit(ZCanvas canvas) {
        
        dialog = new CrescentDialog(this, canvas);
        canvas.arrangePopup(dialog);
        dialog.setVisible(true);

        return false;
    }
    
      @Override
    public void deselectedForEdit() {
        
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }
    
    @Override
    public void removedFrom(ZCanvas canvas) {
        deselectedForEdit();
    }

    
    private static class CrescentDialog extends JFrame {

        private final ZCrescent cre;
        
        private CrescentDialog(ZCrescent c, ZCanvas canvas) {
            super("Edit Crescent");
            cre = c;

            JPanel p = new JPanel();
            
            JLabel amountLabel = new JLabel("Amount");
            JSpinner amountAngleSpinner = new JSpinner();
            amountAngleSpinner.setModel(new javax.swing.SpinnerNumberModel(0.00d, 0.0d, 2.0d, 0.01d));
            amountAngleSpinner.setValue(cre.getAmount());
            amountAngleSpinner.setEditor(new JSpinner.NumberEditor(amountAngleSpinner, "0.00"));
            
            amountAngleSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    cre.setAmount((Double)amountAngleSpinner.getValue());
                    canvas.repaint();
                }
            });
            
            
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
            layout.columnWidths = new int[] {0, 10, 0, 10, 0};
            layout.rowHeights = new int[] {0, 10, 0, 10, 0};
            p.setLayout(layout);
        
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            p.add(amountLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            amountAngleSpinner.setPreferredSize(new Dimension(80, 26));
            p.add(amountAngleSpinner, gridBagConstraints);

            Container main = getContentPane();
            main.setLayout(new BorderLayout());
            
            main.add(p, BorderLayout.CENTER);
            JLabel icon = new JLabel();
            icon.setIcon(crescentIcon);
            Border margin = new EmptyBorder(0,15,0,15);
            icon.setBorder(new CompoundBorder(icon.getBorder(), margin));
            main.add(icon, BorderLayout.WEST);

            pack();
            Dimension d = new Dimension(310, 225);
            setMinimumSize(d);
            setPreferredSize(d);
            
            DialogUtils.addShortcutAndIcon(p, "dispose");
            
        }
    }
        
    
    
}
