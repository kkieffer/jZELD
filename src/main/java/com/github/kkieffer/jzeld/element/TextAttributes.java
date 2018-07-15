
package com.github.kkieffer.jzeld.element;

import com.github.kkieffer.jzeld.JAXBAdapter;
import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlRootElement(name = "TextAttributes")
@XmlAccessorType(XmlAccessType.FIELD)
public class TextAttributes implements Serializable {
    
    public enum HorizontalJustify {LEFT, CENTER, RIGHT};
    
    
    public interface TextInterface {
        
        public TextAttributes getTextAttributes();
        public void changed();
        
        default void setFontSize(int size) {
            Font f = new Font(getTextAttributes().font.getName(), getTextAttributes().font.getStyle(), size);
            setFont(f);
            changed();
        }
        
        
        default void setFont(Font f) {
            getTextAttributes().font = f;
            changed();
        }

        default void setFontStyle(int style) {
            Font f = new Font(getTextAttributes().font.getName(), style, getTextAttributes().font.getSize());
            setFont(f);
            changed();
        }
    
   
        default void setFontName(String name) {
            Font f = new Font(name, getTextAttributes().font.getStyle(), getTextAttributes().font.getSize());
            setFont(f);
            changed();
        }
    
    
        default void setFontColor(Color c) {
            getTextAttributes().fontColor = c;
            changed();
        }
    
        default void setTextJustify(HorizontalJustify j) {
            getTextAttributes().hj = j;
            changed();
        }
    
        /**
         * Change the text attributes for this element
         * @param attr the new attributes
         */
        default void setTextAttributes(TextAttributes attr) {
            setFont(attr.font);
            setFontColor(attr.fontColor);
            setTextJustify(attr.hj);
        }
    
    }
    


    @XmlElement(name = "justify")        
    public HorizontalJustify hj;

    @XmlJavaTypeAdapter(JAXBAdapter.FontAdapter.class)
    public Font font;

    @XmlJavaTypeAdapter(JAXBAdapter.ColorAdapter.class)
    public Color fontColor;


    public TextAttributes(HorizontalJustify _hj, Font _font, Color fontColor) {
        hj = _hj;
        font = _font;
        this.fontColor = fontColor;
    }

    public TextAttributes() {}

    public static String getHtmlHelp() {
        return "This element supports text attributes. The font size, style, and color are adjustable.  The text can be justified left aligned, centered, or right aligned.";
    }
    
}
