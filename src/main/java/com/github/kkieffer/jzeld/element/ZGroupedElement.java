
package com.github.kkieffer.jzeld.element;

import java.awt.Color;
import java.awt.Graphics2D;
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
public final class ZGroupedElement extends ZElement {

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
    
    /**
     * Groups the elements into a ZGroupedElement. Sub-elements are repositioned relative to the grouped element.  The grouped element
     * position and size is set to bound all the sub-elements.
     * @param elements
     * @return 
     */
    public static ZGroupedElement createGroup(ArrayList<ZElement> elements, double scale) {
        
        double x = Integer.MAX_VALUE;  //furthest left
        double y = Integer.MAX_VALUE;  //furthest top
        double x2 = 0;                 //furthest right
        double y2 = 0;                 //furthest bottom
        for (ZElement e : elements) {
            
            Rectangle2D b = e.getBounds2D();
           
            double margin = Math.ceil(3*e.getOutlineWidth()/2.0) / scale; 
            b = new Rectangle2D.Double(b.getX()-margin, b.getY()-margin, b.getWidth() + 2*margin, b.getHeight() + 2*margin); 
            
            AffineTransform t = e.getElementTransform(1.0, false);
            Shape s = t.createTransformedShape(b);
      
            b = s.getBounds2D();  //make bounds something that can hold the transformed shape
              
            if (b.getX() < x)
                x = b.getX();
            if (b.getY() < y)
                y = b.getY();
            
            if (b.getX() + b.getWidth() > x2)
                x2 = b.getX() + b.getWidth();
            if (b.getY() + b.getHeight() > y2)
                y2 = b.getY() + b.getHeight();
            
        }
        
        
        return new ZGroupedElement(x, y, x2-x, y2-y, elements);
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
    
    
    @Override
    public String getHtmlHelp() {
        
        String group = "";
        for (ZElement e : elements)
            group += e.getClass().getSimpleName() + "<br>";
        
        return "<b>ZGroupedElement: A group of multiple elements that can be moved and transformed together.</b><br><br>The transformations " +
                "are applied to the group as a whole, so if they are later ungrouped the elements will revert back to their original transforms. " +
                "However, a ZGroupedElement has no intrinsic attributes such as fill color and line weight; applying those modifications change " +
                "the individual elements and are retained after ungrouping.<br><br>" +
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

            System.out.println(relativeE + " - > " + rotated);
            
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
    public boolean supportsEdit() {
        return false;
    };

    @Override
    public void setAttributes(float outlineWidth, Color outlineColor, Float[] dashPattern, Color fillColor) {
        setFillColor(fillColor);
        setOutlineWidth(outlineWidth);
        setDashPattern(dashPattern);
        setOutlineColor(outlineColor);
    }

    @Override
    public void setOutlineWidth(float width) {
        for (ZElement e : elements) {
            if (e.hasOutline()) {
                e.setOutlineWidth(width);
                hasChanges = true;    
            }
        }
    }
    
    @Override
    public float getOutlineWidth() {
        return 0;
    }

    @Override
    public void setOutlineColor(Color outlineColor) {
        for (ZElement e : elements) {
            if (e.hasOutline()) {
                e.setOutlineColor(outlineColor);
                hasChanges = true;
            }
        }
    }

    @Override
    public void setDashPattern(Float[] dashPattern) {
        for (ZElement e : elements) {
            if (e.hasDash()) {
                e.setDashPattern(dashPattern);
                hasChanges = true;
            }
        } 
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
                hasChanges = true;
            }
        } 
    }
    
    @Override
    public void removeFill() {
        for (ZElement e : elements) {
            if (e.hasFill()) {
                e.removeFill();
                hasChanges = true;
            }
        } 
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
        return false;
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
            e.reposition(newX/scale, newY/scale);
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

    
}
