
package com.github.kkieffer.jzeld.element;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A ZShape is an element that defines an arbitrary shape with a border (that has color and thickness) and an interior color.  The bounds box
 * is set to completely contain the shape bounds. 
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZShape")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZShape extends ZAbstractShape {

    public static final double MIN_SHAPE_DIMENSION = 0.2;
    
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
     * ShapeAdapter takes a Path2D and converts it to a string representation.  Format is:
     * SegmentType:d1,d2,d3,d4,d5,d6; SegmentType:.....  where SegmentType is an enum defining the type of segment and d1-d6
     * are 6 doubles representing the coordinates for that path.
     */
    private static class ShapeAdapter extends XmlAdapter<String, Shape> {

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
    
    
    @XmlJavaTypeAdapter(ShapeAdapter.class)
    protected Shape shape;  //holds the original, unaltered shape
    
    transient protected Shape scaledShape;  //holds a resized version of the shape for painting
    
    protected ZShape() {}
    
    /**
     * Create a shape
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param s the shape from which to draw
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param canSelect if the object can be selected by the ZCanvas mouse click
     * @param canResize if the object can be resized by the mouse drag
     * @param canMove if the object can be moved 
     * @param borderWidth unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param dashPattern the border dash pattern, null for solid
     * @param fillColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     * @param pA paint attributes
     * @param cS any custom stroke
     * @param borderStyle the border style
     */
    public ZShape(double x, double y, Shape s, double rotation, boolean canSelect, boolean canResize, boolean canMove, float borderWidth, Color borderColor, Float[] dashPattern, Color fillColor, PaintAttributes pA, CustomStroke cS, StrokeStyle borderStyle) {
        super(x, y, s.getBounds2D().getWidth() == 0 ? MIN_SHAPE_DIMENSION : s.getBounds2D().getWidth(), 
                    s.getBounds2D().getHeight() == 0 ? MIN_SHAPE_DIMENSION : s.getBounds2D().getHeight(), 
                    rotation, canSelect, canResize, canMove, borderWidth, borderColor, dashPattern, fillColor, borderStyle);        
        this.shape = s;
        this.paintAttr = pA;
        this.customStroke = cS;        
    }
    
    protected ZShape(ZShape src, boolean forNew) {
        super(src, forNew);
        
        try {
            //Make a copy of the shape
            ShapeAdapter a = new ShapeAdapter();
            this.shape = a.unmarshal(a.marshal(src.shape));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    

    @Override
    public ZShape copyOf(boolean forNew) {
        return new ZShape(this, forNew);
    }
    
    public void setShape(Shape s) {
        this.shape = s;
        super.setSize(s.getBounds2D().getWidth(), s.getBounds2D().getHeight(), MIN_SHAPE_DIMENSION, 1.0);
    }
    
    
     @Override
    protected String getShapeSummary() {       
        return "A general shape created by drawing or merging other elements.";
    }
    
    @Override
    protected String getShapeDescription() {
        return "";     
    }
    
    
    @Override
    protected Shape getAbstractShape() {
        ZShape copy = copyOf(false);
        return copy.shape;
    }
    
    /**
     * Resize the object, new width and height in pixels
     * @param w width in pixels
     * @param h height in pixels
     * @param minSize the minimum size, in pixels
     * @param scale scale factor
     */
    @Override
    public void setSize(double w, double h, double minSize, double scale) {
   
        //Hold onto the old values
        Rectangle2D r = getBounds2D(scale);
        double oldWidth = r.getWidth();
        double oldHeight = r.getHeight();

        //Compute the new size, which may limit the minimum
        super.setSize(w, h, minSize, scale);
        
        //Get the new size and calculate the ratio of new to old
        r = getBounds2D(scale);

        double widthRatio = r.getWidth() / oldWidth;
        double heightRatio = r.getHeight() / oldHeight;

        //Scale the shape by the ratio
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(widthRatio, heightRatio);
        shape = scaleInstance.createTransformedShape(shape);
    }
    

    @Override
    protected void fillShape(Graphics2D g, double unitSize, double width, double height) {
        g.fill(scaledShape);
    }

   
    
    @Override
    protected void drawShape(Graphics2D g, double unitSize, double width, double height) {        
        g.draw(scaledShape);
    }

    @Override
    public boolean supportsFlip() {
        return true;
    }
    
    @Override
    public boolean supportsEdit() {
        return false;
    };
    
    @Override
    public void flipHorizontal() {
        Rectangle2D bounds = getBounds2D();
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(-1.0, 1.0);  //scaling negative creates a mirror image the other direction
        shape = scaleInstance.createTransformedShape(shape);
        AffineTransform translateInstance = AffineTransform.getTranslateInstance(bounds.getWidth(), 0);  //move back to where it was
        shape = translateInstance.createTransformedShape(shape);
        super.flipHorizontal();
    }
    
    @Override
    public void flipVertical() {
        Rectangle2D bounds = getBounds2D();
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(1.0, -1.0);  //scaling negative creates a mirror image the other direction
        shape = scaleInstance.createTransformedShape(shape);
        AffineTransform translateInstance = AffineTransform.getTranslateInstance(0, bounds.getHeight());  //move back to where it was
        shape = translateInstance.createTransformedShape(shape);
        super.flipVertical();
    }
    
    
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(unitSize, unitSize);
        scaledShape = scaleInstance.createTransformedShape(shape);
        
        super.paint(g, unitSize, width, height);

    }

    
}
