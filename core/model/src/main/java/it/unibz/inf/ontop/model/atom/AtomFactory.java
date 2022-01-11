package it.unibz.inf.ontop.model.atom;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import org.apache.commons.rdf.api.IRI;

/**
 * Accessible through Guice (recommended) or through CoreSingletons.
 */
public interface AtomFactory {

    AtomPredicate getRDFAnswerPredicate(int arity);

    /**
     * Beware: a DataAtom is immutable
     */
    <P extends AtomPredicate> DataAtom<P> getDataAtom(P predicate, ImmutableList<? extends VariableOrGroundTerm> terms);

    /**
     * Beware: a DataAtom is immutable
     */
    <P extends AtomPredicate> DataAtom<P> getDataAtom(P predicate, VariableOrGroundTerm... terms);


    DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate,
                                                                 ImmutableList<Variable> arguments);

    DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate,
                                                                 Variable ... arguments);

    DistinctVariableOnlyDataAtom getDistinctTripleAtom(Variable subject, Variable property, Variable object);

    /**
     * TODO: change the generic-type to RDFAtomPredicate?
     */
    DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                     VariableOrGroundTerm object);

    /**
     * TODO: change the generic-type to RDFAtomPredicate?
     * Davide: For provenance TODO: Add quads version for each method in this intereface
     */
    DataAtom<AtomPredicate> getIntensionalQuadAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                     VariableOrGroundTerm object, VariableOrGroundTerm graph);

    /**
     * TODO: change the generic-type to RDFAtomPredicate?
     */
    DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, IRI propertyIRI,
                                                     VariableOrGroundTerm object);

    /**
     * TODO: change the generic-type to RDFAtomPredicate?
     */
    DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, IRI classIRI);

    DataAtom<AtomPredicate> getIntensionalQuadAtom(VariableOrGroundTerm subject, IRI classIRI, VariableOrGroundTerm graph);

    DistinctVariableOnlyDataAtom getDistinctQuadAtom(Variable subject, Variable property, Variable object,
                                                     Variable namedGraph);
}
