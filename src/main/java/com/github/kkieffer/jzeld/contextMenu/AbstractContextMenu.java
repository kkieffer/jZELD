
package com.github.kkieffer.jzeld.contextMenu;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JMenuItem;

/**
 * Abstract base class for Context Menu. Notifies listeners when a menu setting is selected
 * @author kkieffer
 */
public class AbstractContextMenu extends JMenuItem {
    
    protected ZCanvas canvas;
    
    private final ArrayList<ContextMenuListener> listeners = new ArrayList<>();
    
    
    protected AbstractContextMenu(ZCanvas c) {
        canvas = c;
    }
    
    public void addListener(ContextMenuListener l) {
        if (!listeners.contains(l))
            listeners.add(l);
    }
    
    public void removeListener(ContextMenuListener l) {
        listeners.remove(l);
    }
    
    protected void fillColorChanged(Color newColor) {
        for (ContextMenuListener l : listeners)
            l.contextMenuFillColorChanged(newColor);
    }
    
    protected void lineColorChanged(Color newColor) {
        for (ContextMenuListener l : listeners)
            l.contextMenuLineColorChanged(newColor);
    }
    
    protected void lineWidthChanged(float newWidth) {
        for (ContextMenuListener l : listeners)
            l.contextMenuLineWidthChanged(newWidth);
    }
    
    protected void lineDashChanged(Float[] newDash) {
        for (ContextMenuListener l : listeners)
            l.contextMenuLineDashChanged(newDash);
    }
    
    public void setCanvas(ZCanvas c) {
        canvas = c;
    }
    
}
