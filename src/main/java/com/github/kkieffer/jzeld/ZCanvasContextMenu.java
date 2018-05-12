
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.element.ZElement;
import java.awt.Component;
import java.util.ArrayList;

/**
 * Classes that implement this interface can be set as the context menu on a ZCanvas.
 * 
 * @author kkieffer
 */
public interface ZCanvasContextMenu {
    
    /**
     * When the ZCanvas context menu is selected, this method is called to show the menu
     * @param component the ZCanvas superclass
     * @param x the x location where the menu should be displayed
     * @param y the y location where the menu should be displayed
     */
    public void show(Component component, int x, int y);

    /**
     * Called when a new element, or set of elements, is selected
     * @param lastSelected the last one that was selected, null if nothing is selected
     * @param selectedElements all the selected elements, which must contain lastSelected if not empty
     */
    public void newSelections(ZElement lastSelected, ArrayList<ZElement> selectedElements);
    
    
}
