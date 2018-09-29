
package com.github.kkieffer.jzeld.contextMenu;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.element.ZElement.StrokeStyle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JMenuItem;

/**
 * Abstract base class for Context Menu. Notifies listeners when a menu setting is selected
 * @author kkieffer
 */
public class AbstractContextMenu extends JMenuItem {
    
    protected ZCanvas canvas;
    
    private final ArrayList<ContextMenuListener> listeners = new ArrayList<>();
    
    protected BufferedImage bufferedImage;
    protected Graphics2D g;
    
    
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
    
     protected void lineStyleChanged(StrokeStyle newStyle) {
        for (ContextMenuListener l : listeners)
            l.contextMenuLineStyleChanged(newStyle);
    }
    
    public void setCanvas(ZCanvas c) {
        canvas = c;
    }
    
    protected void createMenuGraphics() {
        
        GraphicsConfiguration gC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        bufferedImage = gC.createCompatibleImage(100, 16, Transparency.BITMASK);
        g = (Graphics2D)bufferedImage.getGraphics();
        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
    }
    
}
