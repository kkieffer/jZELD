
package com.github.kkieffer.jzeld.adapters;

import com.github.kkieffer.jzeld.attributes.PaintAttributes;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.apache.batik.bridge.SVGPatternElementBridge;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.MarkerShapePainter;
import org.apache.batik.gvt.RasterImageNode;
import org.apache.batik.gvt.ShapePainter;
import org.apache.batik.gvt.StrokeShapePainter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Collection of static methods that provide utilities when converting SVG to AWT
 * 
 * @author kkieffer
 */
class SVGImportUtils {
    
   static class UnsupportedSVGProperty extends Exception {

        private UnsupportedSVGProperty(String s) {
            super(s);
        }
    }
    
   
    static String getPainterInfo(ShapePainter sp) {
        
        if (sp instanceof FillShapePainter) {  
            Paint fillPaint = ((FillShapePainter)sp).getPaint();
            if (fillPaint == null)
                return "";
                       
            return "Fill:" + fillPaint.getClass().getSimpleName() + "  ";
                
        }
        else if (sp instanceof StrokeShapePainter) {
       
            Stroke stroke = ((StrokeShapePainter)sp).getStroke();
            Paint strokePaint = ((StrokeShapePainter)sp).getPaint();   
            
            if (strokePaint == null)
                return "";
            
            return "Stroke:" + (stroke == null ? "null" : stroke.getClass().getSimpleName()) 
                             + ":" 
                             + strokePaint.getClass().getSimpleName()
                             + "  ";
        }
        else if (sp instanceof MarkerShapePainter) {
            
            return "Marker";
            
        }
        
        
        return "";
    }
    
    /**
     * Get the alpha composite of a node that includes all its previous node alphas
     */
    static AlphaComposite getGlobalAlphaComposite(GraphicsNode node) {
        AlphaComposite composite = (AlphaComposite) node.getComposite();
        
        if (composite != null) {
            float alpha = composite.getAlpha();
            while ((node = node.getParent()) != null) {
                AlphaComposite parentComposite = (AlphaComposite) node.getComposite();
                if (parentComposite != null) 
                    alpha = alpha * parentComposite.getAlpha();
                
            }
            composite = AlphaComposite.getInstance(composite.getRule(), alpha);
        }
        
        return composite;
    }
    
    /**
     * Convert a Batik enumerated cycle method to a java.awt CycleMethod
     * @param e the enum to convert
     * @return the matching java.awt enum
     */
    static MultipleGradientPaint.CycleMethod convertCycleMethod(org.apache.batik.ext.awt.MultipleGradientPaint.CycleMethodEnum e) throws UnsupportedSVGProperty  {
        if (e.equals(org.apache.batik.ext.awt.MultipleGradientPaint.NO_CYCLE))
            return MultipleGradientPaint.CycleMethod.NO_CYCLE;
        else if (e.equals(org.apache.batik.ext.awt.MultipleGradientPaint.REFLECT))
            return MultipleGradientPaint.CycleMethod.REFLECT;
        else if (e.equals(org.apache.batik.ext.awt.MultipleGradientPaint.REPEAT))
            return MultipleGradientPaint.CycleMethod.REPEAT;
        else
            throw new UnsupportedSVGProperty("Unknown Multiple Gradient Paint enum: " + e.getClass().getCanonicalName());
    }
    
    /**
     * Converts a point from svg pixel coordinates to fraction relative to bounds rectangle
     * @param p a point, absolute
     * @param bounds the bounds rectangle
     * @param scale the pixel scale
     * @return 
     */
    static Point2D gradientPointToRelative(Point2D p, Rectangle2D bounds, double scale) {
        
        //convert to units
        double x = (p.getX()/scale);
        double y = (p.getY()/scale);
        
        //Make relative to shape coordinates (awt Gradient)
        x -= bounds.getX();
        y -= bounds.getY();
        
        //Relative to width/height  (jZeld convention)
        x /= bounds.getWidth();
        y /= bounds.getHeight();
        
        return new Point2D.Double(x, y);
    }
    
    /**
     * From the parsed svg shape, apply the svg transform, convert to canvas units, and translate to the origin
     * @param svgShape the svg shape
     * @param svgTransform the current svg transform
     * @return a shape suitable for placing on a ZCanvas, the shape 
     */
    static Pair<Shape, Rectangle2D> transformShape(Shape svgShape, AffineTransform svgTransform)  {
    
        if (svgShape == null)
            return new ImmutablePair(null, null);         
        
        Shape s = svgTransform.createTransformedShape(svgShape);  //apply our current node transform
        
        AffineTransform scaleToUnit = AffineTransform.getScaleInstance(1/72.0, 1/72.0);  //scale to units
        Shape unitShape = scaleToUnit.createTransformedShape(s);
        Rectangle2D bounds = unitShape.getBounds2D();
        
  
        AffineTransform backToOrigin = AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY());
        Shape baseShape = backToOrigin.createTransformedShape(unitShape);  //remove the translation from the origin
     
        return new ImmutablePair(baseShape, bounds);
    }
    
    
    static Shape transformClip(Shape clip, AffineTransform svgTransform)  {
    
        if (clip == null)
            return null;         
        
        Shape s = svgTransform.createTransformedShape(clip);  //apply our current node transform
        
        AffineTransform scaleToUnit = AffineTransform.getScaleInstance(1/72.0, 1/72.0);  //scale to units
        Shape unitShape = scaleToUnit.createTransformedShape(s);
       
        return unitShape;
    }
    
    static BasicStroke transformStroke(Stroke stroke, AffineTransform svgTransform) throws UnsupportedSVGProperty {
        
        if (stroke instanceof BasicStroke) {
            
            BasicStroke bs = (BasicStroke)stroke;
            
            double strokeWidthScale = (double)Math.sqrt(svgTransform.getScaleX() * svgTransform.getScaleY());

            
            float width = (float)(strokeWidthScale * (double)bs.getLineWidth());
                     
            float[] da = bs.getDashArray();
            Float[] dashPattern = null;
            if (da != null) {
                dashPattern = ArrayUtils.toObject(da);
                for (int i=0; i<dashPattern.length; i++)
                    dashPattern[i] /= ((float)strokeWidthScale * 72.0f);   //convert to units and scale
            }
           
            return new BasicStroke(width, bs.getEndCap(), bs.getLineJoin(), 10.0f, ArrayUtils.toPrimitive(dashPattern), 0);
        }
        else
            throw new UnsupportedSVGProperty("Unsupported stroke type: " + stroke.getClass().getCanonicalName());
    }
    
    
    /**
     * Converts a RasterImageNode to a BufferedImage
     * @param node
     * @return 
     */
    static BufferedImage imageFromNode(RasterImageNode node) {
        RenderedImage renderedImage = node.getImage().createDefaultRendering();
       
        BufferedImage img = new BufferedImage(renderedImage.getWidth(), renderedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        renderedImage.copyData(img.getRaster());
        return img;
    }
    
    
    /**
     * Converts a batik.awt Paint object to a jZELD PaintAttribute object
     * @param paint the batik paint object
     * @param shapeTransform how the shape that the Paint object is applied to is transformed
     * @param bounds the shape bounds that the Paint object is applied to
     * @return a PaintAttributes object, or null if the batik Paint object is a simple instance of Color
     * 
     * @throws SVGImportUtils.UnsupportedSVGProperty on an unhandled paint type
     */
    static PaintAttributes createPaintAttributes(Paint paint, AffineTransform shapeTransform, Rectangle2D bounds) throws UnsupportedSVGProperty {

        float[] fractions = null;
        AffineTransform gradientTransform = shapeTransform;
        
        if (paint instanceof org.apache.batik.ext.awt.MultipleGradientPaint) {

             //Fixup distribution fractions - handle duplicates
            fractions = ((org.apache.batik.ext.awt.MultipleGradientPaint)paint).getFractions();

            for (int i=1; i<fractions.length; i++)  //increase any duplicate slightly more
                if (fractions[i] <= fractions[i-1])
                    fractions[i] = fractions[i-1] + Float.MIN_VALUE; 

            while (fractions.length > 0 && fractions[fractions.length-1] > 1.0f)
               fractions = Arrays.copyOf(fractions, fractions.length-1);  //remove any values over 1.0f
            //Unlike awt GradientPaints, the ones here are not relative to "user space" (bounds of the shape). They are absolute
            //locations which also may have a transform. So we take the currentTransform and concatenate the gradient transform.
            //Later, gradientPointToRelative() will scale to units and subtract the origin of the shape, converting to a awt GradientPoint.
            //Then this is scaled by the width/height for use as a jZeld paint Attribute
            AffineTransform transform = ((org.apache.batik.ext.awt.MultipleGradientPaint)paint).getTransform();
            gradientTransform.concatenate(transform);
            transform.getScaleX();
            transform.getScaleY();
            transform.getShearX();
            transform.getShearY();

        }


        if (paint instanceof Color)
            return null;
        
        else if (paint instanceof org.apache.batik.ext.awt.LinearGradientPaint) {

            org.apache.batik.ext.awt.LinearGradientPaint lgp = (org.apache.batik.ext.awt.LinearGradientPaint)paint;               

            Point2D start = lgp.getStartPoint();
            Point2D end = lgp.getEndPoint();
            if (gradientTransform != null) {
                start = gradientTransform.transform(start, null);
                end = gradientTransform.transform(end, null);
            }

            return PaintAttributes.createLinearPaintAttribute(SVGImportUtils.gradientPointToRelative(start, bounds, 72.0), 
                                                            SVGImportUtils.gradientPointToRelative(end, bounds, 72.0),
                                                            fractions, lgp.getColors(), SVGImportUtils.convertCycleMethod(lgp.getCycleMethod()));

        }
        
        else if (paint instanceof org.apache.batik.ext.awt.RadialGradientPaint) {

            org.apache.batik.ext.awt.RadialGradientPaint rgp = (org.apache.batik.ext.awt.RadialGradientPaint)paint; 
            Point2D center = rgp.getCenterPoint();
            Point2D focus = rgp.getFocusPoint();
            Point2D radiusPt = new Point2D.Double(rgp.getCenterPoint().getX() + rgp.getRadius(), rgp.getCenterPoint().getY());  //radius is along the x axis, a distance from center
            double radius = rgp.getRadius();
            
            if (gradientTransform != null) {
                center = gradientTransform.transform(center, null);
                focus = gradientTransform.transform(focus, null);
                radiusPt = gradientTransform.transform(radiusPt, null);
                radius = Math.abs(center.distance(radiusPt));
            }

            double longestEdge = bounds.getWidth() > bounds.getHeight() ? bounds.getWidth() : bounds.getHeight();

            return PaintAttributes.createRadialPaintAttribute(SVGImportUtils.gradientPointToRelative(center, bounds, 72.0), 
                                                            SVGImportUtils.gradientPointToRelative(focus, bounds, 72.0),
                                                            (float)((radius/72.0)/longestEdge), PaintAttributes.RadiusRelative.LONGEST, fractions, rgp.getColors(), SVGImportUtils.convertCycleMethod(rgp.getCycleMethod()));     
        }
        
        else if (paint instanceof org.apache.batik.gvt.PatternPaint) {
            org.apache.batik.gvt.PatternPaint ptp = (org.apache.batik.gvt.PatternPaint)paint;
            GraphicsNode gn = ptp.getGraphicsNode();
            if (gn instanceof SVGPatternElementBridge.PatternGraphicsNode) {

                SVGPatternElementBridge.PatternGraphicsNode pgn = (SVGPatternElementBridge.PatternGraphicsNode)gn;
                RenderedImage renderedImage = pgn.getEnableBackgroundGraphicsNodeRable(true).createDefaultRendering();
                if (renderedImage != null) {
                    BufferedImage img = new BufferedImage(renderedImage.getWidth(), renderedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    renderedImage.copyData(img.getRaster());
                    Rectangle2D patternRect = ptp.getPatternRect();
                    return PaintAttributes.createTexturePaintAttribute(img, (float)patternRect.getWidth()/72.0f, (float)patternRect.getHeight()/72.0f);
                }
                else
                    throw new UnsupportedSVGProperty("PatternPaint rendered image is null");
                
            }
            else
               throw new UnsupportedSVGProperty("Unsupported PatternPaint graphics node: " + gn.getClass().getCanonicalName());    

        }
        else
            throw new UnsupportedSVGProperty("Unsupported fill paint type: " + paint.getClass().getCanonicalName());    
    
    }
    
    /**
     * Batik places the GraphicsNodes for each of the markers in a private, proxy node held within the MarkerShapePainter class.
     * The painter uses these to paint, but in order to get to them we need to recreate what is happening internally to the class
     * and then access the private field.
     * @param painter the painter to access
     * @return a group of GraphicsNodes representing the beginning, middle, and end marker GraphicsNodes
     */
    static CompositeGraphicsNode getMarkerNodes(MarkerShapePainter painter) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        
        //First, call the builder method to create the proxy nodes
        Method buildMethod = MarkerShapePainter.class.getDeclaredMethod("buildMarkerGroup", (Class<?>[]) null);
        buildMethod.setAccessible(true);  //normally protected method
        buildMethod.invoke(painter, (Object[]) null);

        Field f = MarkerShapePainter.class.getDeclaredField("markerGroup");
        f.setAccessible(true);  //normally private
        return (CompositeGraphicsNode)f.get(painter);     
    }
    
    
}
