
package com.github.kkieffer.jzeld.contextMenu;

import com.github.kkieffer.jzeld.ZCanvas;
import static com.github.kkieffer.jzeld.ZCanvas.errorIcon;
import com.github.kkieffer.jzeld.element.ZElement;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * An implementation of the ZCanvasContextMenu.  
 * 
 * @author kkieffer
 */
public class ZDefaultContextMenu implements ZCanvasContextMenu {
    
    public static final ImageIcon shearIcon = new ImageIcon(ZCanvas.class.getResource("/shear.png")); 

    
    protected JMenu rotateMenu;
    protected JMenuItem rotateCWMenuItem;
    protected JMenuItem rotateCCWMenuItem;
    protected JMenu orderMenu;
    protected JMenuItem sendToBackMenuItem;
    protected JMenuItem bringToFrontMenuItem;
    protected JMenuItem sendBackwardsMenuItem;
    protected JMenuItem bringForwardsMenuItem;
    protected JMenuItem copyMenuItem;
    protected JMenuItem pasteMenuItem;
    protected JMenuItem deleteMenuItem;
    protected JMenu editMenu;
    protected JMenuItem flipVertMenuItem;
    protected JMenuItem flipHorizMenuItem;
    protected JMenu attributesMenu;
    protected LineBorderMenu lineWeightMenu;
    protected LineBorderMenu lineDashMenu;
    protected LineStyleMenu lineStyleMenu;
    protected JMenu colorMenu;
    protected ColorMenuItem fillColorMenuItem;
    protected ColorMenuItem lineColorMenuItem;
    protected JMenu alignMenu;
    protected JMenu flipMenu;
    protected JPopupMenu contextPopupMenu;
    protected final JMenu combineMenu;
    protected final JMenuItem clearShearMenuItem;
    protected final JMenu shearMenu;
    protected final ColorMenuItem removeFillMenuItem;
    private final JMenuItem setHorizontalShearMenuItem;
    private final JMenuItem setVerticalShearMenuItem;
    
    
    public ZDefaultContextMenu(ZCanvas canvas) {
        
        Font f = new Font("sans-serif", Font.PLAIN, 14);
        UIManager.put("Menu.font", f);
        UIManager.put("MenuItem.font", f);
        
        contextPopupMenu = new JPopupMenu();   
         
        editMenu = new JMenu("Edit");
        copyMenuItem = new JMenuItem("Copy");
        pasteMenuItem = new JMenuItem("Paste");
        deleteMenuItem = new JMenuItem("Delete");
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.add(deleteMenuItem);
   
        rotateMenu = new JMenu("Rotate");
        rotateCWMenuItem = new JMenuItem("Snap Clockwise");
        rotateCCWMenuItem = new JMenuItem("Snap Counter CW");
        rotateMenu.add(rotateCWMenuItem);
        rotateMenu.add(rotateCCWMenuItem);
        
        shearMenu = new JMenu("Shear");
        setHorizontalShearMenuItem = new JMenuItem("Set Horizontal Shear");
        setVerticalShearMenuItem = new JMenuItem("Set Vertical Shear");
        clearShearMenuItem = new JMenuItem("Zero Shear Values");
        shearMenu.add(setHorizontalShearMenuItem);
        shearMenu.add(setVerticalShearMenuItem);
        shearMenu.add(clearShearMenuItem);
        
        orderMenu = new JMenu("Order");
        sendToBackMenuItem = new JMenuItem("Send to Back");
        sendBackwardsMenuItem = new JMenuItem("Send Backward");
        bringToFrontMenuItem = new JMenuItem("Bring to Front");
        bringForwardsMenuItem = new JMenuItem("Bring Forward");
       
        
        orderMenu.add(sendToBackMenuItem);
        orderMenu.add(sendBackwardsMenuItem);
        orderMenu.add(bringToFrontMenuItem);
        orderMenu.add(bringForwardsMenuItem);
               
        flipMenu = new JMenu("Flip");
        flipHorizMenuItem = new JMenuItem("Horizontal");
        flipVertMenuItem = new JMenuItem("Vertical");
        flipMenu.add(flipHorizMenuItem);
        flipMenu.add(flipVertMenuItem);
        
        alignMenu = new JMenu("Align");
        for (ZCanvas.Alignment a : ZCanvas.Alignment.values()) {
            JMenuItem m = new JMenuItem(a.toString());
            m.setName(a.name());
            m.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                   canvas.align(a);
                }
            });
            alignMenu.add(m);
        }
        
        
        combineMenu = new CombineMenu("Combine", canvas);
           
        attributesMenu = new JMenu("Attributes");
        lineWeightMenu = new LineBorderMenu("Line Weight", canvas, LineBorderMenu.Type.WEIGHT);
        lineDashMenu = new LineBorderMenu("Dash Pattern", canvas, LineBorderMenu.Type.DASH);
        lineStyleMenu = new LineStyleMenu("Line Style", canvas);
        colorMenu = new JMenu("Color");
        lineColorMenuItem = new ColorMenuItem("Line Color", canvas, ColorMenuItem.Type.LINE);
        colorMenu.add(lineColorMenuItem);
        fillColorMenuItem = new ColorMenuItem("Fill Color", canvas, ColorMenuItem.Type.FILL);
        colorMenu.add(fillColorMenuItem);

        removeFillMenuItem = new ColorMenuItem("Remove Fill", canvas, ColorMenuItem.Type.CLEAR);
        colorMenu.add(removeFillMenuItem);
        
        attributesMenu.add(lineWeightMenu);
        attributesMenu.add(lineStyleMenu);
        attributesMenu.add(lineDashMenu);
        attributesMenu.add(colorMenu);
        
        contextPopupMenu.add(editMenu);
        contextPopupMenu.add(rotateMenu);
        contextPopupMenu.add(shearMenu);
        contextPopupMenu.add(orderMenu);
        contextPopupMenu.add(flipMenu);
        contextPopupMenu.add(attributesMenu);
        contextPopupMenu.add(new JSeparator());
        contextPopupMenu.add(combineMenu);
        contextPopupMenu.add(alignMenu);
        
        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               canvas.copy();
            }
        });
        pasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               canvas.paste();
            }
        });
        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               canvas.deleteSelected();
            }
        });
        
        rotateCWMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.rotate90CW();
            }
        });
        
        rotateCCWMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.rotate90CCW();
            }
        });  
        
        sendToBackMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.moveToBack();
            }
        }); 
        
        sendBackwardsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.moveBackward();
            }
        }); 
        
        bringToFrontMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.moveToFront();
            }
        }); 
        
        bringForwardsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.moveForward();
            }
        }); 
        
        flipHorizMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.flip(true);
            }
        }); 
        
        flipVertMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.flip(false);
            }
        }); 
         
        removeFillMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.removeFill();
            }
        });
        
        
        clearShearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvas.clearShear();
            }
        });
       
        this.setHorizontalShearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                Component parent = SwingUtilities.getRoot(canvas);

                ZElement e = canvas.getLastSelectedElement();
                
                //Prompt for new radius, and prefill the old radius (scaled by the current unit)
                String rc = (String)JOptionPane.showInputDialog(parent, "Horizontal Shear Ratio", "Modify Horizontal Shear", JOptionPane.QUESTION_MESSAGE, shearIcon,
                                                        (Object[])null, (Object)String.valueOf(e.getShearX()));
                if (rc != null) {
                    try {
                         canvas.setShear(true, Double.parseDouble(rc)); 
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(canvas, "Invalid value: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, errorIcon);
                    }
                }

            }
        });
        
        this.setVerticalShearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                Component parent = SwingUtilities.getRoot(canvas);
      
                ZElement e = canvas.getLastSelectedElement();
                
                //Prompt for new radius, and prefill the old radius (scaled by the current unit)
                String rc = (String)JOptionPane.showInputDialog(parent, "Vertical Shear Ratio", "Modify clearShearMenuItem Shear", JOptionPane.QUESTION_MESSAGE, shearIcon,
                                                        (Object[])null, (Object)String.valueOf(e.getShearY()));
                if (rc != null) {
                    try {
                         canvas.setShear(false, Double.parseDouble(rc));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(canvas, "Invalid value: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, errorIcon);
                    }
                }

            }
        });
                
    }

    @Override
    public void show(Component component, int x, int y) {
        contextPopupMenu.show(component, x, y);
    }
    
    @Override
    public void dispose() {      
        contextPopupMenu.setVisible(false);
        contextPopupMenu.setInvoker(null);
        contextPopupMenu.removeAll();
    }
    
    
    @Override
    public void newSelections(ZElement lastSelected, ArrayList<ZElement> selectedElements) {
        
        if (lastSelected == null)
            return;
            
        
        if (lastSelected.supportsFlip()) {
            flipHorizMenuItem.setEnabled(true);
            flipVertMenuItem.setEnabled(true);
        } else {
            flipHorizMenuItem.setEnabled(false);
            flipVertMenuItem.setEnabled(false);
        }
        lineWeightMenu.setEnabled(lastSelected.hasOutline());
        lineColorMenuItem.setEnabled(lastSelected.hasOutline());
        lineStyleMenu.setEnabled(lastSelected.hasOutline());
        lineDashMenu.setEnabled(lastSelected.hasOutline() && lastSelected.hasDash());
        fillColorMenuItem.setEnabled(lastSelected.hasFill());
         
        alignMenu.setEnabled(selectedElements.size() > 1);
        combineMenu.setEnabled(selectedElements.size() > 1);
        


    }
    
}
