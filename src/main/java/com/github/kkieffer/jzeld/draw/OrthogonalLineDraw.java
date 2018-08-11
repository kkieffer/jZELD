
package com.github.kkieffer.jzeld.draw;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
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

    public OrthogonalLineDraw(ZCanvas canvas, boolean close, float strokeWidth, Color lineColor) {
        super(canvas, close, strokeWidth, lineColor);
    }
    
    private Point2D getDrawToPoint(Point2D mouse, Point2D last) {

        if (Math.abs(mouse.getX() - last.getX()) > Math.abs(mouse.getY() - last.getY()))  //if x distance is greater, use it
            return new Point2D.Double(mouse.getX(), last.getY());
        else
            return new Point2D.Double(last.getX(), mouse.getY());
    }

    @Override
    public void drawClientMouseClicked(Point2D mouse, int clickCount, int button) {
        
        if (mousePoints.isEmpty()) { //first point
            this.addPoint(mouse);
            return;
        }
        

        if (clickCount > 1) {  //connect to first orthogonally 
            
            Point2D first = mousePoints.get(0);
            Point2D last = mousePoints.get(mousePoints.size()-1);
            
            //last point needed orthogonal to first
            Point2D p = getDrawToPoint(last, first);
            this.addPoint(p);
           
            
            complete();
        
        } else {
            
            Point2D last = mousePoints.get(mousePoints.size()-1);
       
            Point2D p = getDrawToPoint(mouse, last);
            this.addPoint(p);
 
        }
    }

    @Override
    protected void drawToMouse(Graphics2D g, Point2D last, Point2D mouse) {
        if (last == null || mouse == null)
            return;
            
        Point2D p = getDrawToPoint(mouse, last);
        
        //Draw temporary line to the current mouse point
        g.draw(new Line2D.Double(last.getX(), last.getY(), p.getX(), p.getY()));
        
    }
    
    
    @Override
    public void drawClientMousePressed(Point2D mouse) {
    }

    @Override
    public void drawClientMouseReleased(Point2D mouse) {
    }

    @Override
    public void drawClientMouseDragged(Point2D mouse) {
    }
    
}
