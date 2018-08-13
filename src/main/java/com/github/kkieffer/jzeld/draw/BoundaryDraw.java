
package com.github.kkieffer.jzeld.draw;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.element.ZShape;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This abstract class can be used to help draw shapes with a boundary and no fill.  The subclass can call addPoint() to add new 
 * mouse points to the shape, and then create the shape and add it to the ZCanvas.  Points are connected by straight lines.
 * @author kkieffer
 */
public abstract class BoundaryDraw implements DrawClient, KeyListener {

    
    
    
    /**
     * From a list of 2D points, creates a 2D line path between them.
     * @param points coordinate points 
     * @param closePath true to close the path, drawing a line from the last point to the first
     * @return a Path2D object, or null if the points array is null
     */
    public static Path2D pathFromPoints(List<Point2D> points, boolean closePath) {
        
        if (points.isEmpty())
            return null;
        
        Path2D path = new Path2D.Double();

        Point2D p = points.get(0);
        path.moveTo(p.getX(), p.getY());

        for (int i=1; i<points.size(); i++) {
            p = points.get(i);
            path.lineTo(p.getX(), p.getY());
        }
       
        if (closePath)
            path.closePath();
        
        return path;
    
    }
    
    /**
     * Get the path from 2D points, which can be overriden for more complex curve fit points
     * @return 
     */
    protected Path2D getPath() {
        return pathFromPoints(mousePoints, close);
    }
    
        
    protected ArrayList<Point2D> mousePoints = new ArrayList<>();
    private final ZCanvas canvas;
    protected final boolean close;
    protected float strokeWidth;
    protected Color lineColor;
 
    protected BoundaryDraw(ZCanvas canvas, boolean close, float strokeWidth, Color lineColor) {
        this.canvas = canvas;
        this.close = close;
        this.strokeWidth = strokeWidth;
        this.lineColor = lineColor;
        this.canvas.addKeyListener(this);
    }

    /**
     * Subclasses call this method when they are done drawing.
     */
    protected void drawComplete() {
        canvas.drawOff();
    }
    
    /**
     * Subclasses can call this method to get the ZShape defined by the points so far
     * @return a shape, or null if there are no mouse points added
     */
    protected ZShape getShape() {
        if (mousePoints.isEmpty())
            return null;
               
        double scale = canvas.getScale();
        
        //Translate everything into units of the canvas
        for (int i=0; i<mousePoints.size(); i++) {
            double x = mousePoints.get(i).getX() / scale;
            double y = mousePoints.get(i).getY() / scale;
            mousePoints.set(i, new Point2D.Double(x, y));
        }
        
        Path2D path = getPath();
        if (path == null)
            return null;
        
        Rectangle2D bounds2D = path.getBounds2D();  //get the bounds to find the x and y location on the canvas
        
        Shape shape = path.createTransformedShape(AffineTransform.getTranslateInstance(-bounds2D.getX(), -bounds2D.getY()));
 
        return new ZShape(bounds2D.getX(), bounds2D.getY(), shape, 0.0, true, true, true, strokeWidth, lineColor, null, null, null);
        
    }
    
    /**
     * Convenience method to add a shape to the canvas
     * @param s the shape to add, fails silently if the shape is null
     */
    protected void addShapeToCanvas(ZShape s) {
        if (s == null)
            return;
        
        canvas.addElement(s);
        canvas.selectElement(s, false);
        canvas.repaint();
    }
    
    
    /**
     * Subclasses call this to add a new point to the shape
     * 
     * @param p the new mouse point 
     */
    protected void addPoint(Point2D p) {
        mousePoints.add(p);
    }
    
    
    protected void drawToMouse(Graphics2D g, Point2D last, Point2D mouse) {
        //Draw temporary line to the current mouse point
        if (last != null && mouse != null)
            g.draw(new Line2D.Double(last.getX(), (int)last.getY(), mouse.getX(), mouse.getY()));
        
    }
    
    @Override
    public void drawClientPaint(Graphics g, Point2D currentMouse) {
        
        Graphics2D g2d = (Graphics2D)g;
        
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.setColor(lineColor);
        
        //Draw lines interconnecting the first point to the last point
        Point2D previous = null;
        Iterator<Point2D> it = mousePoints.iterator();
        while (it.hasNext()) {
            Point2D curr = it.next();
            if (previous != null)
                g2d.draw(new Line2D.Double(previous.getX(), previous.getY(), curr.getX(), curr.getY()));
            previous = curr;
        }
        
        drawToMouse(g2d, previous, currentMouse);
        
        
    }

    /**
     * Complete drawing, add shape to canvas, clean up
     */
    public void complete() {
        drawStop();
        addShapeToCanvas(getShape());
    }
    
    /**
     * Stop drawing, don't add shape to canvas, clean up
     */
    @Override
    public void drawStop() {
        this.drawComplete();  
        this.canvas.removeKeyListener(this);
    }

    
     @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                complete();
                break;
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
                drawStop();
                break;
        }
        
            
    }
   
    
}
