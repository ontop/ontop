package it.unibz.inf.ontop.injection.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import it.unibz.inf.ontop.injection.OntopModelProperties;

import java.util.List;

/**
 * TODO: add generic code about property analysis
 */
public abstract class OntopAbstractModule extends AbstractModule {

    /**
     * Interface not found in the properties or impossibility to load the
     * declared implementation class.
     */
    public class UnknownClassException extends RuntimeException {
        public UnknownClassException(String message) {
            super(message);
        }
    }

    private final OntopModelProperties properties;

    protected OntopAbstractModule(OntopModelProperties configuration) {
        this.properties = configuration;
    }

    public Class getImplementation(String interfaceClassName) throws UnknownClassException {
        String implementationClassName = properties.getProperty(interfaceClassName)
                .orElseThrow(() -> new UnknownClassException(String.format(
                        "No entry for the interface %s in the properties.",
                        interfaceClassName)));

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
    protected void configureCoreConfiguration() {
        bind(OntopModelProperties.class).toInstance(properties);
    }

    protected OntopModelProperties getProperties() {
        return properties;
    }

    /**
     * To bind classes with default constructors.
     */
    protected void bindFromPreferences(Class abstractClass) {
        bind(abstractClass).to(getImplementation(abstractClass.getCanonicalName()));
    }

}
