
package com.github.kkieffer.jzeld.element;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
    public static ZGroupedElement createGroup(ArrayList<ZElement> elements) {
        
        double x = Integer.MAX_VALUE;  //furthest left
        double y = Integer.MAX_VALUE;  //furthest top
        double x2 = 0;                 //furthest right
        double y2 = 0;                 //furthest bottom
        for (ZElement e : elements) {
            Rectangle2D r = e.getBounds2D();
            if (r.getX() < x)
                x = r.getX();
            if (r.getY() < y)
                y = r.getY();
            
            if (r.getX() + r.getWidth() > x2)
                x2 = r.getX() + r.getWidth();
            if (r.getY() + r.getHeight() > y2)
                y2 = r.getY() + r.getHeight();
            
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
        
        return "<b>ZGroupedElement: </b>This is a group of multiple elements that can be moved and transformed together. The transformations " +
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
        
        Point2D position = this.getPosition();
        
        //Restore the x,y offset to each element's position
        for (ZElement e : this.elements)
            e.move(position.getX(), position.getY(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        
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
    
    
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {
                       
        if (!isVisible())
            return;
                        
        double scaleX = (double)width / (this.groupedWidth * unitSize);
        double scaleY = (double)height / (this.groupedHeight * unitSize);
        
        g.scale(scaleX, scaleY);
        
        //Paint each element - each element has been "moved" to its offset within the group already
        for (ZElement e : elements) {    
            AffineTransform orig = g.getTransform();
            Rectangle2D bounds = e.getBounds2D(unitSize);
            
            g.translate(bounds.getX() + bounds.getWidth()/2, bounds.getY() + bounds.getHeight()/2);  //translate to the center of the element
            g.rotate(Math.toRadians(e.getRotation()));  //rotate
            g.translate(-bounds.getWidth()/2, -bounds.getHeight()/2);  //translate so that 0,0 is the top left corner
            

            e.paint(g, unitSize, bounds.getWidth(), bounds.getHeight());
            
            g.setTransform(orig);
                    
        }



    }

    public Iterable<Class<? extends ZElement>> getGroupedClasses() {
        ArrayList<Class<? extends ZElement>> classes = new ArrayList<>(elements.size());
        for (ZElement e : elements)
            classes.add(e.getClass());
        
        return classes;
    }

    
}
