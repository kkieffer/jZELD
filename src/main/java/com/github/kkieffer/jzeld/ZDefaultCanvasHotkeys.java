
package com.github.kkieffer.jzeld;

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

  
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), "Copy");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), "Paste");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "Delete");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_DOWN_MASK), "Repeat");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_DOWN_MASK), "Undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.META_DOWN_MASK), "Group");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.META_DOWN_MASK), "Ungroup");

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
                c.delete();
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
