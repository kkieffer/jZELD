
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.ZCanvas;
import static com.github.kkieffer.jzeld.ZCanvas.errorIcon;
import com.github.kkieffer.jzeld.draw.BoundaryDraw;
import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZQuadrilateral")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZQuadrilateral extends ZPolygon {

    public static final ImageIcon radiusIcon = new ImageIcon(ZCanvas.class.getResource("/skew.png")); 
     
    public enum QuadType {SQUARE, PARALLELOGRAM, TRAPEZOID, RHOMBUS, DIAMOND}
    
    protected QuadType type;
    protected int percent;

    @XmlTransient
    double[] x = new double[4];  //x verticies
    
    @XmlTransient
    double[] y = new double[4];  //y verticies
    
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
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     * @param percent the percent of skew from 0 (rectangle) to 100 (maximum angle in bounds)
     */
    public ZQuadrilateral(QuadType type, double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, int percent) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor);
        this.type = type;
        this.percent = percent;
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
        return "Double click on the polygon to select the skew from 0 to 100 percent.";     
    }
 
    
    @Override
    public ZQuadrilateral copyOf(boolean forNew) {
        return new ZQuadrilateral(this, forNew);
    }
    
    /**
     * Adjust the number of percent of slope (meaning depends on shape)
     * @param p the percent, from 0 to 100
     */
    public void setPercent(int p) {
        if (p < 0 || p > 100)
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        percent = p;
        hasChanges = true;
    }
     
    @Override
    public boolean supportsEdit() {
        return true;
    };
    
    @Override
    public boolean selectedForEdit(ZCanvas canvas) {
        
        if (type == QuadType.SQUARE) //square has no skew
            return false;
        
        String rc = (String)JOptionPane.showInputDialog(canvas, "Update Percent of Maximum Skew", "Modify Quadrilateral", JOptionPane.QUESTION_MESSAGE, radiusIcon,
                                                (Object[])null, (Object)String.valueOf(percent));
        if (rc != null) {
            try {
                int pc = Integer.parseInt(rc);
                if (pc < 0 || pc > 100)
                    throw new NumberFormatException("Percent must be between 0 and 100");
                setPercent(pc);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(canvas, "Invalid value: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, errorIcon);
            }
        }
        
        return false;
    }
    
    
    
    
    
    @Override
    protected Path2D getPath2D(double width, double height) {
                  
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
        x[0] = (double)w * ((double)percent / 100.0);
        x[2] = w - x[0];

    }

    private void getTrapezoid(double w, double h) {
        x[0] = (double)w * ((double)percent / 200.0);
        x[1] = w - x[0];
    }
    
    
    private void getDiamond(double w, double h) {
        x[0] = (double)w * ((double)percent / 100.0);
        x[2] = w - x[0];
        
        y[1] = (double)h * ((double)percent / 100.0);
        y[2] = h;
        y[3] = h - y[1];
    }
    
    
    
    private void getRhombus(double w, double h) {
        
        double shorter = w > h ? h : w;

 
        x[0] = (double)shorter * ((double)percent / 200.0);
        x[1] = shorter;
        x[2] = x[1] - x[0];
        double hypotenuse = x[1] - x[0];

        y[2] = Math.sqrt(Math.pow(hypotenuse, 2) - Math.pow(x[0], 2));
        y[3] = y[2];
        
    }

}
