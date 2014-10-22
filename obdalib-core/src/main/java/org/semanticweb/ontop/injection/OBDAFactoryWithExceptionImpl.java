package org.semanticweb.ontop.injection;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;

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
                                     PrefixManager prefixManager)
            throws DuplicateMappingException {
        try {
            /**
             * Instantiation
             */
            Constructor constructor = findFirstConstructor(OBDAModel.class);
            return (OBDAModel) constructor.newInstance(dataSources,
                    newMappings, prefixManager);
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
            throw new RuntimeException(e.getMessage());
        }
    }
}
