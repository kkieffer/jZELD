package com.github.kkieffer.jzeld;



import com.github.kkieffer.jzeld.attributes.PaintAttributes;
import com.github.kkieffer.jzeld.attributes.ShadowAttributes;
import com.github.kkieffer.jzeld.attributes.TextAttributes;
import com.github.kkieffer.jzeld.adapters.SVGExport;
import com.github.kkieffer.jzeld.contextMenu.ZDefaultContextMenu;
import com.github.kkieffer.jzeld.ZCanvas.Orientation;
import com.github.kkieffer.jzeld.adapters.SVGImport;
import com.github.kkieffer.jzeld.draw.FreeformDraw;
import com.github.kkieffer.jzeld.draw.OrthogonalLineDraw;
import com.github.kkieffer.jzeld.draw.StraightLineDraw;
import com.github.kkieffer.jzeld.element.*;
import com.github.kkieffer.jzeld.attributes.PaintAttributes.RadiusRelative;
import com.github.kkieffer.jzeld.attributes.TextAttributes.HorizontalJustify;
import com.github.kkieffer.jzeld.element.ZArc.ArcType;
import com.github.kkieffer.jzeld.element.ZElement.StrokeStyle;
import com.github.kkieffer.jzeld.element.ZQuadrilateral.QuadType;
import com.github.kkieffer.jzeld.element.ZTriangle.TriType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
        
        c.enableUndoContextSave(false); //disable, so the adding of these elements can't be undone
        
        //Create a red rectangle, large black border, moveable
        ZRectangle r = new ZRectangle(1.0, 1.0, 2.0, 1.0, 25.0, true, true, true, 3, Color.BLACK, new Float[]{.2f, .2f}, Color.RED, StrokeStyle.ROUNDED);
        c.addElement(r);

        //Create an immovable, unselectable green square, rotated to 45 degrees (its a diamond!)
        ZRectangle r2 = new ZRectangle(1.0, 1.0, 1.0, 1.0, 45.0, false, false, false, 1, Color.BLACK, null, Color.GREEN, StrokeStyle.SQUARE);
        c.addElement(r2);
        
        //Create a non-filled rectangle with rounded edges
        ZRectangle r3 = new ZRoundedRectangle(4.0, 1.0, 1.0, 1.0, 0.0, true, true, true, 1, Color.BLACK, new Float[]{.1f, .1f}, null, StrokeStyle.SQUARE, 0.1);
        c.addElement(r3);
        
        //Create a semi-transparent oval
        ZOval o = new ZOval(1.0, 3.0, 1.0, 1.0, 0.0, true, true, true, 0, Color.BLACK, null, new Color(0, 0, 255, 128), StrokeStyle.SQUARE);
        c.addElement(o);
        
        //Create a semi-transparent circle
        ZCircle cr = new ZCircle(1.0, 3.5, 1.0, 1.0, 0.0, true, true, true, 0, Color.BLACK, null, new Color(0, 0, 255, 128), StrokeStyle.SQUARE);
        c.addElement(cr);
        
         //Create a semi-transparent circle
        ZOval o2 = new ZOval(2.5, 3.0, 1.0, 1.0, 0.0, true, true, true, 1, Color.BLACK, null, null, StrokeStyle.SQUARE);
        PaintAttributes radPaint = PaintAttributes.createRadialPaintAttribute(new Point2D.Double(0.5, 0.5), null, 0.5f, RadiusRelative.SHORTEST, new float[]{0.0f, 1.0f}, new Color[]{Color.WHITE, Color.BLACK}, MultipleGradientPaint.CycleMethod.NO_CYCLE);
        o2.setPaintAttributes(radPaint);
        c.addElement(o2);
       
        //Create a simple black line
        ZLine l = new ZLine(5.0, 2.5, 1.0, 0.0, true, true, true, 1, Color.BLACK, null, StrokeStyle.SQUARE);
        c.addElement(l);
        
        //Right triangle, yellow, big borders, immoveable
        ZTriangle t = new ZTriangle(TriType.RIGHT, 1.0, 6.0, 1.0, 1.0, 0.0, true, true, false, 4, Color.BLACK, null, Color.YELLOW, StrokeStyle.SQUARE);
        c.addElement(t);
        
        
        ZEquilateralPolygon p = new ZEquilateralPolygon(6.0, 4.0, 1.0, 1.0, 0.0, true, true, true, 4, Color.BLACK, null, Color.BLUE, StrokeStyle.SQUARE, 5);        
        PaintAttributes linPaint = PaintAttributes.createLinearPaintAttribute(new Point2D.Double(.25,.5), new Point2D.Double(.75,.5), new float[]{0.1f, 0.5f, 0.8f}, new Color[]{Color.BLUE, Color.RED, Color.GREEN}, MultipleGradientPaint.CycleMethod.REFLECT);
        p.setPaintAttributes(linPaint);
        p.setShadowAttributes(new ShadowAttributes());
        c.addElement(p);
          
        ZQuadrilateral pg = new ZQuadrilateral(QuadType.RHOMBUS, 6.0, 2.0, 1.0, 1.0, 0.0, true, true, true, 0, Color.BLACK, null, Color.BLUE, StrokeStyle.SQUARE, 20);
        pg.setOpacity(0.5f);
        pg.setPrintable(false);  //don't print
        c.addElement(pg);
        
         //Create an arc
        ZArc a = new ZArc(1.0, 4.0, 1.0, 1.0, 0.0, true, true, true, 7, Color.ORANGE, null, Color.RED, StrokeStyle.SQUARE, 30.0, 45.0, ArcType.PIE);
        c.addElement(a);
            
        //Add the test image
        InputStream testImg = Demo.class.getClassLoader().getResourceAsStream("tiger.jpg");
        BufferedImage image = ImageIO.read(testImg);
     
        ZImage img = new ZImage(5.0, 5.0, 3.0, 3.0, 0.0, true, true, true, 0, Color.BLACK, null, Color.GRAY, StrokeStyle.SQUARE, image);  
        img.setName("Tiger");
        c.addElement(img);
        
        ZRectangle r4 = new ZRectangle(5.5, 6.0, 2.0, 1.0, 0.0, true, true, true, 3, Color.BLACK, null, Color.RED, StrokeStyle.SQUARE);
        PaintAttributes textPaint = PaintAttributes.createTexturePaintAttribute(image, .4f, .4f);
        r4.setPaintAttributes(textPaint);
        c.addElement(r4);
        
        
        ZRectangle r5 = new ZRectangle(6, 0, 2.0, 1.0, 0.0, true, true, true, 3, Color.BLACK, null, Color.RED, StrokeStyle.SQUARE);
        PaintAttributes conPaint = PaintAttributes.createConicalPaintAttribute(new Point2D.Double(.25, .25), new float[]{0.25f, 0.5f, 0.6f, 0.75f}, new Color[]{Color.BLUE, Color.WHITE, Color.BLACK, Color.GREEN});
        r5.setPaintAttributes(conPaint);
        c.addElement(r5);
        
        
        //Block Arrow
        ZBlockArrow b = new ZBlockArrow(8.0, 2.0, 2.0, 1.0, 0.0, true, true, true, 10, Color.WHITE, null, Color.YELLOW, StrokeStyle.SQUARE);
        c.addElement(b);
        b.setOpacity(0.8f);
        b.setCustomStroke(new CircleStrokeExample());
    
        
        //Create some editable text
        Font f = new Font("SERIF", Font.BOLD, 22);
        TextAttributes t2 = new TextAttributes(HorizontalJustify.CENTER, f, Color.RED);
        ZEditableText etxt = new ZEditableBoundedText(3.0, 6.0, .5, .5, 0.0, true, "MyEditableText", 0, Color.BLACK, Color.LIGHT_GRAY, StrokeStyle.SQUARE, t2);
        c.addElement(etxt);
 
        c.enableUndoContextSave(true); //re-enable undo

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

        ZCanvas c;
        
        if (loadFromFile) {
            File f = new File("test.xml");
            if (!f.exists()) {
                JOptionPane.showMessageDialog(null, "File does not exist yet.  Create by running Demo and CTRL-S to save a file", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            c = CanvasSave.fromFile(f);
            
        } else {
            
            //Create our ZCanvas with a dark grey background, label font, centimeter scale, dark grey cursor lines, 10 undo stack
            c = new ZCanvas(Color.WHITE, LABEL_FONT, UnitMeasure.cmUnit, Color.DARK_GRAY, 10, new Point(36, 36), new Dimension(1400, 800));
            c.setPageSize(new Dimension(1200, 600), Orientation.LANDSCAPE);
            c.setPageMargins(new Rectangle2D.Double(50, 50, 1100, 500));

            //Create two rulers, 20 pixels thick, with different minor tick spacing
             ZCanvasRuler hRule = new ZCanvasRuler(20, true, 1, Color.BLACK, Color.LIGHT_GRAY, LABEL_FONT, UnitMeasure.cmUnit, 4, 2);
             c.setHorizontalRuler(hRule);
             ZCanvasRuler vRule = new ZCanvasRuler(20, false, 1, Color.BLACK, Color.LIGHT_GRAY, LABEL_FONT, UnitMeasure.cmUnit, 2, 2);
             c.setVerticalRuler(vRule);

             //Create a grid in the back, dashed thin line
             ZGrid grid = new ZGrid(0.5f, Color.LIGHT_GRAY, new Float[]{.05f}, UnitMeasure.cmUnit, 2);
             c.setGrid(grid);
            
            createDemoElements(c);
        }

         

        ZDefaultContextMenu m = new ZDefaultContextMenu(c);
        c.setContextMenu(m);
        
        new ZDefaultCanvasHotkeys(c);  //add hotkeys
        
        c.requestFocusInWindow();
        canvasPane.add(c, BorderLayout.CENTER);

 
        InputMap im = c.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = c.getActionMap();

        //Add some shortcuts to add line or free drawings to the canvas
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "OrthoDraw");
        am.put("OrthoDraw", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.drawOn(new OrthogonalLineDraw(c, false, 1.0f, Color.BLACK, StrokeStyle.SQUARE));
            }
        });
        
        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "LineDrawNoClose");
        am.put("LineDrawNoClose", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.drawOn(new StraightLineDraw(c, false, 1.0f, Color.BLACK, StrokeStyle.SQUARE));
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "LineDraw");
        am.put("LineDraw", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.drawOn(new StraightLineDraw(c, true, 1.0f, Color.BLACK, StrokeStyle.SQUARE));
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "FreeDraw");
        am.put("FreeDraw", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.drawOn(new FreeformDraw(c, true, 1.0f, Color.BLACK, StrokeStyle.SQUARE));
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Save");
        am.put("Save", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File f = new File("test.xml");
                    CanvasSave.toFile(c, f);
                    System.out.println("Saved file: " + f.getAbsolutePath());
                } catch (JAXBException ex) {
                    Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "SelectAll");
        am.put("SelectAll", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                c.selectAll();
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Print");
        am.put("Print", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                printCanvas(c, null);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "SaveElementImage");
        am.put("SaveElementImage", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedImage i = c.printSelectedElementsToImage(3);  //print at 216 dpi
                File f = new File("test.png");
                try {
                    ImageIO.write(i, "png", f);
                    System.out.println("Wrote element to " + f.getAbsolutePath());
                } catch (IOException ex) {
                    System.err.println(ex);
                }   
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.ALT_DOWN_MASK), "SaveSVG");
        am.put("SaveSVG", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File f = new File("saved.svg");
                    SVGExport.toSVG(c, f);
                    System.out.println("Wrote canvas to " + f.getAbsolutePath());
                } catch (IOException ex) {
                    System.err.println(ex);
                } 
            }
        });      
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK), "ImportSVG");
        am.put("ImportSVG", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File f = new File("toImport.svg");
                    SVGImport.fromFile(c, f, false);
                    System.out.println("Imported " + f.getAbsolutePath());
                } catch (Exception ex) {
                    System.err.println(ex);
                } 
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

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        canvasPane = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(canvasPane);

        canvasPane.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(canvasPane);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
