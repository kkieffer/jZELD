
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
    Deque<LinkedList<ZElement>> redoHistory = new ArrayDeque<>();
    
    
    private boolean suspend  = false;
    

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
        saveContext(ctx, true);
    }

    
    private LinkedList<ZElement> copyOf(LinkedList<ZElement> src) {
        
        LinkedList<ZElement> copy = new LinkedList<>();
        Iterator<ZElement> it = src.iterator();
        while (it.hasNext()) 
            copy.addLast(it.next().copyOf(false));
        
        return copy;
    }
    
    private void saveContext(LinkedList<ZElement> ctx, boolean clearRedo) {

        if (suspend)
            return;
        
        if (undoHistory.size() == stackDepth)  //remove oldest, if reached capacity limit
            undoHistory.removeLast();
                
        undoHistory.addFirst(copyOf(ctx));  //push a copy to the stack
        
        if (clearRedo)
            redoHistory.clear(); //clear all redo because this is a new context
    } 
    
    /**
     * Pop an element list from the undo stack
     * @param ctx the current context, for redo
     * @return the most recent element list, or null if there's no history
     */
    public LinkedList<ZElement> undo(LinkedList<ZElement> ctx) {
        
        if (undoHistory.isEmpty())
            return null;
        
        if (redoHistory.size() == stackDepth)
            redoHistory.removeLast();
                
        redoHistory.addFirst(copyOf(ctx));
        
        return undoHistory.removeFirst();
        
    }
    
    public LinkedList<ZElement> redo() {
        
         if (redoHistory.isEmpty())
            return null;
        
        LinkedList<ZElement> redoCtx = redoHistory.removeFirst();
        
        saveContext(redoCtx, false);
        return redoCtx;
    }
    
    

    public void suspendSave() {
        suspend = true;
    }

    public void resumeSave() {
        suspend = false;
    }
    
    /**
     * Clear all history
     */
    public void clear() {
        redoHistory.clear();
        undoHistory.clear();
    }
    
}
