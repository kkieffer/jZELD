
package com.github.kkieffer.jzeld;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * These adapters are used to read and write objects into XML using JAXB.  There is no default method for storing these objects in XML.
 * @author kkieffer
 */
public class JAXBAdapter {
    
   //This is needed because of the name conflict on position, the "Double"
    public static class Rectangle2DAdapter extends XmlAdapter<String, Rectangle2D.Double> {

        @Override
        public Rectangle2D.Double unmarshal(final String xml) throws Exception {
           String[] parts = xml.split(",");
           if (parts.length != 4)
               throw new java.text.ParseException("Rectangle2D.Double must have 4 doubles", 0);
           Rectangle2D.Double d = new Rectangle2D.Double();
           d.x = Double.parseDouble(parts[0].trim());
           d.y = Double.parseDouble(parts[1].trim());
           d.width = Double.parseDouble(parts[2].trim());
           d.height = Double.parseDouble(parts[3].trim());
           return d;
        }

        @Override
        public String marshal(final Rectangle2D.Double object) throws Exception {
           return object.x + ", " + object.y + ", " + object.width + ", " + object.height;
        }

     }
    
    public static class ColorAdapter extends XmlAdapter<String, Color> {

        @Override
        public String marshal(final Color object) throws Exception {
           return Integer.toHexString(object.getRGB());
        }

        @Override
        public Color unmarshal(String v) throws Exception {
            return new Color(Integer.parseUnsignedInt(v, 16), true);
        }

     }
    
    public static class FontAdapter extends XmlAdapter<String, Font> {

        @Override
        public String marshal(final Font f) throws Exception {
            return f.getName() + ", " + f.getStyle() + ", " + f.getSize();
        }

        @Override
        public Font unmarshal(String v) throws Exception {
            String[] split = v.split(",");
            if (split.length != 3)
               throw new java.text.ParseException("Font must have 3 parts: \"Name Style Size\"", 0);
            return new Font(split[0].trim(), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
        }

     }
    
    
    public static class PointAdapter extends XmlAdapter<String, Point> {

        @Override
        public Point unmarshal(final String xml) throws Exception {
           String[] parts = xml.split(",");
           if (parts.length != 2)
               throw new java.text.ParseException("Object must have 2 integers", 0);
           Point p = new Point();
           p.x = Integer.parseInt(parts[0].trim());
           p.y = Integer.parseInt(parts[1].trim());
          
           return p;
        }

        @Override
        public String marshal(final Point object) throws Exception {
           return object.x + ", " + object.y;
        }

     }
    
     /**
      * This class just duplicates the functionality of PointAdapter since they are both storing two integers
      */
     public static class DimensionAdapter extends XmlAdapter<String, Dimension> {

         private final PointAdapter pa = new PointAdapter();
         
        @Override
        public Dimension unmarshal(String v) throws Exception {
             Point p = pa.unmarshal(v);
             return new Dimension(p.x, p.y);
        }

        @Override
        public String marshal(Dimension v) throws Exception {
            return pa.marshal(new Point(v.width, v.height));
        }
         
     }
    
    
}
