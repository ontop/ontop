package org.semanticweb.ontop.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.io.SimplePrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDAModelImpl;

import java.util.List;
import java.util.Properties;

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

    private final Properties configuration;

    protected OBDAAbstractModule(Properties configuration) {
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

}
