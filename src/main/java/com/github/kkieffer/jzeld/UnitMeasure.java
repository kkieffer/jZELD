
package com.github.kkieffer.jzeld;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines a unit of measure to apply to a ZCanvas. The canvas provides measurements in abstract "units" that can be converted to
 * physical units.  The standard measurement is 1 inch = 1 unit  (or 72 pixels = 1 unit). The UnitMeasure class also defines a ratio
 * to the actual size on paper, so that one can draw larger items scaled to fit a piece of printable paper.  The ratio for the standard
 * measurement is 1, since 1 unit will have a 1:1 ratio with an inch on the paper.  One could define ratio of 12:1 for a "foot", where
 * 1 foot is represented by 1 inch (or 1 unit) on the canvas.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZCanvasRuler")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitMeasure {
    
    //Define a standard unit of Inch, with a 1:1 ratio
    public static UnitMeasure inchUnit = new UnitMeasure(1.0, "in", 1.0, 1, 4);
    
    //Define a standard unit of Cm, with a 1:1 ratio
    public static UnitMeasure cmUnit = new UnitMeasure(1.0, "cm", 2.54, 2, 4);
    
    //Define a standard unit of Pixel, with a 1:1 ratio
    public static UnitMeasure pixUnit = new UnitMeasure(1.0, "px", 72.0, 100, 10);

    
    private double scale;
    private String name;
    private double ratio;
    private int majorTicks;
    private int minorTicks;
    
    private UnitMeasure() {};
    
    public UnitMeasure(double ratio, String name, double scale, int majorTicks, int minorTicks) {
        this.scale = scale;
        this.name = name;
        this.ratio = ratio;
        this.majorTicks = majorTicks;
        this.minorTicks = minorTicks;
    }
    
    
    public double getScale() {
        return scale;
    }
    
    public String getName() {
        return name;
    }
    
    public double getRatio() {
        return ratio;
    }
    
    public int getMajorTicks() {
        return majorTicks;
    }
    
    public int getMinorTicks() {
        return minorTicks;
    }
    
}
