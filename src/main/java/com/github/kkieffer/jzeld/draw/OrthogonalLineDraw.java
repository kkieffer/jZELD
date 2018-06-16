
package com.github.kkieffer.jzeld.draw;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * A drawing object that creates a straight line drawing with lines at 90 or 180 degrees to each other. Each time the mouse is clicked a line 
 * is drawn from the previous point to the closest orthogonal line using the clicked point.  While the mouse is moved a temporary line is drawn to the new mouse position. 
 * A double-click completes the drawing and it is added to the canvas.
 * If the close parameter is specified, the shape is closed by drawing a line from the last point to the first.
 * 
 * @author kkieffer
 */
public class OrthogonalLineDraw extends BoundaryDraw {

    public OrthogonalLineDraw(ZCanvas canvas, boolean close) {
        super(canvas, close);
    }
    
    private Point getDrawToPoint(Point2D mouse, Point2D last) {
        Point p;
        if (Math.abs(mouse.getX() - last.getX()) > Math.abs(mouse.getY() - last.getY()))  //if x distance is greater, use it
            p = new Point((int)mouse.getX(), (int)last.getY());
        else
            p = new Point((int)last.getX(), (int)mouse.getY());
        return p;
    }

    @Override
    public void drawClientMouseClicked(Point mouse, int clickCount) {
        
        if (mousePoints.isEmpty()) { //first point
            this.addPoint(mouse);
            return;
        }
        

        if (clickCount > 1) {  //connect to first orthogonally 
            
            Point2D first = mousePoints.get(0);
            Point2D last = mousePoints.get(mousePoints.size()-1);
            
            //last point needed orthogonal to first
            Point p = getDrawToPoint(last, first);
            this.addPoint(p);
           
            
            this.drawComplete();
            addShapeToCanvas(getShape());
        
        } else {
            
            Point2D last = mousePoints.get(mousePoints.size()-1);
       
            Point p = getDrawToPoint(mouse, last);
            this.addPoint(p);
 
        }
    }

    @Override
    protected void drawToMouse(Graphics g, Point2D last, Point mouse) {
        if (last == null || mouse == null)
            return;
            
        Point p = getDrawToPoint(mouse, last);
        
        //Draw temporary line to the current mouse point
        g.drawLine((int)last.getX(), (int)last.getY(), p.x, p.y);
        
    }
    
    
    @Override
    public void drawClientMousePressed(Point mouse) {
    }

    @Override
    public void drawClientMouseReleased(Point mouse) {
    }

    @Override
    public void drawClientMouseDragged(Point mouse) {
    }
    
}
