
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.attributes.CustomStroke;
import com.github.kkieffer.jzeld.element.ZElement;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author kkieffer
 */
@XmlRootElement(name = "CircleStroke")
@XmlAccessorType(XmlAccessType.FIELD)

public class CircleStrokeExample extends CustomStroke implements Stroke {

    private final static int width = 10;
    
    private transient Color color;
    
    
    public CircleStrokeExample() {}
            
    @Override
    public CustomStroke copyOf() {
        return new CircleStrokeExample();
    }

    @Override
    public Shape createStrokedShape(Shape p) {
        return new BasicStroke(0.5f).createStrokedShape(new BasicStroke(width).createStrokedShape(p));
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void applyAttributes(double unitSize, Color borderColor, float borderThickness, ZElement.StrokeStyle borderStyle, Float[] dashPattern) {
        color = borderColor;  
        //ignore other attributes
    }
    
}
