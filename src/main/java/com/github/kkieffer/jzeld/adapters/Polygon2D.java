
package com.github.kkieffer.jzeld.adapters;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Convenience class that implements missing double-precision Polygon2D class missing from Java
 * Holds the polygon verticies in arrays and generates a transient Path2D.
 * 
 * This class is immutable and serializable and can be saved with JAXB.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "Polygon2D")
@XmlAccessorType(XmlAccessType.FIELD)
public class Polygon2D implements Shape, Cloneable, Serializable {

    private double[] xpoints;
    private double[] ypoints;
    
    private transient Path2D path;  //dynamically generated
  
    private Polygon2D() {} //for JAXB
    
    /**
     * Create a Polygon2D from the supplied x and y points
     * @param xpoints x coordinates of the verticies
     * @param ypoints y coordinates of the verticies
     * @param npoints number of verticies
     */
    public Polygon2D(double[] xpoints, double[] ypoints, int npoints) { 
        
        if (npoints < 3)
            throw new IndexOutOfBoundsException("Polygons must contain at least 3 verticies");
        
        if (npoints > xpoints.length || npoints > ypoints.length) { 
            throw new IndexOutOfBoundsException("Array size mismatch with npoints"); 
        } 
        
        this.xpoints = new double[npoints]; 
        this.ypoints = new double[npoints]; 
        System.arraycopy(xpoints, 0, this.xpoints, 0, npoints); 
        System.arraycopy(ypoints, 0, this.ypoints, 0, npoints); 

        createPath();
    } 
    
    /**
     * Creates a Polygon2D from a Polygon. The original is not changed.
     * @param p 
     */
    public Polygon2D(Polygon p) { 
        
        this.xpoints = new double[p.npoints]; 
        this.ypoints = new double[p.npoints]; 
        for (int i=0; i<p.npoints; i++) { 
            xpoints[i] = (double)p.xpoints[i]; 
            ypoints[i] = (double)p.ypoints[i]; 
        } 
        createPath();
    } 
    
    protected void afterUnmarshal(Unmarshaller u, Object parent) {
        createPath();
    }
    
    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        createPath();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) 
          return true;
        
        if (!(o instanceof Polygon2D)) 
          return false;
        
        Polygon2D p = (Polygon2D)o;
        
        if (Arrays.equals(xpoints, p.xpoints) && Arrays.equals(ypoints, p.ypoints))
            return true;
         else
            return false;
    }
    
    
    @Override
    public int hashCode() {
        return Objects.hash(xpoints, ypoints);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException { 
        super.clone();
        return new Polygon2D(xpoints, ypoints, xpoints.length); 
    } 
    
    private void createPath() { 
        path = new Path2D.Double(); 
        path.moveTo(xpoints[0], ypoints[0]); 
        for (int i=1; i<xpoints.length; i++) { 
            path.lineTo(xpoints[i], ypoints[i]); 
        } 
        path.closePath();
    } 

    
    //-------- Methods below just pass the call to the Path2D method
    
    @Override
    public Rectangle getBounds() {
        return path.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
        return path.getBounds2D();
    }

    @Override
    public boolean contains(double x, double y) {
        return path.contains(x, y);
    }

    @Override
    public boolean contains(Point2D p) {
        return path.contains(p);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return path.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return path.intersects(r);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return path.contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return path.contains(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return path.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return path.getPathIterator(at, flatness);
    }
    
}
