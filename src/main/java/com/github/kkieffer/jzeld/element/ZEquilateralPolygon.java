
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.ZCanvas;
import static com.github.kkieffer.jzeld.ZCanvas.errorIcon;
import com.github.kkieffer.jzeld.draw.BoundaryDraw;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A ZEquilateralPolygon is a n-dimensional Equilateral Polygon
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZEquilateralPolygon")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZEquilateralPolygon extends ZPolygon {

    public static final ImageIcon radiusIcon = new ImageIcon(ZCanvas.class.getResource("/sides.png")); 
    
    protected int sides;

  
    protected ZEquilateralPolygon(){}
    
     /**
     * Create a ZRoundedRectangle
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
     * @param sides number of sides
     */
    public ZEquilateralPolygon(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, int sides) {
        super(x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor);
        this.sides = sides;
    }
    
    
    public ZEquilateralPolygon(ZEquilateralPolygon copy, boolean forNew) {
        super(copy, forNew);
        this.sides = copy.sides;
    }
    
    @Override
    public ZEquilateralPolygon copyOf(boolean forNew) {
        return new ZEquilateralPolygon(this, forNew);
    }
    
    @Override
    protected String getShapeSummary() {       
        return "A polygon with equal length sides.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "Double click on the polygon to select the number of sides.";     
    }
    
    /**
     * Adjust the number of sides (edges)
     * @param s the number of edges
     */
    public void setSides(int s) {
        if (s < 3)
            throw new IllegalArgumentException("Sides must be greater than 2");
        sides = s;
        hasChanges = true;
    }
     
    @Override
    public boolean supportsEdit() {
        return true;
    };
    
    @Override
    public boolean selectedForEdit(ZCanvas canvas) {
        
  
        String rc = (String)JOptionPane.showInputDialog(canvas, "Update Number of Sides", "Modify Equilateral Polygon", JOptionPane.QUESTION_MESSAGE, radiusIcon,
                                                (Object[])null, (Object)String.valueOf(sides));
        if (rc != null) {
            try {
                int numSides = Integer.parseInt(rc);
                if (numSides < 3)
                    throw new NumberFormatException("Sides must be greater than 2");
                setSides(numSides);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(canvas, "Invalid value: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, errorIcon);
            }
        }
        
        return false;
    }
    
    @Override
    protected Shape getPolygon(double width, double height) {
        
        Point2D center = new Point2D.Double(width/2.0, height/2.0);
        double angleStep = Math.toRadians(360.0/sides);
        
        //Determine the starting angle for the best "look"
        double angle;
        
        if (sides % 2 != 0)  //odd number of sides, always put vertex at top
            angle = Math.PI/2;
        else if (sides % 4 == 0)  //multiple of 4 sides, so a side should go against each face of the bounds box
            angle = angleStep/2;
        else   //even sides but non divisible by 4, so flat side goes to the top
            angle = 0.0;
        
        double radius = (width > height ? height/2 : width/2);
        
     
        ArrayList<Point2D> points = new ArrayList<>(sides);

        for (int i=0; i<sides; i++) {
            double x = radius * Math.cos(angle) + center.getX();
            double y = -(radius * Math.sin(angle)) + center.getY();
            
            points.add(new Point2D.Double(x,y));
            
            angle += angleStep;
        }
        
        return BoundaryDraw.pathFromPoints(points, true);
    }
    
     
    
    
}
