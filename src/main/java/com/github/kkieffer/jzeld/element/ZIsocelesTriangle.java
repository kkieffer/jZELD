
package com.github.kkieffer.jzeld.element;

import java.awt.Color;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author kkieffer
 */
@XmlRootElement(name = "ZIsocelesTriangle")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZIsocelesTriangle extends ZAbstractTriangle {

        
    protected ZIsocelesTriangle() {
        super();
    }
    
    public ZIsocelesTriangle(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor) {
        super(TriType.ISOCELES, x, y, width, height, rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor);
    }
    
    protected ZIsocelesTriangle(ZIsocelesTriangle copy, boolean forNew) {
        super(copy, forNew);    
    }
    
    @Override
    public ZIsocelesTriangle copyOf(boolean forNew) {
        return new ZIsocelesTriangle(this, forNew);
    }
    
}