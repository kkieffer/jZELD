
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.element.ZElement;

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
    
}
