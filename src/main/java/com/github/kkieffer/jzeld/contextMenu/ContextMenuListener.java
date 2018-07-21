
package com.github.kkieffer.jzeld.contextMenu;

import java.awt.Color;

/**
 *
 * @author kkieffer
 */
public interface ContextMenuListener {
    
    public void contextMenuFillColorChanged(Color newColor);
    public void contextMenuLineColorChanged(Color newColor);
    public void contextMenuLineWidthChanged(float width);
    public void contextMenuLineDashChanged(Float[] dash);
    
    
    
}
