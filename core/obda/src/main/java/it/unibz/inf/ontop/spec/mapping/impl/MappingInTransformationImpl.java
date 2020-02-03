package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.stream.Stream;

public class MappingInTransformationImpl implements MappingInTransformation  {

    private final SpecificationFactory specificationFactory;
    private final ImmutableSet<RDFAtomPredicate> rdfAtomPredicates;
    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyDefinitions;
    private final ImmutableTable<RDFAtomPredicate, IRI, IQ> classDefinitions;

    @AssistedInject
    private MappingInTransformationImpl(
                        @Assisted("propertyTable") ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyTable,
                        @Assisted("classTable") ImmutableTable<RDFAtomPredicate, IRI, IQ> classTable,
                        OntopModelSettings settings,
                        SpecificationFactory specificationFactory) {
        this.propertyDefinitions = propertyTable;
        this.classDefinitions = classTable;
        this.specificationFactory = specificationFactory;

        if (settings.isTestModeEnabled()) {
            for (IQ query : propertyDefinitions.values()) {
                checkNullableVariables(query);
            }
            for (IQ query : classDefinitions.values()) {
                checkNullableVariables(query);
            }
        }

        rdfAtomPredicates = Sets.union(propertyTable.rowKeySet(), classTable.rowKeySet())
                .immutableCopy();
    }

    private static void checkNullableVariables(IQ query) throws NullableVariableInMappingException {
        VariableNullability variableNullability = query.getTree().getVariableNullability();
        if (!variableNullability.getNullableGroups().isEmpty())
            throw new NullableVariableInMappingException(query, variableNullability.getNullableGroups());
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
    public Mapping getMapping() {
        return new MappingImpl(propertyDefinitions, classDefinitions);
    }

    @Override
    public ImmutableSet<Table.Cell<RDFAtomPredicate, IRI, IQ>> getRDFPropertyQueries() {
        return propertyDefinitions.cellSet();
    }

    @Override
    public ImmutableSet<Table.Cell<RDFAtomPredicate, IRI, IQ>> getRDFClassQueries() {
        return classDefinitions.cellSet();
    }


    @Override
    public ImmutableCollection<IQ> getQueries(RDFAtomPredicate rdfAtomPredicate) {
        return Stream.concat(classDefinitions.row(rdfAtomPredicate).values().stream(),
                propertyDefinitions.row(rdfAtomPredicate).values().stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableSet<RDFAtomPredicate> getRDFAtomPredicates() {
        return rdfAtomPredicates;
    }

    @Override
    public MappingInTransformation update(ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyUpdateTable,
                          ImmutableTable<RDFAtomPredicate, IRI, IQ> classUpdateTable) {
        ImmutableTable<RDFAtomPredicate, IRI, IQ> newPropertyDefs =
                propertyUpdateTable.isEmpty()
                        ? propertyDefinitions
                        : updateDefinitions(propertyDefinitions, propertyUpdateTable);

        ImmutableTable<RDFAtomPredicate, IRI, IQ> newTripleClassDefs =
                classUpdateTable.isEmpty()
                        ? classDefinitions
                        : updateDefinitions(classDefinitions, classUpdateTable);

        return specificationFactory.createMapping(newPropertyDefs, newTripleClassDefs);
    }

    private ImmutableTable<RDFAtomPredicate, IRI, IQ> updateDefinitions(ImmutableTable<RDFAtomPredicate, IRI, IQ> currentTable,
                                                                        ImmutableTable<RDFAtomPredicate, IRI, IQ> updateTable) {
        return Stream.concat(
                updateTable.cellSet().stream(),
                currentTable.cellSet().stream()
                        .filter(c -> !updateTable.contains(c.getRowKey(), c.getColumnKey())))
                .collect(ImmutableCollectors.toTable());
    }

}
