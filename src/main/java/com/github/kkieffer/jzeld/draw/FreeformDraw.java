
package com.github.kkieffer.jzeld.draw;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Color;
import java.awt.geom.Point2D;

/**
 * A drawing object that creates a freeform drawing while holding the mouse down - straight lines connected the mouse points 
 * together. When the mouse is released the shape is added to the canvas.
 * If the close parameter is specified, the shape is closed by drawing a line from the last point to the first.
 * 
 * @author kkieffer
 */
public class FreeformDraw extends BoundaryDraw {

    public FreeformDraw(ZCanvas canvas, boolean close, float strokeWidth, Color lineColor) {
        super(canvas, close, strokeWidth, lineColor);
    }

    @Override
    public void drawClientMouseClicked(Point2D mouse, int clickCount, int button) {
    }

    @Override
    public void drawClientMousePressed(Point2D mouse) {
        this.addPoint(mouse);
    }

    @Override
    public void drawClientMouseReleased(Point2D mouse) {
        complete();
    }

    @Override
    public void drawClientMouseDragged(Point2D mouse) {
        this.addPoint(mouse);
    }
    
}
