
package com.github.kkieffer.jzeld.draw;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Point;

/**
 * A drawing object that creates a freeform drawing while holding the mouse down - straight lines connected the mouse points 
 * together. When the mouse is released the shape is added to the canvas.
 * If the close parameter is specified, the shape is closed by drawing a line from the last point to the first.
 * 
 * @author kkieffer
 */
public class FreeformDraw extends BoundaryDraw {

    public FreeformDraw(ZCanvas canvas, boolean close) {
        super(canvas, close);
    }

    @Override
    public void drawClientMouseClicked(Point mouse, int clickCount) {
    }

    @Override
    public void drawClientMousePressed(Point mouse) {
        this.addPoint(mouse);
    }

    @Override
    public void drawClientMouseReleased(Point mouse) {
        this.drawComplete();  
        addShapeToCanvas(getShape());
    }

    @Override
    public void drawClientMouseDragged(Point mouse) {
        this.addPoint(mouse);
    }
    
}
