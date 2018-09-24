/*
 * www.javagl.de - SvgGraphics - Utilities for saving Graphics output as SVG
 *
 * Copyright (c) 2017-2017 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.kkieffer.jzeld.adapters;

import static org.apache.batik.util.SVGConstants.SVG_COLOR_INTERPOLATION_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_GRADIENT_UNITS_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_ID_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_LINEAR_GRADIENT_TAG;
import static org.apache.batik.util.SVGConstants.SVG_LINEAR_RGB_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;
import static org.apache.batik.util.SVGConstants.SVG_OFFSET_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_OPAQUE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_PAD_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_RADIAL_GRADIENT_TAG;
import static org.apache.batik.util.SVGConstants.SVG_REFLECT_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_REPEAT_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_SPREAD_METHOD_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_SRGB_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_STOP_COLOR_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STOP_TAG;
import static org.apache.batik.util.SVGConstants.SVG_USER_SPACE_ON_USE_VALUE;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGColor;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.apache.batik.svggen.SVGTexturePaint;
import org.w3c.dom.Element;
/**
 * Taken (with permission) from https://gist.github.com/msteiger/4509119, 
 * including the fixes that are discussed in the comments <br>
 * <br>
 * Extension of Batik's {@link DefaultExtensionHandler} which handles different
 * kinds of Paint objects
 * 
 * I wonder why this is not part of the svggen library.
 * 
 * @author Martin Steiger
 */
@SuppressWarnings("javadoc")
class SVGGradientExtensionHandler extends DefaultExtensionHandler
{
    @Override
    public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext genCtx) {
        
        // Handle LinearGradientPaint
        if (paint instanceof LinearGradientPaint)
            return getLgpDescriptor((LinearGradientPaint) paint, genCtx);

        // Handle RadialGradientPaint
        if (paint instanceof RadialGradientPaint)
            return getRgpDescriptor((RadialGradientPaint) paint, genCtx);
        
        // Handle TexturePaint
        if (paint instanceof TexturePaint)
            return getTextureDescriptor((TexturePaint)paint, genCtx);

        return super.handlePaint(paint, genCtx);
    }

    
    private SVGPaintDescriptor getRgpDescriptor(RadialGradientPaint gradient,SVGGeneratorContext genCtx) {
        
        Element gradElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_RADIAL_GRADIENT_TAG);

        // Create and set unique XML id
        String id = genCtx.getIDGenerator().generateID("gradient");
        gradElem.setAttribute(SVG_ID_ATTRIBUTE, id);

        // Set x,y pairs
        Point2D centerPt = gradient.getCenterPoint();
        gradElem.setAttribute("cx", String.valueOf(centerPt.getX()));
        gradElem.setAttribute("cy", String.valueOf(centerPt.getY()));

        Point2D focusPt = gradient.getFocusPoint();
        gradElem.setAttribute("fx", String.valueOf(focusPt.getX()));
        gradElem.setAttribute("fy", String.valueOf(focusPt.getY()));

        gradElem.setAttribute("r", String.valueOf(gradient.getRadius()));

        addMgpAttributes(gradElem, genCtx, gradient);

        return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, gradElem);
    }

    private SVGPaintDescriptor getLgpDescriptor(LinearGradientPaint gradient, SVGGeneratorContext genCtx) {
        
        Element gradElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_LINEAR_GRADIENT_TAG);

        // Create and set unique XML id
        String id = genCtx.getIDGenerator().generateID("gradient");
        gradElem.setAttribute(SVG_ID_ATTRIBUTE, id);

        // Set x,y pairs
        Point2D startPt = gradient.getStartPoint();
        gradElem.setAttribute("x1", String.valueOf(startPt.getX()));
        gradElem.setAttribute("y1", String.valueOf(startPt.getY()));

        Point2D endPt = gradient.getEndPoint();
        gradElem.setAttribute("x2", String.valueOf(endPt.getX()));
        gradElem.setAttribute("y2", String.valueOf(endPt.getY()));

        addMgpAttributes(gradElem, genCtx, gradient);

        return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, gradElem);
    }

    private void addMgpAttributes(Element gradElem, SVGGeneratorContext genCtx,MultipleGradientPaint gradient) {
        
        gradElem.setAttribute(SVG_GRADIENT_UNITS_ATTRIBUTE, SVG_USER_SPACE_ON_USE_VALUE);

        // Set cycle method
        switch (gradient.getCycleMethod())
        {
            case REFLECT:
                gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REFLECT_VALUE);
                break;
            case REPEAT:
                gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REPEAT_VALUE);
                break;
            case NO_CYCLE:
            default:
                gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_PAD_VALUE); // this is the default
                break;
        }

        // Set color space
        switch (gradient.getColorSpace())
        {
            case LINEAR_RGB:
                gradElem.setAttribute(SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_LINEAR_RGB_VALUE);
                break;

            case SRGB:
            default:
                gradElem.setAttribute(SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_SRGB_VALUE);
                break;
        }

        // Set transform matrix if not identity
        AffineTransform tf = gradient.getTransform();
        if (!tf.isIdentity())
        {
            String matrix = "matrix(" + tf.getScaleX() + " " + tf.getShearY()
                + " " + tf.getShearX() + " " + tf.getScaleY() + " "
                + tf.getTranslateX() + " " + tf.getTranslateY() + ")";
            gradElem.setAttribute(SVG_GRADIENT_TRANSFORM_ATTRIBUTE, matrix);
        }

        // Convert gradient stops
        Color[] colors = gradient.getColors();
        float[] fracs = gradient.getFractions();

        for (int i = 0; i < colors.length; i++)
        {
            Element stop = genCtx.getDOMFactory()
                .createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
            SVGPaintDescriptor pd = SVGColor.toSVG(colors[i], genCtx);

            stop.setAttribute(SVG_OFFSET_ATTRIBUTE,
                (int) (fracs[i] * 100.0f) + "%");
            stop.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, pd.getPaintValue());

            if (colors[i].getAlpha() != 255)
            {
                stop.setAttribute(SVG_STOP_OPACITY_ATTRIBUTE,
                    pd.getOpacityValue());
            }

            gradElem.appendChild(stop);
        }
    }
    
    
    public SVGPaintDescriptor getTextureDescriptor(TexturePaint texture, SVGGeneratorContext genCtx) {
        
        BufferedImage textureImage = texture.getImage();
        Rectangle2D anchorRect = texture.getAnchorRect();
        
        // Rescale only if necessary
        if(textureImage.getWidth() != anchorRect.getWidth() || textureImage.getHeight() != anchorRect.getHeight()){

            // Rescale only if anchor area is not a point or a line
            if(anchorRect.getWidth() > 0 && anchorRect.getHeight() > 0){
                
                double scaleX = anchorRect.getWidth()/textureImage.getWidth();
                double scaleY = anchorRect.getHeight()/textureImage.getHeight();
                
           
                //Use better rendering quality for resize!
                textureImage = SerializableImage.resizeImage(textureImage, (int)(scaleX*textureImage.getWidth()), (int)(scaleY* textureImage.getHeight()), BufferedImage.TYPE_INT_ARGB);
            }
        }
        
        TexturePaint tP = new TexturePaint(textureImage, anchorRect);
     
        SVGTexturePaint svgTexturePaint = new SVGTexturePaint(genCtx);
        return svgTexturePaint.toSVG(tP);
        
    }
  
}