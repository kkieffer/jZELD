
package com.github.kkieffer.jzeld.element;


import com.github.kkieffer.jzeld.adapters.JAXBAdapter.ColorAdapter;
import com.github.kkieffer.jzeld.ZCanvas;
import com.github.kkieffer.jzeld.element.TextAttributes.HorizontalJustify;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The ZEditableText object defines a bounded text box where the reference position is in the upper left corner box. The text is editable,
 * and will increase size based on new characters being entered. It won't automatically decrease size but can be manually resized as small as the
 * allowable text.
 * 
 * If selectable is true, double clicking this element will switch to the edit crosshair and allow text entry. If selectable is false,
 * the text cannot be edited except through the methods of this class.
 * 
 * The ZEditableText supports an outline and a fill color, but not a dash pattern.  The text itself has independent attributes of font, font color,
 * and horizontal justification.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZEditableText")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZEditableText extends ZElement implements TextAttributes.TextInterface {


    /* ----- JAXB FIELDS ---------*/
    protected TextAttributes textAttributes;
    
    @XmlJavaTypeAdapter(ColorAdapter.class)
    protected Color backgroundColor;
    
    @XmlJavaTypeAdapter(ColorAdapter.class)
    protected Color borderColor;
    
    protected float borderThickness;
    
    @XmlJavaTypeAdapter(JTextPaneAdapter.class)
    @XmlElement(name = "Text")
    protected JTextPane textWidget;
    
    public static class JTextPaneAdapter extends XmlAdapter<String, JTextPane> {

        @Override
        public String marshal(JTextPane p) throws Exception {
            return p.getText();
        }

        @Override
        public JTextPane unmarshal(String v) throws Exception {
            JTextPane pane = new JTextPane();
            pane.setText(v);
            return pane;
        }

     }
    
    
    /*---------Transient Fields --------------------*/
    
            
    transient private ZCanvas canvas;

    transient private JPanel textPanel;
    
    transient private boolean isSelected;
    
    transient private Timer timer;
    

    private void setup() {
        
        textWidget.addKeyListener(new KeyListener() {  //pass through key presses to repaint because the canvas won't do it for us
            @Override
            public void keyTyped(KeyEvent e) {
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                textWidget.repaint();
                textWidget.getParent().repaint();
                if (canvas != null)
                    canvas.repaint();
                changed();

        }

            @Override
            public void keyReleased(KeyEvent e) {
                validateSize();                    
            }
        });
                
        textWidget.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                SwingUtilities.invokeLater(new Runnable() {  //Note: The caretUpdate method is not guaranteed to be called in the event-dispatching thread.
                    @Override
                    public void run() {
                        changed();
                        validateSize();                    
                    }
                });
                
            }
            
        });
        
        
        textPanel.add(textWidget);
        textPanel.setFocusTraversalKeysEnabled(false);
        textWidget.setCaretColor(textAttributes.fontColor);

        timer = new Timer(20, new ActionListener() {  //needed to flash the caret
            @Override
            public void actionPerformed(ActionEvent e) {
                textWidget.getParent().repaint();
            }
        });
    }
    
    protected ZEditableText() {}

    protected void afterUnmarshal(Unmarshaller u, Object parent) {
        textPanel = new JPanel();
        setTextAttributes(textAttributes);
        setAttributes(borderThickness, borderColor, null, backgroundColor);
        setup();
    }
    
    //Custom deserialize
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        afterUnmarshal(null, null);
    }
    
    /**
     * Create the ZEditableText element
     * @param x the x coordinate, upper left x, in units
     * @param y the y coordinate, upper left y, in units
     * @param width the width of the object in units, use 0 to size to the text string
     * @param height the height of the object in units, use 0 to size to the text string
     * @param rotation desired rotation of the component in degrees, clockwise
     * @param selectable if the object can be selected by the ZCanvas mouse click
     * @param defaultText the initial text string
     * @param borderThickness unit width of the border, use zero for no border
     * @param borderColor color of the border, which can be null only if the borderWidth is zero
     * @param backgroundColor color of the rectangle area, which can be null for transparent (but not in combination with a zero width border)
     * @param attr attributes of the text string
     */
    public ZEditableText(double x, double y, double width, double height, double rotation, boolean selectable, String defaultText, float borderThickness, Color borderColor, Color backgroundColor, TextAttributes attr) {
        super(x, y, width, height, rotation, selectable, selectable, selectable);  //unknown bounds, height and width until we have a graphics context
               
        
        textPanel = new JPanel();
        textWidget = new JTextPane();
        
        textAttributes = new TextAttributes();
        setTextAttributes(attr);
        modifyText(defaultText);
        setAttributes(borderThickness, borderColor, null, backgroundColor);

        setup();
        SwingUtilities.invokeLater(new Runnable() { public void run() {validateSize();}});
    
    }
    
    protected ZEditableText(ZEditableText copy, boolean forNew) {
        super(copy, forNew);
        
        textPanel = new JPanel();
        textWidget = new JTextPane();
        
        textAttributes = new TextAttributes();
        setTextAttributes(copy.textAttributes);
        modifyText(copy.textWidget.getText());
        setAttributes(copy.borderThickness, copy.borderColor, null, copy.backgroundColor);

        setup();

    }
    
    @Override
    public ZEditableText copyOf(boolean forNew) {
        return new ZEditableText(this, forNew);
    }
    
    
    @Override
    public String getHtmlHelp() {
        
        return "<b>ZEditableText: An editable text box.</b><br><br>Double click on the text box to show the cursor and edit text. If the size of the text box " +
                "is too small to hold the text, it will be resized to fit. " + TextAttributes.getHtmlHelp() + "<br><br>" + 
                "The text box background color can be set, along with the border color and thickness. A dashed border is not supported.<br><br>" + super.getHtmlHelp();
        
    }

    
    @Override
    public void setAttributes(float outlineWidth, Color outlineColor, Float[] dashPattern, Color fillColor) {
        setFillColor(fillColor);
        setOutlineWidth(outlineWidth);
        setDashPattern(dashPattern);
        setOutlineColor(outlineColor);  
        changed();
    }
    
    @Override
    public void setOutlineWidth(float width) {
        borderThickness = width; 
        validateSize();        
        changed();
    }
  
    @Override
    public float getOutlineWidth() {
        return borderThickness;
    }
    
    @Override
    public void setDashPattern(Float[] dashPattern) {
    }
    
    @Override
    public Float[] getDashPattern() {
        return null;
    }

    @Override
    public void setOutlineColor(Color outlineColor) {
        this.borderColor = outlineColor;
        changed();
    }
    
    @Override
    public Color getOutlineColor() {
        return borderColor;
    }
    
    @Override
    public Color getFillColor() {
        return backgroundColor;
    }
    
    @Override
    public void setFillColor(Color fillColor) {
        backgroundColor = fillColor;
        
        /*Nimbus background color bug*/
        UIDefaults defaults = new UIDefaults();
        defaults.put("TextPane[Enabled].backgroundPainter", backgroundColor);
        textWidget.putClientProperty("Nimbus.Overrides", defaults);
        textWidget.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        
        //Set background color
        textWidget.setBackground(backgroundColor);
        if (backgroundColor == null)
            textWidget.setBackground(new Color(0,0,0,0)); //transparent
        
        changed();
        
    }
    
    @Override
    public void removeFill() {
        setFillColor(null);
    }
    
    
    @Override
    public boolean hasOutline() {
        return true;
    }

    @Override
    public boolean hasFill() {
        return true;
    }

    @Override
    public boolean hasDash() {
        return false;
    }
    
    @Override
    public void addedTo(ZCanvas c) {
        c.add(textPanel);
        canvas = c;
    }
    
    
    @Override
    public void removedFrom(ZCanvas c) {
        c.remove(textPanel);
        canvas = null;
    }
    
    
    /**
     * Change the text of this element
     * @param t the new text
     */
    public final void modifyText(String t) {
        textWidget.setText(t);
        validateSize();        
        changed();
    }
    
    /**
     * Get all the text from this element
     * @return all the text
     */
    public String getText() {
        return textWidget.getText();
    }
    
    /**
     * Replace any selected text, or if nothing is selected, insert at the caret position
     * @param t the text to replace with, or insert
     * @return true if it was inserted (the element is currently being edited), false otherwise
     */
    public boolean insertText(String t) {
        if (!isEditing())
            return false;
        
        textWidget.replaceSelection(t);
        validateSize();        
        changed();
        return true;
    }
    
    /**
     * Retrieve the selected text, if nothing is selected, return null
     * @return the selected text, or null
     */
    public String getSelectedText() {
        if (!isEditing())
            return null;
        return textWidget.getSelectedText();
    }
    
    @Override
    public final void setFont(Font f) {
        this.textAttributes.font = f;
        textWidget.setFont(f);
        validateSize();        
        changed();
    }

    @Override
    public final void setFontColor(Color c) {
        this.textAttributes.fontColor = c;
        textWidget.setForeground(c);  
        textWidget.setCaretColor(c);
        changed();
    }
    
    
    @Override
    public final void setTextJustify(HorizontalJustify j) {
        this.textAttributes.hj = j;
        StyledDocument doc = textWidget.getStyledDocument();
        SimpleAttributeSet a = new SimpleAttributeSet();
        switch (j) {
            case LEFT:
                StyleConstants.setAlignment(a, StyleConstants.ALIGN_LEFT);
                break;
            case CENTER:
                StyleConstants.setAlignment(a, StyleConstants.ALIGN_CENTER);
                break;
            case RIGHT:
                StyleConstants.setAlignment(a, StyleConstants.ALIGN_RIGHT);
                break;
        }
         
        doc.setParagraphAttributes(0, doc.getLength(), a, false);

        changed();
    }
    
   
    
    
    /**
     * Returns the element's text attributes
     * @return 
     */
    @Override
    public final TextAttributes getTextAttributes() {
        return textAttributes; 
    }
    
    
     @Override
    public boolean supportsFlip() {
        return false;
    }
    
    @Override
    public boolean supportsEdit() {
        return true;
    };
 
    
    @Override
    public Rectangle2D getMarginBounds(double scale) {  //margins just take into account half the line width
        Rectangle2D bounds = getBounds2D(scale);
        double ow = (getOutlineWidth()/2.0)/72.0 * scale;  //half the line width
        return new Rectangle2D.Double(-ow, -ow, bounds.getWidth() + ow, bounds.getWidth() + ow);
        
    }
    
    /**
     * Determine if the user is actively editing this text (has focus and caret flashing)
     * @return true if the text is being edited, false otherwise
     */
    public boolean isEditing() {
        return isSelected;
    }
    
    @Override
    public boolean selectedForEdit(ZCanvas canvas) {
        isSelected = true;
        canvas.setCurrentCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)); 
        textWidget.requestFocusInWindow();  //show the caret, receive key presses
        timer.start(); 
        return true;
    }
    
    @Override
    public void deselectedForEdit() {
        isSelected = false;
        textWidget.setSelectionStart(0);
        textWidget.setSelectionEnd(0);
        textWidget.getParent().requestFocusInWindow(); //give away focus to remove caret
        timer.stop();
        validateSize();
    }
    
    
    @Override
    public boolean mouseEvent(ZCanvas canvas, MouseEvent e) {
        if (!isSelected) 
            return false;
        
        MouseEvent m = new MouseEvent(textWidget, e.getID(), e.getWhen(), e.getModifiers(), e.getX() , e.getY(), e.getClickCount(), false, e.getButton());
        textWidget.dispatchEvent(m); //send mouse events to the text widget
        canvas.setCurrentCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)); 
        textWidget.getParent().repaint();

        Rectangle2D bounds = getBounds2D(canvas.getScale());
        Point2D mousePress = new Point2D.Double(e.getX(), e.getY());

        //If outside the element, don't swallow event, it will be deselected
        if (mousePress.getX() < 0 || mousePress.getX() > bounds.getWidth() || mousePress.getY() < 0 || mousePress.getY() > bounds.getHeight())
            return false;

        return true;  //swallow the mouse event, don't pass it to the canvas
       
    }
        
   
    
    
    /**
     * Checks to see if the text string will fit in the bounds of the element without resizing it
     * @param text the test string
     * @param font the font to try, if null, use the currently set font attribute
     * @param scale the canvas scale
     * @return true if the text will fit, false otherwise
     */
    public boolean checkTextFit(String text, Font font, double scale) {
                
        JTextPane testPane = new JTextPane();
        testPane.setFont(font == null ? textAttributes.font : font);
        testPane.setText(text);

        Dimension d = testPane.getPreferredSize();  //preferred size is the size that it should be based on the amount of characters
        Rectangle2D b = super.getBounds2D(scale);
        
        if (d.width > b.getWidth() || d.height > b.getHeight())
            return false;
        else
            return true;
    }

    private void validateSize() {
       
        Rectangle2D bounds = this.getBounds2D();
        setSize(bounds.getWidth(), bounds.getHeight(), 0, 1); //validate size against text

    }
    
    
    @Override
    protected void setSize(double w, double h, double minSize, double scale) {
             
        Dimension d = textWidget.getPreferredSize();  //preferred size is the size that it should be based on the amount of characters
        
        double textMinWidth = d.getWidth() * scale/72;
        double textMinHeight = d.getHeight() * scale/72;
        
        //Scale up to fit the characters
        if (textMinWidth > w) 
            w = textMinWidth;
              
        if (textMinHeight > h) 
            h = textMinHeight;
  
        super.setSize(w, h, minSize, scale);        
    }
       
    
    
    @Override
    public void paint(Graphics2D g, double unitSize, double width, double height) {
              
         if (!isVisible())
            return;
         
        textWidget.setBorder(BorderFactory.createLineBorder(this.borderColor, (int)borderThickness));

        textWidget.setSize(new Dimension((int)width, (int)height));

        textWidget.paint(g);  //paint the widget
            
    }
    

    
}
