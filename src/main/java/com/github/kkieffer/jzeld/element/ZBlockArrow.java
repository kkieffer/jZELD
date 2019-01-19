
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.adapters.DialogUtils;
import com.github.kkieffer.jzeld.draw.BoundaryDraw;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
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
 * A ZBlockArrow is a block arrow with an adjustable head and shaft
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZBlockArrow")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZBlockArrow extends ZPolygon {
  
    public static final ImageIcon arrowIcon = new ImageIcon(ZBlockArrow.class.getResource("/arrow.png")); 

    
    private double shaftPercent;  //percent of the height for the height of the shaft
    private double headWidth;  //width, in units, of the arrowhead
    
    transient private ArrowDialog dialog;
  
    protected ZBlockArrow(){}
    
     /**
     * Create a ZBlockArrow
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
     */
    public ZBlockArrow(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, StrokeStyle lineStyle) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor, lineStyle);
        this.shaftPercent = 0.3;
        this.headWidth = 0.25 * width;
    }
    
    
    public ZBlockArrow(ZBlockArrow copy, boolean forNew) {
        super(copy, forNew);
        shaftPercent = copy.shaftPercent;
        headWidth = copy.headWidth;
    }
    
    @Override
    public ZBlockArrow copyOf(boolean forNew) {
        return new ZBlockArrow(this, forNew);
    }
    
    @Override
    protected String getShapeSummary() {       
        return "A block arrow with adjustable head and shaft sizes.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "Double click on the arrow to set the width of the arrow head and the percent height of the arrow shaft.";     
    }
    
    /**
     * Resize the object, new width and height in pixels, preserve the head size
     * @param w width in pixels
     * @param h height in pixels
     * @param minSize the minimum size, in pixels
     * @param scale scale factor
     */
    @Override
    public void setSize(double w, double h, double minSize, double scale) {
   
        //Hold onto the old values
        Rectangle2D r = getBounds2D(scale);
        double oldWidth = r.getWidth();
        double oldHeight = r.getHeight();

        //Compute the new size, which may limit the minimum
        super.setSize(w, h, minSize, scale);
        
        //Get the new size and calculate the ratio of new to old
        r = getBounds2D(scale);

        double widthRatio = r.getWidth() / oldWidth;
        double heightRatio = r.getHeight() / oldHeight;

        double headScale = (double)Math.sqrt(widthRatio * heightRatio);

        headWidth *= headScale;
        
        if (dialog != null)
            dialog.updateValue();
        
    }
    
    @Override
    protected Shape getPolygon(double width, double height, double scale) {
          
        ArrayList<Point2D> vertexes = new ArrayList<>(7);
       
        //arrow point
        vertexes.add(new Point2D.Double(width, height/2.0));
        
        if (headWidth*scale > width)
            headWidth = width/scale;
        
        double hw = headWidth*scale;
       
        //bottom arrow vertex
        vertexes.add(new Point2D.Double(width-hw, height));
        
        double lowerCenter = height/2.0 + 0.5*(shaftPercent*height);
        double upperCenter = height/2.0 - 0.5*(shaftPercent*height);
        
        //bottom center arrow vertex
        vertexes.add(new Point2D.Double(width-hw, lowerCenter));
        
        //lower shaft end
        vertexes.add(new Point2D.Double(0, lowerCenter));
        
        //upper shaft end
        vertexes.add(new Point2D.Double(0, upperCenter));
        
        //top center arrow vertex
        vertexes.add(new Point2D.Double(width-hw, upperCenter));
        
        //top arrow vertex
        vertexes.add(new Point2D.Double(width-hw, 0));
        
        return BoundaryDraw.pathFromPoints(vertexes, true);
    }
    
   
   @Override
    public boolean selectedForEdit(ZCanvas canvas) {
        
        dialog = new ArrowDialog(this, canvas);
        //Put the editor on the right side of the canvas frame
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
    public boolean supportsEdit() {
        return true;
    }
    
    @Override
    public void removedFrom(ZCanvas canvas) {
        deselectedForEdit();
    }

    private void setShaftHeightPercent(double s) {
        if (s < 0)
            s = 0;
        if (s > 100)
            s = 100;
        shaftPercent = s/100.0;
        changed();
    }

    private void setHeadWidth(double w) {
        headWidth = w;
        changed();
    }

    
    private static class ArrowDialog extends JFrame {

        private final ZBlockArrow arrow;
        private ZCanvas canvas;
        private final double unitScale;
        private final JSpinner headSpinner;
        
        private double getMaxWidth() {
            Rectangle2D b = arrow.getBounds2D();
            double max = b.getWidth();
            max *= unitScale;  //convert to unit
            return max;
        }
        
        private ArrowDialog(ZBlockArrow s, ZCanvas c) {
            super("Modify Block Arrow");
            arrow = s;
            canvas = c;
            unitScale = canvas.getUnit().getScale();

            JPanel p = new JPanel();
            
            JLabel shaftHeight = new JLabel("Shaft Height (Percent of Height)");
            JSpinner heightSpinner = new JSpinner();
            heightSpinner.setModel(new javax.swing.SpinnerNumberModel(arrow.shaftPercent, 0, 100, 1));
            heightSpinner.setValue(arrow.shaftPercent * 100);
            
            JLabel headWidth = new JLabel("Head Width (" + canvas.getUnit().getName() + ")");
            headSpinner = new JSpinner();
            headSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, getMaxWidth(), getMaxWidth()/100));
            headSpinner.setValue(arrow.headWidth*unitScale);
 
         
            heightSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    arrow.setShaftHeightPercent((double)heightSpinner.getValue());
                    canvas.repaint();
                }
            });
            
            headSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    arrow.setHeadWidth((double)headSpinner.getValue()/unitScale);
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
            p.add(shaftHeight, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            heightSpinner.setPreferredSize(new Dimension(80, 26));
            p.add(heightSpinner, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            p.add(headWidth, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            headSpinner.setPreferredSize(new Dimension(80, 26));
            p.add(headSpinner, gridBagConstraints);

            
            Container main = getContentPane();
            main.setLayout(new BorderLayout());
            
            main.add(p, BorderLayout.CENTER);
            JLabel icon = new JLabel();
            icon.setIcon(arrowIcon);
            Border margin = new EmptyBorder(0,15,15,15);
            icon.setBorder(new CompoundBorder(icon.getBorder(), margin));
            main.add(icon, BorderLayout.WEST);

            pack();
            Dimension d = new Dimension(340, 225);
            setMinimumSize(d);
            setPreferredSize(d);
            
            DialogUtils.addShortcutAndIcon(p, "dispose");

        }
        
        private void updateValue() {
            headSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, getMaxWidth(), getMaxWidth()/100));
            headSpinner.setValue(arrow.headWidth*unitScale);
        }
    }
    
}
