
package com.github.kkieffer.jzeld.attributes;

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

    
}
