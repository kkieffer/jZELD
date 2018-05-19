package com.github.kkieffer.jzeld;



import com.github.kkieffer.jzeld.draw.FreeformDraw;
import com.github.kkieffer.jzeld.draw.StraightLineDraw;
import com.github.kkieffer.jzeld.element.ZCanvasRuler;
import com.github.kkieffer.jzeld.element.ZCanvasRuler.Unit;
import com.github.kkieffer.jzeld.element.ZEditableText;
import com.github.kkieffer.jzeld.element.ZEditableText.HorizontalJustify;
import com.github.kkieffer.jzeld.element.ZEditableText.TextAttributes;
import com.github.kkieffer.jzeld.element.ZGrid;
import com.github.kkieffer.jzeld.element.ZImage;
import com.github.kkieffer.jzeld.element.ZLine;
import com.github.kkieffer.jzeld.element.ZOval;
import com.github.kkieffer.jzeld.element.ZRectangle;
import com.github.kkieffer.jzeld.element.ZTriangle;
import com.github.kkieffer.jzeld.element.ZRoundedRectangle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.xml.bind.JAXBException;



/**
 *
 * @author kkieffer
 */
public class Demo extends javax.swing.JFrame {

    
    private static final Font LABEL_FONT = javax.swing.UIManager.getDefaults().getFont("Label.font");
    
    
    private void createDemoElements(ZCanvas c) throws IOException {
        //Create a grid in the back, dashed thin line
        ZGrid grid = new ZGrid(1, Color.LIGHT_GRAY, new Float[]{.05f}, Unit.CM, 2);
        c.addElement(grid);
        
        //Create two rulers, 1/4" thick, with different minor tick spacing, just outside the desired drawing space
        ZCanvasRuler hRule = new ZCanvasRuler(0.0, -c.getOrigin().getY(), 1, 0.25, true, Color.BLACK, Color.LIGHT_GRAY, LABEL_FONT, Unit.CM, 4, 2);
        c.addElement(hRule);        
        ZCanvasRuler yRule = new ZCanvasRuler(-c.getOrigin().getX(), 0.0, 1, 0.25, false, Color.BLACK, Color.LIGHT_GRAY, LABEL_FONT, Unit.CM, 2, 2);
        c.addElement(yRule);
       
        
        //Create a red rectangle, large black border, moveable
        ZRectangle r = new ZRectangle(1.0, 1.0, 2.0, 1.0, 25.0, true, true, 3, Color.BLACK, new Float[]{.2f, .2f}, Color.RED);
        c.addElement(r);

        //Create an immovable, unselectable green square, rotated to 45 degrees (its a diamond!)
        ZRectangle r2 = new ZRectangle(1.0, 1.0, 1.0, 1.0, 45.0, false, false, 1, Color.BLACK, null, Color.GREEN);
        c.addElement(r2);
        
        //Create a non-filled rectangle with rounded edges
        ZRectangle r3 = new ZRoundedRectangle(4.0, 1.0, 1.0, 1.0, 0.0, true, true, 1, Color.BLACK, new Float[]{.1f, .1f}, null, 0.1);
        c.addElement(r3);
        
        //Create a semi-transparent circle
        ZOval o = new ZOval(1.0, 3.0, 1.0, 1.0, 0.0, true, true, 0, Color.BLACK, null, new Color(0, 0, 255, 128));
        c.addElement(o);
       
        //Create a simple black line
        ZLine l = new ZLine(5.0, 2.5, 1.0, 0.0, true, true, 1, Color.BLACK, null);
        c.addElement(l);
        
        //Right triangle, yellow, big borders
        ZTriangle t = new ZTriangle(ZTriangle.Type.RIGHT, 1.0, 6.0, 1.0, 1.0, 0.0, true, true, 4, Color.BLACK, null, Color.YELLOW);
        c.addElement(t);
            
        //Add the test image
        InputStream testImg = Demo.class.getClassLoader().getResourceAsStream("tiger.jpg");
        BufferedImage image = ImageIO.read(testImg);
     
        ZImage img = new ZImage(5.0, 5.0, 3.0, 3.0, 0.0, true, true, 0, Color.BLACK, null, Color.GRAY, image);  
        c.addElement(img);

        //Create some editable text
        Font f = new Font("SERIF", Font.BOLD, 22);
        TextAttributes t2 = new TextAttributes(HorizontalJustify.CENTER, f, Color.RED);
        ZEditableText etxt = new ZEditableText(3.0, 6.0, .5, .5, 0.0, true, "MyEditableText", 0, Color.BLACK, Color.LIGHT_GRAY, t2);
        c.addElement(etxt);
 
        
    }
            
    
    public void printCanvas(ZCanvas c, PageFormat pf) {
        PrinterJob printJob = PrinterJob.getPrinterJob();
   
        if (pf != null)
            printJob.setPrintable(c, pf);
        else
            printJob.setPrintable(c);
        if (printJob.printDialog()) {
            try {
              printJob.print();
            } catch(PrinterException pe) {
                JOptionPane.showMessageDialog(this, "Error printing: " + pe, "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

            
            
    public Demo(boolean loadFromFile) throws IOException, JAXBException, FileNotFoundException, ClassNotFoundException {
        

        initComponents();
        setSize(900, 600);
        setTitle("jZELD Demo");

        //Create our ZCanvas with a dark grey background, label font, centimeter scale, dark grey cursor lines, 10 undo stack
        ZCanvas c = new ZCanvas(Color.DARK_GRAY, LABEL_FONT, Unit.CM, Color.DARK_GRAY, 10, new Point(36, 36), new Dimension(1400, 800));
        canvasPane.add(c, BorderLayout.CENTER);
        
        ZDefaultContextMenu m = new ZDefaultContextMenu(c);
        c.setContextMenu(m);
        
        new ZDefaultCanvasHotkeys(c);  //add hotkeys
        
        
        if (loadFromFile) {
            File f = new File("test.xml");
            if (!f.exists()) {
                JOptionPane.showMessageDialog(null, "File does not exist yet.  Create by running Demo and CTRL-S to save a file", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            c.fromFile(f);
        } else {
            createDemoElements(c);
        }

        c.requestFocusInWindow();
        
 
        InputMap im = c.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = c.getActionMap();

        //Add some shortcuts to add line or free drawings to the canvas
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.META_DOWN_MASK), "LineDraw");
        am.put("LineDraw", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.drawOn(new StraightLineDraw(c));
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_DOWN_MASK), "FreeDraw");
        am.put("FreeDraw", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.drawOn(new FreeformDraw(c));
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK), "Save");
        am.put("Save", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File f = new File("test.xml");
                    c.toFile(f);
                    System.out.println("Saved file: " + f.getAbsolutePath());
                } catch (JAXBException ex) {
                    Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.META_DOWN_MASK), "Print");
        am.put("Print", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                printCanvas(c, null);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        canvasPane = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        canvasPane.setSize(new java.awt.Dimension(600, 600));
        canvasPane.setLayout(new java.awt.BorderLayout());
        getContentPane().add(canvasPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Demo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Demo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Demo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Demo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
               

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    
                    int rc = JOptionPane.showConfirmDialog(null, "Load from a saved XML File?", "Select", JOptionPane.YES_NO_OPTION);
        
                    boolean load = (rc == JOptionPane.YES_OPTION);
                    
                    new Demo(load).setVisible(true);
                    
                } catch (IOException | JAXBException | ClassNotFoundException ex) {
                    Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel canvasPane;
    // End of variables declaration//GEN-END:variables
}
