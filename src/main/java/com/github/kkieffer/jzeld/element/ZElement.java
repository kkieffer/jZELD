
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.adapters.JAXBAdapter.Rectangle2DAdapter;
import com.github.kkieffer.jzeld.UnitMeasure;
import com.github.kkieffer.jzeld.ZCanvas;
import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_MITER;
import static java.awt.BasicStroke.JOIN_ROUND;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
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
public abstract class ZElement implements Serializable {
 
    public enum StrokeStyle {ROUNDED(CAP_ROUND, JOIN_ROUND), 
                             SQUARE(CAP_BUTT, JOIN_MITER);
    
        public int getCapType() { return cap; }
        public int getJoinType() { return join; }
    
        private final int cap;
        private final int join;
        private StrokeStyle(int c, int j) {
            cap = c;
            join = j;
        }
    }

    private String name;  //user friendly name
    
    @XmlJavaTypeAdapter(Rectangle2DAdapter.class)
    private Rectangle2D.Double bounds; 
    private Point2D.Double position;
    private double rotation;  //degrees
    private double shearX; //ratio
    private double shearY; 
    private boolean canSelect;
    private boolean resizable;
    private boolean canMove;
    private boolean visible;
    
    protected boolean flipHoriz = false;
    protected boolean flipVert = false;
    
    private UUID uuid;  //immutable
    
    @XmlAttribute(name = "class")
    private String className;  //needed to reload subclasses by classname
    
    transient private boolean hasChanges = false;  //marks any changes to the Element prior to saving
  
    transient private boolean selected = false;
    
  
    /**
     * Create an object with the initial position (in units) and bounds (in units)
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the object in units, or null for unlimited width
     * @param height the height of the object in units, or null for unlimited height
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param selectable if the object can be selected by the ZCanvas mouse click
     * @param resizable if the object can be resized by the mouse drag
     * @param moveable if the object can be moved by the mouse drag
     */
    protected ZElement(double x, double y, double width, double height, double rotation, boolean selectable, boolean resizable, boolean moveable) {
        bounds = new Rectangle2D.Double(0, 0, width, height);
        position = new Point2D.Double(x, y);
        this.rotation = rotation;
        canSelect = selectable;
        this.resizable = resizable;
        this.canMove = moveable;
        this.className = this.getClass().getName();
        this.name = this.getClass().getSimpleName();
        this.visible = true;
        this.hasChanges = false;
        this.uuid = UUID.randomUUID();
        
    }
    
    protected ZElement() {
        super();
    }
    
    
    /**
     * Create a copy of the element from another.
     * @param src the source of the copy    
     * @param forNew true if the element is for a true copy, false if it is for saving for later restoration (such as undo, grouping)
     */
    protected ZElement(ZElement src, boolean forNew) {
        
        this.bounds = new Rectangle2D.Double(src.bounds.x, src.bounds.y, src.bounds.width, src.bounds.height);
        this.position = new Point2D.Double(src.position.x, src.position.y);
        this.rotation = src.rotation;
        this.shearX = src.shearX;
        this.shearY = src.shearY;
        this.canSelect = src.canSelect;
        this.resizable = src.resizable;
        this.canMove = src.canMove;
        this.visible = src.visible;
        this.name = src.name; 
        flipHoriz = src.flipHoriz;
        flipVert = src.flipVert;
        className = src.className;
        
        if (forNew)
            this.uuid = UUID.randomUUID();  //new one 
        else
            this.uuid = src.uuid;
    } 
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    /**
     * Return html formatted help about the Element.  Note: do not use <html> and </html> tags, as this will interfere with including superclass help.
     * @return 
     */
    public String getHtmlHelp() {
        return (resizable ? "Resize the element by clicking and dragging the resize box in the lower right corner.<br><br>" : "") +
               (canMove ? "Move the element by selecting it and dragging it to the new position or use the arrow keys.<br><br>" : "This element cannot be moved.<br><br>") +
               (resizable ? "Resize the element, maintaining its aspect ratio, by selecting it and using the mouse wheel or press <i>CTRL + or CTRL -</i><br><br>" : 
                "This element cannot be resized but pressing <i>CTRL + or CTRL - </i>will force a resize.<br><br>") +
               "Rotate the element by selecting it and using the mouse wheel while holding Shift. Rotations to 90 degree positions are available in the right-click context menu.<br><br>" +
               "Shear the element horizontally by selecting it and using the mouse wheel while pressing keys Alt-S.<br><br>" +
               "Shear the element vertically by selecting it and using the mouse wheel while pressing keys Alt-Shift-S.<br><br>" +
               (flipHoriz || flipVert ? "Flip the element horizontally or vertically by right-clicking and selecting from the context menu.<br><br>" : "") +
               "Adjust element attributes by by right-clicking and selecting from the context menu.<br><br>" +
               "The Z-plane order can be adjusted by right-clicking and selecting from the context menu.<br><br>";
        
    }
    
    public abstract ZElement copyOf(boolean forNew);
   
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
     * Flag that the element has changed
     */
    public void changed() {
        hasChanges = true;
    }
    
    
    public boolean isSelected() {
        return selected;
    }
    
    public void select() {
        selected = true;
    }
    
    public void deselect() {
        selected = false;
    }
    
    /**
     * Sets the attributes of an element.  An Element does not need to support all or any of these fields.
     * @param outlineWidth thickness of the element's outline, in pixels
     * @param outlineColor color of the element's outline, null for transparent
     * @param dashPattern a dash pattern for the outline, in pixels.  Null for solid line.  Values in units
     * @param fillColor interior fill color
     * @param outlineStyle the style for the outline
     */
    public abstract void setAttributes(float outlineWidth, Color outlineColor, Float[] dashPattern, Color fillColor, StrokeStyle outlineStyle);
    
    /**
     * For elements that support an outline, sets the current width in pixels of the outline.  Width of 0 is no outline 
     * @param width width in pixels
     */
    public abstract void setOutlineWidth(float width);
    
    
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
     * For elements that support an outline, sets the style the outline.  \
     * @param style the style to set
     */
    public abstract void setOutlineStyle(StrokeStyle style);
    
    /**
     * For elements that support an outline, gets the dash pattern of the outline.  Null is a solid line.  Dash pattern is the same
     * as for the Stroke component.
     * @return dashPattern dash pattern with values in units
     */
    public abstract Float[] getDashPattern();
    
    /**
     * For elements that support a fill, sets the color of the fill.  
     * @param fillColor 
     */
    public abstract void setFillColor(Color fillColor);
    
    
    /**
     * For elements that support a fill, removes the fill color or paint attributes
     */
    public abstract void removeFill();
    
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
    public abstract float getOutlineWidth();
    
    /**
     * For elements that support an outline, gets the current style of the outline.  
     * @return the style
     */
    public abstract StrokeStyle getOutlineStyle();
    
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
    
    public void setSelectable(boolean selectable) {
        canSelect = selectable;
    }
    
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }
    
    /**
     * True if the element can be resized by the cursor
     * @return 
     */
    public boolean isResizable() {
        return resizable;
    }
    
    /**
     * True if the element can be moved about the canvas
     * @return 
     */
    public boolean isMoveable() {
        return canMove;
    }
    
    /**
     * True if the element is mutable. A mutable object can be copied, deleted, grouped, and joined other elements. 
     * Note that size, rotation, transformations, and positions have their own controls and aren't affected by this.
     * @return 
     */
    public abstract boolean isMutable();
   
    
    public void setMoveable(boolean move) {
        canMove = move;
    }
    
    /**
     * True if the element is visible on the canvas
     * @return 
     */
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean vis) {
        visible = vis;
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
        rotation = rotation % 360.0;
        if (rotation < 0)
            rotation = 360.0 + rotation;
        hasChanges = true;
    }
    
    /**
     * Retrieves the user friendly name
     * @return 
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets a user-friendly name for this specific element, for reference purposes
     * @param n the name
     */
    public void setName(String n) {
        name = n;
    }
    
    /**
     * Adjust the rotation, adding the given angle (angle clockwise)
     * @param angle 
     */
    public void rotate(double angle) {
        setRotation(rotation + angle);
    }
    
    public void shearX(double s) {
        shearX += s;
        hasChanges = true;
    }
    
    
    public void shearY(double d) {
        shearY += d;
        hasChanges = true;
    }

    public void setShearX(double s) {
        shearX = s;
        hasChanges = true;
    }
    public void setShearY(double s) {
        shearY = s;
        hasChanges = true;
    }
    
    public double getShearX() {
        return shearX;
    }
    
    public double getShearY() {
        return shearY;
    }
    
    
    /**
     * Reposition top left corner of the element 
     * @param x position in units
     * @param y position in units
     * @param xLimit the furthest x
     * @param yLimit the furthest y
     */
    public void reposition(double x, double y, double xLimit, double yLimit) {
        
        if (!canMove)
            return;
        
        if (x + bounds.width <= 0 || y + bounds.height <= 0)  //too far left or top
            return;
        
        if (x >= xLimit || y >= yLimit) //too far right or bottom
            return;
        
        position.x = x;
        position.y = y;
        hasChanges = true;
    }
    
    
    /**
     * Move the object by x and y, not to exceed the limits or where it's width and height will take it beyond the left or top of the canvas
     * @param x position in units
     * @param y position in units
     * @param xLimit the furthest x
     * @param yLimit the furthest y
     */
    public void move(double x, double y, double xLimit, double yLimit) {
        
        if (!canMove)
            return;
        
        if (position.x + x <= xLimit && position.x + x + bounds.width >= 0)   
            position.x += x;
        
        if (position.y + y <= yLimit && position.y + y + bounds.height >= 0)
            position.y += y;
    
        hasChanges = true;
        
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
         hasChanges = true;
   }
    
    /**
     * Change size to the specified width and height in units.
     * @param w width in units
     * @param h height in units
     * @param minSize the minimum size, in pixels
     * @param scale scale factor
     */
    public final void changeSize(double w, double h, double minSize, double scale) {
        if (!resizable)
            return;
        
        setSize(w, h, minSize, scale);
    }
    
    
    public void scaleSize(double wScale, double hScale) {         
        double w = bounds.width * wScale;
        double h = bounds.height * hScale;
     
        setSize(w, h, 0, 1.0);
    }
    
    /**
     * Increase the size of the element by the specified amount, maintaining the aspect ratio of width to height
     * @param widthIncrease the amount to increase the width in pixels. The height is increased by the radio of height to width.
     * @param minSize the minimum size, in pixels
     * @param scale scale factor
     */
    public final void increaseSizeMaintainAspect(double widthIncrease, double minSize, double scale) {
        if (!resizable)
            return;
    
        
        double w = widthIncrease;
        double h = (bounds.height/bounds.width * widthIncrease);
        
        increaseSize(w, h, minSize, scale);
    }
    
    /**
     * Increase the size of the element by the specified width and height in pixels (negative reduces size).
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
     * Returns the pixel bounds of the object relative to its position using the scaling unit as a Rectangle2D. The element may require additional margin
     * around itself in order to properly draw any additional features such as shadows or reflections.
     * @param scale
     * @return the bounding rectangle, x is negative if additional space is required left of the object, 0 otherwise.
     *                                 y is negative if additional space is required right of the object, 0 otherwise.
     *                                 width is the required width, which is equal to or exceeds the object width bounds
     *                                 height is the required height, which is equal to or exceeds the object height bounds
     */
    public abstract Rectangle2D getMarginBounds(double scale);
    
    
    /**
     * Returns the position of the top left corner in pixels
     * @param scale the scale of pixels per unit
     * @return 
     */
    public final Point2D getPosition(double scale) {
        return new Point2D.Double(position.x * scale, position.y * scale);
    }
    
    /**
     * Returns the position of the top left corner in units
     * @return 
     */
    public final Point2D getPosition() {
        return new Point2D.Double(position.x, position.y);
    }
    
    /**
     * Return the transformed center point of the element
     * @param scale the scale of pixels per unit
     * @return 
     */
    public final Point2D getCenterPosition(double scale) {
        Rectangle2D b = getBounds2D(scale);
        Point2D.Double center = new Point2D.Double(b.getCenterX(), b.getCenterY());
        return getElementTransform(scale, false).transform(center, null);
    }
    
    
    /**
     * Get the transform that describes how this element is rotated and sheared
     * @param scale scale of pixels per unit
     * @param toBase true to transform back to the base coordinate system (no rotation), false to move to the transformed coordinate system
     * @return 
     */
    public final AffineTransform getElementTransform(double scale, boolean toBase) {
        Rectangle2D boundsBox = getBounds2D(scale);

        double x = boundsBox.getX() + boundsBox.getWidth()/2; 
        double y = boundsBox.getY() + boundsBox.getHeight()/2;
        
        //Translate to center
        AffineTransform t = AffineTransform.getTranslateInstance(x, y);
        t.rotate((toBase ? -1 : 1) * Math.toRadians(getRotation()));        
        t.shear((toBase ? -1 : 1) * shearX, (toBase ? -1 : 1) * shearY);
        t.translate(-x, -y);
                
        return t;
    }
    
    /**
     * Paint the component at the x and y coordinates (pixels)
     * @param g graphics context to paint on
     * @param unitSize the number of pixels per unit 
     * @param width the width, in pixels.  If the object's bounding width is negative, this will be the canvas width
     * @param height the height, in pixels.  If the object's bounding height is negative, this will be the canvas height
     */
    public abstract void paint(Graphics2D g, double unitSize, double width, double height);

    
    /**
     * Flip the object horizontally (in place).  If an object doesn't support this, it doesn't override this method
     */
    public void flipHorizontal() {
        flipHoriz = !flipHoriz;
        hasChanges = true;
    }
        
    
    /**
     * Flip the object vertically (in place).  If an object doesn't support this, it doesn't override this method
     */
    public void flipVertical() {
        flipVert = !flipVert;
        hasChanges = true;
    }    
    
    /**
     * True if the element supports flipping horizontal/vertical
     * @return 
     */
    public abstract boolean supportsFlip();
    
    /**
     * True if the element has properties than can be edited by double-clicking
     * @return 
     */
    public abstract boolean supportsEdit();
    
    /**
     * When the element is double-click selected by the cursor, this method is called
     * @param canvas canvas where the click is coming from
     * @return true if the element has special functions when selected, false otherwise
     */
    public boolean selectedForEdit(ZCanvas canvas) {
        return false;
    }

    /**
     * When the element is deselected by the cursor, this method is called
     */
    public void deselectedForEdit() {}

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
     * When the canvas measure unit is changed, this method is called
     * @param canvas
     * @param u  the new unit
     */
    public void unitChanged(ZCanvas canvas, UnitMeasure u) {}
    
    
    /**
     * When the element is selected for edit, a mouse event calls this method. The method may either swallow the mouse event
     * or pass it back to the caller
     * @param canvas from which the event originated
     * @param e the mouse event, which is set to the element and coordinates relative to the top left corner of the element
     * @return true if the element swallowed the event (meaning it should not be further processed).  If false, the event
     * can also be processed by the canvas (potentially to deselect the element)
     * */
    public boolean mouseEvent(ZCanvas canvas, MouseEvent e) {
        return false;
    }



    
          
}
