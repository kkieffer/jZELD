
package com.github.kkieffer.jzeld.attributes;

import java.awt.Shape;

/**
 *
 * @author kevin
 */
public interface Clippable {
    public void setClippingShape(Shape s);
    public Shape getClippingShape();
    public boolean hasClip();
    
    
    public static String getHtmlHelp() {
        
        return "Applying the clipping operation to two selected elements sets the visible area of the reference element (the element to be clipped) to the area of the clipping element. " +
                "The lowest layer element is the reference element and the top layer element sets the clipping area. Any visible part of the reference element outside the clipping area is hidden. " +
                "The element that sets the clipping area is deleted from the canvas after applying the clip." +
                "<br><br>Unlike combining shapes, the clipping can be removed by selecting the element by itself and choosing <i>Remove Clip</i> from the context menu.";
              
    }
    
}
