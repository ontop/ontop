package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

public class TargetAtomFactoryImpl implements TargetAtomFactory {

    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final Variable s, p, o, g;

    @Inject
    private TargetAtomFactoryImpl(AtomFactory atomFactory, SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
        this.s = termFactory.getVariable("s");
        this.p = termFactory.getVariable("p");
        this.o = termFactory.getVariable("o");
        this.g = termFactory.getVariable("g");
    }

    @Override
    public TargetAtom getTripleTargetAtom(ImmutableTerm subject, ImmutableTerm pred, ImmutableTerm object) {
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctTripleAtom(
                (subject instanceof Variable) ? (Variable) subject : s,
                (pred instanceof Variable) && !pred.equals(subject) ? (Variable) pred : p,
                (object instanceof Variable) && !object.equals(subject) && !object.equals(pred) ? (Variable) object : o);

        return getTargetAtom(projectionAtom, ImmutableList.of(subject, pred, object));
    }

    @Override
    public TargetAtom getQuadTargetAtom(ImmutableTerm subject, ImmutableTerm pred, ImmutableTerm
            object, ImmutableTerm graph) {
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctQuadAtom(
                (subject instanceof Variable) ? (Variable) subject : s,
                (pred instanceof Variable) && !pred.equals(subject) ? (Variable) pred : p,
                (object instanceof Variable) && !object.equals(subject) && !object.equals(pred)
                        ? (Variable) object : o,
                (graph instanceof Variable) && !graph.equals(subject) && !graph.equals(pred)
                        && !graph.equals(object) ? (Variable) graph : g);

        return getTargetAtom(projectionAtom, ImmutableList.of(subject, pred, object, graph));
    }

    private TargetAtom getTargetAtom(DistinctVariableOnlyDataAtom projectionAtom, ImmutableList<ImmutableTerm> initialTerms) {
        ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(projectionAtom.getArguments(), initialTerms);
        return new TargetAtomImpl(projectionAtom, substitution);
    }

    @Override
    public TargetAtom getTargetAtom(DistinctVariableOnlyDataAtom projectionAtom, ImmutableSubstitution<ImmutableTerm> substitution) {
        return new TargetAtomImpl(projectionAtom, substitution);
    }
}
