
package com.github.kkieffer.jzeld.adapters;

import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.attributes.PaintAttributes;
import com.github.kkieffer.jzeld.element.ZElement;
import com.github.kkieffer.jzeld.element.ZElement.StrokeStyle;
import com.github.kkieffer.jzeld.element.ZGroupedElement;
import com.github.kkieffer.jzeld.element.ZImage;
import com.github.kkieffer.jzeld.element.ZShape;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.CompositeShapePainter;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.apache.batik.gvt.StrokeShapePainter;
import org.apache.batik.bridge.TextNode;
import static org.apache.batik.bridge.TextNode.PAINT_INFO;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.MarkerShapePainter;
import org.apache.batik.gvt.ProxyGraphicsNode;
import org.apache.batik.gvt.RasterImageNode;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Methods to import SVG elements into a ZCanvas
 * 
 * @author kkieffer
 */
public class SVGImport {

    //Prints the error right where it occurs
    private static final class ParseErrorList extends ArrayList<String> {
        @Override
        public boolean add(String s) {
            boolean rc = super.add(s);
            System.err.println("PARSE ERRROR: " + s);  
            return rc;
        }
    };
    
    private final URL url;  
    private AlphaComposite currentAlphaComposite;
    private AffineTransform currentTransform;
    private final ZCanvas canvas;
    private final ParseErrorList parseErrors = new ParseErrorList();
    private BridgeContext bridge;
    private int svgElements = 0;
    
    /**
     * Import elements from an SVG file, adding them to the specified canvas
     * @param canvas the canvas to place the elements
     * @param f the .svg file
     * @return a list of errors that occurred
     * @throws MalformedURLException 
     */
    public static String[] fromFile(ZCanvas canvas, File f, boolean renderAll) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        SVGImport svgImport = new SVGImport(canvas, f.toURI().toURL() );
        svgImport.parse(renderAll);
        return svgImport.getErrors();
    }
    
    /**
     * Import elements from an SVG URL, adding them to the specified canvas
     * @param canvas the canvas to place the elements
     * @param url SVG URL
     * @return a list of errors that occurred
     * @throws MalformedURLException 
     */
    public static String[] fromURL(ZCanvas canvas, URL url, boolean renderAll) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        SVGImport svgImport = new SVGImport(canvas, url);
        svgImport.parse(renderAll);
        return svgImport.getErrors();
    }
 
    private String[] getErrors() {
        String[] errList = new String[parseErrors.size()];
        parseErrors.toArray(errList);
        return errList;
    }
    
    //Private constructor - use factory methods above
    private SVGImport(ZCanvas canvas, URL url) {
        this.url = url;
        this.canvas = canvas;
    }

 
    
    private InputStream getInputStream() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String pid, String sid) throws SAXException {
                return new InputSource(new StringReader(""));
            }
        });
        
        InputSource input;
        if (url.toString().endsWith(".svgz")) 
            input = new InputSource(new GZIPInputStream(url.openStream()));
        else 
            input = new InputSource(url.openStream());
        
        SAXSource source = new SAXSource(reader, input);
        
        Result result = new StreamResult(buffer);
        
        StreamSource stylesheet = new StreamSource(getClass().getResourceAsStream("/svg-cleanup.xsl"));
        
        Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesheet);
        transformer.transform(source, result);
        
        stylesheet.getInputStream().close();
        
        return new ByteArrayInputStream(buffer.toByteArray());
    }


    private void parse(boolean renderAll) throws IOException, ParserConfigurationException, SAXException, TransformerException {

        UserAgentAdapter ua = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(ua);
        bridge = new BridgeContext(ua, loader);
        bridge.setDynamicState(BridgeContext.DYNAMIC);
        ua.setBridgeContext(bridge);
        
        Document svgDoc = loader.loadDocument(url.toString(), getInputStream());
        new GVTBuilder().build(bridge, svgDoc);

        parseNodes(renderAll);
            
        
    }
    
    private String getIDString(GraphicsNode n) {
        Element domElement = bridge.getElement(n);
        if (domElement == null)
            return " ";
        String id = domElement.getAttribute("id");
        if (id == null || id.isEmpty())
            return " ";
        else
            return " #" + id + " ";
    }

 
    private void parseNodes(boolean renderAll) throws IOException {
        
        
        GraphicsNode root = bridge.getGraphicsNode(bridge.getDocument());
        
        ArrayList<ZElement> elements = new ArrayList<>();
        ZElement topElement;
        
        if (renderAll) {  //render everything into a single image
           topElement = renderRootNode(root);
        }
        else { //parse nodes, creating Z objects for each
            
            parseGraphicsNode(root, elements);  //parse the root node

            if (elements.isEmpty())
                throw new IOException("Failed to parse any elements");  
        
            topElement = elements.get(0);     
            topElement.setName(("SVGRoot (" + svgElements + " elements)"));
        }
        
        
        topElement.reposition(0, 0, 1.0, 1.0);  //move to origin

        Rectangle2D groupBounds = topElement.getBounds2D();
        Rectangle2D canvasBounds = canvas.getCanvasBounds();
        
        //Scale to maximally fit canvas
        double scale = Math.min(canvasBounds.getWidth()/groupBounds.getWidth(), canvasBounds.getHeight()/groupBounds.getHeight());
        
        topElement.scaleSize(scale, scale);
        
        canvas.addElement(topElement);
        
    }
    
    
    private ZImage renderRootNode(GraphicsNode node) throws IOException {

        currentAlphaComposite = SVGImportUtils.getGlobalAlphaComposite(node);        
        currentTransform = node.getGlobalTransform();
        
   
        ZImage img = renderNodeToImage(node);
        if (img != null) {
            img.setName("SVGRenderedImage");
            return img;
        }
        else
            throw new IOException("Failed to render SVG");
    }

    
    /**
     * Recursive function called to parse the graphics node tree
     * @param node the current node, starting from the head of the tree
     * @param parentElements the parent node's list of elements
     */
    private void parseGraphicsNode(GraphicsNode node, ArrayList<ZElement> parentElements) {
        
   
        //Get the current alpha and transform for this node
        currentAlphaComposite = SVGImportUtils.getGlobalAlphaComposite(node);        
        currentTransform = node.getGlobalTransform();
            
        //If the node has a filter or mask, it can't be converted to a awt object, so create a rendered image here
        if (node.getFilter() != null || node.getMask() != null) {
            ZImage img = renderNodeToImage(node);
            if (img != null) {
                parentElements.add(img); //add the created element to the parent's group
                img.setName("SVGFilteredImage" + getIDString(node));
                svgElements++;
                return;
            }
        }
        
        if (node instanceof CompositeGraphicsNode) {  
            
            Shape clipShape = null;
            if (!(node instanceof CanvasGraphicsNode)) {  //don't use clipping from Canvas - this can clip everything out
                
               clipShape = node.getClip() == null ? null : node.getClip().getClipPath();  //clipping path for the composite
                if (clipShape != null) 
                    clipShape = SVGImportUtils.transformClip(clipShape, currentTransform);  //transform to a clip shape for the canvas (now in canvas units, and transformed)
            }
            
            ArrayList<ZElement> svgConvertedElements = new ArrayList<>();  //new list of elements for this composite group
            List children = ((CompositeGraphicsNode)node).getChildren();
            for (int i = 0; i < children.size(); i++) {
                parseGraphicsNode((GraphicsNode) children.get(i), svgConvertedElements); //recursively call this function again with each child node
            }
            
            if (svgConvertedElements.isEmpty())
                return;
                
            ZGroupedElement groupedSVG = ZGroupedElement.createGroup(svgConvertedElements, clipShape);  //create a new group with the elements
            
            String name = node.getClass().getSimpleName().replace("Node", "").replace("Graphics", "");
            
            groupedSVG.setName("SVG" + name + getIDString(node) + "(" + svgConvertedElements.size() + " elements)");
            
            if (clipShape != null)
                System.out.println("Clip: " + clipShape.getBounds2D());
            
            parentElements.add(groupedSVG);
            return;
            
        } 
                
        
        //Process this Node Below -------------

        ArrayList<ZElement> zElements = new ArrayList<>();
        
        if (node instanceof ShapeNode) {
            zElements.addAll(parseShapeNode((ShapeNode)node)); //first element is shape, any others are markers 
        } 
        else if (node instanceof TextNode) {
            ZElement e = parseTextNode((TextNode)node);
            if (e != null) 
                zElements.add(e);
            
        } 
        else if (node instanceof RasterImageNode) {
            ZElement e = parseRasterImageNode((RasterImageNode)node);
            if (e != null) 
                zElements.add(e);            
            
        }
        else {
            parseErrors.add(getIDString(node) + ": Unsupported GraphicsNode: " + node.getClass().getCanonicalName());
        }
        
        for (ZElement e : zElements) {
            parentElements.add(e); //add the created element to the parent's group
            svgElements++;
        }
    }

    //Recursive function to get all the shape painters
    private void getShapePainters(ShapePainter p, ArrayList<ShapePainter> painters) {
        
        if (p instanceof CompositeShapePainter) {
            CompositeShapePainter cp = (CompositeShapePainter)p;
            for (int i=0; i<cp.getShapePainterCount(); i++)
                getShapePainters(cp.getShapePainter(i), painters);
        }
        else
            painters.add(p);
        
    }
    
    
    /**
     * Parse a ShapePainter graphics node, creating a ZShape from the node
     * @param painter 
     * @return array of created shapes (can contain marker shapes too)
     */
    private ArrayList<ZElement> parseShapeNode(ShapeNode shapeNode) {

        Paint fillPaint = null;
        Stroke stroke = null;
        Paint strokePaint = null;
        CompositeGraphicsNode markerNode = null;
        ArrayList<ZElement> elements = new ArrayList<>();  //create an array to hold the shape and markers
        
        Shape svgShape = shapeNode.getShape();
        Shape clipShape = shapeNode.getClip() == null ? null : shapeNode.getClip().getClipPath();
        
        ArrayList<ShapePainter> painters = new ArrayList<>();
        
        getShapePainters(shapeNode.getShapePainter(), painters);  //get all shape painters
            
        StringBuilder b = new StringBuilder("Parsing Shape Node: " + getIDString(shapeNode));
        
        for (ShapePainter p : painters)  {
            b.append(SVGImportUtils.getPainterInfo(p));
            
            if (p instanceof FillShapePainter) {
                fillPaint = ((FillShapePainter)p).getPaint();
            } 
            else if (p instanceof StrokeShapePainter) {
                stroke = ((StrokeShapePainter)p).getStroke();
                strokePaint = ((StrokeShapePainter)p).getPaint();                    
            } 
            else if (p instanceof MarkerShapePainter) {  
                        
                try {
                    markerNode = SVGImportUtils.getMarkerNodes(((MarkerShapePainter)p));                                          
                } catch (Exception ex) {
                    parseErrors.add(getIDString(shapeNode) + ": Failed to access Marker GraphicsNodes");
                } 
                
            }
            else {
                parseErrors.add(getIDString(shapeNode) + ": Unsupported painter: " + p.getClass().getCanonicalName());
            }       
        } 
            
        System.out.println(b.toString());
 
        if (fillPaint == null && strokePaint == null && markerNode == null)  //nothing to paint
            return elements;

        Pair<Shape, Rectangle2D> shapePair = SVGImportUtils.transformShape(svgShape, currentTransform);  //transform to a shape for the canvas
        clipShape = SVGImportUtils.transformClip(clipShape, currentTransform);  //transform to a clip shape for the canvas
    
        try {
            if (stroke != null)
                stroke = SVGImportUtils.transformStroke(stroke, currentTransform);
        } catch (SVGImportUtils.UnsupportedSVGProperty ex) {
            parseErrors.add(getIDString(shapeNode) + ": Unknown stroke type");
            stroke = null;
        }
        
        Shape baseShape = shapePair.getLeft();
        Rectangle2D bounds = shapePair.getRight();

        
        ZShape zshape = createZShape(shapeNode, baseShape, clipShape, bounds, fillPaint, stroke, strokePaint);
        if (zshape == null)
            return elements;  //return empty, no shape
        
        zshape.setName("SVGShape" + getIDString(shapeNode));

        elements.add(zshape);
       
        /**
         * Batik uses proxy nodes for the markers. However, the global transform including this shape cannot be retrieved
         * from the proxy's source.  So we create the zshapes for each of the markers, and then apply the current transform
         * concatenated with the proxy's transform
         */
        if (markerNode != null) {
            AffineTransform savedTransform = currentTransform; //save our current for restore
            

            for (Object c : markerNode.getChildren()) {  //this is an array of ProxyGraphicsNodes
                
                ProxyGraphicsNode n = (ProxyGraphicsNode)c;
                
                //Call our parse method - this creates markerElements for each marker
                GraphicsNode gn = null;
                if (n.getSource() instanceof CompositeGraphicsNode)
                    gn = (GraphicsNode)(((CompositeGraphicsNode)n.getSource()).getChildren().get(0));
                else
                    gn = n.getSource();
                
                if (!(gn instanceof ShapeNode))
                    continue; //shouldn't happen - markers should always be ShapeNodes
                
                    
                //Take our saved transform, concat with the proxy's transform
                currentTransform = new AffineTransform(savedTransform);
                currentTransform.concatenate(n.getTransform());
                //Parse, using the new currentTransform 
                ZElement zMarkerShape = parseShapeNode((ShapeNode)gn).get(0); 
                if (zMarkerShape != null) {
                    zMarkerShape.setName("SVGMarker " + getIDString(shapeNode));
                    elements.add(zMarkerShape);
                }

                
            }  
            
            
            currentTransform = savedTransform;  //restore
        }

        return elements;
    }


    /**
     * Parse a TextNode graphics node, creating a ZShape from the node
     * @param painter 
     * @return the created ZShape
     */
    private ZShape parseTextNode(TextNode textNode) {
         
        Paint fillPaint = null;
        Stroke stroke = null;
        Paint strokePaint = null;
        Shape clipShape = textNode.getClip() == null ? null : textNode.getClip().getClipPath();
     
        System.out.println("Parsing Text Node: " + textNode.getText());
        
        AttributedCharacterIterator runaci = textNode.getAttributedCharacterIterator();
        if (runaci != null) {
            runaci.first();
            TextPaintInfo tpi = (TextPaintInfo) runaci.getAttribute(PAINT_INFO);
            if (tpi != null && tpi.visible) {
                fillPaint = tpi.fillPaint;
                stroke = tpi.strokeStroke;
                strokePaint = tpi.strokePaint;
            } 
            else
                parseErrors.add(getIDString(textNode) + ": Unknown text paint and stroke attributes");
        } else
            return null;

        Pair<Shape, Rectangle2D> shapePair = SVGImportUtils.transformShape(textNode.getOutline(), currentTransform);  //transform to a shape for the canvas
        clipShape = SVGImportUtils.transformClip(clipShape, currentTransform);  //transform to a clip shape for the canvas
        
        try {
            if (stroke != null)
                stroke = SVGImportUtils.transformStroke(stroke, currentTransform);
        } catch (SVGImportUtils.UnsupportedSVGProperty ex) {
            parseErrors.add(getIDString(textNode) + ": Unknown stroke type");
            stroke = null;
        }

        
        Shape baseShape = shapePair.getLeft();
        Rectangle2D bounds = shapePair.getRight();
        
        ZShape zShape = createZShape(textNode, baseShape, clipShape, bounds, fillPaint, stroke, strokePaint); 
        if (zShape != null) {
            String txt = textNode.getText();
            if (txt != null && txt.length() > 10)
                txt = txt.substring(0, 8) + "...";
            zShape.setName("SVGText" + getIDString(textNode) + txt);
        }
        return zShape;
        
    }
    
    /**
     * Parse a RasterImageNode graphics node, creating a ZImage from the node
     * @param node the RasterImageNode
     * @return the created ZImage
     */
    private ZImage parseRasterImageNode(RasterImageNode node) {
          
        BufferedImage img = SVGImportUtils.imageFromNode(node);
       
        System.out.println("Parsing Raster Image Node: " + getIDString(node) + " (" + img.getWidth() + "x" + img.getHeight() + ")");
   
        Pair<Shape, Rectangle2D> pair = SVGImportUtils.transformShape(node.getBounds(), currentTransform);  //transform to a shape for the canvas

        Rectangle2D bounds = pair.getRight();
              
        ZImage zimg = new ZImage(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0.0, true, true, true, 0.0f, Color.BLACK, null, null, null, img);
        
        if (currentAlphaComposite != null)
            zimg.setOpacity(currentAlphaComposite.getAlpha());
        
        zimg.setName("SVGRasterImage" + getIDString(node));
        
        return zimg;
    }
  
    
    /**
     * From the provided shape and fill/stroke characteristics, create a ZShape
     * @param baseShape the java.awt shape 
     * @param bounds location and bounds of the shape
     * @param fillPaint how the shape is filled
     * @param stroke how the shape is stroked
     * @param strokePaint how the stroke is painted
     * @return the created shape
     */
    private ZShape createZShape(GraphicsNode node, Shape baseShape, Shape clipShape, Rectangle2D bounds, Paint fillPaint, Stroke stroke, Paint strokePaint) {
        
        Color fillColor = null;        
        float borderWidth = 0.0f;
        Color borderColor = null;
        Float[] dashArray = null;
        StrokeStyle borderStyle = StrokeStyle.SQUARE;
        PaintAttributes fillPaintAttributes = null;
        PaintAttributes strokePaintAttributes = null;
                
        if (baseShape.getBounds().isEmpty())  //shape with no area
            return null;
        
        //Decode the stroke and stroke paint
        if (strokePaint != null) {
            
            BasicStroke bs = (BasicStroke)stroke;  //must be true, was checked in transforming stroke
            borderWidth = bs.getLineWidth();

            float[] da = bs.getDashArray();
            if (da != null)
                dashArray = ArrayUtils.toObject(da);
            
            if (bs.getEndCap() == BasicStroke.CAP_ROUND)
                borderStyle = StrokeStyle.ROUNDED;

            try {
                strokePaintAttributes = SVGImportUtils.createPaintAttributes(strokePaint, currentTransform, bounds);

                if (strokePaintAttributes == null)  //simple case of solid color, just assign it to the Color
                    borderColor = (Color)strokePaint;

            } catch (SVGImportUtils.UnsupportedSVGProperty ex) {
                parseErrors.add(getIDString(node) + ": " + ex.getMessage());
            }
       
        }

        //Decode the fill 
        if (fillPaint != null) {
            
            try {
                fillPaintAttributes = SVGImportUtils.createPaintAttributes(fillPaint, currentTransform, bounds);
                
                if (fillPaintAttributes == null)  //simple case of solid color, just assign it to the Color
                    fillColor = (Color)fillPaint;
                
                
            } catch (SVGImportUtils.UnsupportedSVGProperty ex) {
                parseErrors.add(getIDString(node) + ": " + ex.getMessage());
            }

            
        }
        

        ZShape zshape = new ZShape(bounds.getX(), bounds.getY(), baseShape, 0.0, true, true, true, borderWidth, borderColor, 
                                    dashArray, fillColor, fillPaintAttributes, strokePaintAttributes, null, borderStyle);
        
        if (currentAlphaComposite != null)
            zshape.setOpacity(currentAlphaComposite.getAlpha());
        
        zshape.setClippingShape(clipShape);
        zshape.scaleBorderWithShape(true);
        
        return zshape;
    }
    
    
    private ZImage renderNodeToImage(GraphicsNode node) {

        Rectangle2D bounds = node.getBounds();
        
              
        //Paint the node into the image
        BufferedImage renderedImage = new BufferedImage((int)bounds.getWidth(), (int)bounds.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = renderedImage.createGraphics();
        g.translate(-bounds.getX(), -bounds.getY());
        RenderingHints renderingHints = node.getRenderingHints();
        if (renderingHints != null)
            g.setRenderingHints(node.getRenderingHints());
        else {  //set some nice defaults
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
        node.paint(g);
        g.dispose();        
        
        bounds = SVGImportUtils.transformClip(bounds, currentTransform).getBounds2D();

        ZImage zimg = new ZImage(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0.0, true, true, true, 0.0f, Color.BLACK, null, null, null, renderedImage);
        
        if (currentAlphaComposite != null)
            zimg.setOpacity(currentAlphaComposite.getAlpha());
        
        return zimg;
         
    }
    
    
    
}
