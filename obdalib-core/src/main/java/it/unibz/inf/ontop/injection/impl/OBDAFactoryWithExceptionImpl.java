package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * TODO: describe
 */
public class OBDAFactoryWithExceptionImpl
        implements OBDAFactoryWithException {

    private final OBDAProperties preferences;

    @Inject
    private OBDAFactoryWithExceptionImpl(OBDAProperties preferences) {
        // TODO: use them
        this.preferences = preferences;
    }

    private Constructor[] findConstructors(Class genericClass) {
        String implementationName = preferences.getProperty(
                genericClass.getCanonicalName());
        if (implementationName == null) {
            //TODO: find a better exception
            throw new RuntimeException("No implementation declared for " +
            genericClass.getCanonicalName());
        }
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
    public OBDAModel createOBDAModel(Set<OBDADataSource> dataSources,
                                     Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                                     PrefixManager prefixManager, ImmutableOntologyVocabulary ontologyVocabulary)
            throws DuplicateMappingException {
        try {
            /**
             * Instantiation
             */
            Constructor constructor = findFirstConstructor(OBDAModel.class);
            return (OBDAModel) constructor.newInstance(dataSources,
                    newMappings, prefixManager, ontologyVocabulary);
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
