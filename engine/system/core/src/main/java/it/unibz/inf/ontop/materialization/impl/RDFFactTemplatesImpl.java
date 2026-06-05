package it.unibz.inf.ontop.materialization.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.materialization.RDFFactTemplates;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RDFFactTemplatesImpl implements RDFFactTemplates {

    private final ImmutableCollection<ImmutableList<Variable>> templates;

    public RDFFactTemplatesImpl(ImmutableCollection<ImmutableList<Variable>> triplesOrQuads) {
        this.templates = triplesOrQuads;
    }

    @Override
    public ImmutableCollection<ImmutableList<Variable>> getTriplesOrQuadsVariables() {
        return templates;
    }

    @Override
    public RDFFactTemplates apply(Substitution<Variable> substitution) {
        ImmutableMap<Variable, Variable> variableMap = getVariables().stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        v -> (Variable) substitution.apply(v)));

        return new RDFFactTemplatesImpl(templates.stream()
                .map(template -> template.stream()
                        .map(variableMap::get)
                        .collect(ImmutableCollectors.toList()))
                .collect(ImmutableCollectors.toList()));

    }

    @Override
    public RDFFactTemplates merge(RDFFactTemplates other) {
        if (getVariables().stream().anyMatch(other.getVariables()::contains)) {
            throw new IllegalArgumentException("Variables are not distinct");
        }

        return new RDFFactTemplatesImpl(
                ImmutableList.<ImmutableList<Variable>>builder()
                        .addAll(templates)
                        .addAll(other.getTriplesOrQuadsVariables())
                        .build());
    }

    @Override
    public Stream<RDFFact> convert(Substitution<ImmutableTerm> tupleSubstitution) {
        ImmutableList<ImmutableList<RDFConstant>> newTerms = IntStream.range(0, getVariables().size())
                .mapToObj(i -> getTemplate(i).stream()
                        .map(v -> (RDFConstant) tupleSubstitution.applyToTerm(v))
                        .collect(ImmutableCollectors.toList()))
                .collect(ImmutableCollectors.toList());

        return newTerms.stream()
                .map(vars -> vars.size() == 3
                        ? RDFFact.createTripleFact((ObjectConstant) vars.get(0), (IRIConstant) vars.get(1), vars.get(2))
                        : RDFFact.createQuadFact((ObjectConstant) vars.get(0), (IRIConstant) vars.get(1), vars.get(2), (ObjectConstant) vars.get(3))
                );
    }

    @Override
    public RDFFactTemplates compress(ImmutableSet<ImmutableCollection<Variable>> equivalentVariables) {
        return new RDFFactTemplatesImpl(templates.stream()
                .map(t -> t.stream()
                        .map(v -> getEquivalentVariable(v, equivalentVariables))
                        .collect(ImmutableCollectors.toList()))
                .collect(ImmutableCollectors.toList()));
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return templates.stream()
                .flatMap(ImmutableList::stream)
                .collect(ImmutableCollectors.toSet());
    }

    private Variable getEquivalentVariable(Variable variable, ImmutableSet<ImmutableCollection<Variable>> equivalentVariables) {
        return equivalentVariables.stream()
                .filter(equivalent -> equivalent.contains(variable))
                .flatMap(ImmutableCollection::stream)
                .findFirst()
                .orElse(variable);
    }

    private ImmutableList<Variable> getTemplate(int index) {
        return templates.asList().get(index);
    }


}
