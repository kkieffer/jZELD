
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.adapters.DialogUtils;
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
 * Singleton class that provides an upgrade to the JColorChooser to include the color history.  Uses reflection to access the color history swatch
 * panel in the JColorChooser.  Class also maintains a bounded list of colors (when full, the oldest color is dropped). When selecting a color from
 * the dialog, the color is added to the history.  Note that duplicate colors are never added to the history.
 * @author kkieffer
 */
public class ZColorChooser {
    
    private static final int MAX_HISTORY = 30;
    private static final LinkedList<Color> colorHistory = new LinkedList<>();
    
    private ZColorChooser() {}  //no instantiation, all static
    
    /**
     * Retrieves the color history as an array
     * @return array of colors, oldest color first
     */
    public static Color[] getColorHistory() {
        Color[] array = new Color[colorHistory.size()];
        colorHistory.toArray(array);
        return array;
    }
    
    /**
     * Replaces the color history with the supplied array. Duplicate colors are not added to the history.
     * @param history the color history, oldest color 
     */
    public static void setColorHistory(Color[] history) {
        colorHistory.clear();
        addToColorHistory(history);
    }
    
    /**
     * Adds to the color history with the supplied array. Duplicate colors are not added to the history.
     * @param history the color history, oldest color 
     */
    public static void addToColorHistory(Color[] history) {

        if (history == null)
            return;
        for (Color c : history) {
            if (!colorHistory.contains(c))
                colorHistory.addLast(c);
        }
        
         while (colorHistory.size() > MAX_HISTORY)
            colorHistory.removeFirst();
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
    
    /**
     * Show the Color Chooser dialog. Selecting a Color and pressing "OK" will add the selected color to the color history.
     * @param parent parent component
     * @param title dialog title
     * @param initialColor the initial selected color, if null, initial color will be set to WHITE
     * @param panels optional array of additional panels to add to the color chooser, null for none
     * @return the selected color, or null if "CANCEL" is pressed
     */
    public static Color showDialog(Component parent, String title, Color initialColor, AbstractColorChooserPanel[] panels) {
      
        //Create the chooser
        final JColorChooser chooser = new JColorChooser(initialColor == null ? Color.WHITE : initialColor);
        if (panels != null) {
            for (AbstractColorChooserPanel p : panels)
                chooser.addChooserPanel(p);
        }
               
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

        DialogUtils.addShortcutAndIcon(chooser, "dispose");

        
        
        dialog.setVisible(true);

        Color c = ok.getColor();
        if (c != null)
            addToColorHistory(new Color[]{c});
        
        dialog.dispose();
        
        if (panels != null) {
            for (AbstractColorChooserPanel p : panels)  //remove panels, so they can be reused
                chooser.removeChooserPanel(p);
        }
        
        return ok.getColor();
    }
    
    /**
     * Color difference calculation in RGB space, using some effects of perception:
     * https://en.wikipedia.org/wiki/Color_difference
     * @param c1 the first color
     * @param c2 the second color
     * @return a value indicating the relative difference in color
     */
    public static double deltaColor(Color c1, Color c2) {
        
        double r = (c1.getRed() + c2.getRed())/2;
        double deltaR = c1.getRed() - c2.getRed();
        double deltaG = c1.getGreen() - c2.getGreen();
        double deltaB = c1.getBlue() - c2.getBlue();
        
        double t = r * (Math.pow(deltaR, 2) - Math.pow(deltaB, 2)) / 256;
        
        double deltac2 = 2*Math.pow(deltaR, 2) + 4*Math.pow(deltaG, 2) + 3*Math.pow(deltaB, 2) + t;
        
        return Math.sqrt(deltac2);
        
    }
    
    
}
