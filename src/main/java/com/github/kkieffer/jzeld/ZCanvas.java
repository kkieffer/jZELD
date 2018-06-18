
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.JAXBAdapter.ColorAdapter;
import com.github.kkieffer.jzeld.JAXBAdapter.DimensionAdapter;
import com.github.kkieffer.jzeld.JAXBAdapter.FontAdapter;
import com.github.kkieffer.jzeld.JAXBAdapter.PointAdapter;
import com.github.kkieffer.jzeld.draw.DrawClient;
import com.github.kkieffer.jzeld.element.ZCanvasRuler.Unit;
import com.github.kkieffer.jzeld.element.ZElement;
import com.github.kkieffer.jzeld.element.ZAbstractShape;
import com.github.kkieffer.jzeld.element.ZCanvasRuler;
import com.github.kkieffer.jzeld.element.ZGrid;
import com.github.kkieffer.jzeld.element.ZGroupedElement;
import com.github.kkieffer.jzeld.element.ZShape;
import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_MITER;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.RepaintManager;
import javax.swing.Timer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * ZCanvas is where the ZElements are drawn and manipulated
 * 
 * Once they are added, the can be selected by the mouse, and will be highlighted with a dashed line.  Once selected it
 * can be dragged to new locations, and resized by dragging the corner drag box.  Moving the mouse wheel resizes the object
 * maintaining the aspect ratio.  Holding shift while rotating the mouse wheel rotates the object.
 * 
 * Double clicking an element will notify it that it is selected and it will receive mouse events. Clicking outside that
 * element will notify the element it no longer is selected.  Shift clicking will select multiple items.
 *  
 * A selected elements can be moved with the arrow keys.
 * 
 * Holding shift while clicking elements selects multiple elements.
 * 
 * The canvas can be zoomed with the plus/minus keys, zoom in up to 4.0 factor.
 * 
 * The canvas can be moved with the arrow keys if nothing is selected.
 * 
 * A popup menu can be added when selecting each element.  Additional hotkeys can also be added.
 *
 * 
 * @author kkieffer
 */
public class ZCanvas extends JComponent implements Printable, MouseListener, MouseMotionListener, MouseWheelListener  {

    public enum CombineOperation {Join, Subtract, Intersect, Exclusive_Join;
    
        @Override
        public String toString() {
            return this.name().replace("_", " ");
        }
    }
    
    public enum Orientation {PORTRAIT, LANDSCAPE, REVERSE_LANDSCAPE}  //The ordinals conform to the PageFormat integer defines
   
    public enum Alignment {Left_Edge, Top_Edge, Right_Edge, Bottom_Edge, Centered_Vertically, Centered_Horizontally;
        
        @Override
        public String toString() {
            String[] split = this.name().split("_");
          
            return split[0] + " " + split[1];
        }
    
    }
    
    public static final ImageIcon errorIcon = new ImageIcon(ZCanvas.class.getResource("/error.png")); 
  
    
    private static final double ROTATION_MULTIPLIER = 1.0;
    private static final double SIZE_INCREASE_MULTIPLIER = 0.5;
    private final static float SCALE = 72.0f;

    /* -------- FIELDS BELOW CAN BE SAVED TO FILE USING JAXB ---------------*/
    /* This class is used because JaxB can't handle JComponent superclass*/
    @XmlRootElement(name="ZCanvas")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CanvasStore implements Serializable {

        @XmlElement(name="ZElement")        
        private LinkedList<ZElement> zElements = new LinkedList<>();  //list of all Z-plane objects, first is top, bottom is last
     
        @XmlJavaTypeAdapter(ColorAdapter.class)
        private Color backgroundColor;  //canvas background color

        @XmlElement(name="MouseCursorColor")
        @XmlJavaTypeAdapter(ColorAdapter.class)
        private Color mouseCursorColor;
        
        @XmlElement(name="MouseCoordFont")
        @XmlJavaTypeAdapter(FontAdapter.class)
        private Font mouseCoordFont;
        
        @XmlElement(name="MeasureScale")
        private Unit unit;

        @XmlElement(name="UndoStackCount")        
        private int undoStackCount = 1;       
        
        @XmlElement(name="Origin")
        @XmlJavaTypeAdapter(PointAdapter.class)
        private Point origin;
        
        @XmlElement(name="Bounds")
        @XmlJavaTypeAdapter(DimensionAdapter.class)
        private Dimension bounds;
        
        @XmlElement(name="Orientation")        
        private Orientation orientation;
        
        @XmlElement(name="HorizontalRuler")        
        private ZCanvasRuler horizontalRuler;
        
        @XmlElement(name="VerticalRuler")        
        private ZCanvasRuler verticalRuler;
        
        @XmlElement(name="Grid")
        private ZGrid grid;
        
        
    }
    /*----------------------------------------------------------------------*/
    
    CanvasStore fields = new CanvasStore();
        
    private final float pixScale = SCALE/72.0f;
    
    private final float[] dashedBorder = new float[]{0.0f, pixScale*5.0f, pixScale*5.0f};
    private final float[] altDashedBorder = new float[]{pixScale*5.0f};

    private final int DRAG_BOX_SIZE = (int)(10 * pixScale);
    
    private double zoom = 1.0;
    
    private final ArrayList<ZElement> selectedElements = new ArrayList<>();
    private ZElement[] clipboard;

    private UndoStack undoStack;

    private Cursor currentCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    
    private ZElement passThruElement = null;
    private int selectedObj_yOffset;
    private int selectedObj_xOffset;
    private Point mouseIn;
    private boolean selectedElementResizeOn = false;
    private ZElement selectedResizeElement = null;
    private ZElement lastSelectedElement;

    private boolean selectedAlternateBorder;
    private Method lastMethod = null;
    private Object[] lastMethodParams;

    private boolean canvasModified;  //tracks any changes to the Z-plane order of the elements

    private long mouseWheelLastMoved = -1;
    private long mouseFirstPressed = -1;
    private DrawClient drawClient = null;
    
    private boolean shiftPressed = false;
    private Point2D selectedMouseDrag;
    private Point mouseDrag;
    private Point mousePress;
    private Point2D selectedMousePress;
    private ZCanvasContextMenu contextMenu;
    
    private final ArrayList<SelectListener> selectListeners = new ArrayList<>();

    //For restoration by JAXB
    private ZCanvas() {
        super();
    }
    
    /**
     * Create the ZCanvas
     * @param background the background color of the canvas
     * @param mouseCoordFont the font of the mouse coordinates, null to remove the coordinates from being drawn
     * @param unitType the unit to use for mouse coordinates
     * @param mouseCursorColor color for the mouse cursor lines, use null to remove the cursor
     * @param undoStackCount the amount of history to keep in undo (the higher the count, the more memory used
     * @param origin the desired coordinate origin of the top left corner. If null, origin = 0,0
     * @param bounds the maximum bounds of the canvas (width and height).  If null, unlimited
     * @param o the page orientation, for printing
     */
    public ZCanvas(Color background, Font mouseCoordFont, Unit unitType, Color mouseCursorColor, int undoStackCount, Point origin, Dimension bounds, Orientation o) {
        super();
        fields.backgroundColor = background;
        fields.unit = unitType;
        fields.mouseCoordFont = mouseCoordFont;
        fields.mouseCursorColor = mouseCursorColor;
        fields.undoStackCount = undoStackCount;
        if (origin == null)
            fields.origin = new Point(0,0);
        else
            fields.origin = new Point(origin);
        
        setCanvasBounds(bounds, o);
        init();
    }
    
   
    private void init() {
    
        undoStack = new UndoStack(fields.undoStackCount);
        canvasModified = false;
        
        addMouseListener(this);	
        addMouseMotionListener(this);    
        addMouseWheelListener(this);
       

        Timer timer = new Timer(200, new ActionListener() {  //timer to animate the selected dashed line border, draw coordinates
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedAlternateBorder = !selectedAlternateBorder;
                if (selectedMousePress != null && System.nanoTime() - mouseFirstPressed > 500000000)
                    selectedMouseDrag = selectedMousePress;
                    
                    
                repaint();
            }
        });
        timer.start();
        
        
        //Set up the standard hotkeys for the canvas, more can be added by custom implementations
        InputMap im = getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "Plus");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "Minus");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, KeyEvent.SHIFT_DOWN_MASK), "ShiftPressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true), "ShiftReleased");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK), "MoveLeft");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK), "MoveRight");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK), "MoveUp");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK), "MoveDown");
        
        am.put("Plus", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomIn();
            }
        });
        am.put("Minus", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomOut();
            }
        });
        am.put("ShiftPressed", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                shiftPressed = true;
            }
        });
        am.put("ShiftReleased", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                shiftPressed = false;
            }
        });
        am.put("MoveLeft", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelected(-1/SCALE, 0);
            }
        });
        am.put("MoveRight", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelected(1/SCALE, 0);
                System.out.println("rgith");
            }
        });
        am.put("MoveUp", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelected(0, -1/SCALE);
            }
        });
        am.put("MoveDown", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelected(0, 1/SCALE);
            }
        });
        
        
        
        updatePreferredSize();
        repaint();

    }
    
    /**
     * Set the horizontal ruler
     * @param r the ruler to use, or null to remove
     */
    public void setHorizontalRuler(ZCanvasRuler r) {
        fields.horizontalRuler = r;
        canvasModified = true;
        repaint();
    }
    
     /**
     * Set the vertical ruler
     * @param r the ruler to use, or null to remove
     */
    public void setVerticalRuler(ZCanvasRuler r) {
        fields.verticalRuler = r;
        canvasModified = true;
        repaint();
    }
    
    /**
     * Set the grid to use
     * @param g the grid to use, or null to remove
     */
    public void setGrid(ZGrid g) {
        fields.grid = g;
        canvasModified = true;
        repaint();
    }
    
    /**
     * True if there is a grid set, false otherwise
     * @return 
     */
    public boolean hasGrid() {
        return fields.grid != null;
    }
    
    
    /**
     * Change the measure unit
     * @param u the new measure unit (cannot be null)
     */
    public void changeUnit(Unit u) {
        fields.unit = u;
        canvasModified = true;
        repaint();
    }
    
    
    public Unit getUnit() {
        return fields.unit;
    }
    
    public void registerSelectListener(SelectListener l) {
        if (!selectListeners.contains(l))
            selectListeners.add(l);
    }
    
    
    //This method is overriden to force the component to draw only to its bounds. 
    @Override
    public void reshape(int x, int y, int width, int height) {
        
        if (fields.bounds != null) {
            if (width > fields.bounds.width * zoom + fields.origin.x)
                width = (int)(fields.bounds.width * zoom + fields.origin.x);
            if (height > fields.bounds.height * zoom + fields.origin.y)
                height = (int)(fields.bounds.height * zoom + fields.origin.y);
        }
        
        super.reshape(x, y, width, height);
    }
    
    /**
     * Returns the origin in values of units
     * @return 
     */
    public Point2D getOrigin() {
        return new Point2D.Double(fields.origin.getX()/SCALE, fields.origin.getY()/SCALE);
    }

    /**
     * Provides the drawing area of the canvas, in units 
     * @return a Rectangle2D where x,y are the origin and width,height are the bounds, returns null if unbounded
     */
    public Rectangle2D getCanvasBounds() {
        if (fields.bounds == null)
            return null;
        else
            return new Rectangle2D.Double(fields.origin.getX()/SCALE, fields.origin.getY()/SCALE, fields.bounds.width/SCALE, fields.bounds.height/SCALE);
    }
    
    
    /**
     * Provides the drawing area of the canvas, in pixels 
     * @return a Rectangle2D where x,y are the origin and width,height are the bounds, returns null if unbounded
     */
    public Rectangle getCanvasPixelBounds() {
        if (fields.bounds == null)
            return null;
        else
            return new Rectangle((int)fields.origin.getX(), (int)fields.origin.getY(), fields.bounds.width, fields.bounds.height);
    }
    
    
    public Orientation getOrientation() {
        return fields.orientation;
    }
    
    /**
     * Sets the drawing area of the canvas, in pixels 
     * @param bounds the drawing area dimension
     * @param o the orientation, for printing purposes
     */
    public final void setCanvasBounds(Dimension bounds, Orientation o) {
        if (bounds != null) { 
            fields.bounds = new Dimension(bounds.width, bounds.height);
        }
        fields.orientation = o;
        
        canvasModified = true;
        updatePreferredSize();
        repaint();
    }
    
    private void updatePreferredSize() {
        if (fields.bounds == null)
            setPreferredSize(null);
        else {
            Dimension d = new Dimension((int)(fields.bounds.width * zoom + fields.origin.x), (int)(fields.bounds.height * zoom + fields.origin.y));
            setPreferredSize(d);
        }
        canvasModified = true;

        revalidate();
    }
      
    public double getScale() {
         return SCALE;
    }

    /**
     * Set the context menu (generally right mouse click popup) when selecting an element
     * @param m the menu to set, use null to remove
     */
    public void setContextMenu(ZCanvasContextMenu m) {
        contextMenu = m;
    }
    
    
    /**
     * Starts drawing using the specified draw client.
     * @param c the desired drawing client
     * @return true if accepted, false if there is already a draw client
     */
    public boolean drawOn(DrawClient c) {
        if (drawClient != null)
            return false;
        
        drawClient = c;
        return true;
    }
    
    /**
     * Stops the draw client drawing.
     */
    public void drawOff() {
        drawClient = null;
    }
    
    /**
     * Restore the view to the default of no zoom and 0,0 at the top left corner
     */
    public void resetView() {
        zoom = 1.0;
        repaint();
    }
    
    /**
     * Zoom in, to 4:1
     */
    public void zoomIn() {
        if (zoom < 4.0)
            zoom += .25;
        
        updatePreferredSize();
    }
    
    /**
     * Zooms out, as far as 1:1 
     */
    public void zoomOut() {
        if (zoom > 1.0)
            zoom -= .25;
        
        updatePreferredSize();
    }
    
    private Rectangle getDragSelectRectangle() {
    
        int x = mousePress.x < mouseDrag.x ? mousePress.x : mouseDrag.x;
        int y = mousePress.y < mouseDrag.y ? mousePress.y : mouseDrag.y;
        int w = mousePress.x < mouseDrag.x ? mouseDrag.x - mousePress.x : mousePress.x - mouseDrag.x;
        int h = mousePress.y < mouseDrag.y ? mouseDrag.y - mousePress.y : mousePress.y - mouseDrag.y;
        
        return new Rectangle(x, y, w, h);
    }
    
   
    private void setLastMethod(String name, Object... params) {
        try {
            Class[] classes = new Class[params.length];
            for (int i=0; i<params.length; i++)
                classes[i] = params[i].getClass();
            
            lastMethod = ZCanvas.class.getMethod(name, classes);  
            lastMethodParams = params;
        } catch (NoSuchMethodException |SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Removes the previous change from the canvas
     */
    public void undo() {
        
        LinkedList<ZElement> restoreContext = undoStack.restoreContext();
        if (restoreContext != null) {
            
            //Tell all they were removed
            for (ZElement e : fields.zElements) 
                e.removedFrom(this);

            
            fields.zElements = restoreContext;  //replace all the elements
            for (ZElement e : fields.zElements)
                e.addedTo(this); //tell they were added
            
            
            selectedElements.clear();
            lastSelectedElement = null;
            passThruElement = null;
        }
        repaint();
    }
    
    /**
     * Repeats the previous change 
     */
    public void repeat() {
        if (selectedElements.isEmpty() || passThruElement != null || lastMethod == null) 
            return;
        
        try {
            lastMethod.invoke(this, lastMethodParams);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Flips the selected elements.
     * @param horiz if true, flips horizontal, else vertical
     */
    public void flip(Boolean horiz) {
        if (selectedElements.isEmpty() || passThruElement != null) 
            return;
                       
        undoStack.saveContext(fields.zElements);

        for (ZElement e : selectedElements) {
            if (horiz)
                e.flipHorizontal();
            else
                e.flipVertical();
        }
        
        setLastMethod("flip", horiz);
        
    }

    /**
     * Rotates the selected elements clockwise
     */
    public void rotate90CW() {
        rotate90(true);
        setLastMethod("rotate90CW");
    }
    
    /**
     * Rotates the selected elements counter-clockwise
     */
    public void rotate90CCW() {
        rotate90(false);
        setLastMethod("rotate90CCW");
    }
    
    /**
     * Rotate the object 90 degrees
     * @param clockwise true to rotate clockwise, false to rotate counterclockwise
     */
    private void rotate90(boolean clockwise) {
        if (selectedElements.isEmpty() || passThruElement != null) 
            return;
 
        undoStack.saveContext(fields.zElements);
        
        for (ZElement selectedElement : selectedElements) {
            double curr = selectedElement.getRotation();
            double newRotation = curr;

            if (curr % 90.0 == 0) { //already was at floor or ceil
                if (clockwise)
                    newRotation += 90.0;
                else
                    newRotation -= 90.0;
            } 
            else {
                if (clockwise)
                    newRotation = 90.0 * Math.ceil(curr / 90.0);
                else
                    newRotation = 90.0 * Math.floor(curr / 90.0);

            }
            selectedElement.setRotation(newRotation);
        }
        repaint();
    }
    
    public void moveSelected(double x, double y) {
         
        Iterator<ZElement> it = selectedElements.iterator();
        while (it.hasNext()) {
           ZElement e =  it.next();
           e.move(x, y, this.getScaledWidth()/SCALE, this.getScaledHeight()/SCALE);
        }
        repaint();
    }
    
    /**
     * Align the selected elements to the desired alignment type.  At least 2 elements must be selected
     * @param atype the desired alignment type
     */
    public void align(Alignment atype) {
        
        if (selectedElements.size() <= 1 || passThruElement != null) 
            return;
 
        undoStack.saveContext(fields.zElements);
        
        ZElement key = selectedElements.get(0);
        for (ZElement e : selectedElements) {  //loop through elements, looking for the key - closest to the edge
            
            Rectangle p = e.getBounds(SCALE);
            Rectangle k = key.getBounds(SCALE);
            
            switch (atype) {
                case Left_Edge:
                case Centered_Vertically:
                    if (p.getX() < k.getX())
                        key = e;
                    break;
                case Right_Edge:
                    if (p.getX() + p.getWidth() > k.getX() + k.getWidth())
                        key = e;
                    break;
                case Top_Edge:
                case Centered_Horizontally:
                    if (p.getY() < k.getY())
                        key = e;
                    break;
                case Bottom_Edge:
                    if (p.getY() + p.getHeight() > k.getY() + k.getHeight())
                        key = e;
                    break;
            }  
        }
            
        Rectangle k = key.getBounds(SCALE);  //must align all selected with this element
 
        for (ZElement e : selectedElements) {
                    
            Rectangle p = e.getBounds(SCALE);

             switch (atype) {
                case Left_Edge:
                    e.reposition(k.getX()/SCALE, p.getY()/SCALE);
                    break;
                case Right_Edge:
                    e.reposition((k.getX() + k.getWidth() - p.getWidth())/SCALE, p.getY()/SCALE);
                    break;
                case Top_Edge:
                    e.reposition(p.getX()/SCALE, k.getY()/SCALE);
                    break;
                case Bottom_Edge:
                    e.reposition(p.getX()/SCALE, (k.getY() + k.getHeight() - p.getHeight())/SCALE);
                    break;
                case Centered_Vertically:
                    double ctrX = k.getX() + k.getWidth()/2;
                    e.reposition((ctrX - p.getWidth()/2)/SCALE, p.getY()/SCALE);
                    break;
                case Centered_Horizontally:
                    double ctrY = k.getY() + k.getHeight()/2;
                    e.reposition(p.getX()/SCALE, (ctrY - p.getHeight()/2)/SCALE);
                    break;
                    
            }
        }
        
    }
    
    
    
    
    

    /**
     * For all selected elements, builds a ZGroupedElement from them, removes the elements the canvas, and adds the ZGroupedElement to the 
     * canvas, and selects it.
     */
    public void groupSelectedElements() {
        if (selectedElements.size() <= 1 || passThruElement != null) 
            return;

        undoStack.saveContext(fields.zElements);

         
        ZGroupedElement group = ZGroupedElement.createGroup(selectedElements);  //create the group of elements
        
        for (ZElement e: selectedElements) //remove all selected
            removeElement(e);
        
        addElement(group);  //add the group element
        lastMethod = null;
        selectedElements.clear();
        selectedElements.add(group);  //make the group the selected one
    }
    
    /**
     * For all selected elements that are of type ZGroupedElement, removes the elements from the group, adds them back to the canvas,
     * and deletes the ZGroupedElement
     */
    public void ungroup() {
        
        if (selectedElements.isEmpty() || passThruElement != null) 
            return;

        undoStack.saveContext(fields.zElements);

        ArrayList<ZElement> restoredElements = new ArrayList<>();  //create a temporary list to hold all restored elements

        Iterator<ZElement> it = selectedElements.iterator();
        while (it.hasNext()) {
            
            ZElement e = it.next();
            
            if (e instanceof ZGroupedElement) {
                
                ZGroupedElement g =  (ZGroupedElement)e;
                ArrayList<ZElement> ungroup = g.ungroup();
                
                for (ZElement u : ungroup)
                    restoredElements.add(u);
                
                it.remove();
                this.removeElement(g);
                
            }
            
            
        }
        
        for (ZElement e : restoredElements) {
            this.addElement(e);
            selectedElements.add(e);
        }
         
        
    }
    
    
    /**
     * Merge the selected elements, starting with the first selected and applying the operation to each selected one
     * The attributes of the newly combined shape are those of the first selected element
     * @param operation the operation to apply
     */
    public void combineSelectedElements(CombineOperation operation) {
        if (selectedElements.size() <= 1 || passThruElement != null) 
            return;

        undoStack.saveContext(fields.zElements);

        ZAbstractShape ref = null;
        
        for (ZElement e: selectedElements) {   
            if (e instanceof ZAbstractShape) {  
                 
                if (ref == null) {
                    ref = (ZAbstractShape)e;  //the first one selected is the reference element
                    removeElement(ref);
                }
                else {
                   
                    Shape mergedShape = ref.combineWith(operation, (ZAbstractShape)e);  //combine shapes
                    if (mergedShape != null) {  //was something that could be merged
                        ZShape shape = new ZShape(ref.getPosition().getX(), ref.getPosition().getY(), mergedShape, 0.0, true, true, ref.getOutlineWidth(), ref.getOutlineColor(), ref.getDashPattern(), ref.getFillColor());
                        removeElement(e);  //remove the merged element
                        ref = shape;  //assign to the new reference
                    }
                    
                }

            }
        }
        
        if (ref == null)  //nothing merged
            return;
        
        
        addElement(ref);  //add the merged shape
        lastMethod = null;
        selectedElements.clear();
        selectedElements.add(ref);  //make the reference the selected one
    }
   
    
    /**
     * Changes the background color of the canvas
     * @param c the color to set
     */
    public void setCanvasBackgroundColor(Color c) {
        fields.backgroundColor = c;
        lastMethod = null;
        canvasModified = true;

        repaint();
    }
    
    
    /**
     * Sets the outline width of the selected elements
     * @param width the desired width, 0 for no outline
     */
    public void setOutlineWidth(Float width) {
        if (selectedElements.isEmpty() || passThruElement != null) 
            return;
        
        undoStack.saveContext(fields.zElements);
    
        for (ZElement selectedElement : selectedElements)
            selectedElement.setOutlineWidth(width);
        
        setLastMethod("setOutlineWidth", width);
        repaint();
        
    }
    
    /**
     * Sets the selected elements border dash pattern
     * @param dash the desired pattern, or null to use a solid line
     */
    public void setDashPattern(Float[] dash) {
        if (selectedElements.isEmpty() || passThruElement != null) 
            return;
        
        undoStack.saveContext(fields.zElements);
    
        setLastMethod("setDashPattern", (Object)dash);
        
        if (dash.length == 0)
            dash = null;
        
        for (ZElement selectedElement : selectedElements)
            selectedElement.setDashPattern(dash);
        
        repaint();
        
    }
    
    /**
     * Sets the outline color of the selected elements
     * @param c 
     */
    public void setOutlineColor(Color c) {
        if (selectedElements.isEmpty() || passThruElement != null) 
            return;
        
        undoStack.saveContext(fields.zElements);

        for (ZElement selectedElement : selectedElements)
            selectedElement.setOutlineColor(c);
        
        setLastMethod("setOutlineColor", c);
        repaint();
    }
    
    /**
     * Sets the fill color of the selected elements
     * @param c color to fill, null to remove color
     */
    public void setFillColor(Color c) {
        if (selectedElements.isEmpty() || passThruElement != null) 
            return;

        undoStack.saveContext(fields.zElements);
    
        for (ZElement selectedElement : selectedElements)
            selectedElement.setFillColor(c);
        
        setLastMethod("setFillColor", c);
    }
    
   
    
    /**
     * Adds an object to the canvas, on the top layer
     * @param o object to add
     */
    public void addElement(ZElement o) {
        if (fields.zElements.contains(o))
            throw new RuntimeException("Already contains object");
        fields.zElements.addFirst(o);
        o.addedTo(this);

        lastMethod = null;
        canvasModified = true;
      
    }
    
    /**
     * Removes an object from the canvas
     * @param o the object to remove, fails silently if not found
     */
    public void removeElement(ZElement o) {
        fields.zElements.remove(o);
        o.removedFrom(this);
    
        canvasModified = true;

        lastMethod = null;
    }
    
    /**
     * Sends the selected elements to the lowest Z plane layer, fails silently if nothing selected
     */
    public void moveToBack() {
        if (selectedElements.isEmpty())
            return;

        undoStack.saveContext(fields.zElements);
        
        for (ZElement selectedElement : selectedElements) {
            fields.zElements.remove(selectedElement);
            fields.zElements.addLast(selectedElement);   
        }
        canvasModified = true;

        setLastMethod("moveToBack");
        repaint();     
    }
    
    /**
     * Moves the selected elements to the top Z plane layer, fails silently if nothing selected
     */
    public void moveToFront() {
         if (selectedElements.isEmpty())
            return;

        undoStack.saveContext(fields.zElements);
        
        for (ZElement selectedElement : selectedElements) {
            fields.zElements.remove(selectedElement);
            fields.zElements.addFirst(selectedElement);
        }
        canvasModified = true;

        setLastMethod("moveToFront");
        repaint();

    }
    
    /**
     * Moves the selected elements one Z plane layer backward, unless already at back.  Fails silently if not found.
     */
    public void moveBackward() {
                
         if (selectedElements.isEmpty())
            return;

        undoStack.saveContext(fields.zElements);
        
        for (ZElement selectedElement : selectedElements) {

            int index = fields.zElements.indexOf(selectedElement);
            if (index < 0 || index == fields.zElements.size()-1)  //not found or already at back
                continue;

            fields.zElements.remove(index);
            fields.zElements.add(index+1, selectedElement);
        }
        canvasModified = true;

        setLastMethod("moveBackward");
        repaint();
        
    }
   
    /**
     * Moves the selected elements one Z plane layer forward, unless already at front.  Fails silently if not found.
     */
    public void moveForward() {
        
        if (selectedElements.isEmpty())
            return;

        undoStack.saveContext(fields.zElements);
        
        for (ZElement selectedElement : selectedElements) {
        
            int index = fields.zElements.indexOf(selectedElement);
            if (index < 0 || index == 0)  //not found or already at front
                continue;

            fields.zElements.remove(index);
            fields.zElements.add(index-1, selectedElement);
        }

        canvasModified = true;

        setLastMethod("moveForward");
        repaint();
    }
    
    /**
     * Returns the currently selected elements
     * @return the selected elements, or null if nothing is currently selected
     */
    public ZElement[] getSelectedElements() {
        ZElement[] e = new ZElement[selectedElements.size()];
        selectedElements.toArray(e);
        return e;
    }
    
    /**
     * Makes a deep copy of the selected elements and stores it for later.  Does nothing if the control is currently with an element.
     * @return a copy of the copied elements, or null if none was copied
     */
    public ZElement[] copy() {
        if (selectedElements.size() > 0) {
            
            clipboard = new ZElement[selectedElements.size()];
            ZElement[] externalCopy = new ZElement[selectedElements.size()];
            for (int i=0; i<clipboard.length; i++) {
                clipboard[i] = selectedElements.get(i).copyOf();
                externalCopy[i] = selectedElements.get(i).copyOf();
            }
            
            lastMethod = null;
            return externalCopy;
        }
        else
            return null;
    }
    
    /**
     * Makes a deep copy of the last selected element, if there is one, and returns it _and_ stores it for later.
     * @return a copy of the cut elements, or null if none was cut
     */
    public ZElement[] cut() {
        ZElement[] copied = copy();
        if (copied != null) {
            delete();
            return copied;
        }
        else
            return null;
    }
    
    
    /**
     * Pastes any stored elements (and slightly shifts it to distinguish it from its source). Does nothing if no element was copied or control is
     * currently with an element.
     * @return true if at least one element was pasted, null otherwise
     */
    public boolean paste() {
        if (clipboard != null && passThruElement == null) {
            selectedElements.clear();

            undoStack.saveContext(fields.zElements);
            for (ZElement e : clipboard)
                paste(e);          
            return true;
        }
        else
            return false;
             
    }
    
    /**
     * Pastes an external ZElement and selects it
     * @param e the element to paste 
     */
    public void paste(ZElement e) {

        e.move(0.2, 0.2, this.getScaledWidth()/SCALE, this.getScaledHeight()/SCALE);  //move slighty down to distinguish from original
        
        ZElement toPaste = e.copyOf();  //make a copy to paste, for multiple pastes
        
        addElement(toPaste);
        selectedElements.add(toPaste);  //select the pasted element
        lastMethod = null;            

        repaint();  
    }
    
    
    
    /**
     * Deletes the selected elements. Does nothing if no element is selected or control is
     * currently with an element.
     */
     public void delete() {
        if (selectedElements.isEmpty() && passThruElement != null)
            return;

        undoStack.saveContext(fields.zElements);

        Iterator<ZElement> it = selectedElements.iterator();
        while (it.hasNext()) {
            ZElement s = it.next();
            fields.zElements.remove(s);
            s.removedFrom(this);
            it.remove();
        }
          
        canvasModified = true;
      
        lastMethod = null;            
        repaint();
     }
    
    
    //Paint the element, if the element has no width or height, provide the canvas width and height
    private void paintElement(Graphics2D g2d, ZElement o, boolean highlightSelectedOnly) {
        if (o != null) {
            
            Rectangle r = o.getBounds(SCALE);  //find the location and bounds of the element to paint
            AffineTransform t = g2d.getTransform();
            g2d.translate(r.x + r.width/2, r.y + r.height/2);  //translate to the center of the element
            g2d.rotate(Math.toRadians(o.getRotation()));  //rotate
            g2d.translate(-r.width/2, -r.height/2);  //translate so that 0,0 is the top left corner
            
            if (!highlightSelectedOnly) {  //paint the element
                o.paint(g2d, (int)SCALE, r.width<0 ? getScaledWidth() : r.width, r.height<0 ? getScaledHeight() : r.height);      
            }
                      
            if (selectedElements.contains(o) && highlightSelectedOnly) {  //highlight selected element, just outside its boundaries
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke((int)(1.0f * pixScale), CAP_SQUARE, JOIN_MITER, 10.0f, selectedAlternateBorder ? dashedBorder : altDashedBorder, 0.0f));
                int pixelsOut = (int)((o.getOutlineWidth()/2 + 1) * pixScale);
                g2d.drawRect(-pixelsOut, -pixelsOut, r.width+pixelsOut*2, r.height+pixelsOut*2); 
                   
                //draw drag box in the corner
                if (o.isResizable()) {  
                    g2d.setColor(Color.BLACK);  
                    g2d.fillRect(r.width-DRAG_BOX_SIZE, r.height-DRAG_BOX_SIZE, DRAG_BOX_SIZE, DRAG_BOX_SIZE);
                }
                               
            }

            g2d.setTransform(t);  //restore transform

        } 
    }
    
    /**
     * Select  an element, if the element is on the canvas
     * @param toSel the element to select
     * @return if the element was selected, returns true.  Returns false if the element is not selectable or not found on the canvas.
     */
    public boolean selectElement(ZElement toSel) {
        
        if (!toSel.isSelectable())
            return false;
                    
        for (ZElement e : fields.zElements) {
            if (e.equals(toSel)) {
                if (!selectedElements.contains(e)) {  // not selected, add it
                    selectedElements.add(e);  
                    lastSelectedElement = e;   
                    for (SelectListener l : selectListeners)
                        l.elementSelected(e);
                }
                
                repaint();
                return true;
            }
        }
        return false;
     
    }
    
    
    public void selectAll() {
        
        selectedElements.clear();
        for (ZElement e : fields.zElements) {
            if (e.isSelectable()) {
                selectedElements.add(e);
                for (SelectListener l : selectListeners)
                    l.elementSelected(e);   
            }
        }
        if (lastSelectedElement == null)
            lastSelectedElement = selectedElements.get(0);
     
        repaint();
    }
    
    
    @Override
    public synchronized void paintComponent(Graphics g) {

        super.paintComponent(g);  
        Graphics2D g2d = (Graphics2D)g;
        
        DecimalFormat fmt = new DecimalFormat("0.00");
          
          
        //Paint any rulers before scaling and translations
        if (fields.horizontalRuler != null) {
            g2d.translate(fields.origin.x, 0); 
            fields.horizontalRuler.paint(g2d, (int)(SCALE*zoom), getScaledWidth(), getScaledHeight());    
            g2d.translate(-fields.origin.x, 0);         
        }
        if (fields.verticalRuler != null) {
            g2d.translate(0, fields.origin.y); 
            fields.verticalRuler.paint(g2d, (int)(SCALE*zoom), getScaledWidth(), getScaledHeight());    
            g2d.translate(0, -fields.origin.y);         
        }

        g2d.translate(fields.origin.x, fields.origin.y);
        g2d.scale(1/pixScale, 1/pixScale);
        g2d.scale(zoom, zoom);
               
        if (fields.backgroundColor != null) {
            g2d.setBackground(fields.backgroundColor);
            g2d.clearRect(0, 0, getScaledWidth(), getScaledHeight());
        }
        
        if (fields.grid != null)
            paintElement(g2d, fields.grid, false);
        
        //Start from the deepest point in the stack, drawing elements up to the top z layer
        Iterator<ZElement> it = fields.zElements.descendingIterator();  
        while (it.hasNext()) {
            paintElement(g2d, it.next(), false); 
        }
        
        for (ZElement s : selectedElements)
            paintElement(g2d, s, true); //apply highlights to selected elements
        
        Font mouseFont = new Font(fields.mouseCoordFont.getFontName(), fields.mouseCoordFont.getStyle(), (int)Math.ceil(fields.mouseCoordFont.getSize2D()*pixScale/zoom));
        FontMetrics fontMetrics = g2d.getFontMetrics(mouseFont);
        g2d.setFont(mouseFont);
   
        //ELEMENT IS BEING DRAGGED - DRAW SELECTED HIGHLIGHT AND POSITION LINES/TEXT
        if (selectedMouseDrag != null && !selectedElements.isEmpty()) {
                        
            ZElement selectedElement = lastSelectedElement;
            
            Rectangle r = selectedElement.getBounds(SCALE);  //find the location and bounds of the selected element

            AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians(selectedElement.getRotation()), r.x + r.width/2, r.y + r.height/2);
            Point2D tMouse = t.transform(selectedMouseDrag, null);   
            
            //Draw crosshair
            if (fields.mouseCursorColor != null) {
                g2d.setColor(fields.mouseCursorColor);
                g2d.setStroke(new BasicStroke(1.0f * pixScale / (float)zoom, CAP_SQUARE, JOIN_MITER, 10.0f, selectedAlternateBorder ? dashedBorder : altDashedBorder, 0.0f));
                g2d.drawLine(-fields.origin.x, (int)(tMouse.getY()), (int)(tMouse.getX()), (int)(tMouse.getY())); //horiz crosshair
                g2d.drawLine((int)(tMouse.getX()), -fields.origin.y, (int)(tMouse.getX()), (int)(tMouse.getY())); //vert crosshair
            }
            
            //Draw Position string
            if (fields.mouseCoordFont != null) {
                g2d.setColor(Color.BLACK);               

                String mouseCoord = fmt.format(fields.unit.getScale()*tMouse.getX()/SCALE) + ", " + fmt.format(fields.unit.getScale()*tMouse.getY()/SCALE);                                       
                int stringX = (int)tMouse.getX() - (int)Math.ceil(fontMetrics.stringWidth(mouseCoord) + 10.0 * pixScale/zoom);
                int stringY = (int)tMouse.getY() - (int)Math.ceil(10*pixScale/zoom);
                
                g2d.drawString(mouseCoord, stringX, stringY);
                               
                String rotationString = fmt.format(selectedElement.getRotation());
                stringX = (int)tMouse.getX() - (int)Math.ceil(fontMetrics.stringWidth(rotationString) + 10.0 * pixScale/zoom);
                stringY = (int)tMouse.getY() + (int)Math.ceil(10*pixScale/zoom) + fontMetrics.getHeight();

                g2d.drawString(rotationString, stringX, stringY);
            }

        }
        
        //Draw Resize String
        if (selectedElementResizeOn && mouseIn != null && lastSelectedElement != null && fields.mouseCoordFont != null) {
            g2d.setColor(Color.BLACK);
            Rectangle2D bounds = lastSelectedElement.getBounds2D();
            String mouseCoord = fmt.format(fields.unit.getScale()*bounds.getWidth()) + ", " + fmt.format(fields.unit.getScale()*bounds.getHeight());
            g2d.drawString(mouseCoord, mouseIn.x + DRAG_BOX_SIZE*2*pixScale/(float)zoom, mouseIn.y + DRAG_BOX_SIZE*2*pixScale/(float)zoom);
            
        }
        
        
        //When nothing selected, draw the mouse
        if (mouseIn != null && selectedElements.isEmpty() && mouseIn.x >= 0 && mouseIn.y >= 0) {  
                        
            //Draw crosshair
            if (fields.mouseCursorColor != null) {
                g2d.setColor(fields.mouseCursorColor);
                g2d.setStroke(new BasicStroke(1.0f * pixScale / (float)zoom));
                g2d.drawLine(-fields.origin.x, mouseIn.y, getScaledWidth(), mouseIn.y); //horiz crosshair
                g2d.drawLine(mouseIn.x, -fields.origin.y, mouseIn.x, getScaledHeight()); //vert crosshair
            }

            //Draw mouse position
            if (fields.mouseCoordFont != null) {
                g2d.setColor(Color.BLACK);
                String mouseCoord = fmt.format(fields.unit.getScale()*mouseIn.x/SCALE) + ", " + fmt.format(fields.unit.getScale()*mouseIn.y/SCALE);
                g2d.drawString(mouseCoord, mouseIn.x + (int)Math.ceil(10.0 * pixScale/zoom), mouseIn.y + (int)Math.ceil(fontMetrics.getHeight() + 10.0 * pixScale/zoom));
            }
            
            if (mouseDrag != null) {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1.0f * pixScale / (float)zoom));
                g2d.draw(getDragSelectRectangle());
            }
            
        }
            
        //Paint anything the client is drawing
        if (drawClient != null) {
            drawClient.drawClientPaint(g, mouseIn);
        }
        
    }

 
    
    //Determine the selected object, if any, from the mouse pick.  If the object is selected, the upper left corner (transformed) is returned
    private void selectElement(MouseEvent e) {
        
        Point mouseLoc = getScaledMouse(e);
        
        //Select the pointed object, if there is one
        //See if the mouse click was within the bounds of any component, checking upper objects before moving down the z stack
        Iterator<ZElement> it = fields.zElements.iterator();
        while (it.hasNext()) {
            ZElement o = it.next();
            if (!o.isSelectable()) //don't select anything that's unselectable
                continue;
                          
            Rectangle boundsBox = o.getBounds(SCALE);

            Point lowerRightCorner = new Point(boundsBox.x + boundsBox.width, boundsBox.y + boundsBox.height);
            AffineTransform t = o.getElementTransform(SCALE, false);
            Shape s = t.createTransformedShape(boundsBox);
            Point2D lowerRightTransformed = t.transform(lowerRightCorner, null);  //also transform the lower right corner
      
            if (s.contains(mouseLoc)) {  //see if the mouse point is in the shape
                
                if (!selectedElements.contains(o)) {  //newly selected element
                    
                    if (!shiftPressed)
                        selectedElements.clear();  //no shift, so clear all others
                        
                    selectedElements.add(o);  //add it
                    repaint();
                        
                }
                
                lastSelectedElement = o;
                
                if (contextMenu != null)
                    contextMenu.newSelections(lastSelectedElement, selectedElements);
               
                for (SelectListener l : selectListeners)
                    l.elementSelected(o);
       
                Point location = o.getPosition(SCALE);  //get the upper left, find the mouse offset from the upper left
                selectedObj_xOffset = mouseLoc.x - location.x;
                selectedObj_yOffset = mouseLoc.y - location.y;
    
                //Check if the mouse was within the drag box
                if (o.isResizable() && lowerRightTransformed.distance(mouseLoc)/pixScale < DRAG_BOX_SIZE) { 
                    selectedElementResizeOn = true;
                    selectedResizeElement = o;
                }
                else {
                    selectedElementResizeOn = false;         
                    selectedResizeElement = null;
                }

                return;
                
            }
            

        }
        if (!selectedElements.isEmpty()) {
            for (ZElement s : selectedElements)
                s.deselected();
            passThruElement = null;
        }
        selectedResizeElement = null;
        lastSelectedElement = null;
        for (SelectListener l : selectListeners)
            l.noneSelected();
        lastMethod = null;        
        selectedElements.clear();
    }
    
    
    private void passThroughMouse(MouseEvent e) {
        
        if (passThruElement == null)
            return;
                
        AffineTransform elementTransform = passThruElement.getElementTransform(SCALE, true);
        Point2D transformedMouse = elementTransform.transform(getScaledMouse(e), null);
        Point position = passThruElement.getPosition(SCALE);

        int xOffset = (int)transformedMouse.getX() - position.x;
        int yOffset = (int)transformedMouse.getY() - position.y;
        
        MouseEvent m = new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiers(), xOffset, yOffset, e.getClickCount(), false, e.getButton());
        passThruElement.mouseEvent(this, m);  //tell the element about the mouse                  
        repaint();
    }
    
    
    
    @Override
    public void mouseClicked(MouseEvent e) {
                        
        if (drawClient != null) {
            drawClient.drawClientMouseClicked(getScaledMouse(e), e.getClickCount());
            return;
        }
        
        if (passThruElement != null) {
            passThroughMouse(e);
            return;
        }

        selectElement(e); //check to select an object
                         
        if (e.getClickCount() > 1 && lastSelectedElement != null) {  //Transfer control to the selected element

            if (lastSelectedElement.selected(this))  //tell the element it was selected
                passThruElement = lastSelectedElement;  

            undoStack.saveContext(fields.zElements);

            lastMethod = null;            
        }
       
        selectedElementResizeOn = false;    
                    
    }

    
    public void setCurrentCursor(Cursor c) {
        currentCursor = c;
        changeCursor(currentCursor);
    }
    
    
    private void changeCursor(Cursor cursor) {
        Component c = this;
        while (c != null) { //keep setting all the parents, until we get to the panel
          c.setCursor(cursor);   
          c = c.getParent();
        }
    }
    
    private Point getScaledMouse(MouseEvent e) {
        Point p = e.getPoint();

        p.x *= (pixScale / zoom);
        p.y *= (pixScale / zoom);
        
        p.x -= fields.origin.x / zoom;
        p.y -= fields.origin.y / zoom;
        
        return p;
    }
    
    public int getScaledHeight() {
        return (int)(this.getHeight() * pixScale);
    }
    
    public int getScaledWidth() {
        return (int)(this.getWidth() * pixScale);
    }
     
    
    @Override
    public void mousePressed(MouseEvent e) {
        
        this.requestFocusInWindow();
        
        if (mouseFirstPressed < 0)
            mouseFirstPressed = System.nanoTime();
        
        
        Point mouseLoc = getScaledMouse(e);
        mousePress = mouseLoc;
        
        if (drawClient != null) {
            drawClient.drawClientMousePressed(mouseLoc);
            return;
        }
       
        selectElement(e);  //check to select an object
        
        if (passThruElement != null) { 
            passThroughMouse(e);
            return;
        } 


        if (!selectedElements.isEmpty()) {
            
            setCurrentCursor(Cursor.getDefaultCursor());

            if (e.isPopupTrigger() && contextMenu != null) { 
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
                return;
            }
            
            if (!selectedElementResizeOn) 
                selectedMousePress = new Point2D.Double((mouseLoc.x - selectedObj_xOffset), (mouseLoc.y - selectedObj_yOffset));  


            undoStack.saveContext(fields.zElements);


        } else 
            setCurrentCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
        if (drawClient != null) {
            drawClient.drawClientMouseReleased(getScaledMouse(e));
            return;
        }
        
        if (passThruElement != null) { 
            passThroughMouse(e);
            return;
        }
        
        //If drag-selecting, select all elements falling in the bounds
        if (mousePress != null && mouseDrag != null) {
            
            Rectangle dragSelect = getDragSelectRectangle();
            
            Iterator<ZElement> it = fields.zElements.iterator();
            while (it.hasNext()) {
                ZElement o = it.next();
                if (!o.isSelectable()) //don't select anything that's unselectable
                    continue;
                
                Rectangle boundsBox = o.getBounds(SCALE);
                AffineTransform t = o.getElementTransform(SCALE, false);
                Shape s = t.createTransformedShape(boundsBox);
                
                if (dragSelect.contains(s.getBounds()))
                    selectElement(o);
            }
            
            
        }
        
        selectedMouseDrag = null;
        selectedMousePress = null;
        mousePress = null;
        mouseDrag = null;
        mouseFirstPressed = -1;
        selectedElementResizeOn = false;
        
        repaint();
    }

    @Override
    public synchronized void mouseEntered(MouseEvent e) {
        changeCursor(currentCursor);
        mouseIn = getScaledMouse(e);
        repaint();
    }


    @Override
    public synchronized void mouseExited(MouseEvent e) {
        changeCursor(Cursor.getDefaultCursor());
        mouseIn = null;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
                 
        Point mouseLoc = getScaledMouse(e);
        mouseIn = mouseLoc;

        selectedMouseDrag = null;
        selectedMousePress = null;
 
        if (drawClient != null) {
            drawClient.drawClientMouseDragged(getScaledMouse(e));
            return;
        }
        
        
        if (passThruElement != null) { 
            passThroughMouse(e);
            return;
        }
        
        //If element selected and mouse is within the canvas
        if (!selectedElements.isEmpty() && mouseLoc.x < getScaledWidth() && mouseLoc.y < getScaledHeight())  {      
                
            
            if (!selectedElementResizeOn) { //Reposition the object to the mouse, only when it won't take the object off the component
                
                lastSelectedElement.reposition((mouseLoc.x - selectedObj_xOffset)/SCALE, (mouseLoc.y - selectedObj_yOffset)/SCALE);  
                selectedMouseDrag = new Point2D.Double((mouseLoc.x - selectedObj_xOffset), (mouseLoc.y - selectedObj_yOffset));  
                 
            } else { //Resize the object
                
                
                double theta = Math.toRadians(selectedResizeElement.getRotation());

                Rectangle boundsBox = selectedResizeElement.getBounds(SCALE);
                                
                //Create a transform that will rotate the shape to the coordinates where it lies
                AffineTransform t = AffineTransform.getRotateInstance(theta, boundsBox.x + boundsBox.width/2, boundsBox.y + boundsBox.height/2);

                //Find the upper left corner
                Point2D upperleftCorner = t.transform(new Point(boundsBox.x, boundsBox.y), null);
                 
                //Find the new center, based on the new width and height (from mouse)
                double centerX = upperleftCorner.getX() + ((mouseLoc.getX()-upperleftCorner.getX())/2);
                double centerY = upperleftCorner.getY() + ((mouseLoc.getY()-upperleftCorner.getY())/2);
                
                                
                //Now transform back to zero rotation
                t = AffineTransform.getRotateInstance(-theta, centerX, centerY);

                //Find the new upper left corner
                Point2D newLeftCorner = t.transform(upperleftCorner, null);
                
                //Find the new lower right corner
                Point2D newMouse = t.transform(mouseLoc, null);
                Point2D newLowerRight = new Point2D.Double(newMouse.getX() - newLeftCorner.getX(), newMouse.getY() - newLeftCorner.getY());
                
                     
                
                //Set the new bounds box
                selectedResizeElement.reposition(newLeftCorner.getX()/SCALE, newLeftCorner.getY()/SCALE);
                selectedResizeElement.changeSize(newLowerRight.getX(), newLowerRight.getY(), DRAG_BOX_SIZE, SCALE);
               
            }
                        
            lastMethod = null;                 
        }
        else {  //nothing selected
            mouseDrag = mouseLoc;

        }
        repaint();  //update selected object
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseIn = getScaledMouse(e);
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        
        if (drawClient != null)
            return;
        
        
        if (passThruElement != null) { 
            passThroughMouse(e);
            return;
        } 
        
        if (!selectedElements.isEmpty()) {
            if (System.nanoTime() - mouseWheelLastMoved > 1000000000) {
                undoStack.saveContext(fields.zElements);
            }
            
            for (ZElement selectedElement : selectedElements) {
                if (!shiftPressed) {
                    double increase = e.getPreciseWheelRotation() * SIZE_INCREASE_MULTIPLIER;
                    selectedElement.increaseSize(increase, increase, DRAG_BOX_SIZE, SCALE);
                }
                else {       
                    selectedElement.rotate(e.getPreciseWheelRotation() * ROTATION_MULTIPLIER);
                }
            }
            
            lastMethod = null;            
            mouseWheelLastMoved = System.nanoTime();
            repaint();
        }

    }

    /**
     * For all elements on the canvas, checks to see if there are unsaved changes
     * @return true if there are unsaved changes, false otherwise
     */
    public boolean hasUnsavedChanges() {
                
        if (canvasModified)
            return true;
        
        for (ZElement e : fields.zElements)  //mark all has having been saved
            if (e.hasChanges())
                return true;
        
        return false;
    }
   
    /**
     * Retrieves an array of required classes for storing a ZCanvas using a JAXB context.  Includes required classes plus all element classes for elements
     * that have been added to the canvas
     * @return 
     */
    public Class[] getContextClasses() {
        
        ArrayList<Class<? extends ZElement>> elementTypes = new ArrayList<>();
        for (ZElement e : fields.zElements) {         
            if (!elementTypes.contains(e.getClass()))  //Add this class type to our list of types
                 elementTypes.add(e.getClass());
        }
        
        Class[] contextClasses = new Class[elementTypes.size() + 5]; 
        elementTypes.toArray(contextClasses);

        contextClasses[contextClasses.length-5] = ZGrid.class;        
        contextClasses[contextClasses.length-4] = ZCanvasRuler.class;        
        contextClasses[contextClasses.length-3] = ZAbstractShape.class;        
        contextClasses[contextClasses.length-2] = ZCanvas.CanvasStore.class;
        contextClasses[contextClasses.length-1] = ZElement.class;
        return contextClasses;
    }
    
    /**
     * Retrieves an array of required classes for loading a ZCanvas using a JAXB context.  Includes required classes plus all element classes for elements
     * that are in the file
     * @param f the file to search
     * @return
     * @throws java.io.IOException f cannot be found or read
     * @throws java.lang.ClassNotFoundException if an Element defined in the file has no corresponding subclass of ZElement
     */
    public static Class[] getContextClasses(File f) throws IOException, ClassNotFoundException {
        //First, look through the file to find all the specific Element classes
        int lineNo = 1;
        LinkedList<Class<? extends ZElement>> newElementClasses = new LinkedList<>(); 
        BufferedReader b = new BufferedReader(new FileReader(f));
        while (true) {
            String line = b.readLine();
            if (line == null)
                break;
            line = line.trim();
            if (line.startsWith("<ZElement")) {  //start of an element, the element type
                int i = line.indexOf("class=");
                if (i < 0)
                    throw new IOException("Failed to find \"class\" attribute for ZElement on line " + lineNo);
                String[] split = line.substring(i).split("\"");
                if (split.length != 3)
                    throw new IOException("Malformed \"class\" attribute on line " + lineNo);
                
                String className = split[1];  //second piece, between quotes
                Class c = Class.forName(className);
                newElementClasses.add(c);
            }
            
            lineNo++;
        }
        b.close();
        
  
        Class[] contextClasses = new Class[newElementClasses.size() + 5]; 
        newElementClasses.toArray(contextClasses);
       
        contextClasses[contextClasses.length-5] = ZGrid.class;        
        contextClasses[contextClasses.length-4] = ZCanvasRuler.class;        
        contextClasses[contextClasses.length-3] = ZAbstractShape.class;        
        contextClasses[contextClasses.length-2] = ZCanvas.CanvasStore.class;
        contextClasses[contextClasses.length-1] = ZElement.class;
        
        return contextClasses;
    }
    
    /**
     * Retrieves an object that can be used to store the ZCanvas, in a custom format (if JAXB is not desired). The returned object is
     * serializable and also marshallable with JAXB
     * @return 
     */
    public CanvasStore getCanvasStore() {
        return this.fields;
    }
    
    /**
     * Creates a new ZCanvas from the provided canvas store
     * @param s the store to use
     * @return a restored ZCanvas
     */
    public static ZCanvas fromCanvasStore(CanvasStore s) {

        ZCanvas c = new ZCanvas();
        c.fields = s;
        
        c.canvasModified = false;
        c.init();
        return c;
    }
    
    /**
     * Mark the canvas and its elements has having been saed
     */
    public void markAsSaved() {
        for (ZElement e : fields.zElements)  //mark all has having been saved
            e.wasSaved();
        
        canvasModified = false;
    }
    
    /**
     * Save the canvas to an XML file, and mark all elements as saved
     * @param f the file to write, if f is null, nothing is saved, but the canvas is marked as no longer modified
     * @throws JAXBException 
     */
    public void toFile(File f) throws JAXBException {
               
  
        JAXBContext jaxbContext = JAXBContext.newInstance(getContextClasses());
 
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        if (f != null)
            jaxbMarshaller.marshal(fields, f);
        
        markAsSaved();
        
    }
    
    
    /**
     * Restores a canvas from an XML file
     * @param f the file to read
     * @return the loaded ZCanvas
     * @throws JAXBException on unmarshall error
     * @throws java.io.IOException f cannot be found or read
     * @throws java.lang.ClassNotFoundException if an Element defined in the file has no corresponding subclass of ZElement
     */
    public static ZCanvas fromFile(File f) throws JAXBException, IOException, ClassNotFoundException {
      
        JAXBContext jaxbContext = JAXBContext.newInstance(getContextClasses(f));
 
        Unmarshaller jaxbUnMarshaller = jaxbContext.createUnmarshaller();
 
        ZCanvas c = new ZCanvas();
        c.fields = (ZCanvas.CanvasStore)jaxbUnMarshaller.unmarshal(f);
        
        c.canvasModified = false;
        c.init();
        return c;
    }
    
   
    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) 
            return(NO_SUCH_PAGE);

        resetView();
        selectedElements.clear();

        RepaintManager currentManager = RepaintManager.currentManager(this);
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(-fields.origin.x, -fields.origin.y);
        currentManager.setDoubleBufferingEnabled(false);
        this.paint(g2d);
        currentManager.setDoubleBufferingEnabled(true);
        return(PAGE_EXISTS);
      
    }


    
}
