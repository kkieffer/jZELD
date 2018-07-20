
package com.github.kkieffer.jzeld;

import com.github.kkieffer.jzeld.element.ZElement;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Holds copies of the ZElement list on a stack for undo purposes.  Saving context pushes a copy onto the stack,
 * restoring pops the most recent from the stack.
 * 
 * @author kkieffer
 */
public class UndoStack {

    private final int stackDepth;
       
    //History of all the ZElement changes, push each new revision to the stack, pop to undo
    Deque<LinkedList<ZElement>> undoHistory = new ArrayDeque<>();
    

    /**
     * Create the undo stack with the specified amount of history
     * @param stackDepth 
     */
    public UndoStack(int stackDepth) {
        if (stackDepth < 1)
            throw new IllegalArgumentException("Stack depth must be greater than zero");
        this.stackDepth = stackDepth;
    }
 

    /**
     * Copy the element list and push it to the undo stack
     * @param ctx 
     */
    public void saveContext(LinkedList<ZElement> ctx) {
        
        if (undoHistory.size() == stackDepth)  //remove oldest, if reached capacity limit
            undoHistory.removeLast();
        
        LinkedList<ZElement> copy = new LinkedList<>();
        Iterator<ZElement> it = ctx.iterator();
        while (it.hasNext()) 
            copy.addLast(it.next().copyOf(false));
        
        undoHistory.addFirst(copy);  //push a copy to the stack
        
    } 
    
    /**
     * Pop an element list from the undo stack
     * @return the most recent element list, or null if there's no history
     */
    public LinkedList<ZElement> restoreContext() {
        
        if (undoHistory.isEmpty())
            return null;
        
        return undoHistory.removeFirst();
        
    }
    
    
    
}
