package org.openspaces.core.space.mode.registry;

import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceFinder;

public class Test {
    public static void main(String[] args) throws SecurityException, NoSuchMethodException {
        Test t = new Test();
        t.test();
    }
    

    public void test() throws SecurityException, NoSuchMethodException {
        ModeAnnotationRegistry x = new ModeAnnotationRegistry();
        System.out.println("--------------------------------");
        //x.printRegistry();
        x.registerAnnotation(PrePrimary.class, 
                             this, 
                             this.getClass().getMethod("print", new Class[] {BeforeSpaceModeChangeEvent.class}));
        System.out.println("--------------------------------");
        //x.printRegistry();
        IJSpace space;
        try {
            space = (IJSpace)SpaceFinder.find("/./myTest?groups=shaiw");
            System.out.println("hello1");
        } catch (FinderException e) {
            System.out.println("hello2");
            return;
        }
        System.out.println("hell3");
        x.onBeforePrimary(new BeforeSpaceModeChangeEvent(space, SpaceMode.PRIMARY));
        System.out.println("hello4");
        System.out.println("--------------------------------");
        x.registerAnnotation(PrePrimary.class, 
                this, 
                this.getClass().getMethod("print", new Class[] {BeforeSpaceModeChangeEvent.class}));
        //x.printRegistry();
    }
    
    public void print(BeforeSpaceModeChangeEvent o) {
        System.out.println("hello" + o);
    }
}
