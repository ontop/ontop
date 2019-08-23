package it.unibz.inf.ontop.model.atom;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import org.apache.commons.rdf.api.IRI;

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

    Function getMutableTripleAtom(Term subject, Term predicate, Term object);

    /**
     * In the body, constant IRIs are currently wrapped into a URI function but in the future they will not
     */
    Function getMutableTripleBodyAtom(Term subject, IRI propertyIRI, Term object);
    
    /**
     * In the body, constant IRIs are currently wrapped into a URI function but in the future they will not
     */
    Function getMutableTripleBodyAtom(Term subject, IRI classIRI);

    /**
     * In the head, constant IRIs are wrapped into a URI function
     */
    Function getMutableTripleHeadAtom(Term subject, IRI propertyIRI, Term object);

    /**
     * In the head, constant IRIs are wrapped into a URI function
     */
    Function getMutableTripleHeadAtom(Term subject, IRI classIRI);

    DistinctVariableOnlyDataAtom getDistinctTripleAtom(Variable subject, Variable property, Variable object);

    /**
     * TODO: change the generic-type to RDFAtomPredicate?
     */
    DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, VariableOrGroundTerm property,
                                                     VariableOrGroundTerm object);

    /**
     * TODO: change the generic-type to RDFAtomPredicate?
     */
    DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, IRI propertyIRI,
                                                     VariableOrGroundTerm object);

    /**
     * TODO: change the generic-type to RDFAtomPredicate?
     */
    DataAtom<AtomPredicate> getIntensionalTripleAtom(VariableOrGroundTerm subject, IRI classIRI);


    DistinctVariableOnlyDataAtom getDistinctQuadAtom(Variable subject, Variable property, Variable object,
                                                     Variable namedGraph);
}
