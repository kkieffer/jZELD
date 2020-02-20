
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.adapters.JAXBAdapter.FontAdapter;
import com.github.kkieffer.jzeld.UnitMeasure;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * ZCanvasRuler defines a fixed, immovable ruler on the top of the window (horizontal) or the left of the window (vertical)
 * It measures the ZCanvas area in units.  The ruler has all the attributes of a rectangle, and supports major ticks with labels
 * on the unit boundary or multiple thereof, as well as minor ticks.  The ruler has a fixed width however, in pixels, that does not
 * scale with the canvas.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZCanvasRuler")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ZCanvasRuler extends ZRectangle {
    
    private boolean isHoriz;
    private UnitMeasure unit;

    @XmlJavaTypeAdapter(FontAdapter.class)
    private Font labelFont;
        
    private int fixedWidth;  //in pixels
    
    private int majorTickStep;
    private int minorTicks;
    
    private int majorValOffset = 0;
    
    protected ZCanvasRuler() {}
    
    /**
     * Create a horizontal or vertical ruler
     * @param borderThickness border thickness
     * @param width the width in pixels
     * @param isHorizontal true for a horizontal ruler, false for vertical
     * @param borderColor color of the ruler border, null for no border
     * @param backgroundColor fill color of the ruler, null for transparent
     * @param labelFont the font for the unit and major tick markers
     * @param unit the type of unit
     * @param majorTickStep number of units between major ticks
     * @param minorTicks minor ticks per unit, 0 for none. One minor tick will lie directly on the major tick.
     */
    public ZCanvasRuler(int width, boolean isHorizontal, float borderThickness, Color borderColor, Color backgroundColor, Font labelFont, UnitMeasure unit, int majorTickStep, int minorTicks) {
        super(0, 0, isHorizontal ? -1 : width, !isHorizontal ? -1 : width, 0.0, false, false, false, borderThickness, borderColor, null, backgroundColor, StrokeStyle.SQUARE);
        fixedWidth = width;
        isHoriz = isHorizontal;
        this.unit = unit;
        this.labelFont = labelFont;
        this.majorTickStep = majorTickStep;
        this.minorTicks = minorTicks;
    }
    
    public void setMajorValOffset(int offset) {
        this.majorValOffset = offset;
    }
    
    public int getMajorValOffset() {
        return majorValOffset;
    }
    
    
    private void drawZeroMajorTick(Graphics2D g, double unitSize, int pos, int len, boolean isHoriz) {
        
        Stroke s = g.getStroke();
        Color c = g.getColor();
        
        g.setStroke(createBasicStroke(unitSize, borderThickness*3, borderStyle, null));

        if (isHoriz)
            g.drawLine(pos, 0, pos, len);  //draw a vertical bar at pos for major tick
        else
            g.drawLine(0, pos, len, pos);  //draw a horizontal bar at pos for major tick

        g.setStroke(s);  //back to original
        
        g.setColor(Color.WHITE);
        
        if (isHoriz)
            g.drawLine(pos, 0, pos, len);  //draw a vertical bar at pos for major tick
        else
            g.drawLine(0, pos, len, pos);  //draw a horizontal bar at pos for major tick

        g.setColor(c);  //back to original
    }
    
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {
        if (!isVisible())
            return;
                
        width =  isHoriz ? width : fixedWidth;  //horizontal ruler uses the canvas width, vertical uses the fixed width
        height = !isHoriz ? height : fixedWidth;  //horizontal ruler uses the fixedWidth, vertical uses the canvas height
        
        super.paint(g, unitSize, width, height);
        
        FontMetrics fontMetrics = g.getFontMetrics(labelFont);
        g.setFont(labelFont);

        double scale = majorTickStep * (double)unitSize / unit.getScale();
       
        int majorVal = majorValOffset;
        if (isHoriz) {
            for (double i=0; i<width; i+=scale) {
                
                int inc = (int)Math.round(i);
                
                //Draw minors         
                for (double j=i; j<width; j+=scale/minorTicks) {
                    int jnc = (int)Math.round(j);
                    g.drawLine(jnc, 0, jnc, (int)(height/4));  //draw a vertical bar at j for minor tick
                }
                
                //Draw major
                if (majorVal == 0 && majorValOffset != 0)
                    drawZeroMajorTick(g, unitSize, inc, (int)height, true);
                else           
                    g.drawLine(inc, 0, inc, (int)(3*height/4));  //draw a vertical bar at i for major tick
    
                //draw label
                if (i==0)
                    g.drawString(unit.getName(), 2, fontMetrics.getHeight());  //draw unit name instead of zero
                else
                    g.drawString(Integer.toString(majorVal), inc+2, (int)height-2);

                majorVal+=majorTickStep;
            }
        } 
        
        else { //vertical
             for (double i=0; i<height; i+=scale) {
                 
                int inc = (int)Math.round(i);
                
                //draw minors
                for (double j=i; j<height; j+=scale/minorTicks) {
                    int jnc = (int)Math.round(j);
                    g.drawLine(0, jnc, (int)(width/4), jnc);  //draw a horizontal bar at j for minor tick
                }
                 
                //draw major
                if (majorVal == 0 && majorValOffset != 0)
                    drawZeroMajorTick(g, unitSize, inc, (int)width, false);
                else           
                    g.drawLine(0, inc, (int)(3*width/4), inc);  //draw a horizontal bar at i for major tick
      
                //draw label
                if (i==0)
                    g.drawString(unit.getName(), 0, fontMetrics.getHeight());  //draw unit name instead of zero
                else
                    g.drawString(Integer.toString(majorVal), 2, inc-2);

                majorVal+=majorTickStep;
             }
        }
    
        
    }
    
}
