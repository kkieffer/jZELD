
package com.github.kkieffer.jzeld.attributes;

import java.awt.Shape;

/**
 *
 * @author kevin
 */
public interface Clippable {
    public void setClippingShape(Shape s);
    public Shape getClippingShape();
    public boolean hasClip();
}
