package com.github.kkieffer.jzeld.element;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A ZPolygon is a abstract closed polygon
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZPolygon")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ZPolygon extends ZAbstractShape {
    
    @XmlTransient
    private Path2D polygon; 
  
    protected ZPolygon(){}
    

    public ZPolygon(ZPolygon copy) {
        super(copy);
    }
    
    public ZPolygon(double x, double y, double width, double height, double rotation, boolean canSelect, boolean canResize, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor) {
        super(x, y, width, height, rotation, canSelect, canResize, borderWidth, borderColor, dashPattern, fillColor);
    }
    
    protected abstract Path2D getPath2D(double width, double height); 
    
    @Override
    protected void fillShape(Graphics2D g, int unitSize, int width, int height) {
        g.fill(polygon);
    }
    
    @Override
    protected void drawShape(Graphics2D g, int unitSize, int width, int height) {
        g.draw(polygon);
    }
    
    
    @Override
    public void paint(Graphics2D g, int unitSize, int width, int height) {
 
       polygon = getPath2D(width, height);
       super.paint(g, unitSize, width, height);
    }

    @Override
    protected Shape getAbstractShape() {
        Rectangle2D r = getBounds2D();
        return getPath2D(r.getWidth(), r.getHeight());
    }

    @Override
    public boolean supportsFlip() {
        return false;
    }
    
   

    
}
