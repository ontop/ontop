package it.unibz.inf.ontop.spec.fact.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

@Singleton
public class FactExtractorWithSaturatedTBox extends AbstractFactExtractor {

    private final TermFactory termFactory;
    private final IRIConstant someValuesFrom, subClassOf, subPropertyOf, domain, range;

    @Inject
    protected FactExtractorWithSaturatedTBox(TermFactory termFactory, OntopMappingSettings settings) {
        super(settings);
        this.termFactory = termFactory;
        someValuesFrom = termFactory.getConstantIRI(OWL.SOME_VALUES_FROM);
        subClassOf = termFactory.getConstantIRI(RDFS.SUBCLASSOF);
        subPropertyOf = termFactory.getConstantIRI(RDFS.SUBPROPERTYOF);
        domain = termFactory.getConstantIRI(RDFS.DOMAIN);
        range = termFactory.getConstantIRI(RDFS.RANGE);
    }

    @Override
    protected Stream<RDFFact> extractTbox(ClassifiedTBox tbox) {
        // TODO: also consider simple RDF properties
        return Stream.concat(
                Stream.concat(
                        extractFromDAG(
                                tbox.classesDAG(),
                                this::generateIdFromClassExpression,
                                this::convertClassExpression,
                                subClassOf),
                        extractFromDAG(
                                tbox.dataPropertiesDAG(),
                                this::generateIdFromDataPropertyExpression,
                                this::convertDataPropertyExpression,
                                subPropertyOf
                                )),
                extractFromDAG(
                        tbox.objectPropertiesDAG(),
                        this::generateIdFromObjectPropertyExpression,
                        this::convertObjectPropertyExpression,
                        subPropertyOf));
    }

    private ObjectConstant generateIdFromClassExpression(ClassExpression expression) {
        if (expression instanceof OClass)
            return termFactory.getConstantIRI(((OClass) expression).getIRI());
        else
            // TODO: use a better name (make sure expression.toString() is safe
            return termFactory.getConstantBNode(UUID.randomUUID().toString());
    }

    private ObjectConstant generateIdFromDataPropertyExpression(DataPropertyExpression expression) {
        return termFactory.getConstantIRI(expression.getIRI());
    }

    private ObjectConstant generateIdFromObjectPropertyExpression(ObjectPropertyExpression expression) {
        return expression.isInverse()
                ? termFactory.getConstantBNode("op-inv" + expression.getIRI())
                : termFactory.getConstantIRI(expression.getIRI());
    }

    private Stream<RDFFact> convertClassExpression(ClassExpression e, ObjectConstant objectConstant) {
        throw new RuntimeException("TODO: continue");
    }

    private Stream<RDFFact> convertDataPropertyExpression(DataPropertyExpression e, ObjectConstant objectConstant) {
        throw new RuntimeException("TODO: continue");
    }

    private Stream<RDFFact> convertObjectPropertyExpression(ObjectPropertyExpression e, ObjectConstant objectConstant) {
        throw new RuntimeException("TODO: continue");
    }

    private <T extends DescriptionBT> Stream<RDFFact> extractFromDAG(EquivalencesDAG<T> dag,
                                                                     Function<T, ObjectConstant> idGenerator,
                                                                     BiFunction<T, ObjectConstant, Stream<RDFFact>> expressionConverter,
                                                                     IRIConstant subPredicateProperty) {
        ImmutableMap<T, ObjectConstant> expressionIdMap = dag.stream()
                .flatMap(e -> e.getMembers().stream())
                .collect(ImmutableCollectors.toMap(
                        m -> m,
                        idGenerator));

        Stream<RDFFact> expressionFacts = expressionIdMap.entrySet().stream()
                .flatMap(e -> expressionConverter.apply(e.getKey(), e.getValue()));

        // All subPropertyOf or subClassOf (after transitive closure)
        Stream<RDFFact> subFacts = dag.stream()
                .flatMap(supEq -> supEq.getMembers().stream()
                        .flatMap(sup -> dag.getSub(supEq).stream()
                                .flatMap(subEq -> subEq.getMembers().stream()
                                        .map(sub -> RDFFact.createTripleFact(
                                                expressionIdMap.get(sub),
                                                subPredicateProperty,
                                                expressionIdMap.get(sup))))));

        return Stream.concat(expressionFacts, subFacts);
    }
}
