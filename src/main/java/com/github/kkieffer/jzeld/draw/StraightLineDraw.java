
package com.github.kkieffer.jzeld.draw;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.element.ZElement;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * A drawing object that creates a straight line drawing. Each time the mouse is clicked a line is drawn from the previous point to
 * the clicked point.  While the mouse is moved a temporary line is drawn to the new mouse position. A double-click completes the 
 * drawing and it is added to the canvas.  If the close parameter is specified, the shape is closed by drawing a line from the last point to the first.
 * 
 * @author kkieffer
 */
public class StraightLineDraw extends BoundaryDraw {

    public StraightLineDraw(ZCanvas canvas, boolean close, float strokeWidth, Color lineColor, ZElement.StrokeStyle lineStyle) {
        super(canvas, close, strokeWidth, lineColor, lineStyle);
    }

    @Override
    public void drawClientMouseClicked(Point2D mouse, MouseEvent e) {
        
        if (e.getClickCount() > 1) 
            complete();
    }

    @Override
    public void drawClientMousePressed(Point2D mouse, MouseEvent e) {
    }

    @Override
    public void drawClientMouseReleased(Point2D mouse, MouseEvent e) {
        this.addPoint(mouse);
    }

    @Override
    public void drawClientMouseDragged(Point2D mouse, MouseEvent e) {
    }
    
}
