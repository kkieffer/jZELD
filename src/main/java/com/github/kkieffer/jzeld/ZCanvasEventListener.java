
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.element.ZElement;
import java.awt.geom.Point2D;

/**
 * Classes that implement this interface receive notifications when things happen on the canvas
 * @author kkieffer
 */
public interface ZCanvasEventListener {
    
    /**
     * A new element has been selected.  This is the latest element to be selected (others may remain selected if the shift
     * key is held while selecting more elements)  If selections have been removed, this method is called with a null argument
     * @param e the latest element to be selected, or null if selections are cleared.
     */
    public void elementSelected(ZElement e);
    
   
    /**
     * An element has been selected for edit (double click). 
     * @param e the element selected
     * @param supportsEdit true if the element supported editing, false otherwise (nothing happened)
     */
    public void elementEdited(ZElement e, boolean supportsEdit);

    
    /**
     * This method is called when a draw client is added or removed from the canvas
     * @param hasClient true if client is now drawing, false if one stopped
     */
    public void canvasHasDrawClient(boolean hasClient);

    
    /**
     * Called whenever the canvas has been repainted
     */
    public void canvasRepainted();
    
    /**
     * Called whenever the canvas has been zoomed in or out
     */
    public void canvasChangedZoom();

    /**
     * Called whenever the mouse is pressed in the canvas
     * @param mouseLoc the coordinates, in units, where the mouse was pressed
     * @param selected the element selected with the mouse press, or null if nothing was selected (or an element was deselected)
     */
    public void canvasMousePress(Point2D mouseLoc, ZElement selected);
    
}
