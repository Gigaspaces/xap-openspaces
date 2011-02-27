package org.openspaces.grid.gsm.machines;



public class EagerMachinesSlaPolicy extends AbstractMachinesSlaPolicy {

    public boolean isStopMachineSupported() {
        return false;
    }

    @Override
    public String getScaleStrategyName() {
        return "Eager Scale Strategy";
    }
}
