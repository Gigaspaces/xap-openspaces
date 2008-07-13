package org.openspaces.core.space.mode.registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;
import org.openspaces.core.space.mode.PostBackup;
import org.openspaces.core.space.mode.PostPrimary;
import org.openspaces.core.space.mode.PreBackup;
import org.openspaces.core.space.mode.PrePrimary;
import org.openspaces.core.space.mode.SpaceAfterBackupListener;
import org.openspaces.core.space.mode.SpaceAfterPrimaryListener;
import org.openspaces.core.space.mode.SpaceBeforeBackupListener;
import org.openspaces.core.space.mode.SpaceBeforePrimaryListener;

import com.gigaspaces.cluster.activeelection.SpaceMode;

/**
 * Receives space mode change events and routs them to beans that use annotations to register as listeners on
 * those events.
 * 
 * When the application starts beans that has one or more of the annotation {@link PreBackup}, {@link PostBackup}, 
 * {@link PrePrimary}, {@link PostPrimary} are registered in this bean, and when events arrive they are routed
 * to the registered beans' methods. 
 * 
 * @author shaiw
 */
public class ModeAnnotationRegistry implements SpaceBeforePrimaryListener,
                                               SpaceAfterPrimaryListener,
                                               SpaceBeforeBackupListener,
                                               SpaceAfterBackupListener {

    /**
     * Holds the valid parameter types of a target listener method. 
     */
    protected static final HashSet<Class<?>> validTypes = new HashSet<Class<?>>(); 
        
    static {
        validTypes.add(BeforeSpaceModeChangeEvent.class);
        validTypes.add(AfterSpaceModeChangeEvent.class);
    }
    
    /**
     * Maps the annotation to the list of beans' methods to invoke. 
     */
    protected Hashtable<Class<?>, HashSet<RegistryEntry>> registry = new Hashtable<Class<?>, HashSet<RegistryEntry>>();
    
    
    private static Log logger = LogFactory.getLog(ModeAnnotationRegistry.class);
    
    /**
     * Registers the bean as a listener for a space mode event specified by the annotation. When a event
     * fires the specified bean method will be invoked.
     * 
     * The method may have no parameters of a single parameter of type {@link BeforeSpaceModeChangeEvent}
     * or {@link AfterSpaceModeChangeEvent}.
     *  
     * @param annotation The space mode annotation used to register the bean.
     * @param object The bean instance.
     * @param method The bean's method to invoke when the event fires.
     * @throws IllegalArgumentException When the specified method has more than one parameter, or when the
     * method's parameter is not of the types {@link BeforeSpaceModeChangeEvent} or {@link AfterSpaceModeChangeEvent},
     * or when the specified bean is not the one declaring the specified method.
     */
    public void registerAnnotation(Class<?> annotation, Object object, Method method) throws IllegalArgumentException {
        
        if (annotation == null || object == null || method == null) {
            return;
        }
        
        // check that the specified method has no more than 1 parameter; when it
        // has 1 parameter, checks that it is of the mode change event type.
        Class<?>[] types = method.getParameterTypes();
        if (types.length > 1) {
            throw new IllegalArgumentException("The specified method has more than one parameter. A valid" +
            		" method may have no parameters or a single parameter of the types: " + validTypes);
        }
        else if (types.length == 1) {
            if (!validTypes.contains(types[0])) {
                throw new IllegalArgumentException("The specified method's parameter is of an illegal type. " +
               		"A valid method may have no parameters or a single parameter of the types: " + validTypes);
            }
        }
        
        // check that the supplied method belongs to the passed object
        Method objMethod;
        try {
            objMethod = object.getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to validate method: " + method, e);
        }
        if (!objMethod.equals(method)) {
            // 
            throw new IllegalArgumentException("The specified bean is not the one declaring the" +
            		" specified method. Bean: " + object + ", Method: " + method);
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
    
    /**
     * Invoked before a space changes its mode to {@link SpaceMode.PRIMARY}.
     */
    public void onBeforePrimary(BeforeSpaceModeChangeEvent event) {
        fireEvent(registry.get(PrePrimary.class), event);
    }

    /**
     * Invoked after a space changes its mode to {@link SpaceMode.PRIMARY}.
     */
    public void onAfterPrimary(AfterSpaceModeChangeEvent event) {
        fireEvent(registry.get(PostPrimary.class), event);
    }

    /**
     * Invoked before a space changes its mode to {@link SpaceMode.BACKUP}.
     */
    public void onBeforeBackup(BeforeSpaceModeChangeEvent event) {
        fireEvent(registry.get(PreBackup.class), event);
    }

    /**
     * Invoked after a space changes its mode to {@link SpaceMode.BACKUP}.
     */
    public void onAfterBackup(AfterSpaceModeChangeEvent event) {
        fireEvent(registry.get(PostBackup.class), event);
    }
    
    /**
     * Invokes the registered beans' methods passing them the space mode change event.
     * @param entries A list of beans and methods to invoke.  
     * @param event The event to pass to the methods in case they expect a parameter.
     */
    protected void fireEvent(HashSet<RegistryEntry> entries, Object event) {
        if (entries != null) {
            for (RegistryEntry registryEntry : entries) {
                try {
                    if (registryEntry.method.getParameterTypes().length == 0) {
                        registryEntry.method.invoke(registryEntry.object, null);
                    } else {
                        registryEntry.method.invoke(registryEntry.object, event);
                    }
                } catch (InvocationTargetException e) {
                    logger.error("Target invocation method threw an exception. Bean: " + 
                            registryEntry.object + ", Method: " + registryEntry.method, e);
                } catch (Exception e) {
                    logger.error("Failed to invoke target invocation method. Bean: " + 
                            registryEntry.object + ", Method: " + registryEntry.method, e);
                }
            }
        }
    }
    

    /**
     * An entry in the registry that holds the bean instace and the method to invoke.
     */
    class RegistryEntry {
        
        /**
         * The bean instance.
         */
        Object object;
        
        /**
         * The method to invoke.
         */
        Method method;
        
        /**
         * Create a new {@link RegistryEntry} instance.
         * @param object The bean instance.
         * @param method The method to invoke.
         */
        RegistryEntry(Object object, Method method) {
            this.object = object;
            this.method = method;
        }
        
        @Override
        public boolean equals(Object o) {
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
