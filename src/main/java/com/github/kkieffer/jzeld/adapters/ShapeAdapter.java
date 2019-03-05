
package com.github.kkieffer.jzeld.adapters;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import javax.xml.bind.annotation.adapters.XmlAdapter;

 /**
  * ShapeAdapter takes a Path2D and converts it to a string representation.  Format is:
  * SegmentType:d1,d2,d3,d4,d5,d6; SegmentType:.....  where SegmentType is an enum defining the type of segment and d1-d6
  * are 6 doubles representing the coordinates for that path.
 */
public class ShapeAdapter extends XmlAdapter<String, Shape> {
    
    public enum SegmentType {   POINT       {@Override public int getPathType() { return PathIterator.SEG_MOVETO;}},
                                LINE        {@Override public int getPathType() { return PathIterator.SEG_LINETO;}},
                                QUADRCURVE  {@Override public int getPathType() { return PathIterator.SEG_QUADTO;}},
                                CUBICCURVE  {@Override public int getPathType() { return PathIterator.SEG_CUBICTO;}},
                                CLOSE       {@Override public int getPathType() { return PathIterator.SEG_CLOSE;}};
                             
        public abstract int getPathType();
        
        public static SegmentType fromPathType(int type) {
            for (SegmentType t : SegmentType.values()) {
                if (t.getPathType() == type)
                    return t;
            }
            return null;
        }
        
        public void addToPath(Path2D path, double[] coord) {
            switch (this) {
                case POINT:
                    path.moveTo(coord[0], coord[1]);
                    break;
                case LINE:
                    path.lineTo(coord[0], coord[1]);
                    break;
                case QUADRCURVE:
                    path.quadTo(coord[0], coord[1], coord[2], coord[3]);
                    break;
                case CUBICCURVE:
                    path.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[5]);
                    break;
                case CLOSE:
                    path.closePath();
            }
        }
        
    }
      
    /**
     * Convenience method to deep copy a shape using marshall/unmarshall 
     * @param s the shape to copy
     * @return a deep copy of the shape
     */
    public static Shape copyOf(Shape s) {
    
        ShapeAdapter a = new ShapeAdapter();
        try {
            return a.unmarshal(a.marshal(s));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    } 
   

    @Override
    public String marshal(final Shape s) throws Exception {

        StringBuilder b = new StringBuilder();
        Path2D.Double path = new Path2D.Double(s);

        if (path.getWindingRule() == Path2D.WIND_NON_ZERO)
            b.append(("WIND_NON_ZERO "));
        else
            b.append("WIND_EVEN_ODD ");

        PathIterator pi = path.getPathIterator(null);
        while (!pi.isDone()) {
            double[] coords = new double[6];
            int type = pi.currentSegment(coords);
            SegmentType t = SegmentType.fromPathType(type);

            b.append(t.toString() + ":");
            for (int i=0; i<5; i++) {
                b.append(coords[i] + ",");
            }
            b.append(coords[5]);
            b.append("; ");
            pi.next();
        }

        return b.toString();

    }

    @Override
    public Shape unmarshal(String v) throws Exception {

        Path2D path = new Path2D.Double();

        //The string may or may not start with a winding rule, if so set it and remove it from the string. Default if not set is NON_ZERO
        if (v.startsWith("WIND_NON_ZERO")) {
            path.setWindingRule(Path2D.WIND_NON_ZERO);
            v = v.substring("WIND_NON_ZERO".length());
        }
        else if (v.startsWith("WIND_EVEN_ODD")) {
            path.setWindingRule(Path2D.WIND_EVEN_ODD);
            v = v.substring("WIND_EVEN_ODD".length());
        }

        String[] segments = v.trim().split(";");  //Get all segments separated by semicolons

        for (String seg : segments) {

            String[] segPieces = seg.trim().split(":");
            if (segPieces.length != 2)
                throw new Exception("Each segment must have 2 pieces: type and coordinates");

            SegmentType t = SegmentType.valueOf(segPieces[0].trim());
            if (t == null)
                throw new Exception("Unknown Segment type: " + segPieces[0].trim());

            String[] coordStrings = segPieces[1].split(",");
            if (coordStrings.length != 6)
                throw new Exception("Must have 6 coordinates for each Segment");

            double[] coords = new double[6];
            for (int i=0; i<6; i++)
                coords[i] = Double.parseDouble(coordStrings[i]);

            t.addToPath(path, coords);

        }

        return path;

    }


    
    
    
}
