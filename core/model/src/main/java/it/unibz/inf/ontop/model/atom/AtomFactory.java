package it.unibz.inf.ontop.model.atom;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;

public interface AtomFactory {

    @Deprecated
    AtomPredicate getAtomPredicate(String name, int arity);

    AtomPredicate getAtomPredicate(String name, ImmutableList<TermType> expectedBaseTypes);

    AtomPredicate getAtomPredicate(Predicate datalogPredicate);

    @Deprecated
    AtomPredicate getObjectPropertyPredicate(String name);

    AtomPredicate getObjectPropertyPredicate(IRI iri);

    AtomPredicate getDataPropertyPredicate(String name, TermType type);

    @Deprecated
    AtomPredicate getAnnotationPropertyPredicate(String name);

    AtomPredicate getAnnotationPropertyPredicate(IRI iri);

    @Deprecated
    AtomPredicate getDataPropertyPredicate(String name);

    AtomPredicate getDataPropertyPredicate(IRI iri);

    @Deprecated
    AtomPredicate getClassPredicate(String name);

    AtomPredicate getClassPredicate(IRI iri);

    AtomPredicate getOWLSameAsPredicate();

    AtomPredicate getOBDACanonicalIRI();

    /**
     * Beware: a DataAtom is immutable
     */
    DataAtom getDataAtom(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> terms);

    /**
     * Beware: a DataAtom is immutable
     */
    DataAtom getDataAtom(AtomPredicate predicate, VariableOrGroundTerm... terms);

    DistinctVariableDataAtom getDistinctVariableDataAtom(AtomPredicate predicate,
                                                                ImmutableList<? extends VariableOrGroundTerm> arguments);
    DistinctVariableDataAtom getDistinctVariableDataAtom(AtomPredicate predicate, VariableOrGroundTerm ... arguments);

    DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate,
                                                                        ImmutableList<Variable> arguments);

    DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate,
                                                                        Variable ... arguments);

    VariableOnlyDataAtom getVariableOnlyDataAtom(AtomPredicate predicate, Variable... terms);

    VariableOnlyDataAtom getVariableOnlyDataAtom(AtomPredicate predicate, ImmutableList<Variable> terms);

    Function getTripleAtom(Term subject, Term predicate, Term object);

    AtomPredicate getTripleAtomPredicate();

    /**
     * TODO: create an abstraction of DataAtom (Atom) that accepts arbitrary ImmutableTerms as arguments
     * (not only Variable or ground terms)
     */
    ImmutableFunctionalTerm getImmutableTripleAtom(ImmutableTerm subject, ImmutableTerm predicate, ImmutableTerm object);
}
