
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
import java.io.IOException;
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
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZQuadrilateral")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZQuadrilateral extends ZPolygon {

    public static final ImageIcon skewIcon = new ImageIcon(ZCanvas.class.getResource("/skew.png")); 
     
    public enum QuadType {SQUARE, PARALLELOGRAM, TRAPEZOID, RHOMBUS, DIAMOND}
    
    protected QuadType type;
    protected double percent;

    private transient double[] x = new double[4];  //x verticies
    private transient double[] y = new double[4];  //y verticies
    
 
    private transient QuadrilateralDialog dialog;

    //Custom deserialize - needed to create new transient objects
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        x = new double[4];
        y = new double[4];
    }
        
        
    protected ZQuadrilateral(){}
    
     /**
     * Create a ZQuadrilateral
     * @param type the specific type
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the object in units, or -1 for unlimited width
     * @param height the height of the object in units, or -1 for unlimited height
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     * @param borderStyle
     * @param percent the percent of skew from 0 (rectangle) to 100 (maximum angle in bounds)
     */
    public ZQuadrilateral(QuadType type, double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, StrokeStyle borderStyle, double percent) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor, borderStyle);
        this.type = type;
        this.percent = percent;
        this.setName("ZQuadr:" + type.name());
    }
    
    
    public ZQuadrilateral(ZQuadrilateral copy, boolean forNew) {
        super(copy, forNew);
        this.type = copy.type;
        this.percent = copy.percent;
    }
    
    @Override
    protected String getShapeSummary() {       
        return "A 4-sided polygon that is a " + type.name().toLowerCase() + ".";
    }
    
    @Override
    protected String getShapeDescription() {
        return "Double click on the polygon to select the skew from 0 to 100 percent, and set the shape bounds to the minimum.";     
    }
 
    
    @Override
    public ZQuadrilateral copyOf(boolean forNew) {
        return new ZQuadrilateral(this, forNew);
    }
    
    /**
     * Adjust the number of percent of slope (meaning depends on shape)
     * @param p the percent, from 0 to 100
     */
    public void setPercent(double p) {
        if (p < 0 || p > 100)
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        percent = p;
        changed();
    }
    
    public double getPercent() {
        return percent;
    }
     
    @Override
    public boolean supportsEdit() {
        return true;
    };
    
    
    @Override
    protected Shape getPolygon(double width, double height, double scale) {
                  
        //Move counterclockwise around - these are defaults (rectangle)
        x[0] = 0;
        x[1] = width;
        x[2] = width;
        x[3] = 0;
       
        y[0] = 0;
        y[1] = 0;
        y[2] = height;
        y[3] = height;
        
        
        switch (type) {
            case SQUARE:
                getSquare(width, height);
                break;
            case PARALLELOGRAM:
                getParallelogram(width, height);
                break;
            case TRAPEZOID:
                getTrapezoid(width, height);
                break;
            case DIAMOND:
                getDiamond(width, height);
                break;
            case RHOMBUS:
                getRhombus(width, height);
                break;
            default:
                break;
                
        }
        
        ArrayList<Point2D> points = new ArrayList<>(4);
        for (int i=0; i<4; i++)
            points.add(new Point2D.Double(x[i], y[i]));

        
        return BoundaryDraw.pathFromPoints(points, true);
  
    }
    
    private void getSquare(double w, double h) {
       
        double side = w > h ? h : w;
        
        x[1] = side;
        x[2] = side;
        y[2] = side;
        y[3] = side;
    }
       
    private void getParallelogram(double w, double h) {            
        x[0] = (double)w * (percent / 100.0);
        x[2] = w - x[0];

    }

    private void getTrapezoid(double w, double h) {
        x[0] = (double)w * (percent / 200.0);
        x[1] = w - x[0];
    }
    
    
    private void getDiamond(double w, double h) {
        x[0] = (double)w * (percent / 100.0);
        x[2] = w - x[0];
        
        y[1] = (double)h * (percent / 100.0);
        y[2] = h;
        y[3] = h - y[1];
    }
    
    
    
    private void getRhombus(double w, double h) {
        
        double shorter = w > h ? h : w;
        
 
        x[0] = (double)shorter * (percent / 200.0);
        x[1] = shorter;
        x[2] = x[1] - x[0];
        double hypotenuse = x[1] - x[0];

        y[2] = Math.sqrt(Math.pow(hypotenuse, 2) - Math.pow(x[0], 2));
        y[3] = y[2];
        
    }
    
    
    @Override
    public boolean selectedForEdit(ZCanvas canvas) {
            
        if (type != QuadType.SQUARE) {
            
            dialog = new QuadrilateralDialog(this, canvas);
            canvas.arrangePopup(dialog);
            dialog.setVisible(true);
            return false;
        }
        
        if (type == QuadType.SQUARE || type == QuadType.RHOMBUS) {  //just reduce the bounds to fit
            Rectangle2D bounds = this.getBounds2D(canvas.getScale());
            double w = bounds.getWidth();
            double h = bounds.getHeight();
                
            double s = w > h ? h : w;
            setSize(s, s, 0.0, canvas.getScale());
        }
        
        
        return false;
    }
    
    
   
    
    @Override
    public void deselectedForEdit() {

        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }
    
    
    
    private static class QuadrilateralDialog extends JFrame {

        private final ZQuadrilateral quad;
        
        private QuadrilateralDialog(ZQuadrilateral r, ZCanvas canvas) {
            super("Edit Quadrilateral");
            quad = r;        

           
            JPanel p = new JPanel();
            
            JLabel skewLabel = new JLabel("Percent of Maximum Skew");
            JSpinner percentSkewSpinner = new JSpinner();
            percentSkewSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 100.0d, 1.0d));
            percentSkewSpinner.setValue(quad.getPercent());
 
            percentSkewSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    quad.setPercent((double)percentSkewSpinner.getValue());  
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
            p.add(skewLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            p.add(percentSkewSpinner, gridBagConstraints);

            percentSkewSpinner.setPreferredSize(new Dimension(100, 26));
            Container main = getContentPane();
            main.setLayout(new BorderLayout());
            
            main.add(p, BorderLayout.CENTER);
            JLabel icon = new JLabel();
            icon.setIcon(skewIcon);
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
