
package com.github.kkieffer.jzeld.element;


import com.github.kkieffer.jzeld.attributes.TextAttributes;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The ZEditableBoundedText extends the ZEditableText object by defining a text box with the text font size scaled to maximally fit the bounds of the box. The text is editable,
 * and will decrease in font size based on new characters being entered.
 * 
 * Setting the font size will force the new font size and resize the box, but scaling the text box will adjust the font size to fit.
 * 
 * @author kkieffer
 */
@XmlRootElement(name = "ZEditableText")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZEditableBoundedText extends ZEditableText {


    protected ZEditableBoundedText() {}

    
    /**
     * Create the ZEditableBoundedText element
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
     * @param borderStyle the style of the border, which can be null only if the borderWidth is zero
     * @param attr attributes of the text string
     */
    public ZEditableBoundedText(double x, double y, double width, double height, double rotation, boolean selectable, String defaultText, float borderThickness, Color borderColor, Color backgroundColor, StrokeStyle borderStyle, TextAttributes attr) {
        super(x, y, width, height, rotation, selectable, defaultText, borderThickness, borderColor, backgroundColor, borderStyle, attr);   
    }
    
    protected ZEditableBoundedText(ZEditableBoundedText copy, boolean forNew) {
        super(copy, forNew);
    }
    
    @Override
    public ZEditableBoundedText copyOf(boolean forNew) {
        return new ZEditableBoundedText(this, forNew);
    }
    
    
    @Override
    public String getHtmlHelp() {
        
        return "<b>ZEditableText: An editable text box.</b><br><br>Double click on the text box to show the cursor and edit text. The font size of the text " +
                "is automatically sized to fit the text box size. " + TextAttributes.getHtmlHelp() + "<br><br>" + 
                "The text box background color can be set, along with the border color and thickness. A dashed border is not supported.<br><br>" + super.getHtmlHelp();
        
    }

    
    private boolean fits(double w, double h) {  //true of the text widget bounds fit the bounds of this element, false otherwise
        
        
        Dimension d = textWidget.getPreferredSize();  //preferred size is the size that it should be based on the amount of characters     
        if (d.width == 0)  //break out on zero characters
            return true;
        
        double textMinWidth = d.getWidth()/72;
        double textMinHeight = d.getHeight()/72;
        
        return textMinWidth <= w && textMinHeight <= h;
    }
                    
    
    private void validateSize(double w, double h) {
             
        int fontSize = getTextAttributes().font.getSize()-1;
    
        if (fits(w, h)) { //already fits, scale font up to fit
            do {

                fontSize++;
                textAttributes.font = new Font(getTextAttributes().font.getName(), getTextAttributes().font.getStyle(), fontSize);
                textWidget.setFont(textAttributes.font);

            } while (fits(w, h));
            
        }
        
        
        do {  //scale font down to fit
            if (fontSize < 2)
                break;
            
            fontSize--;
            textAttributes.font = new Font(getTextAttributes().font.getName(), getTextAttributes().font.getStyle(), fontSize);
            textWidget.setFont(textAttributes.font);

        } while (!fits(w, h));
            
                      
    }

    
    @Override
    protected void setSize(double w, double h, double minSize, double scale) {
        validateSize(w/scale, h/scale);
        super.setSize(w, h, minSize, scale);
    }

    
    @Override
    public final void setFont(Font f) {  //forces a new size based on the selected font
        this.textAttributes.font = f;
        textWidget.setFont(f);
        Rectangle2D bounds = this.getBounds2D();
        super.setSize(bounds.getWidth(), bounds.getHeight(), 0, 1); //validate size against text
    }
    
}
