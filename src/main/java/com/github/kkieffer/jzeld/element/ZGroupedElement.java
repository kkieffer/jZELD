
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.attributes.CustomStroke;
import com.github.kkieffer.jzeld.attributes.ShadowAttributes;
import com.github.kkieffer.jzeld.attributes.TextAttributes;
import com.github.kkieffer.jzeld.attributes.PaintAttributes;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A ZGroupedElement holds a collection of elements that are drawn together and treated as a single element. The elements in the group are arranged
 * such that the last one in the element list is the top z-plane (last drawn).
 * @author kkieffer
 */
@XmlRootElement(name = "ZGroupedElement")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ZGroupedElement extends ZElement implements TextAttributes.TextInterface, ShadowAttributes.ShadowInterface {

    @XmlElement(name="ZElement")        
    private ArrayList<ZElement> elements;
    
    private double groupedWidth;
    private double groupedHeight;
    
    private static ArrayList<ZElement> copyElements(ArrayList<ZElement> src) {
        ArrayList<ZElement> copy = new ArrayList<>(src.size());
        for (int i=0; i<src.size(); i++) {
            copy.add(src.get(i).copyOf(false));  //not for copy - for grouping
        }
        return copy;
    }
    
    private static Rectangle2D getElementBounds(ZElement e) {
        
        Point2D p = e.getPosition();
           
        Rectangle2D margin = e.getMarginBounds(1.0); 
        
        Rectangle2D.Double b = new Rectangle2D.Double(p.getX() + margin.getX(), p.getY() + margin.getY(), margin.getWidth(), margin.getHeight()); 

        AffineTransform t = e.getElementTransform(1.0, false);
        Shape s = t.createTransformedShape(b);

        return s.getBounds2D();  //make bounds something that can hold the transformed shape
              
    }
    
    protected static Rectangle2D getEnclosingBounds(ArrayList<ZElement> elements) {
        double left = Integer.MAX_VALUE;  //furthest left
        double top = Integer.MAX_VALUE;  //furthest top
        double right = 0;                 //furthest right
        double bottom = 0;                 //furthest bottom
        
        //Find furthest left and top
        for (ZElement e : elements) {
            
            Rectangle2D b = getElementBounds(e);
            
            if (b.getX() < left)
                left = b.getX();
            if (b.getY() < top)
                top = b.getY();
            
            if (b.getX() + b.getWidth() > right)
                right = b.getX() + b.getWidth();
            if (b.getY() + b.getHeight() > bottom)
                bottom = b.getY() + b.getHeight();
  
        }    
        
        return new Rectangle2D.Double(left, top, right-left, bottom-top);
        
    }
    
    /**
     * Groups the elements into a ZGroupedElement. Sub-elements are repositioned relative to the grouped element.  The grouped element
     * position and size is set to bound all the sub-elements.
     * @param elements
     * @return 
     */
    public static ZGroupedElement createGroup(ArrayList<ZElement> elements) {
        
        Rectangle2D b = getEnclosingBounds(elements);
        return new ZGroupedElement(b.getX(), b.getY(), b.getWidth(), b.getHeight(), elements);
    }

    
    private ZGroupedElement(double x, double y, double w, double h, ArrayList<ZElement> srcElements) {
        super(x, y, w, h, 0.0, true, true, true);
        groupedWidth = w;  //maintain the original grouped size in case of resize
        groupedHeight = h;
        
        elements = copyElements(srcElements);
        
        //Remove the x,y offset from each element's position
        for (ZElement e : this.elements) 
            e.move(-x, -y, Integer.MAX_VALUE, Integer.MAX_VALUE);
        
    }
    
    private ZGroupedElement() {}
    
    private ZGroupedElement(ZGroupedElement src, boolean forNew) {
        super(src, forNew);
        this.elements = copyElements(src.elements);
        this.groupedWidth = src.groupedWidth;
        this.groupedHeight = src.groupedHeight;
    }
    
    
    @Override
    public ZGroupedElement copyOf(boolean forNew) {
        return new ZGroupedElement(this, forNew);
    }
    
    protected void regroup() {
        Rectangle2D b = getEnclosingBounds(elements);
        for (ZElement e : elements)
            e.move(-b.getX(), -b.getY(), Double.MAX_VALUE, Double.MAX_VALUE);
        super.setSize(b.getWidth(), b.getHeight(), 0, 1.0);
    }
    
    
    
    @Override
    public String getHtmlHelp() {
        
        String group = "";
        for (ZElement e : elements)
            group += e.getClass().getSimpleName() + "<br>";
        
        return "<b>ZGroupedElement: A group of multiple elements that are moved and transformed together.</b><br><br>Transformations " +
                "are applied to the group as a whole, however, a ZGroupedElement has no intrinsic attributes such as fill color and line weight; applying those modifications change " +
                "the individual elements and are retained after ungrouping. Note: transformations persist when elements are ungrouped with exception of shear.<br><br>" +
                "This group contains the following elements: <br>" + group + "<br><br>" + super.getHtmlHelp();
   
    }
    
    /**
     * Removes all elements from this group and returns them.  The elements are repositioned to their position outside of the group.
     * This group will have zero elements in it and should be removed from any canvas it is on.
     * 
     * @return the list of ungrouped elements 
     */
    public ArrayList<ZElement> ungroup() {
        
        Rectangle2D bounds = this.getBounds2D();
        Point2D center = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
        
        for (ZElement e : this.elements) {

            e.move(bounds.getX(), bounds.getY(), Integer.MAX_VALUE, Integer.MAX_VALUE);  //Restore the x,y offset
            Rectangle2D eBounds = e.getBounds2D();

            Point2D centerE = new Point2D.Double(eBounds.getCenterX(), eBounds.getCenterY());

            Point2D ePos = e.getPosition();
            
            Point2D relativeE = new Point2D.Double(centerE.getX() - center.getX(), centerE.getY() - center.getY());
           
            AffineTransform rotateInstance = AffineTransform.getRotateInstance(Math.toRadians(getRotation()));
            Point2D rotated = rotateInstance.transform(relativeE, null);

            
            double xMove = rotated.getX() - relativeE.getX();
            double yMove = rotated.getY() - relativeE.getY();
            
            e.move(xMove, yMove,  Integer.MAX_VALUE, Integer.MAX_VALUE);
            e.rotate(getRotation());
            
        }
        
        ArrayList<ZElement> copy = copyElements(elements);
        this.elements.clear();  //invalidate 
        
        return copy;
    }
    
    @Override
    public boolean isMutable() {
        return true;
    }
    
    @Override
    public boolean supportsEdit() {
        return false;
    };

    @Override
    public void setAttributes(float outlineWidth, Color outlineColor, Float[] dashPattern, Color fillColor, StrokeStyle borderStyle) {
        setFillColor(fillColor);
        setOutlineWidth(outlineWidth);
        setDashPattern(dashPattern);
        setOutlineColor(outlineColor);
        setOutlineStyle(borderStyle);
    }

    @Override
    public void setOutlineWidth(float width) {
        for (ZElement e : elements) {
            if (e.hasOutline()) {
                e.setOutlineWidth(width);
            }
        }
        changed();
    }
    
    @Override
    public void setOutlineStyle(StrokeStyle borderStyle) {
         for (ZElement e : elements) {
            if (e.hasOutline()) {
                e.setOutlineStyle(borderStyle);
            }
        }
        changed();
    }
    
    @Override
    public float getOutlineWidth() {
        return 0;
    }
    
    @Override
    public StrokeStyle getOutlineStyle() {
        return null;
    }

    @Override
    public void setOutlineColor(Color outlineColor) {
        for (ZElement e : elements) {
            if (e.hasOutline()) {
                e.setOutlineColor(outlineColor);
            }
        }
        changed();
    }

    @Override
    public void setDashPattern(Float[] dashPattern) {
        for (ZElement e : elements) {
            if (e.hasDash()) {
                e.setDashPattern(dashPattern);
            }
        } 
        changed();
    }

    @Override
    public Float[] getDashPattern() {
        return null;
    }
    
    @Override
    public void setFillColor(Color fillColor) {
        for (ZElement e : elements) {
            if (e.hasFill()) {
                e.setFillColor(fillColor);
            }
        } 
        changed();
    }
    
    @Override
    public void removeFill() {
        for (ZElement e : elements) {
            if (e.hasFill()) {
                e.removeFill();
            }
        } 
        changed();
    }

    @Override
    public Color getFillColor() {
        return null;
    }

    @Override
    public Color getOutlineColor() {
        return null;
    }

    @Override
    public boolean hasOutline() {
        return true;
    }

    @Override
    public boolean hasDash() {
        return true;
    }

    @Override
    public boolean hasFill() {
        return true;
    }

    @Override
    public boolean supportsFlip() {
        return true;
    }
    
    
    public void setCustomStroke(CustomStroke s) {
        for (ZElement e : elements) {
            if (e instanceof ZAbstractShape) {
                ((ZAbstractShape)e).setCustomStroke(s);
            }
        } 
        changed();
    }
    
    public CustomStroke getCustomStroke() {
        return null;
    }
    
    /**
     * Set the paint attributes for the element.  The paint attributes (linear, radial, or texture) are applied over the shape's fill color.
     * @param p the paint attributes.  To remove, use null
     */
    public void setPaintAttributes(PaintAttributes p) {
        for (ZElement e : elements) {
            if (e instanceof ZAbstractShape) {
                ((ZAbstractShape)e).setPaintAttributes(p);
            }
        } 
        changed();
    }
    
    
    public PaintAttributes getPaintAttributes() {
        return null;
    }
    
    
    /**
     * When this group changes, send the change update to all the grouped elements
     */
    @Override
    public void changed() {
        for (ZElement e : elements) {
            e.changed();
        } 
        regroup();
        super.changed();
    }
    
    
    @Override
    public void flipHorizontal() {

        Rectangle2D bounds = this.getBounds2D();

        //Flip each element and reflect it across the group horizontal midpoint
        for (ZElement e : this.elements) { 
            
            Point2D position = e.getPosition();

            double relPositionX = position.getX() / bounds.getWidth();  //find relative position
            relPositionX = (1.0 - relPositionX)*bounds.getWidth() - e.getBounds2D().getWidth(); //reflect about midpoint
            e.reposition(relPositionX, position.getY(), Double.MAX_VALUE, Double.MAX_VALUE); //y is unchanged

            e.flipHorizontal();
           
        }
        
    }
    
    @Override
    public void flipVertical() {

        Rectangle2D bounds = this.getBounds2D();

        //Flip each element and reflect it across the group vertical midpoint
        for (ZElement e : this.elements) { 
            
            Point2D position = e.getPosition();

            double relPositionY = position.getY() / bounds.getHeight();  //find relative position
            relPositionY = (1.0 - relPositionY)*bounds.getHeight() - e.getBounds2D().getHeight(); //reflect about midpoint
            e.reposition(position.getX(), relPositionY, Double.MAX_VALUE, Double.MAX_VALUE); //x is unchanged

            e.flipVertical();
           
        }
        
    }
    
    
    //Override setSize for ZGroupedElement. In this case, find the radio of how much the group has increased,
    //and apply that ratio to the individual elements. Also determine for each element, the ratio of the x,y offset of the element 
    //from the group's position to the width and height of the group.  Apply that ratio to the new group size to find the new locatiin
    //of the element. 
    @Override
    protected void setSize(double w, double h, double minSize, double scale) {
        
        Rectangle2D bounds = this.getBounds2D(scale);
        double scaleX = w / bounds.getWidth(); 
        double scaleY = h / bounds.getHeight(); 
  
        
        //Restore the x,y offset to each element's position
        for (ZElement e : this.elements) { 
            e.scaleSize(scaleX, scaleY);
            Point2D position = e.getPosition(scale);
            double relPositionX = position.getX() / bounds.getWidth();
            double relPositionY = position.getY() / bounds.getHeight();
            double newX = relPositionX * w;
            double newY = relPositionY * h;
            e.reposition(newX/scale, newY/scale, Double.MAX_VALUE, Double.MAX_VALUE);
        }
        
        super.setSize(w, h, minSize, scale);
    }
    
   
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {
                       
        if (!isVisible())
            return;
        
         
        //Paint each element - each element has been "moved" to its offset within the group already
        for (ZElement e : elements) {    
            AffineTransform orig = g.getTransform();
            Rectangle2D bounds = e.getBounds2D(unitSize);

            AffineTransform elementTransform = e.getElementTransform(unitSize, false);
            g.transform(elementTransform);
            g.translate(bounds.getX(), bounds.getY());
            e.paint(g, unitSize, bounds.getWidth(), bounds.getHeight());
            
            g.setTransform(orig);
                    
        }



    }


    /**
     * Add all grouped element classes to the provided list if they aren't already in the list. If the group contains another ZGroupedElement,
     * its elements are added by calling this method again recursively.
     * @param elementTypes the array to add element classes ot
     */
    public void addGroupedClasses(final ArrayList<Class<? extends ZElement>> elementTypes) {

        for (ZElement e : elements) {
            
            if (e instanceof ZGroupedElement)
                ((ZGroupedElement)e).addGroupedClasses(elementTypes);  //recursively add its element's classes
            else {
                Class<? extends ZElement> theClass = e.getClass();
                if (!elementTypes.contains(theClass))  //Add this class type to our list of types, if it doesn't already exist there
                    elementTypes.add(theClass);
            }
            
        }

    }

    @Override
    public Rectangle2D getMarginBounds(double scale) {
        Rectangle2D b = getBounds2D(scale); 
        return new Rectangle2D.Double(0, 0, b.getWidth(), b.getHeight()); //no additional margins
    }

    
    /* ------------- TEXT INTERFACE METHODS ---------------------- */
    @Override
    public void setFontSize(int size) {
        for (ZElement e : elements) {
            if (e instanceof TextAttributes.TextInterface) {
                ((TextAttributes.TextInterface)e).setFontSize(size);
            }
        } 
        changed();
    }
        
        
    @Override
    public void setFont(Font f) {
        for (ZElement e : elements) {
            if (e instanceof TextAttributes.TextInterface) {
                ((TextAttributes.TextInterface)e).setFont(f);
            }
        } 
        changed();
    }

    @Override
    public void setFontStyle(int style) {
        for (ZElement e : elements) {
            if (e instanceof TextAttributes.TextInterface) {
                ((TextAttributes.TextInterface)e).setFontStyle(style);
            }
        }
        changed();
    }
    
   
    @Override
     public void setFontName(String name) {
       for (ZElement e : elements) {
            if (e instanceof TextAttributes.TextInterface) {
                ((TextAttributes.TextInterface)e).setFontName(name);
            }
        }
        changed();
    }
    
    
    @Override
     public void setFontColor(Color c) {
        for (ZElement e : elements) {
            if (e instanceof TextAttributes.TextInterface) {
                ((TextAttributes.TextInterface)e).setFontColor(c);
            }
        }
        changed();
    }
    
    @Override
    public void setTextJustify(TextAttributes.HorizontalJustify j) {
        for (ZElement e : elements) {
            if (e instanceof TextAttributes.TextInterface) {
                ((TextAttributes.TextInterface)e).setTextJustify(j);
            }
        }
        changed();
    }

    @Override
    public TextAttributes getTextAttributes() {
        for (ZElement e : elements) {
            if (e instanceof TextAttributes.TextInterface) {
                return ((TextAttributes.TextInterface)e).getTextAttributes();
            }
        }
        return null;  //none with text attributes
    }
    
    /* ------------- SHADOW INTERFACE METHODS ---------------------- */

    
    @Override
    public Image getShadowImage() {
        return null;
    }
    
    @Override
    public ShadowAttributes getShadowAttributes() {  //get the attributes of the first one, if any
        for (ZElement e : elements) {
            if (e instanceof ShadowAttributes.ShadowInterface) {
                return ((ShadowAttributes.ShadowInterface)e).getShadowAttributes();
            }
        } 
        return null;  //none with shadow attributes
    }

    @Override
    public void setShadowAttributes(ShadowAttributes s) {
        for (ZElement e : elements) {
            if (e instanceof ShadowAttributes.ShadowInterface) {
                ((ShadowAttributes.ShadowInterface)e).setShadowAttributes(s);
            }
        } 
        changed();
    }
    
    
    
    @Override
    public void setEnabled(boolean en) {
        for (ZElement e : elements) {
            if (e instanceof ShadowAttributes.ShadowInterface) {
                ((ShadowAttributes.ShadowInterface)e).setEnabled(en);
            }
        }
        changed();
    }
    
    @Override
    public void setColor(Color c) {
        for (ZElement e : elements) {
            if (e instanceof ShadowAttributes.ShadowInterface) {
                ((ShadowAttributes.ShadowInterface)e).setColor(c);
            }
        }
        changed();
    }

    @Override
    public void setRadius(int r) {
        for (ZElement e : elements) {
            if (e instanceof ShadowAttributes.ShadowInterface) {
                ((ShadowAttributes.ShadowInterface)e).setRadius(r);
            }
        }
        changed();    
    }

    @Override
    public void setOpacity(float o) {
        for (ZElement e : elements) {
            if (e instanceof ShadowAttributes.ShadowInterface) {
                ((ShadowAttributes.ShadowInterface)e).setOpacity(o);
            }
        }
        changed();   
    }

    @Override
    public void setXOffset(double x) {
        for (ZElement e : elements) {
            if (e instanceof ShadowAttributes.ShadowInterface) {
                ((ShadowAttributes.ShadowInterface)e).setXOffset(x);
            }
        }
        changed();  
    }

    @Override
    public void setYOffset(double y) {
        for (ZElement e : elements) {
            if (e instanceof ShadowAttributes.ShadowInterface) {
                ((ShadowAttributes.ShadowInterface)e).setYOffset(y);
            }
        }
        changed();    
    }

    @Override
    public void setSizeRatio(double s) {
        for (ZElement e : elements) {
            if (e instanceof ShadowAttributes.ShadowInterface) {
                ((ShadowAttributes.ShadowInterface)e).setSizeRatio(s);
            }
        }
        changed();   
    }
  
    
    
}
