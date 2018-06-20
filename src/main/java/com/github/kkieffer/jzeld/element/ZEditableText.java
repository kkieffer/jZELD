
package com.github.kkieffer.jzeld.element;


import com.github.kkieffer.jzeld.JAXBAdapter.ColorAdapter;
import com.github.kkieffer.jzeld.JAXBAdapter.FontAdapter;
import com.github.kkieffer.jzeld.ZCanvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
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
public class ZEditableText extends ZElement {

    
    public enum HorizontalJustify {LEFT, CENTER, RIGHT};
    
    /**
     * Specifies text attributes for drawing
     */
    @XmlRootElement(name = "TextAttributes")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TextAttributes {

        @XmlElement(name = "justify")        
        public HorizontalJustify hj;

        @XmlJavaTypeAdapter(FontAdapter.class)
        public Font font;
    
        @XmlJavaTypeAdapter(ColorAdapter.class)
        public Color fontColor;
        
        
        public TextAttributes(HorizontalJustify _hj, Font _font, Color fontColor) {
            hj = _hj;
            font = _font;
            this.fontColor = fontColor;
        }
        
        protected TextAttributes() {}
        
        protected TextAttributes(TextAttributes source) {
            hj = source.hj;
            font = new Font(source.font.getName(), source.font.getStyle(), source.font.getSize());
            fontColor = source.fontColor;
        }
    }

    @XmlTransient
    private ZCanvas canvas;


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
    
    
    /*-----------------------------*/
    
    @XmlTransient
    private JPanel textPanel;
    
    @XmlTransient
    private boolean isSelected;
    
    @XmlTransient
    private Timer timer;
    

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
                hasChanges = true;
        }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
                
        
        textPanel.add(textWidget);
        
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
        hasChanges = false;
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
        super(x, y, width, height, rotation, selectable, selectable);  //unknown bounds, height and width until we have a graphics context
               
        
        textPanel = new JPanel();
        textWidget = new JTextPane();
        
        setTextAttributes(attr);
        modifyText(defaultText);
        setAttributes(borderThickness, borderColor, null, backgroundColor);

        setup();
        hasChanges = false;

    }
    
    protected ZEditableText(ZEditableText copy) {
        super(copy);
        
        textPanel = new JPanel();
        textWidget = new JTextPane();
        
        setTextAttributes(copy.textAttributes);
        modifyText(copy.textWidget.getText());
        setAttributes(copy.borderThickness, copy.borderColor, null, copy.backgroundColor);

        setup();
        hasChanges = false;

    }
    
     @Override
    public ZEditableText copyOf() {
        return new ZEditableText(this);
    }
    
    
    @Override
    public void setAttributes(float outlineWidth, Color outlineColor, Float[] dashPattern, Color fillColor) {
        setFillColor(fillColor);
        setOutlineWidth(outlineWidth);
        setDashPattern(dashPattern);
        setOutlineColor(outlineColor);  
        hasChanges = true;
    }
    
    @Override
    public void setOutlineWidth(float width) {
        borderThickness = width; 
        hasChanges = true;
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
        hasChanges = true;
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
        
        hasChanges = true;
        
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
        hasChanges = true;
    }
    
    
    public final void setFont(Font f) {
        this.textAttributes.font = f;
        textWidget.setFont(f);
        hasChanges = true;
    }

    public final void setFontStyle(int style) {
        Font f = new Font(textAttributes.font.getName(), style, textAttributes.font.getSize());
        this.textAttributes.font = f;
        hasChanges = true;
    }
    
    public final void setFontSize(int size) {
        Font f = new Font(textAttributes.font.getName(), textAttributes.font.getStyle(), size);
        this.textAttributes.font = f;
        hasChanges = true;
    }
    
    public final void setFontName(String name) {
        Font f = new Font(name, textAttributes.font.getStyle(), textAttributes.font.getSize());
        this.textAttributes.font = f;
        hasChanges = true;
    }
    
    
    public final void setFontColor(Color c) {
        this.textAttributes.fontColor = c;
        textWidget.setForeground(c);     
        hasChanges = true;
    }
    
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

        hasChanges = true;
    }
    
    /**
     * Change the text attributes for this element
     * @param attr the new attributes
     */
    public final void setTextAttributes(TextAttributes attr) {
                
        this.textAttributes = attr;
        setFont(attr.font);
        setFontColor(attr.fontColor);
        setTextJustify(attr.hj);
    }
    
    
    /**
     * Returns a copy of the element's text attributes
     * @return 
     */
    public final TextAttributes getTextAttributes() {
        return new TextAttributes(this.textAttributes); //copy
    }
    
    
     @Override
    public boolean supportsFlip() {
        return false;
    }
    
    
    @Override
    public boolean selected(ZCanvas canvas) {
        isSelected = true;
        canvas.setCurrentCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)); 
        textWidget.requestFocusInWindow();  //show the caret, receive key presses
        timer.start(); 
        return true;
    }
    
    @Override
    public void deselected() {
        isSelected = false;
        textWidget.getParent().requestFocusInWindow(); //give away focus to remove caret
        timer.stop();
    }
    
    
    @Override
    public void mouseEvent(ZCanvas canvas, MouseEvent e) {
        if (isSelected) {
            MouseEvent m = new MouseEvent(textWidget, e.getID(), e.getWhen(), e.getModifiers(), e.getX() , e.getY(), e.getClickCount(), false, e.getButton());
            textWidget.dispatchEvent(m); //send mouse events to the text widget
            canvas.setCurrentCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)); 
            textWidget.getParent().repaint();
        }
    }
        
   
    
    @Override
    public Rectangle2D getBounds2D(double scale) {
        Rectangle r = getBounds(scale);
        
        return new Rectangle2D.Double(r.x, r.y, r.width, r.height);
        
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
        Rectangle b = super.getBounds(scale);
        
        if (d.width > b.getWidth() || d.height > b.getHeight())
            return false;
        else
            return true;
    }

    @Override
    public Rectangle getBounds(double scale) {
        
        Dimension d = textWidget.getPreferredSize();  //preferred size is the size that it should be based on the amount of characters
        Rectangle b = super.getBounds(scale);
        
        //Scale up to fit the characters
        if (d.width > b.getWidth())
            b.width = d.width;
        
        if (d.height > b.getHeight())
            b.height = d.height;
        
        
        super.setSize(b.width, b.height, 1, scale); 
        
        //Adjust the font size by the scaling factor to maintain consistency
        Font f = new Font(textAttributes.font.getFontName(), textAttributes.font.getStyle(), (int)(textAttributes.font.getSize2D()*scale/72.0));
        textWidget.setFont(f);
        
        return super.getBounds(scale);
    }
       
    
    
    @Override
    public void paint(Graphics2D g, int unitSize, int width, int height) {
              
 
        textWidget.setBorder(BorderFactory.createLineBorder(this.borderColor, (int)borderThickness));

        textWidget.setSize(new Dimension(width, height));

        textWidget.paint(g);  //paint the widget
            
    }
    

    
}
