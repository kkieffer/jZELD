
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.adapters.DialogUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
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
 * A ZRoundedRectangle is a ZRectangle with rounded corners
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZRoundedRectangle")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZRoundedRectangle extends ZRectangle {

    public static final ImageIcon radiusIcon = new ImageIcon(ZCanvas.class.getResource("/radius.png")); 

    
    protected double radius;

    transient private RoundedRectDialog dialog;

    
    protected ZRoundedRectangle(){}
    
     /**
     * Create a ZRoundedRectangle
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the object in units, or -1 for unlimited width
     * @param height the height of the object in units, or -1 for unlimited height
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     * @param radius the radius of the rounded edges, in units
     */
    public ZRoundedRectangle(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, StrokeStyle borderStyle, double radius) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor, borderStyle);
        this.radius = radius;
    }
    
    
    public ZRoundedRectangle(ZRoundedRectangle copy, boolean forNew) {
        super(copy, forNew);
        this.radius = copy.radius;
    }
    
    @Override
    protected String getShapeSummary() {       
        return "A rectangle with rounded corners.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "Double click on the rectangle to select the radius of the corners. The radius unit is the same as the canvas measurement unit.";   
    }
    
    @Override
    public ZRoundedRectangle copyOf(boolean forNew) {
        return new ZRoundedRectangle(this, forNew);
    }
    
    /**
     * Adjust the radius
     * @param r the value in canvas units
     */
    public void setRadius(double r) {
        radius = r;
        changed();
    }
    
    public double getRadius() {
        return radius;
    }
     
    @Override
    protected Shape getPolygon(double width, double height, double scale) {
        return new RoundRectangle2D.Double(0, 0, width, height, scale*radius*2, scale*radius*2);
    }
    
    @Override
    public boolean supportsEdit() {
        return true;
    };
    
    @Override
    public boolean selectedForEdit(ZCanvas canvas) {
        
        dialog = new RoundedRectDialog(this, canvas);
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
    
    
    
    private static class RoundedRectDialog extends JFrame {

        private final ZRoundedRectangle rect;
        
        private RoundedRectDialog(ZRoundedRectangle r, ZCanvas canvas) {
            super("Edit Rounded Rectangle");
            rect = r;        
            double unitScale = canvas.getUnit().getScale();

            
            Rectangle2D b = rect.getBounds2D();
            double max = b.getWidth() > b.getHeight() ? b.getWidth() : b.getHeight();
            
            max *= unitScale/2;  //half the max, and convert to unit

            JPanel p = new JPanel();
            
            JLabel cornerLabel = new JLabel("Corner Radius (" + canvas.getUnit().getName() + ")");
            JSpinner cornerRadiusSpinner = new JSpinner();
            cornerRadiusSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, max, max/100));
            cornerRadiusSpinner.setValue(rect.getRadius()*unitScale);
 
            cornerRadiusSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    double rad = (double)cornerRadiusSpinner.getValue();
                    rect.setRadius(rad/unitScale);  //set the new radius in canvas units
                    canvas.repaint();
               }
            });
            
            
            
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
            layout.columnWidths = new int[] {0, 10, 0, 40, 0};
            layout.rowHeights = new int[] {0, 10, 0};
            p.setLayout(layout);
        
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            p.add(cornerLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            p.add(cornerRadiusSpinner, gridBagConstraints);

            cornerRadiusSpinner.setPreferredSize(new Dimension(100, 26));
            Container main = getContentPane();
            main.setLayout(new BorderLayout());
            
            main.add(p, BorderLayout.CENTER);
            JLabel icon = new JLabel();
            icon.setIcon(radiusIcon);
            Border margin = new EmptyBorder(0,15,0,15);
            icon.setBorder(new CompoundBorder(icon.getBorder(), margin));
            main.add(icon, BorderLayout.WEST);

            pack();
            Dimension d = new Dimension(340, 175);
            setMinimumSize(d);
            setPreferredSize(d);
            
            DialogUtils.addShortcutAndIcon(p, "dispose");
            
        }
    }
   

    
}
