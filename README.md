# jZELD - Java Z-plane Element Layout and Drawing 

jZELD is a Java Swing Framework for layout and emplacement of various ZElements (drawn shapes and other more complex items) on a 
canvas (the ZCanvas) that supports a z-plane layer.  ZElements can placed in the z-plane so they overlap and/or are overlapped by other ZElements.
The framework can be user-interactive and supports changing position, size, z-plane order, and other attributes of ZElements. 
Furthermore, new elements can be defined by a user creating drawings on the canvas. The canvas can be printed, saved to an image, or written to a file.

There are three main components to the jZELD framework described below, and other supporting classes documented in the source code.

## ZCanvas

The ZCanvas (a JComponent) is the presentation layer for ZElements.  It holds a list of these ZElements in z-plane order and renders them on
the paintable area. The user interacts with the ZCanvas with the mouse and keyboard, manipulating ZElements and drawing new ones. An easy way
to become familiar with the operations of a ZCanvas is to run the included Demo class. The core operations that are supported in the canvas are:

1.  Select a ZElement with a mouse click (shift-Click to select multiple ZElements)
2.  Select multiple ZElements by dragging a selection box around them
3.  Drag the ZElement to a new position with the mouse
4.  Drag the ZElement's resize box (bottom-right corner of the element) to resize it
5.  Resize selected ZElements using the mouse wheel
6.  Move the selected ZElements with the arrow keys while holding Shift down
7.  Rotate selected ZElements using the mouse wheel while holding Shift down
8.  Shear selected ZElements in the x-direction using the mouse wheel while holding ALT-S down
9.  Shear selected ZElements in the y-direction using the mouse wheel while holding ALT-SHIFT-S down
10.  Zoom in/out the ZCanvas with the +/- keys

The ZCanvas supports other operations that can be invoked by shortcut keys, menu items, or programmatically.  The 
Demo illustrates these using keys and right-click context menus.  The framework provides a default set of hotkeys and an optional
default context menu.  The context menu is invoked by right-clicking on a ZElement. The context menu can be replaced, and additional hotkeys added, as
illustrated in the demo.

Operations supported by the ZCanvas are below.  All operations are available in the default context menu, and if there is a hotkey that invokes it as
well it is defined in parenthesis.  For MacOS, substitute Command instead of Ctrl below.

1.  Copy selected ZElements  (Ctrl-C)
2.  Paste copied ZElements   (Ctrl-V)
3.  Delete selected ZElements  (Delete or Backspace)
4.  Repeat the last operation (not all operations can be repeated)  (Ctrl-Y)
5.  Undo the previous operation (a undo-stack is supported for multiple undo, the stack size is user defined)  (Ctrl-Z)
6.  Group selected ZElements into a single ZElement  (Ctrl-G)
7.  Ungroup a grouped-ZElement  (Ctrl-U)
8.  Snap the ZElement rotation to the nearest 90 degrees (CW or CCW)
9.  Control the Z-plane order of selected ZElements
10. Flip the ZElement vertical or horizontal (not all ZElements support this)
11. Specify the ZElement line width, line color, dash pattern, and fill color (if ZElement supports)
12. Aligning multiple ZElements to each other

The ZCanvas can be printed to a printer, saved as an image, or saved to a file.  The native file format uses an XML format (using JAXB). The ZCanvas can
later be restored from this file. The ZCanvas can also be exported to a SVG file.

## ZElements

A ZElement is an abstract superclass for items that can be drawn on the canvas.  ZElements all define a position on the canvas and size (boundary box that defines
the range of where the ZElement is painted), as well as a rotation. Three units are defined for position and sizes: inches, centimeters, and pixels.  Rotation is in
degrees.  

Optional items that can subclasses can allow are the ability to select, resize, and flip the ZElement, a line width, color, and dash-pattern, and fill color, gradient, or pattern.  
ZElements can receive mouseEvents passed to them by the ZCanvas, as well as notifications when they are selected, deselected, added to, or removed from a ZCanvas. 
Double-clicking a ZElement forwards mouseEvents to the ZElement, and clicking outside the ZElement stops the forwarding of mouse events.  All ZElements must also 
support a copy operation.

Several ZElements are provided by this package, including simple shapes such as rectangles, rounded-rectangles, ovals, lines, triangles, images, and text boxes, and generic
shapes. 

Two final ZElements are provided for use by the ZCanvas - rulers for the top and left edges of the canvas and gridlines for the canvas.  
 

## DrawClient

DrawClients provide implementations for user-drawing on the ZCanvas with the mouse. When a draw operation is invoked on the ZCanvas, mouse events are passed to the specified
DrawClient which constructs a shape from the mouse positions.  When drawing has concluded, the drawn shape can be added to the ZCanvas.  While drawing, the ZCanvas can paint
the partially drawn shape on the canvas.

The BoundaryDraw abstract superclass is provided to provide methods for drawing the outline of a shape, creating a ZShape from the drawing, and adding it to the ZCanvas.  Two
DrawClients that subclass BoundaryDraw are provided.

1.  FreeformDraw draws a continous path while the mouse is held down.  When released, a line is drawn from the last mouse point to the first mouse point to complete the shape.
2.  StraightLineDraw draws straight lines between each mouse click.  Double-clicking the mouse draws a line from the last mouse point to the first mouse point and completes the shape.
3.  OrthogonalLineDraw draws straight lines at 90 degree angles with each mouse click.  Double clicking closes the shape with right angle lines.

In both of the cases below, when the draw is complete the ZShape is added to the ZCanvas.  It can then be modified by filling, changing color, etc.

## Demo

The Demo class illustrates all features that are available in jZELD. In addition to all the default hotkeys and default context menu described above, it provides a set of 
additional hotkeys. 

1.  Send an image of the ZCanvas to the printer  (Ctrl-P)
2.  Save the ZCanvas to an XML file (Ctrl-S)
3.  Create a freeform drawing (Ctrl-F)
4.  Create a straight-line drawing (Ctrl-D) and close, or (Ctrl-L) not to close
5.  Create a orthogonal-line drawing (Ctrl-O)
6.  Copy the selected element as an image to a png file (Ctrl-E)
7.  Export to an svg file (Ctrl-G)

When starting the demo, if a previous instance was saved with Ctrl-S, it can be loaded.  If the user chooses not to load, a default ZCanvas is loaded with a collection
of demo ZElements.  The default Demo is shown below.

![Demo Screenshot](https://github.com/kkieffer/jZELD/blob/master/demo.jpg "Demo Screenshot")


## Getting Started

Build and Run using Maven:  "mvn package"
Navigate to the "target" directory

Run: java -cp classes:test-classes com.github.kkieffer.jzeld.Demo

## Dependencies

Java JRE 1.8 is required.  Other dependencies are provided in the pom.xml file:

The [filters library from jhlabs](http://www.jhlabs.com/ip/filters/download.html) which is licensed by the [Apache license](https://www.apache.org/licenses/LICENSE-2.0.html)
The [batik library from Apache](https://xmlgraphics.apache.org/batik/) which is licensed by the [Apache license](https://www.apache.org/licenses/LICENSE-2.0.html)

The source code also contains an [extension to the batik library](https://gist.github.com/msteiger/4509119) as modified by Marco Hutter (see comments on github page) 

## Extending

There are many ways to use jZELD, either as a standalone application or embedded in another Java project. The Demo illustrates a basic standalone application.  The ZCanvas
could be embedded in another JPanel that has a toolbar or menus for invoking various features. Further, ZElement or its subclasses can be further subclassed to define more
complex shapes and items.

Here are some ideas for using jZELD in a larger application:

1. Signature panel, to capture a user signature (freeform draw).  Call the ZElement "paint" method to create an image of the element, or the "print" method of ZCanvas to capture an image of the canvas.
2. Label maker. Create mailing labels from an address book, with a company image.  Programmatically set the ZEditableText element and send the ZCanvas to the printer.
3. Scrapbook application.  Layout photos using ZImage, rotate them, overlap them, add text.  Use ZRectangle to create shadows, borders, etc.  Create a multi-page book with an
	ArrayList of ZCanvas sheets.  
4. Create a Java version of Powerpoint! 


## License

This project is licensed under the LGPL License - see the [LICENSE.md](LICENSE.md) file for details.







