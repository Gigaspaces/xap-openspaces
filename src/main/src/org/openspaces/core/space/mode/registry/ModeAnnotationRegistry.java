package org.openspaces.core.space.mode.registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;

import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;
import org.openspaces.core.space.mode.SpaceAfterBackupListener;
import org.openspaces.core.space.mode.SpaceAfterPrimaryListener;
import org.openspaces.core.space.mode.SpaceBeforeBackupListener;
import org.openspaces.core.space.mode.SpaceBeforePrimaryListener;

public class ModeAnnotationRegistry implements SpaceBeforePrimaryListener,
                                               SpaceAfterPrimaryListener,
                                               SpaceBeforeBackupListener,
                                               SpaceAfterBackupListener {

    protected static final HashSet<Class<?>> validTypes = new HashSet<Class<?>>(); 
        
    static {
        validTypes.add(BeforeSpaceModeChangeEvent.class);
        validTypes.add(AfterSpaceModeChangeEvent.class);
    }
    
    protected Hashtable<Class<?>, HashSet<RegistryEntry>> registry = new Hashtable<Class<?>, HashSet<RegistryEntry>>();
    
    public void registerAnnotation(Class<?> annotation, Object object, Method method) {
        
        if (annotation == null || object == null || method == null) {
            return;
        }
        
        // check that the specified method has no more than 1 parameter; when it
        // has 1 parameter, checks that it is of the mode change event type.
        Class<?>[] types = method.getParameterTypes();
        if (types.length > 1) {
            throw new IllegalArgumentException("Can't accept method with more than one arguments");
        }
        else if (types.length == 1) {
            if (!validTypes.contains(types[0])) {
                throw new IllegalArgumentException("Bad arguments for method");
            }
        }
        
        // check that the supplied method belongs to the passed object
        Method objMethod;
        try {
            objMethod = object.getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (SecurityException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
        if (!objMethod.equals(method)) {
            return;
        }
        
        // if the annotation is not yet in the registry create and add it.
        HashSet<RegistryEntry> methods = registry.get(annotation);
        if (methods == null) {
            methods = new HashSet<RegistryEntry>();
            registry.put(annotation, methods);
        }
        // add the entry to the registry
        RegistryEntry entry = new RegistryEntry(object, method);
        methods.add(entry);
    }
    
    public void onBeforePrimary(BeforeSpaceModeChangeEvent event) {
        fireEvent(registry.get(PrePrimary.class), event);
    }

    public void onAfterPrimary(AfterSpaceModeChangeEvent event) {
        fireEvent(registry.get(PostPrimary.class), event);
    }

    public void onBeforeBackup(BeforeSpaceModeChangeEvent event) {
        fireEvent(registry.get(PreBackup.class), event);
    }

    public void onAfterBackup(AfterSpaceModeChangeEvent event) {
        fireEvent(registry.get(PostBackup.class), event);
    }
    
    protected void fireEvent(HashSet<RegistryEntry> entries, Object event) {
        if (entries != null) {
            for (RegistryEntry registryEntry : entries) {
                try {
                    if (registryEntry.method.getParameterTypes().length == 0) {
                        registryEntry.method.invoke(registryEntry.object, null);
                    }
                    else {
                        registryEntry.method.invoke(registryEntry.object, event);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    

    class RegistryEntry {
        Object object;
        Method method;
        
        RegistryEntry(Object object, Method method) {
            this.object = object;
            this.method = method;
        }
        
        @Override
        public boolean equals(Object o) {
            System.out.println("here");
            if (o != null && o instanceof RegistryEntry) {
                RegistryEntry entry = (RegistryEntry)o;
                // need to be the same object instance(!) and same method
                if (entry.object == this.object &&
                    entry.method.equals(method)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public int hashCode() { 
            int hash = 1;
            hash = hash * 31 + object.hashCode();
            hash = hash * 31 + method.hashCode();
            return hash;
        }
    }
}
