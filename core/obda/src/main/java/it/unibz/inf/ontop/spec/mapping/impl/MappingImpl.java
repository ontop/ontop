package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.stream.Stream;


public class MappingImpl implements Mapping {

    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyDefinitions;
    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> classDefinitions;

    public MappingImpl(ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable,
                        ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable) {

        this.propertyDefinitions = propertyTable;
        this.classDefinitions = classTable;
    }

    @Override
    public Optional<IQ> getRDFPropertyDefinition(RDFAtomPredicate rdfAtomPredicate, IRI propertyIRI) {
        return Optional.ofNullable(propertyDefinitions.get(rdfAtomPredicate, propertyIRI));
    }

    @Override
    public Optional<IQ> getRDFClassDefinition(RDFAtomPredicate rdfAtomPredicate, IRI classIRI) {
        return Optional.ofNullable(classDefinitions.get(rdfAtomPredicate, classIRI));
    }

    @Override
    public ImmutableSet<IRI> getRDFProperties(RDFAtomPredicate rdfAtomPredicate) {
        return Optional.ofNullable(propertyDefinitions.rowMap().get(rdfAtomPredicate))
                .map(m -> ImmutableSet.copyOf(m.keySet()))
                .orElseGet(ImmutableSet::of);
    }

    @Override
    public ImmutableSet<IRI> getRDFClasses(RDFAtomPredicate rdfAtomPredicate) {
        return Optional.ofNullable(classDefinitions.rowMap().get(rdfAtomPredicate))
                .map(m -> ImmutableSet.copyOf(m.keySet()))
                .orElseGet(ImmutableSet::of);
    }

    @Override
    public ImmutableCollection<IQ> getQueries(RDFAtomPredicate rdfAtomPredicate) {
        return Stream.concat(classDefinitions.row(rdfAtomPredicate).values().stream(),
                    propertyDefinitions.row(rdfAtomPredicate).values().stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableSet<RDFAtomPredicate> getRDFAtomPredicates() {
        return Sets.union(propertyDefinitions.rowKeySet(), classDefinitions.rowKeySet())
                .immutableCopy();
    }

}
