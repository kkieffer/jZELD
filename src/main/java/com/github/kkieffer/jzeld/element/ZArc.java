
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.adapters.DialogUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
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
@XmlRootElement(name = "ZArc")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZArc extends ZRectangle {

    public static final ImageIcon arcIcon = new ImageIcon(ZCanvas.class.getResource("/arc.png")); 

    public enum ArcType {
        OPEN (Arc2D.OPEN), 
        PIE (Arc2D.PIE), 
        CHORD (Arc2D.CHORD);
    
        private final int typeVal;
        private ArcType(int t) {
            typeVal = t;
        }
    };
    
    private double startAngle;
    private double arcAngle;
    private ArcType type;
    
    transient private ArcDialog dialog;
    
    protected ZArc() {}
    
    /**
     * Create a ZArc
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
     * @param startAngle start angle, 0 = "east pointing"
     * @param arcAngle sweep angle, counterclockwise
     * @param arcType the type of closure
     */
    public ZArc(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, StrokeStyle borderStyle, double startAngle, double arcAngle, ArcType arcType) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor, borderStyle);
        this.startAngle = startAngle;
        this.arcAngle = arcAngle;
        this.type = arcType;
    }
    
    public ZArc(ZArc copy, boolean forNew) {
        super(copy, forNew);
        this.startAngle = copy.startAngle;
        this.arcAngle = copy.arcAngle;
        this.type = copy.type;
    }
    
    @Override
    protected String getShapeSummary() {       
        return "A section of an ellispe.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "Double click on the arc to set its start angle and extent angle, and its type.";   
    }
    
    @Override
    public ZRectangle copyOf(boolean forNew) {
        return new ZArc(this, forNew);
    }
    
    @Override
    protected Shape getPolygon(double width, double height, double scale) {
        return new Arc2D.Double(0, 0, width, height, -startAngle, -arcAngle, type.typeVal);
    }
    
    
   
    public void setStartAngle(double start) {
        startAngle = start;
        changed();
    }

    public void setArcAngle(double angle) {
        arcAngle = angle;
        changed();
    }
    
    public void setArcType(ArcType t) {
        type = t;
        changed();
    }
    
    public double getStartAngle() {
        return startAngle;
    }

    public double getArcAngle() {
        return arcAngle;
    }
   
    public ArcType getArcType() {
        return type;
    }
    
     @Override
    public boolean supportsEdit() {
        return true;
    };

    @Override
    public boolean selectedForEdit(ZCanvas canvas) {
        
        dialog = new ArcDialog(this, canvas);
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

    
    private static class ArcDialog extends JFrame {

        private final ZArc arc;
        
        private ArcDialog(ZArc a, ZCanvas canvas) {
            super("Edit Arc");
            arc = a;

            JPanel p = new JPanel();
            
            JLabel startLabel = new JLabel("Start Angle");
            JSpinner startAngleSpinner = new JSpinner();
            startAngleSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 360.0d, 1.0d));
            startAngleSpinner.setValue(arc.getStartAngle());
            
            JLabel arcLabel = new JLabel("Arc Angle");
            JSpinner arcAngleSpinner = new JSpinner();
            arcAngleSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 360.0d, 1.0d));
            arcAngleSpinner.setValue(arc.getArcAngle());

            JLabel typeLabel = new JLabel("Closure Type");
            JComboBox<ArcType> typeCombo = new JComboBox<>();
            for (ArcType t : ArcType.values())
                typeCombo.addItem(t);
            typeCombo.setSelectedItem(arc.getArcType());
            typeCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    arc.setArcType((ArcType)typeCombo.getSelectedItem());
                    canvas.repaint();
                }
                
            });
            
            startAngleSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    arc.setStartAngle((Double)startAngleSpinner.getValue());
                    canvas.repaint();
                }
            });
            arcAngleSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    arc.setArcAngle((Double)arcAngleSpinner.getValue());
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
            p.add(startLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            p.add(startAngleSpinner, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            p.add(arcLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            p.add(arcAngleSpinner, gridBagConstraints);
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            p.add(typeLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            p.add(typeCombo, gridBagConstraints);

            Container main = getContentPane();
            main.setLayout(new BorderLayout());
            
            main.add(p, BorderLayout.CENTER);
            JLabel icon = new JLabel();
            icon.setIcon(arcIcon);
            Border margin = new EmptyBorder(0,15,0,15);
            icon.setBorder(new CompoundBorder(icon.getBorder(), margin));
            main.add(icon, BorderLayout.WEST);

            pack();
            Dimension d = new Dimension(340, 225);
            setMinimumSize(d);
            setPreferredSize(d);
            
            DialogUtils.addShortcutAndIcon(p, "dispose");
            
        }
    }
        
    
    
}
