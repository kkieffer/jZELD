
package com.github.kkieffer.jzeld;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 *
 * @author kkieffer
 */
public class ZColorChooser {
    
    private static final int MAX_HISTORY = 30;
    private static final LinkedList<Color> colorHistory = new LinkedList<>();
    
    private ZColorChooser() {}
    
    public static Color[] getColorHistory() {
        Color[] array = new Color[colorHistory.size()];
        colorHistory.toArray(array);
        return array;
    }
    
    public static void setColorHistory(Color[] history) {
        colorHistory.clear();
        addToColorHistory(history);
    }
    
    public static void addToColorHistory(Color[] history) {

        if (history == null)
            return;
        for (Color c : history)
            colorHistory.addLast(c);
    }
    
    
    private static class ColorTracker implements ActionListener, Serializable {
        JColorChooser chooser;
        Color color;

        public ColorTracker(JColorChooser c) {
            chooser = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            color = chooser.getColor();
        }

        public Color getColor() {
            return color;
        }
    }
    
    
    public static Color showDialog(Component parent, String title, Color initialColor) {
      
        //Create the chooser
        final JColorChooser chooser = new JColorChooser(initialColor == null ? Color.WHITE : initialColor);
        
        //Populate the recent colors using reflection (because the objects are not publicly accessible
        if (!colorHistory.isEmpty()) {
            
            for (AbstractColorChooserPanel p : chooser.getChooserPanels()) {

                if (p.getClass().getSimpleName().equals("DefaultSwatchChooserPanel")) {  
                    
                    try {

                        //Find the package private member field for the recent swatch panel
                        Field recentPanelField = p.getClass().getDeclaredField("recentSwatchPanel"); 
                        recentPanelField.setAccessible(true);

                        Object recentPanel = recentPanelField.get(p);  //member object of type "RecentSwatchPanel" (package private class)

                        Method recentColorMethod = recentPanel.getClass().getMethod("setMostRecentColor", Color.class);  //method to set recent colors
                        recentColorMethod.setAccessible(true);

                        for (Color c : colorHistory)
                            recentColorMethod.invoke(recentPanel, c);

                        
                    } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                        System.err.println(ex);  //print exception but continue on...
                    }
                }

            }
        }
        

        ColorTracker ok = new ColorTracker(chooser);
        JDialog dialog = JColorChooser.createDialog(null, title, true, chooser, ok, null);

        dialog.setVisible(true);

        Color c = ok.getColor();
        if (c != null)
            colorHistory.addLast(c);
        
        while (colorHistory.size() > MAX_HISTORY)
            colorHistory.removeFirst();
        
        return ok.getColor();
    }
}
