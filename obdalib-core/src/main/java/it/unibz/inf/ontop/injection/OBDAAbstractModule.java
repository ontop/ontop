package it.unibz.inf.ontop.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import java.util.List;

/**
 * TODO: add generic code about property analysis
 */
public abstract class OBDAAbstractModule extends AbstractModule {

    /**
     * Interface not found in the configuration or impossibility to load the
     * declared implementation class.
     */
    public class UnknownClassException extends RuntimeException {
        public UnknownClassException(String message) {
            super(message);
        }
    }

    private final OBDAProperties configuration;

    protected OBDAAbstractModule(OBDAProperties configuration) {
        this.configuration = configuration;
    }

    public Class getImplementation(String interfaceClassName) throws UnknownClassException {
        String implementationClassName = configuration.getProperty(interfaceClassName);

        if (implementationClassName == null)
            throw new UnknownClassException(String.format("No entry for the interface %s in the configuration.",
                    interfaceClassName));

        try {
            return Class.forName(implementationClassName);
        } catch (ClassNotFoundException e) {
            throw new UnknownClassException(e.getMessage());
        }
    }

    protected Module buildFactory(List<Class> types,  Class factoryInterface) {
        FactoryModuleBuilder builder = new FactoryModuleBuilder();

        /**
         * Types to be implemented by the factory
         */
        for (Class type : types) {
            builder = builder.implement(type, getImplementation(type.getCanonicalName()));
        }
        return builder.build(factoryInterface);
    }

    /**
     * TO be called by sub-classes, inside the configure() method.
     */
    protected void configurePreferences() {
        bind(OBDAProperties.class).toInstance(configuration);
    }

    protected OBDAProperties getPreferences() {
        return configuration;
    }

    /**
     * To bind classes with default constructors.
     */
    protected void bindFromPreferences(Class abstractClass) {
        bind(abstractClass).to(getImplementation(abstractClass.getCanonicalName()));
    }

}
