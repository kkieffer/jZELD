
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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
    
    @XmlTransient
    private ArcDialog dialog;
    
    @XmlTransient     
    private Shape shape;

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
     * @param startAngle start angle, 0 = "east pointing"
     * @param arcAngle sweep angle, counterclockwise
     * @param arcType the type of closure
     */
    public ZArc(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, double startAngle, double arcAngle, ArcType arcType) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor);
        this.startAngle = startAngle;
        this.arcAngle = arcAngle;
        this.type = arcType;
    }
    
    public ZArc(ZArc copy) {
        super(copy);
        this.startAngle = copy.startAngle;
        this.arcAngle = copy.arcAngle;
        this.type = copy.type;
    }
    
    @Override
    public ZRectangle copyOf() {
        return new ZArc(this);
    }
    
    @Override
    protected Shape getAbstractShape() {
        Rectangle2D r = getBounds2D();
        return new Arc2D.Double(0, 0, r.getWidth(), r.getHeight(), startAngle, arcAngle, type.typeVal);
    }
    
    @Override
    protected void fillShape(Graphics2D g, int unitSize, int width, int height) {
        g.fill(shape);
    }

    @Override
    protected void drawShape(Graphics2D g, int unitSize, int width, int height) {
        g.draw(shape);
    }
    
    public void setStartAngle(double start) {
        startAngle = start;
        hasChanges = true;
    }

    public void setArcAngle(double angle) {
        arcAngle = angle;
        hasChanges = true;
    }
    
    public void setArcType(ArcType t) {
        type = t;
        hasChanges = true;
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
    public boolean selected(ZCanvas canvas) {
        
        dialog = new ArcDialog(this);
        dialog.setLocationRelativeTo(canvas);
        dialog.setVisible(true);

        return false;
    }
    
      @Override
    public void deselected() {
        
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }
    
    @Override
    public void removedFrom(ZCanvas canvas) {
        deselected();
    }

     @Override
    public void paint(Graphics2D g, int unitSize, int width, int height) {
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(unitSize, unitSize);
        shape = scaleInstance.createTransformedShape(getAbstractShape());
        
        super.paint(g, unitSize, width, height);

    }
  
    
    private static class ArcDialog extends JFrame {

        private final ZArc arc;
        
        private ArcDialog(ZArc a) {
            super("Modify Arc");
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
                }
                
            });
            
            startAngleSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    arc.setStartAngle((Double)startAngleSpinner.getValue());
                }
            });
            arcAngleSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    arc.setArcAngle((Double)arcAngleSpinner.getValue());
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

            getContentPane().add(p, java.awt.BorderLayout.CENTER);

            pack();
            Dimension d = new Dimension(300, 300);
            setMinimumSize(d);
            setPreferredSize(d);
        }
    }
        
    
    
}
