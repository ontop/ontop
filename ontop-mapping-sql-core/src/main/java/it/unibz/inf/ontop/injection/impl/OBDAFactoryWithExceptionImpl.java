package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.SQLPPMappingAxiom;
import it.unibz.inf.ontop.model.SQLPPMapping;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * TODO: describe
 */
public class OBDAFactoryWithExceptionImpl
        implements OBDAFactoryWithException {

    private final OntopMappingSettings settings;

    @Inject
    private OBDAFactoryWithExceptionImpl(OntopMappingSettings settings) {
        this.settings = settings;
    }

    private Constructor[] findConstructors(Class genericClass) {
        String implementationName = settings.getProperty(genericClass.getCanonicalName())
                //TODO: find a better exception
                .orElseThrow(() -> new RuntimeException("No implementation declared for " +
                        genericClass.getCanonicalName()));

        try {
            Class implementationClass = Class.forName(implementationName);
            return implementationClass.getConstructors();
        } catch (ClassNotFoundException e) {
            // TODO: find a better exception
            throw new RuntimeException(e.getMessage());
        }
    }

    private Constructor findFirstConstructor(Class genericClass) {
        return findConstructors(genericClass)[0];
    }

    /**
     * OBDA model creation
     */
    @Override
    public SQLPPMapping createSQLPreProcessedMapping(ImmutableList<SQLPPMappingAxiom> newMappings,
                                                     MappingMetadata mappingMetadata)
            throws DuplicateMappingException {
        try {
            /**
             * Instantiation
             */
            Constructor constructor = findFirstConstructor(SQLPPMapping.class);
            return (SQLPPMapping) constructor.newInstance(newMappings, mappingMetadata);
            /**
             * Exception management
             */
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            /**
             * Expected exception: rethrown
             */
            if (targetException instanceof DuplicateMappingException) {
                throw (DuplicateMappingException) targetException;
            }
            /**
             * Unexcepted: throw a unexpected RuntimeException.
             */
            throw new RuntimeException(targetException.getMessage());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
