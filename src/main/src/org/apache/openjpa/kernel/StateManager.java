package org.apache.openjpa.kernel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.openjpa.kernel.BrokerImpl;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.openspaces.jpa.openjpa.Broker;

/**
 * A GigaSpaces extended version of OpenJPA's StateManager
 * which has an additional member for storing an entity's owner reference
 * for supporting GigaSpaces owned relationships model.
 * 
 * @author Idan Moyal
 * @since 8.0.1
 *
 */
public class StateManager extends StateManagerImpl {
    //
    private static final long serialVersionUID = 1L;
    private StateManager _ownerStateManager;
    
    public StateManager(Object id, ClassMetaData meta, BrokerImpl broker) {
        super(id, meta, broker);
    }

    public void setOwnerStateManager(StateManager ownerStateManager) {
        this._ownerStateManager = ownerStateManager;
    }

    public StateManager getOwnerStateManager() {
        return _ownerStateManager;
    }
    
    /**
     * Called after an instance is persisted by a user through the broker.
     * Cascades the persist operation to fields marked
     * {@link ValueMetaData#CASCADE_IMMEDIATE}.
     */
    @Override
    void cascadePersist(OpCallbacks call) {
        FieldMetaData[] fmds = getMetaData().getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (!getLoaded().get(i))
                continue;

            if (fmds[i].getCascadePersist() == ValueMetaData.CASCADE_IMMEDIATE
             || fmds[i].getKey().getCascadePersist() == ValueMetaData.CASCADE_IMMEDIATE
             || fmds[i].getElement().getCascadePersist() == ValueMetaData.CASCADE_IMMEDIATE) {
                cascadePersist(i, call, fetchField(i, false));
            }
        }        
    }
    
    /**
     * Cascade-persists the provided value and setting an owner for the owned
     * relationships entities (One-to-one & One-to-many).
     * @param field The field to cascade persist.
     * @param call Operation call back.
     * @param value The field value to cascade persist.
     */
    private void cascadePersist(int field, OpCallbacks call, Object value) {
        if (value == null)
            return;

        FieldMetaData fmd = getMetaData().getField(field);
        Broker broker = (Broker) getBroker();
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.PC:
            case JavaTypes.PC_UNTYPED:
                if (!broker.isDetachedNew() && broker.isDetached(value))
                    return; // allow but ignore
                StateManager stateManager = (StateManager) broker.persist(value, null, true, call);
                // Set owner reference for one-to-one relationship only
                if (fmd.getAssociationType() == FieldMetaData.ONE_TO_ONE)
                    stateManager.setOwnerStateManager(this);
                break;
            case JavaTypes.ARRAY:
                broker.persistCollection((Collection<?>) Arrays.asList((Object[]) value), true, call, this);
                break;
            case JavaTypes.COLLECTION:
                broker.persistCollection((Collection<?>) value, true, call, this);
                break;
            case JavaTypes.MAP:
                if (fmd.getKey().getCascadePersist()
                    == ValueMetaData.CASCADE_IMMEDIATE)
                    broker.persistCollection(((Map<?, ?>) value).keySet(), true, call, this);
                if (fmd.getElement().getCascadePersist()
                    == ValueMetaData.CASCADE_IMMEDIATE)
                    broker.persistCollection(((Map<?, ?>) value).values(), true, call, this);
                break;
        }
    }
            
}
