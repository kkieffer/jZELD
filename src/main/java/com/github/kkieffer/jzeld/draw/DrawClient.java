
package com.github.kkieffer.jzeld.draw;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * A DrawClient is an object that can draw on the ZCanvas to create new elements. The draw client is passed to the ZCanvas's drawOn() 
 * method, and it stops drawing by calling the ZCanvas's drawOff() method. The ZCanvas calls the various methods on the client for
 * mouse activities and to repaint whatever it is drawing.
 * 
 * 
 * @author kkieffer
 */
public interface DrawClient {

 
    
     /**
      * Stop drawing
      */
     public void drawStop();
    
    /**
     * The ZCanvas is repainting, so repaint the shape the client is drawing.
     * @param g the graphics context to paint on
     * @param currentMouse the current location of the mouse
     */
    public void drawClientPaint(Graphics g, Point2D currentMouse);

    /**
     * The mouse was clicked on the canvas.
     * @param mouse mouse location on the canvas, taking into account the zoom and origin offset
     * @param e the original mouse event
     */
    public void drawClientMouseClicked(Point2D mouse, MouseEvent e);

    /**
     * The mouse was pressed down on the canvas
     * @param mouse mouse location on the canvas, taking into account the zoom and origin offset
     * @param e the original mouse event
     */
    public void drawClientMousePressed(Point2D mouse, MouseEvent e);

    /**
     * The mouse was released on the canvas
     * @param mouse mouse location on the canvas, taking into account the zoom and origin offset
     * @param e the original mouse event
     */
    public void drawClientMouseReleased(Point2D mouse, MouseEvent e);

    /**
     * The mouse was dragged on the canvas
     * @param mouse mouse location on the canvas, taking into account the zoom and origin offset
     * @param e the original mouse event
     */
    public void drawClientMouseDragged(Point2D mouse, MouseEvent e);
    
}
