
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.element.CustomStroke;
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

    public int width = 10;
    
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
        return Color.MAGENTA;
    }
    
}
