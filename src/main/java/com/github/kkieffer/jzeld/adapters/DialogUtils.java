
package com.github.kkieffer.jzeld.adapters;

import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Utilities for managing dialogs and frames
 * @author kkieffer
 */
public class DialogUtils {
    
    private static Image dialogIconImage;
    
    private DialogUtils() {}
    
    
    /**
     * Sets the icon to use for all dialog and popup windows that use the method below
     * @param i the image to use.
     */
    public static void setDialogIcon(Image i) {
        dialogIconImage = i;
    }
    
    /**
     * Adds the Image set using setDialogIcon() as the frame icon, and uses the ESCAPE key as a shortcut to cancel or dispose of the frame
     * @param c a component on the frame, such as the close button or the root pane
     * @param methodName method to call on escape, which is passed the Window container of the Component c.  Must be public and take no arguments. If null, ESCAPE is not 
     * used as a shortcut.
     * @throws RuntimeException if the method is not found or could not be invoked, or throws an exception
     */
    public static void addShortcutAndIcon(JComponent c, String methodName) {
    
        Window w = SwingUtilities.windowForComponent(c);
        
        if (dialogIconImage != null)
            w.setIconImage(dialogIconImage);
        
        InputMap im = c.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = c.getActionMap();
     
        if (methodName == null)
            return;
        
        Method m;
        try {
            m = w.getClass().getMethod(methodName, (Class<?>[]) null);  //call the specified method with no arguments
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
            
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Escape");
       
        am.put("Escape", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    m.invoke(w, (Object[]) null);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {}
                
            }
        });
        
       
    }
    
}
