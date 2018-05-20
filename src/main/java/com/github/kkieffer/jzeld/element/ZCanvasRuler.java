
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.JAXBAdapter.FontAdapter;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * ZCanvasRuler defines a fixed, immovable ruler on the top of the window (horizontal) or the left of the window (vertical)
 * It measures the ZCanvas area in units.  The ruler has all the attributes of a rectangle, and supports major ticks with labels
 * on the unit boundary or multiple thereof, as well as minor ticks.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZCanvasRuler")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ZCanvasRuler extends ZRectangle {


    public enum Unit {INCHES(1.0)    {@Override public String toString(){return "in";}},
                      CM(2.54)        {@Override public String toString(){return "cm";}},
                      PIXELS(72.0)    {@Override public String toString(){return "px";}};
                      
            
        private final double scale;
        private Unit(double s) {
            scale = s;
        }
        public double getScale() {return scale;};
    }
    
    private boolean isHoriz;
    private Unit unit;

    @XmlJavaTypeAdapter(FontAdapter.class)
    private Font labelFont;
        
    private int majorTickStep;
    private int minorTicks;
    
    protected ZCanvasRuler() {}
    
    /**
     * Create a horizontal or vertical ruler
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param border border
     * @param width the width in units of the ruler
     * @param isHorizontal true for a horizontal ruler, false for vertical
     * @param borderColor color of the ruler border, null for no border
     * @param backgroundColor fill color of the ruler, null for transparent
     * @param labelFont the font for the unit and major tick markers
     * @param unit the type of unit, inches or centimeters
     * @param majorTickStep number of units between major ticks
     * @param minorTicks minor ticks per unit, 0 for none. One minor tick will lie directly on the major tick.
     */
    public ZCanvasRuler(double x, double y, int border, double width, boolean isHorizontal, Color borderColor, Color backgroundColor, Font labelFont, Unit unit, int majorTickStep, int minorTicks) {
        super(x, y, isHorizontal ? -1 : width, !isHorizontal ? -1 : width, 0.0, false, false, border, borderColor, null, backgroundColor);
        isHoriz = isHorizontal;
        this.unit = unit;
        this.labelFont = labelFont;
        this.majorTickStep = majorTickStep;
        this.minorTicks = minorTicks;
    }
    
    @Override
    public ZCanvasRuler copyOf() {
        return this;  //don't copy
    }

    @Override
    public void paint(Graphics2D g, int unitSize, int width, int height) {
        
        
        super.paint(g, unitSize, width, height);
        
        Font f = new Font(labelFont.getFontName(), labelFont.getStyle(), (int)(labelFont.getSize2D()*unitSize/72.0));
        FontMetrics fontMetrics = g.getFontMetrics(f);
        g.setFont(f);

        double scale = majorTickStep * (double)unitSize / unit.getScale();
       
        int majorVal = 0;
        if (isHoriz) {
            for (double i=0; i<width; i+=scale) {
                
                int inc = (int)Math.round(i);
                
                g.drawLine(inc, 0, inc, 3*height/4);  //draw a vertical bar at i for major tick
                
                for (double j=i; j<width; j+=scale/minorTicks) {
                    int jnc = (int)Math.round(j);
                    g.drawLine(jnc, 0, jnc, height/4);  //draw a vertical bar at j for minor tick
                }
                
                if (i==0)
                    g.drawString(unit.toString(), 2, fontMetrics.getHeight());  //draw unit name instead of zero
                else
                    g.drawString(Integer.toString(majorVal), inc+2, height-2);

                majorVal+=majorTickStep;
            }
        } 
        
        else { //vertical
             for (double i=0; i<height; i+=scale) {
                 
                int inc = (int)Math.round(i);

                g.drawLine(0, inc, 3*width/4, inc);  //draw a horizontal bar at i for major tick
                
                 for (double j=i; j<height; j+=scale/minorTicks) {
                        int jnc = (int)Math.round(j);
                     g.drawLine(0, jnc, width/4, jnc);  //draw a horizontal bar at j for minor tick
                 }
                 
                if (i==0)
                    g.drawString(unit.toString(), 0, fontMetrics.getHeight());  //draw unit name instead of zero
                else
                    g.drawString(Integer.toString(majorVal), 2, inc-2);

                majorVal+=majorTickStep;
             }
        }
    
        
    }
    
}
