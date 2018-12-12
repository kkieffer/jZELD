
package com.github.kkieffer.jzeld.contextMenu;

import com.github.kkieffer.jzeld.ZCanvas;
import static com.github.kkieffer.jzeld.ZCanvas.errorIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author kkieffer
 */
public class CombineMenu extends JMenu {

    private ZCanvas canvas;
    
    public CombineMenu(String title, ZCanvas c) {
        
        super(title);
        canvas = c;
        
        for (ZCanvas.CombineOperation g : ZCanvas.CombineOperation.values()) {
            JMenuItem m = new JMenuItem(g.toString());
            m.setName(g.name());
            m.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    int numSel = canvas.getSelectedElementsArray().length;
                    int combined = canvas.combineSelectedElements(g);
                
                    if (combined < 0)
                        JOptionPane.showMessageDialog(canvas, "Combine operation resulted in a shape with no area", "Warning", JOptionPane.ERROR_MESSAGE, errorIcon);

                    else if (numSel != combined) {
                        if (combined == 0)
                            JOptionPane.showMessageDialog(canvas, "No elements combined.\nOther selected elements are not shapes and cannot be combined. ", "Warning", JOptionPane.ERROR_MESSAGE, errorIcon);                         
                        else
                            JOptionPane.showMessageDialog(canvas, "Only " + combined + " elements combined.\nOther selected elements are not shapes and cannot be combined. ", "Warning", JOptionPane.ERROR_MESSAGE, errorIcon);
                    }
                }
            });
            add(m);         
        }     
    }
    
    public void setCanvas(ZCanvas c) {
        canvas = c;
    }
    
}
