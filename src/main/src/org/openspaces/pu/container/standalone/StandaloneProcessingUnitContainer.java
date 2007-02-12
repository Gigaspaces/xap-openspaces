package org.openspaces.pu.container.standalone;

import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.support.CommandLineParser;
import org.openspaces.pu.container.support.BeanLevelPropertiesParser;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationContext;

/**
 * @author kimchy
 */
public class StandaloneProcessingUnitContainer implements ProcessingUnitContainer {

    private StandaloneContainerRunnable containerRunnable;

    public StandaloneProcessingUnitContainer(StandaloneContainerRunnable containerRunnable) {
        this.containerRunnable = containerRunnable;
    }

    public ApplicationContext getApplicationContext() {
        return this.containerRunnable.getApplicationContext();
    }

    public void close() throws CannotCloseContainerException {
        containerRunnable.stop();
        // TODO wait till it shuts down
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser.Parameter[] params = CommandLineParser.parse(args);
        if (params.length == 0) {
            throw new IllegalArgumentException("-pu parameter must be defined");
        }
        String puLocation = null;
        for (int i = 0; i < params.length; i++) {
            if (params[i].getName().equalsIgnoreCase("pu")) {
                puLocation = params[i].getArguments()[0];
            }
        }
        if (puLocation == null) {
            throw new IllegalArgumentException("-pu parameter must be defined");
        }
        StandaloneProcessingUnitContainerProvider provider = new StandaloneProcessingUnitContainerProvider(puLocation);

        provider.setBeanLevelProperties(BeanLevelPropertiesParser.parse(params));

        // parse the config location parameters
        for (int i = 0; i < params.length; i++) {
            if (params[i].getName().equalsIgnoreCase("config")) {
                for (int j = 0; j < params[i].getArguments().length; j++) {
                    provider.addConfigLocation(params[i].getArguments()[j]);
                }
            }
        }

        StandaloneProcessingUnitContainer container = (StandaloneProcessingUnitContainer) provider.createContainer();
        ((ConfigurableApplicationContext) container.getApplicationContext()).registerShutdownHook();
    }
}
