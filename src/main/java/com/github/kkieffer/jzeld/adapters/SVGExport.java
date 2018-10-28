
package com.github.kkieffer.jzeld.adapters;

import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Methods to export the canvas to SVG format
 * @author kkieffer
 */
public class SVGExport {
    
    /**
     * Export the canvas to SVG format, sending the output to the specified Writer
     * @param c the canvas to convert
     * @param w the writer
     * @throws UnsupportedEncodingException
     * @throws SVGGraphics2DIOException 
     */
    public static void toSVG(ZCanvas c, Writer w) throws UnsupportedEncodingException, SVGGraphics2DIOException {
        
        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
        ctx.setEmbeddedFontsOn(true);
        ctx.setExtensionHandler(new SVGGradientExtensionHandler());

        // Create an instance of the SVG Generator.
        SVGGraphics2D g2d = new SVGGraphics2D(ctx, true);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Ask the canvas to render into the SVG Graphics2D implementation.
        c.paintToGraphicsContext(g2d);
        

        boolean useCSS = true; // we want to use CSS style attributes
        g2d.stream(w, useCSS);
    
    }
    
     /**
     * Export the canvas to SVG format, sending the output to the specified File
     * @param c the canvas to convert
     * @param f the file to write
     * @throws UnsupportedEncodingException
     * @throws java.io.FileNotFoundException
     * @throws SVGGraphics2DIOException 
     */
    public static void toSVG(ZCanvas c, File f) throws UnsupportedEncodingException, FileNotFoundException, SVGGraphics2DIOException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        toSVG(c, writer);
        writer.close();
    }
    
    
}
