
package com.github.kkieffer.jzeld;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Simple utility class to add additional hotkeys to the ZCanvas
 * 
 * @author kkieffer
 */
public class ZDefaultCanvasHotkeys {
    
    public ZDefaultCanvasHotkeys(ZCanvas c) {
        
        InputMap im = c.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = c.getActionMap();

  
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Cut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Copy");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Paste");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "Delete");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Repeat");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Group");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Ungroup");
 
        am.put("Cut", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.cut();
            }
        });
        am.put("Copy", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.copy();
            }
        });
        am.put("Paste", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.paste();
            }
        });
        am.put("Delete", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.deleteSelected();
            }
        });      
       
         am.put("Repeat", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.repeat();
            }
        });
        am.put("Undo", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.undo();
            }
        });
        am.put("Undo", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.undo();
            }
        });
        
        am.put("Group", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.groupSelectedElements();

            }
        });
        am.put("Ungroup", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.ungroup();

            }
        });
    }
    
    
}
