
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.element.ZAbstractShape;
import com.github.kkieffer.jzeld.element.ZElement;
import com.github.kkieffer.jzeld.element.ZGroupedElement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author kkieffer
 */
public class ContextClasses {
    
    
    private final Class[] contextClasses;   
    private final String[] unknownClasses;
    
    private ContextClasses(Class[] ctx, String[] unk) {
        contextClasses = ctx;
        unknownClasses = unk;
    };
    
    
    /**
     * Retrieves the context classes
     * @return 
     */
    public Class[] getClasses() {
        return contextClasses;
    }
    
    /**
     * When creating this object from a file, some classes may be unknown. This method retrieves an array of strings naming the unknown classes
     * @return 
     */
    public String[] getUnknownClasses() {
        return unknownClasses;
    }
    
    
    
    private static Class[] addDefaultClasses(List<Class<? extends ZElement>> elementClasses) {
        
        Class<?>[] cvsClasses = ZCanvas.CanvasStore.getContextClasses();
        Class[] contextClasses = new Class[elementClasses.size() + cvsClasses.length + 3]; 
        elementClasses.toArray(contextClasses);

        System.arraycopy(cvsClasses, 0, contextClasses, elementClasses.size(), cvsClasses.length);
         
        contextClasses[contextClasses.length-3] = ZAbstractShape.class;        
        contextClasses[contextClasses.length-2] = ZCanvas.CanvasStore.class;
        contextClasses[contextClasses.length-1] = ZElement.class;
        return contextClasses;
    }
    
    /**
     * Retrieves a ContextClasses object with the required classes for storing a ZCanvas using a JAXB context.  Includes required classes plus all element classes for elements
     * that have been added to the canvas, and for any ZGroupedElements, the classes that it contains
     * @param zElements a list of ZElements on the canvas
     * @return 
     */
    public static ContextClasses getContextClasses(Iterable<ZElement> zElements) {
        
        ArrayList<Class<? extends ZElement>> elementTypes = new ArrayList<>();
        
        for (ZElement e : zElements) {         
            if (!elementTypes.contains(e.getClass()))  //Add this class type to our list of types
                 elementTypes.add(e.getClass());
            
            if (e instanceof ZGroupedElement)   //find classes contained within group
                ((ZGroupedElement)e).addGroupedClasses(elementTypes);  
            
        }
                    
        Class[] ctx = addDefaultClasses(elementTypes);  //add all our known default classes

        return new ContextClasses(ctx, new String[]{});  //create with classes and nothing unknown
       
    }
    
    /**
     * Constructs a ContextClasses object with the required classes for loading a ZCanvas using a JAXB context.  Includes required classes plus all element classes for elements
     * that are in the file
     * @param f the file to search
     * @return a ContextClasses object
     * @throws java.io.IOException f cannot be found or read
     */
    public static ContextClasses getContextClasses(File f) throws IOException {
        
        //First, look through the file to find all the specific Element classes
        int lineNo = 1;
        LinkedList<Class<? extends ZElement>> newElementClasses = new LinkedList<>(); 
        LinkedList<String> unknownClasses = new LinkedList<>();
        
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
                Class c;
                try {
                    c = Class.forName(className);
                    newElementClasses.add(c);
                } catch (ClassNotFoundException ex) {
                    unknownClasses.add(className);
                }
            }
            
            lineNo++;
        }
        b.close();
        
  
        Class[] ctx = addDefaultClasses(newElementClasses);  //add all our known default classes
        String[] unknown = new String[unknownClasses.size()];
        unknownClasses.toArray(unknown);
        
        return new ContextClasses(ctx, unknown);

    }
    
    
    
    
    
}
