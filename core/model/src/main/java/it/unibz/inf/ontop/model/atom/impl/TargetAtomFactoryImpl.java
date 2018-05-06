package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.stream.IntStream;

public class TargetAtomFactoryImpl implements TargetAtomFactory {

    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final Variable s, p, o;
    private final TermFactory termFactory;

    @Inject
    private TargetAtomFactoryImpl(AtomFactory atomFactory, SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.s = this.termFactory.getVariable("s");
        this.p = this.termFactory.getVariable("p");
        this.o = this.termFactory.getVariable("o");

    }

    @Override
    public TargetAtom getTripleTargetAtom(ImmutableTerm subject, ImmutableTerm pred, ImmutableTerm object) {
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctTripleAtom(
                (subject instanceof Variable) ? (Variable) subject : s,
                (pred instanceof Variable) && (!pred.equals(subject)) ? (Variable) pred : p,
                (object instanceof Variable) && (!object.equals(subject)) && (!object.equals(pred)) ? (Variable) object : o);

        ImmutableList<ImmutableTerm> initialTerms = ImmutableList.of(subject, pred, object);

        ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(
                IntStream.range(0, 3)
                        .boxed()
                        .map(i -> Maps.immutableEntry(projectionAtom.getTerm(i), initialTerms.get(i)))
                        .filter(e -> !e.getKey().equals(e.getValue()))
                        .collect(ImmutableCollectors.toMap()));
        return new TargetAtomImpl(projectionAtom, substitution);
    }

    @Override
    public TargetAtom getTripleTargetAtom(ImmutableTerm subjectTerm, IRI classIRI) {
        return getTripleTargetAtom(subjectTerm, createGroundFunctionalTerm(RDF.TYPE),
                createGroundFunctionalTerm(classIRI));
    }

    @Override
    public TargetAtom getTripleTargetAtom(ImmutableTerm subjectTerm, IRI propertyIRI, ImmutableTerm objectTerm) {
        return getTripleTargetAtom(subjectTerm, createGroundFunctionalTerm(propertyIRI), objectTerm);
    }

    @Override
    public TargetAtom getTargetAtom(DistinctVariableOnlyDataAtom projectionAtom, ImmutableSubstitution<ImmutableTerm> substitution) {
        return new TargetAtomImpl(projectionAtom, substitution);
    }

    private ImmutableFunctionalTerm createGroundFunctionalTerm(IRI iri) {
        return termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(iri.getIRIString()));
    }
}
