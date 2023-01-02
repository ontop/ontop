package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SQLPPMappingFactoryImpl implements SQLPPMappingFactory {

    private final OntopMappingSettings settings;

    @Inject
    private SQLPPMappingFactoryImpl(OntopMappingSettings settings) {
        this.settings = settings;
    }

    private Constructor<? extends SQLPPMapping> findConstructor(Class<SQLPPMapping> genericClass) {
        String implementationName = settings.getProperty(genericClass.getCanonicalName())
                //TODO: find a better exception
                .orElseThrow(() -> new RuntimeException("No implementation declared for " +
                        genericClass.getCanonicalName()));

        try {
            Class<? extends SQLPPMapping> implementationClass = Class.forName(implementationName).asSubclass(genericClass);
            return implementationClass.getConstructor(ImmutableList.class, PrefixManager.class);
        }
        catch (ClassNotFoundException | NoSuchMethodException e) {
            // TODO: find a better exception
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * SQLPPPMapping creation
     */
    @Override
    public SQLPPMapping createSQLPreProcessedMapping(ImmutableList<SQLPPTriplesMap> ppMappingAxioms,
                                                     PrefixManager prefixManager) {
        try {
            /**
             * Instantiation
             */
            Constructor<? extends SQLPPMapping> constructor = findConstructor(SQLPPMapping.class);
            return  constructor.newInstance(ppMappingAxioms, prefixManager);
            /**
             * Exception management
             */
        }
        catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            /**
             * Unexpected: throw a unexpected RuntimeException.
             */
            throw new RuntimeException(targetException.getMessage());

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
