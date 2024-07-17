package it.unibz.inf.ontop.model.atom;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

/**
 * Abstraction for triples, quads and so on.
 */
public interface RDFAtomPredicate extends AtomPredicate {

    Optional<IRI> getClassIRI(ImmutableList<? extends ImmutableTerm> atomArguments);
    Optional<IRI> getPropertyIRI(ImmutableList<? extends ImmutableTerm> atomArguments);
    /**
     * Returns a class or (non rdf:type) property IRI if available
     */
    Optional<IRI> getPredicateIRI(ImmutableList<? extends ImmutableTerm> atomArguments);

    Optional<IRI> getGraphIRI(ImmutableList<? extends ImmutableTerm> atomArguments);

    /**
     * the type of the three getters
     */
    @FunctionalInterface
    interface ComponentGetter {
        <T extends ImmutableTerm> T get(ImmutableList<T> atomArguments);
    }

    <T extends ImmutableTerm> T getSubject(ImmutableList<T> atomArguments);
    <T extends ImmutableTerm> T getProperty(ImmutableList<T> atomArguments);
    <T extends ImmutableTerm> T getObject(ImmutableList<T> atomArguments);
    <T extends ImmutableTerm> Optional<T> getGraph(ImmutableList<T> atomArguments);

    <T extends ImmutableTerm> ImmutableList<T> updateSPO(ImmutableList<T> originalArguments, T newSubject,
                                                         T newProperty, T newObject);


    /**
     * the type of the two updater methods
     */

    @FunctionalInterface
    interface ComponentUpdater {
        <T extends ImmutableTerm> ImmutableList<T> update(ImmutableList<T> originalArguments, T newComponent);
    }

    default <T extends ImmutableTerm> ImmutableList<T> updateSubject(ImmutableList<T> originalArguments, T newSubject) {
        return updateSPO(originalArguments, newSubject, getProperty(originalArguments), getObject(originalArguments));
    }

    default <T extends ImmutableTerm> ImmutableList<T> updateObject(ImmutableList<T> originalArguments, T newObject) {
        return updateSPO(originalArguments, getSubject(originalArguments), getProperty(originalArguments), newObject);
    }
}
