
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.adapters.DialogUtils;
import com.github.kkieffer.jzeld.attributes.PaintAttributes;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Using cubic bezier curves, approximates a sinewave based on this analysis:
 * https://stackoverflow.com/questions/29022438/how-to-approximate-a-half-cosine-curve-with-bezier-paths-in-svg
 * 
 * The number of half-wavelengths are adjustable from 1 to unlimited
 *  
 * @author kkieffer
 */
public class ZWave extends ZShape {
  
    public static final ImageIcon waveIcon = new ImageIcon(ZCanvas.class.getResource("/wave.png")); 

    
    private static double MAGIC_NO = 0.3642124232;  //The least squares fit of a cubic bezier curve to a sinewave (see stackoverflow link) 
    
    
    
    private static void createHalfWave(Path2D path, double xOffset, boolean rising) {
        if (rising)
            path.curveTo(xOffset+MAGIC_NO, 0.0, xOffset+1-MAGIC_NO, 1.0, xOffset+1, 1.0);
        else
            path.curveTo(xOffset+MAGIC_NO, 1.0, xOffset+1-MAGIC_NO, 0.0, xOffset+1, 0.0);            
    }

    //Create a waveform with the specified number of halfwaves.  Starts with a rising wave (rising = false because Y coord is positive down in swing)
    private static Shape createWave(int halfWaves) {
        
        Path2D path = new Path2D.Double();
        path.moveTo(0, 1);
        
        boolean rising = false;
        int xOffset = 0;
        
        for (; xOffset<halfWaves; xOffset++) {
            createHalfWave(path, xOffset, rising);
            rising = !rising;
        }
        
        if (halfWaves % 2 != 0) 
            path.lineTo(xOffset, 1);

        return path;
        
    }
    
    
    private int halfWaves;
    
    private transient WaveDialog dialog;
   
    protected ZWave() {}
    
    /**
     * Create a ZWave
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param halfWaves number of halfwavelengths
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove if the object can be moved 
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     * @param lineStyle
     * @param pA paint attributes
     */
    public ZWave(double x, double y, int halfWaves, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, StrokeStyle lineStyle, PaintAttributes pA) {
        super(x, y, new Rectangle2D.Double(0, 0, 2.0, 1.0), rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor, pA, null, null, lineStyle);        
        this.halfWaves = halfWaves;
        updateShape();
    }
    
    protected ZWave(ZWave src, boolean forNew) {
        super(src, forNew);
        this.halfWaves = src.halfWaves;
        //note: don't update the text shape (only create the shape when text or attributes changes
    }
    

    @Override
    public ZWave copyOf(boolean forNew) {
        return new ZWave(this, forNew);
    }
    
    
     @Override
    protected String getShapeSummary() {       
        return "An sinusoidal wave shape with an adjustable number of wavelengths.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "Double-click to set the number of half-wavelengths.";
    }
   
    @Override
    public boolean selectAsShape() {
        return false;
    }
    
    public int getHalfwaves() {
        return halfWaves;
    }
    
    
    public void setHalfWaves(int h) {
        if (h > 0) {
            halfWaves = h;
            updateShape();
        }
    }
    
    protected void updateShape() {
        
             
        Shape wave = createWave(halfWaves);
        
        Rectangle2D bounds = wave.getBounds2D();  
        Rectangle2D currentSize = this.getBounds2D();
        
        //Reset the shape to origin bounds, and scale it to the current shape bounds
        wave = AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY()).createTransformedShape(wave);
        wave = AffineTransform.getScaleInstance(currentSize.getWidth()/bounds.getWidth(), currentSize.getHeight()/bounds.getHeight()).createTransformedShape(wave);
        
        super.setShape(wave);
    }
    
   
    @Override
    public boolean supportsEdit() {
        return true;
    };
    
    @Override
    public boolean selectedForEdit(ZCanvas canvas) {
        
        dialog = new WaveDialog(this, canvas);
        canvas.arrangePopup(dialog);
        dialog.setVisible(true);
        return false;
    }
    
    @Override
    public void deselectedForEdit() {

        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }
    

    
    private static class WaveDialog extends JFrame {

        private final ZWave wave;
        private final JSpinner halfwavesSpinner;
        
        
        private WaveDialog(ZWave w, ZCanvas canvas) {
            super("Edit Wave");
            wave = w;
            
            JPanel p = new JPanel();
            
            JLabel cornerLabel = new JLabel("Number of Half Wavelengths");
            halfwavesSpinner = new JSpinner();
            halfwavesSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
            halfwavesSpinner.setValue(wave.getHalfwaves());
 
            halfwavesSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int n = (int)halfwavesSpinner.getValue();
                    wave.setHalfWaves(n);
                    canvas.repaint();
               }
            });
            
            
                      
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
            layout.columnWidths = new int[] {0, 10, 0, 40, 0};
            layout.rowHeights = new int[] {0, 10, 0};
            p.setLayout(layout);
        
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            p.add(cornerLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            p.add(halfwavesSpinner, gridBagConstraints);

            halfwavesSpinner.setPreferredSize(new Dimension(100, 26));
            Container main = getContentPane();
            main.setLayout(new BorderLayout());
            
            main.add(p, BorderLayout.CENTER);
            JLabel icon = new JLabel();
            icon.setIcon(waveIcon);
            Border margin = new EmptyBorder(0,15,0,15);
            icon.setBorder(new CompoundBorder(icon.getBorder(), margin));
            main.add(icon, BorderLayout.WEST);

            pack();
            Dimension d = new Dimension(340, 175);
            setMinimumSize(d);
            setPreferredSize(d);
            
            DialogUtils.addShortcutAndIcon(p, "dispose");
            
        }
        
       
        
    }
    
   
    

}
