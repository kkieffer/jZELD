
package com.github.kkieffer.jzeld;

import java.text.DecimalFormat;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines a unit of measure to apply to a ZCanvas. The canvas provides measurements in abstract "units" that can be converted to
 * physical units.  The standard measurement is 1 inch = 1 unit  (or 72 pixels = 1 unit).  The scale defines how the physical unit
 * maps to the standard unit.  For instance, centimeter = 2.54, or 2.54 centimeters per 72 pixels.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "UnitMeasure")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitMeasure {
    
    //Define a standard unit of Inch, with a 1:1 ratio
    public static UnitMeasure inchUnit = new UnitMeasure("in", 1.0, 1, 4);
    
    //Define a standard unit of Cm, with a 1:1 ratio
    public static UnitMeasure cmUnit = new UnitMeasure("cm", 2.54, 2, 4);
    
    //Define a standard unit of Pixel, with a 1:1 ratio
    public static UnitMeasure pixUnit = new UnitMeasure("px", 72.0, 100, 10);

    protected static final DecimalFormat fmt = new DecimalFormat("0.00");

    private double scale;
    private String name;
    private int majorTicks;
    private int minorTicks;
    
    protected UnitMeasure() {};
    
    public UnitMeasure(String name, double scale, int majorTicks, int minorTicks) {
        this.scale = scale;
        this.name = name;
        this.majorTicks = majorTicks;
        this.minorTicks = minorTicks;
    }
    
    
    public double getScale() {
        return scale;
    }
    
    public String getName() {
        return name;
    }
    
  
    public int getMajorTicks() {
        return majorTicks;
    }
    
    public int getMinorTicks() {
        return minorTicks;
    }
    
    public String format(double val) {
        return fmt.format(val * scale);
    }
    
    
    public double parseFormat(String text) {
        return Double.parseDouble(text);
    }
    
}
