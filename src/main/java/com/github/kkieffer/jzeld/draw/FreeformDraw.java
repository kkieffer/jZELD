
package com.github.kkieffer.jzeld.draw;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Point;

/**
 * A drawing object that creates a freeform drawing while holding the mouse down - straight lines connected the mouse points 
 * together. When the mouse is released the shape is added to the canvas.
 * 
 * @author kkieffer
 */
public class FreeformDraw extends BoundaryDraw {

    public FreeformDraw(ZCanvas canvas) {
        super(canvas);
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
