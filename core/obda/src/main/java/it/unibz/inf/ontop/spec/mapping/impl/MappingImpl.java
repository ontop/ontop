package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.stream.Stream;


public class MappingImpl implements Mapping {

    private final MappingMetadata metadata;
    private final ImmutableMap<IRI, IQ> triplePropertyDefinitions;
    private final ImmutableMap<IRI, IQ> tripleClassDefinitions;
    private final ImmutableSet<RDFAtomPredicate> rdfAtomPredicates;

    /**
     * TODO: consider Map of Map instead (to work with triple, quads and maybe something else)
     */
    @AssistedInject
    private MappingImpl(@Assisted MappingMetadata metadata,
                        @Assisted("triplePropertyMap") ImmutableMap<IRI, IQ> triplePropertyMap,
                        @Assisted("tripleClassMap") ImmutableMap<IRI, IQ> tripleClassMap,
                        OntopModelSettings settings) {
        this.metadata = metadata;
        this.triplePropertyDefinitions = triplePropertyMap;
        this.tripleClassDefinitions = tripleClassMap;

        if (settings.isTestModeEnabled()) {
            for (IQ query : triplePropertyDefinitions.values()) {
                checkNullableVariables(query);
            }
            for (IQ query : tripleClassDefinitions.values()) {
                checkNullableVariables(query);
            }
        }

        rdfAtomPredicates = Stream.concat(tripleClassDefinitions.values().stream(),
                triplePropertyMap.values().stream())
                .map(iq -> iq.getProjectionAtom().getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate)p)
                .findFirst()
                .map(ImmutableSet::of)
                .orElseGet(ImmutableSet::of);
    }

    private static void checkNullableVariables(IQ query) throws NullableVariableInMappingException {
        ImmutableSet<Variable> nullableVariables = query.getTree().getNullableVariables();
        if (!nullableVariables.isEmpty())
            throw new NullableVariableInMappingException(query, nullableVariables);
    }

    @Override
    public MappingMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Optional<IQ> getRDFPropertyDefinition(RDFAtomPredicate rdfAtomPredicate, IRI propertyIRI) {
        if (rdfAtomPredicate instanceof TriplePredicate)
            return Optional.ofNullable(triplePropertyDefinitions.get(propertyIRI));
        // TODO: consider quads
        else
            return Optional.empty();
    }

    @Override
    public Optional<IQ> getRDFClassDefinition(RDFAtomPredicate rdfAtomPredicate, IRI classIRI) {
        if (rdfAtomPredicate instanceof TriplePredicate)
            return Optional.ofNullable(tripleClassDefinitions.get(classIRI));
        // TODO: consider quads
        else
            return Optional.empty();
    }

    @Override
    public ImmutableSet<IRI> getRDFProperties(RDFAtomPredicate rdfAtomPredicate) {
        if (rdfAtomPredicate instanceof TriplePredicate)
            return triplePropertyDefinitions.keySet();
            // TODO: consider quads
        else
            return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<IRI> getRDFClasses(RDFAtomPredicate rdfAtomPredicate) {
        if (rdfAtomPredicate instanceof TriplePredicate)
            return tripleClassDefinitions.keySet();
            // TODO: consider quads
        else
            return ImmutableSet.of();
    }

    @Override
    public ImmutableCollection<IQ> getQueries(RDFAtomPredicate rdfAtomPredicate) {
        if (rdfAtomPredicate instanceof TriplePredicate)
            return Stream.concat(tripleClassDefinitions.values().stream(), triplePropertyDefinitions.values().stream())
                .collect(ImmutableCollectors.toList());
            // TODO: consider quads
        else
            return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<RDFAtomPredicate> getRDFAtomPredicates() {
        return rdfAtomPredicates;
    }
}
