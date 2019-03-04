
package com.github.kkieffer.jzeld.attributes;

import com.github.kkieffer.jzeld.element.ZElement.StrokeStyle;
import java.awt.Color;
import java.awt.Stroke;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author kkieffer
 */
@XmlRootElement(name = "CustomStroke")
public abstract class CustomStroke implements Stroke {
    
    public abstract CustomStroke copyOf();
    public abstract Color getColor();

    /**
     * Prior to drawing with the custom stroke, this method is called with the currently set attributes.  These may be ignored or applied to the custom stroke as desired
     * @param unitSize unit to pixel scale
     * @param borderColor the current set line color
     * @param borderThickness the current set line thickness
     * @param borderStyle the current set StrokeStyle
     * @param dashPattern dash pattern, null if none
     */
    public abstract void applyAttributes(double unitSize, Color borderColor, float borderThickness, StrokeStyle borderStyle, Float[] dashPattern);

}
