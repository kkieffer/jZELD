
package com.github.kkieffer.jzeld.draw;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Point;

/**
 * A drawing object that creates a straight line drawing. Each time the mouse is clicked a line is drawn from the previous point to
 * the clicked point.  While the mouse is moved a temporary line is drawn to the new mouse position. A double-click completes the 
 * drawing and it is added to the canvas.
 * 
 * @author kkieffer
 */
public class StraightLineDraw extends BoundaryDraw {

    public StraightLineDraw(ZCanvas canvas) {
        super(canvas);
    }

    @Override
    public void drawClientMouseClicked(Point mouse, int clickCount) {
        this.addPoint(mouse);
        
        if (clickCount > 1) {
            this.drawComplete();
            addShapeToCanvas(getShape());
        }
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
