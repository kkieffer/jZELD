
package com.github.kkieffer.jzeld;

import java.io.Serializable;
import java.text.DecimalFormat;
import javax.xml.bind.Unmarshaller;
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
public class UnitMeasure implements Serializable {
    
    protected static final String SQ = "\u00B2";  //superscript 2
    
    //Define a standard unit of Inch, with a 1:1 ratio
    public static UnitMeasure inchUnit = new UnitMeasure("in", 1.0, 1, 4, 2);
    
    //Define a standard unit of Cm, with a 1:2.54 ratio
    public static UnitMeasure cmUnit = new UnitMeasure("cm", 2.54, 2, 4, 2);
 
     //Define a standard unit of MM, with a 1:25.4 ratio
    public static UnitMeasure mmUnit = new UnitMeasure("mm", 25.4, 20, 40, 1);
 
    
    //Define a standard unit of Pixel, with a 1:1 ratio
    public static UnitMeasure pixUnit = new UnitMeasure("px", 72.0, 100, 10, 0);

   
    private double scale;
    private String name;
    private int majorTicks;
    private int minorTicks;
    private int precision;
    
    protected transient DecimalFormat dFormat;
    
    protected void afterUnmarshal(Unmarshaller u, Object parent) {
        setDecimalFormatString();
    }
     
    protected UnitMeasure() {};
    
    public UnitMeasure(String name, double scale, int majorTicks, int minorTicks, int precision) {
        this.scale = scale;
        this.name = name;
        this.majorTicks = majorTicks;
        this.minorTicks = minorTicks;
        this.precision = precision;
        setDecimalFormatString();
    }
    
    public UnitMeasure(UnitMeasure copy) {
        name = copy.name;
        scale = copy.scale;
        majorTicks = copy.majorTicks;
        minorTicks = copy.minorTicks;
        precision = copy.precision;
        setDecimalFormatString();
    }
    
    private void setDecimalFormatString() {
        if (precision == 0)
            dFormat = new DecimalFormat("0");
        else {
            String zero = "";
            for (int i=0; i<precision; i++)
                zero = zero + "0";
            dFormat = new DecimalFormat("0." + zero);
            
        }
    }
        
    public void setPrecision(int p) {
        precision = p;
        setDecimalFormatString();
    }
    
    public int getPrecision() {
        return precision;
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
    
    /**
     * Format the value as a linear measurement 
     * @param val the linear value (in units) - this is multiplied by the UnitMeasure scale
     * @param withUnit true to append the unit name
     * @return the formatted value string
     */
    public String format(double val, boolean withUnit) {
        return dFormat.format(val * scale) + (withUnit ? " " + name : "");
    }
    
    /**
     * Format the value as an area measurement (squared)
     * @param val the area value
     * @param withUnit true to append the unit name "squared"
     * @return the formatted value string
     */
    public String formatArea(double val, boolean withUnit) {
        return dFormat.format(val * Math.pow(scale, 2)) + (withUnit ? " " + name + SQ : "");
    }
    
    public double parseFormat(String text) {
        return Double.parseDouble(text);
    }
    
}
