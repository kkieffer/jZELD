
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.JAXBAdapter.Rectangle2DAdapter;
import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * The abstract object defines a bounded object that can be placed on the canvas.  The object location reference is its top left corner.  The 
 * object bounds defines its drawable area, based on its width and height.  When drawn, the object's
 * paint method must render the object relative to its position reference (top left corner).  Rotation is automatically handled by the ZCanvas - the
 * drawing canvas is transformed prior to painting such that the object will be rotated about the center of the bounds box.  
 * 
 * The element knows about its size in units - the canvas takes care of scaling to the proper number of pixels.  However, the paint() method is passed
 * the scaled pixels width and height which are suitable for directly drawing pixels.
 * 
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZElement")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ZElement {
    
    @XmlJavaTypeAdapter(Rectangle2DAdapter.class)
    private Rectangle2D.Double bounds; 
    private Point2D.Double position;
    private double rotation;  //degrees
    private boolean canSelect;
    private boolean resizable;
    
    protected boolean flipHoriz = false;
    protected boolean flipVert = false;
    
    @XmlAttribute(name = "class")
    private String className;  //needed to reload subclasses by classname
    
    @XmlTransient
    protected boolean hasChanges = false;  //marks any changes to the Element prior to saving
  
    
  
    /**
     * Create an object with the initial position (in units) and bounds (in units)
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the object in units, or null for unlimited width
     * @param height the height of the object in units, or null for unlimited height
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param selectable if the object can be selected by the ZCanvas mouse click
     * @param resizable if the object can be resized by the mouse drag
     */
    protected ZElement(double x, double y, double width, double height, double rotation, boolean selectable, boolean resizable) {
        bounds = new Rectangle2D.Double(0, 0, width, height);
        position = new Point2D.Double(x, y);
        this.rotation = rotation;
        canSelect = selectable;
        this.resizable = resizable;
        this.className = this.getClass().getName();
    }
    
    protected ZElement() {
        super();
    }
    
    
    
    protected ZElement(ZElement src) {
        
        this.bounds = new Rectangle2D.Double(src.bounds.x, src.bounds.y, src.bounds.width, src.bounds.height);
        this.position = new Point2D.Double(src.position.x, src.position.y);
        this.rotation = src.rotation;
        this.canSelect = src.canSelect;
        this.resizable = src.resizable;
        flipHoriz = src.flipHoriz;
        flipVert = src.flipVert;
        className = src.className;
    } 
    
    
    
    public abstract ZElement copyOf();
   
    /**
     * Call this after saving an element to permanent storage. This marks the "hasChanges" flag to false, and hasChanges() will return false
     * until the next time the element is modified.
     */
    public final void wasSaved() {
        hasChanges = false;
    }
    
    
    /**
     * Checks to see if the element has changed since the last call to wasSaved().  Generally changing the position, size, rotation, or
     * other attributes of a element will indicate changes.  Immediately after construction the element should not have any changes.
     * @return true if the element has changed, false otherwise
     */
    public final boolean hasChanges() {
        return hasChanges;
    }
    
    
    /**
     * Sets the attributes of an element.  An Element does not need to support all or any of these fields.
     * @param outlineWidth thickness of the element's outline, in pixels
     * @param outlineColor color of the element's outline, null for transparent
     * @param dashPattern a dash pattern for the outline, in pixels.  Null for solid line.  Values in units
     * @param fillColor interior fill color
     */
    public abstract void setAttributes(int outlineWidth, Color outlineColor, Float[] dashPattern, Color fillColor);
    
    /**
     * For elements that support an outline, sets the current width in pixels of the outline.  Width of 0 is no outline 
     * @param width width in pixels
     */
    public abstract void setOutlineWidth(int width);
    
    
    /**
     * For elements that support an outline, sets the current color of the outline.
     * @param outlineColor color of the outline.  Null will set the color transparent
     */
    public abstract void setOutlineColor(Color outlineColor);
    
    /**
     * For elements that support an outline, sets the dash pattern of the outline.  Null is a solid line.  Dash pattern is the same
     * as for the Stroke component.
     * @param dashPattern dash pattern with values in units
     */
    public abstract void setDashPattern(Float[] dashPattern);
    
    /**
     * For elements that support a fill, sets the color of the fill.  Null is transparent.
     * @param fillColor 
     */
    public abstract void setFillColor(Color fillColor);
    
    /**
     * Get the current fill color
     * @return fill color, null if transparent
     */
    public abstract Color getFillColor();
    
    /**
     * Get the current outline color
     * @return outline color, null if transparent
     */
    public abstract Color getOutlineColor();
    
     /**
     * For elements that support an outline, gets the current width in pixels of the outline.  Width of 0 is no outline 
     * @return width in pixels
     */
    public abstract int getOutlineWidth();
    
    /**
     * True if the element supports an outline
     * @return 
     */
    public abstract boolean hasOutline();
    
    /**
     * True if the element supports a dashed outline
     * @return 
     */
    public abstract boolean hasDash();
    
    /**
     * True if the element supports fill color
     * @return 
     */
    public abstract boolean hasFill();

    
    /**
     * True if the element can be selected by the cursor
     * @return 
     */
    public boolean isSelectable() {
        return canSelect;
    }
    
    /**
     * True if the element can be resized by the cursor
     * @return 
     */
    public boolean isResizable() {
        return resizable;
    }
    
    /**
     * Return the rotation, clockwise, in degrees
     * @return 
     */
    public double getRotation() {
        return rotation;
    }
    
    
    /**
     * Set the rotation, clockwise, in degrees
     * @param r 
     */
    public void setRotation(double r) {
        rotation = r;
    }
    
    /**
     * Adjust the rotation, adding the given angle (angle clockwise)
     * @param angle 
     */
    public void rotate(double angle) {
        rotation += angle;
    }
    
    /**
     * Reposition top left corner of the element 
     * @param x position in units
     * @param y position in units
     */
    public void reposition(double x, double y) {
        position.x = x;
        position.y = y;
    }
    
    
    /**
     * Move the object by x and y, not to exceed the limits or where it's width and height will take it beyond the left or top of the canvas
     * @param x position in units
     * @param y position in units
     * @param xLimit the furthest x
     * @param yLimit the furthest y
     */
    public void move(double x, double y, double xLimit, double yLimit) {
        
        if (position.x + x < xLimit && position.x + x + bounds.width > 0)   
            position.x += x;
        
        if (position.y + y < yLimit && position.y + y + bounds.height > 0)
            position.y += y;
        
    }
    
 
    
    /**
     * Resize the object, new width and height in pixels
     * @param w width in pixels
     * @param h height in pixels
     * @param minSize the minimum size, in pixels
     * @param scale scale factor
     */
    protected void setSize(double w, double h, double minSize, double scale) {
   
        bounds.width = w/scale;
        bounds.height = h/scale;
        
        if (bounds.width <= 0)
            bounds.width = minSize/scale;  //don't go to zero
        if (bounds.height <= 0)
            bounds.height = minSize/scale; //don't go to zero
    }
    
    public final void changeSize(double w, double h, double minSize, double scale) {
        if (!resizable)
            return;
        
        setSize(w, h, minSize, scale);
    }
    
    
    /**
     * Increase the size of the element by the specified width and height (negative reduces size).
     * If the element size is reduced negative, the absolute value will be taken.
     * @param w width in pixels
     * @param h height in pixels
     * @param minSize the minimum size, in pixels
     * @param scale scale factor
     */
    public final void increaseSize(double w, double h, double minSize, double scale) {
        if (!resizable)
            return;
        
        w = w/scale;
        h = h/scale;
        
        double newWidth = bounds.width + w;
        double newHeight = bounds.height + h;
        
        setSize(newWidth * scale, newHeight * scale, minSize, scale);
     
    }
    
    /**
     * Returns the unit bounds of the object about the top left corner
     * @return a bounding rectangle
     */
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double((position.x + bounds.x), (position.y + bounds.y), bounds.width, bounds.height);
    }
    
    /**
     * Returns the pixel bounds of the object about the top left corner using the scaling unit as a Rectangle2D - double
     * @param scale the scale of pixels per unit
     * @return a bounding rectangle
     */
    public Rectangle2D getBounds2D(double scale) {
        return new Rectangle2D.Double((position.x + bounds.x) * scale, (position.y + bounds.y) * scale, bounds.width * scale, bounds.height * scale);
    }
    
    
     /**
     * Returns the pixel bounds of the object about the top left corner using the scaling unit as a Rectangle - integer
     * @param scale the scale of pixels per unit
     * @return a bounding rectangle
     */
    public Rectangle getBounds(double scale) {
        return new Rectangle((int)((position.x + bounds.x) * scale), (int)((position.y + bounds.y) * scale), (int)(bounds.width * scale), (int)(bounds.height * scale));
    }
    
    /**
     * Returns the position of the top left corner in pixels
     * @param scale the scale of pixels per unit
     * @return 
     */
    public final Point getPosition(double scale) {
        return new Point((int)(position.x * scale), (int)(position.y * scale));
    }
    
    /**
     * Returns the position of the top left corner in units
     * @return 
     */
    public final Point2D getPosition() {
        return new Point2D.Double(position.x, position.y);
    }
    
    /**
     * Get the transform that describes how this element is rotated
     * @param scale scale of pixels per unit
     * @param toBase true to rotate back to the base coordinate system (no rotation), false to move to the rotated coordinate system
     * @return 
     */
    public final AffineTransform getElementTransform(double scale, boolean toBase) {
        Rectangle2D boundsBox = getBounds(scale);

        //Rotate the shape if needed
        AffineTransform t = AffineTransform.getRotateInstance((toBase ? -1 : 1) * Math.toRadians(getRotation()), boundsBox.getX() + boundsBox.getWidth()/2, boundsBox.getY() + boundsBox.getHeight()/2);
        return t;
    }
    
    /**
     * Paint the component at the x and y coordinates (pixels)
     * @param g graphics context to paint on
     * @param unitSize the number of pixels per unit 
     * @param width the width, in pixels.  If the object's bounding width is negative, this will be the canvas width
     * @param height the height, in pixels.  If the object's bounding height is negative, this will be the canvas height
     */
    public abstract void paint(Graphics2D g, int unitSize, int width, int height);

    
    /**
     * Flip the object horizontally (in place).  If an object doesn't support this, it doesn't override this method
     */
    public void flipHorizontal() {
        flipHoriz = !flipHoriz;
    }
        
    
    /**
     * Flip the object vertically (in place).  If an object doesn't support this, it doesn't override this method
     */
    public void flipVertical() {
        flipVert = !flipVert;
    }    
    
    /**
     * True if the element supports flipping horizontal/vertical
     * @return 
     */
    public abstract boolean supportsFlip();
    
    
    /**
     * When the element is double-click selected by the cursor, this method is called
     * @param canvas canvas where the click is coming from
     * @return true if the element has special functions when selected, false otherwise
     */
    public boolean selected(ZCanvas canvas) {
        return false;
    }

    /**
     * When the element is deselected by the cursor, this method is called
     */
    public void deselected() {}

    /**
     * When the element is added to a canvas, this method is called
     * @param canvas 
     */
    public void addedTo(ZCanvas canvas) {}
    
    /**
     * When the element is removed from a canvas, this method is called
     * @param canvas 
     */
    public void removedFrom(ZCanvas canvas) {}
    
    
    /**
     * When selected, a mouse event within the element calls this method. 
     * @param canvas from which the event originated
     * @param e the mouse event, which is set to the element and coordinates relative to the top left corner of the element
     * */
    public void mouseEvent(ZCanvas canvas, MouseEvent e) {}


    
        
    
}
