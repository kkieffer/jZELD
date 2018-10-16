
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.ZCanvas.CanvasStore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Demonstration of saving a canvas with JAXB.  Note that in addition to the context classes retrieved from the canvas,
 * the CircleStrokeExample class was added as well to implement a custom stroke.
 * @author kkieffer
 */
public class CanvasSave {
    
    //Get classes to be saved from the canvas
    private static Class[] getContextClasses(ZCanvas c) {
        
        ContextClasses cc = c.getContextClasses();
        
        ArrayList<Class> contextClasses = new ArrayList<>();
        for (Class cl : cc.getClasses()) {
            if (!contextClasses.contains(cl))
                contextClasses.add(cl);    
        }
      
        contextClasses.add(CircleStrokeExample.class);       
        
        Class[] ary = new Class[contextClasses.size()];
        contextClasses.toArray(ary);
        return ary;
    }
    
    
    /**
     * Save a canvas to an XML file, and mark all elements as saved
     * @param c the canvas to save
     * @param f the file to write, if f is null, nothing is saved, but the canvas is marked as no longer modified
     * @throws JAXBException 
     */
    public static void toFile(ZCanvas c, File f) throws JAXBException {
               
  
        JAXBContext jaxbContext = JAXBContext.newInstance(getContextClasses(c));
 
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        if (f != null)
            jaxbMarshaller.marshal(c.getCanvasStore(), f);
        
        c.markAsSaved();
    }
    
    
    /**
     * Restores a canvas from an XML file
     * @param f the file to read
     * @return the loaded ZCanvas
     * @throws JAXBException on unmarshall error
     * @throws java.io.IOException f cannot be found or read
     */
    public static ZCanvas fromFile(File f) throws JAXBException, IOException {
       
        ContextClasses cc = ContextClasses.getContextClasses(f);
        
        String[] unknown = cc.getUnknownClasses();
        if (unknown != null && unknown.length > 0) {
            System.out.println("Note: the following classes are unknown in file " + f.getName() + ": ");
            for (String s : unknown)
                System.out.println(s);
        }
        
        Class[] fileClasses = cc.getClasses();
        Class[] contextClasses = new Class[fileClasses.length + 1];
        
        System.arraycopy(fileClasses, 0, contextClasses, 0, fileClasses.length);        
        contextClasses[contextClasses.length-1] = CircleStrokeExample.class;
        
        JAXBContext jaxbContext = JAXBContext.newInstance(contextClasses);
 
        Unmarshaller jaxbUnMarshaller = jaxbContext.createUnmarshaller();
 
        CanvasStore store = (CanvasStore)jaxbUnMarshaller.unmarshal(f);
        ZCanvas c = ZCanvas.fromCanvasStore(store);
       
        return c;
    }
    
}
