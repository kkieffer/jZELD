
package com.github.kkieffer.jzeld.draw;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.element.ZElement;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

/**
 * A drawing object that creates a freeform drawing while holding the mouse down - straight lines connected the mouse points 
 * together. When the mouse is released the shape is added to the canvas.
 * If the close parameter is specified, the shape is closed by drawing a line from the last point to the first.
 * 
 * @author kkieffer
 */
public class FreeformDraw extends BoundaryDraw {

    public FreeformDraw(ZCanvas canvas, boolean close, float strokeWidth, Color lineColor, ZElement.StrokeStyle lineStyle) {
        super(canvas, close, strokeWidth, lineColor, lineStyle);
    }

    @Override
    public void drawClientMouseClicked(Point2D mouse, MouseEvent e) {
    }

    @Override
    public void drawClientMousePressed(Point2D mouse, MouseEvent e) {
        this.addPoint(mouse);
    }

    @Override
    public void drawClientMouseReleased(Point2D mouse, MouseEvent e) {
        complete();
    }

    @Override
    public void drawClientMouseDragged(Point2D mouse, MouseEvent e) {
        this.addPoint(mouse);
    }

    @Override
    public void drawClientMouseWheelMoved(Point2D scaledMouse, MouseWheelEvent e) {  //no op
    }
    
}
