package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class TreeWitnessRewriterReasoner {

    private final ClassifiedTBox classifiedTBox;
    private final ImmutableList<TreeWitnessGenerator> treeWitnessGenerators;

    TreeWitnessRewriterReasoner(ClassifiedTBox classifiedTBox) {
        this.classifiedTBox = classifiedTBox;
        this.treeWitnessGenerators = classifiedTBox.classesDAG().stream()
                .flatMap(this::getTreeWitnessGenerators)
                .collect(ImmutableCollectors.toList());
    }

    public ClassifiedTBox getClassifiedTBox() { return classifiedTBox; }

    public DownwardSaturatedImmutableSet<ObjectPropertyExpression> getSubProperties(Collection<DataAtom<RDFAtomPredicate>> atoms, Map.Entry<VariableOrGroundTerm, VariableOrGroundTerm> idx) {
        return atoms.stream().map(a -> getSubProperties(a, idx)).collect(DownwardSaturatedImmutableSet.toIntersection());
    }

    private DownwardSaturatedImmutableSet<ObjectPropertyExpression> getSubProperties(DataAtom<RDFAtomPredicate> atom, Map.Entry<VariableOrGroundTerm, VariableOrGroundTerm> idx) {
        return atom.getPredicate().getPropertyIRI(atom.getArguments())
                .filter(i -> classifiedTBox.objectProperties().contains(i))
                .map(i -> classifiedTBox.objectProperties().get(i))
                .map(p -> isInverse(atom, idx) ? p.getInverse() : p)
                .map(p -> DownwardSaturatedImmutableSet.create(classifiedTBox.objectPropertiesDAG().getSubRepresentatives(p)))
                .orElse(DownwardSaturatedImmutableSet.bottom());
    }

    private boolean isInverse(DataAtom<RDFAtomPredicate> a, Map.Entry<VariableOrGroundTerm, VariableOrGroundTerm> idx) {
        VariableOrGroundTerm subject = a.getPredicate().getSubject(a.getArguments());
        VariableOrGroundTerm object = a.getPredicate().getObject(a.getArguments());
        if (subject.equals(idx.getKey()) && object.equals(idx.getValue()))
            return false;
        if (subject.equals(idx.getValue()) && object.equals(idx.getKey()))
            return true;
        throw new MinorOntopInternalBugException("non-matching arguments: " + a + " " + idx);
    }


    public DownwardSaturatedImmutableSet<ClassExpression> getSubConcepts(Collection<DataAtom<RDFAtomPredicate>> atoms) {
        return atoms.stream().map(this::getSubConcepts).collect(DownwardSaturatedImmutableSet.toIntersection());
    }

    private DownwardSaturatedImmutableSet<ClassExpression> getSubConcepts(DataAtom<RDFAtomPredicate> atom) {
        return atom.getPredicate().getClassIRI(atom.getArguments())
                .filter(i -> classifiedTBox.classes().contains(i))
                .map(i -> getSubConcepts(classifiedTBox.classes().get(i)))
                .orElse(DownwardSaturatedImmutableSet.bottom());
    }

    public DownwardSaturatedImmutableSet<ClassExpression> getSubConcepts(ClassExpression c) {
        return DownwardSaturatedImmutableSet.create(classifiedTBox.classesDAG().getSubRepresentatives(c));
    }


    // tree witness generators of the ontology (i.e., positive occurrences of \exists R.B)

    public ImmutableList<TreeWitnessGenerator> getTreeWitnessGenerators() { return treeWitnessGenerators; }

    private Stream<TreeWitnessGenerator> getTreeWitnessGenerators(Equivalences<ClassExpression> eq) {
        ImmutableList<ObjectPropertyExpression> properties = getDistinctRepresentativesForProperties(eq)
                .collect(ImmutableCollectors.toList());

        if (properties.isEmpty())
            return Stream.of();

        ImmutableSet<ClassExpression> maximalRepresentatives = Stream.concat(
                classifiedTBox.classesDAG().getDirectSub(eq).stream()
                        .map(Equivalences::getRepresentative),
                getNonTrivialEquivalents(eq))
                    .collect(ImmutableCollectors.toSet());

        if (maximalRepresentatives.isEmpty())
            return Stream.of();

        return properties.stream()
                .map(p -> new TreeWitnessGenerator(p, getSubConcepts(eq.getRepresentative()), maximalRepresentatives));
    }

    private Stream<ClassExpression> getNonTrivialEquivalents(Equivalences<ClassExpression> eq) {
        if (!(eq.getRepresentative() instanceof ObjectSomeValuesFrom))
            return Stream.of(eq.getRepresentative());

        // in particular, there are no class names in eq,
        // but the property is a representative its equivalence class
        ObjectPropertyExpression property = ((ObjectSomeValuesFrom)eq.getRepresentative()).getProperty();
        return getDistinctRepresentativesForProperties(eq)
                .filter(p -> p != property)
                .map(ObjectPropertyExpression::getDomain);
    }

    private Stream<ObjectPropertyExpression> getDistinctRepresentativesForProperties(Equivalences<ClassExpression> eq) {
        return eq.stream()
                .filter(ce -> ce instanceof ObjectSomeValuesFrom)
                .map(ce -> (ObjectSomeValuesFrom) ce)
                .map(ObjectSomeValuesFrom::getProperty)
                .map(p -> classifiedTBox.objectPropertiesDAG().getVertex(p))
                .map(Equivalences::getRepresentative)
                .distinct();
    }

}
