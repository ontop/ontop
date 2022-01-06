package it.unibz.inf.ontop.spec.fact.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.term.BNode;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

@Singleton
public class FactExtractorWithSaturatedTBox extends AbstractFactExtractor {

    private final TermFactory termFactory;
    private final OntopMappingSettings settings;
    private final IRIConstant someValuesFrom, subClassOf, subPropertyOf, domain, range, rdfType;
    private final IRIConstant rdfsClass, owlClass, owlRestriction, onProperty, owlThing;
    private final IRIConstant rdfProperty, objectProperty, dataProperty;
    private final IRIConstant inverseOf;

    @Inject
    protected FactExtractorWithSaturatedTBox(TermFactory termFactory, OntopMappingSettings settings) {
        super(settings);
        this.settings = settings;
        this.termFactory = termFactory;
        someValuesFrom = termFactory.getConstantIRI(OWL.SOME_VALUES_FROM);
        subClassOf = termFactory.getConstantIRI(RDFS.SUBCLASSOF);
        subPropertyOf = termFactory.getConstantIRI(RDFS.SUBPROPERTYOF);
        domain = termFactory.getConstantIRI(RDFS.DOMAIN);
        range = termFactory.getConstantIRI(RDFS.RANGE);
        inverseOf = termFactory.getConstantIRI(OWL.INVERSE_OF);
        rdfType = termFactory.getConstantIRI(RDF.TYPE);
        rdfsClass = termFactory.getConstantIRI(RDFS.CLASS);
        owlClass = termFactory.getConstantIRI(OWL.CLASS);
        owlRestriction = termFactory.getConstantIRI(OWL.RESTRICTION);
        onProperty = termFactory.getConstantIRI(OWL.ON_PROPERTY);
        owlThing = termFactory.getConstantIRI(OWL.THING);
        rdfProperty = termFactory.getConstantIRI(RDF.PROPERTY);
        dataProperty = termFactory.getConstantIRI(OWL.DATATYPE_PROPERTY);
        objectProperty = termFactory.getConstantIRI(OWL.OBJECT_PROPERTY);
    }

    @Override
    protected Stream<RDFFact> extractTbox(ClassifiedTBox tbox) {
        // TODO: also consider simple RDF properties

        ImmutableMap<DescriptionBT, ObjectConstant> expressionIdMap = Stream.concat(Stream.concat(
                generateIdEntries(tbox.classesDAG(), this::generateIdFromClassExpression),
                generateIdEntries(tbox.dataPropertiesDAG(), this::generateIdFromDataPropertyExpression)),
                generateIdEntries(tbox.objectPropertiesDAG(), this::generateIdFromObjectPropertyExpression))
                .filter(v -> !(v.getValue() instanceof BNode)) // Remove BNodes
                .filter(v -> !((v.getValue().getValue().contains("urn:AUX.ROLE")))) // Remove auxiliary object property
                .collect(ImmutableCollectors.toMap());

        // TODO: also consider class disjointness

        EquivalencesDAG<ClassExpression> classDag = tbox.classesDAG();

        return Stream.concat(
                Stream.concat(
                        extractFromDAG(
                                classDag,
                                e -> convertClassExpression(e, expressionIdMap),
                                expressionIdMap,
                                ClassExpression.class,
                                subClassOf),
                        extractFromDAG(
                                tbox.dataPropertiesDAG(),
                                e -> convertDataPropertyExpression(e, expressionIdMap, classDag),
                                expressionIdMap,
                                DataPropertyExpression.class,
                                subPropertyOf)),
                extractFromDAG(
                        tbox.objectPropertiesDAG(),
                        e -> convertObjectPropertyExpression(e, expressionIdMap, classDag),
                        expressionIdMap,
                        ObjectPropertyExpression.class,
                        subPropertyOf));
    }

    private <T extends DescriptionBT> Stream<Map.Entry<DescriptionBT, ObjectConstant>> generateIdEntries(
            EquivalencesDAG<T> dag, Function<T, ObjectConstant> idGenerator) {
        return dag.stream()
                .flatMap(e -> e.getMembers().stream())
                .map(m -> Maps.immutableEntry(m, idGenerator.apply(m)));
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

    private <T extends DescriptionBT> Stream<RDFFact> extractFromDAG(EquivalencesDAG<T> dag,
                                                                     Function<T, Stream<RDFFact>> expressionConverter,
                                                                     ImmutableMap<DescriptionBT, ObjectConstant> expressionIdMap,
                                                                     Class<T> expressionClass,
                                                                     IRIConstant subClassOrSubProperty) {
        return Stream.concat(
                expressionIdMap.keySet().stream()
                        .filter(expressionClass::isInstance)
                        .filter(v -> !(expressionIdMap.get(v) instanceof BNode))
                        .flatMap(e -> expressionConverter.apply((T) e)),
                extractSub(dag, subClassOrSubProperty, expressionIdMap));
    }

    private <T extends DescriptionBT> Stream<RDFFact> extractSub(EquivalencesDAG<T> dag, IRIConstant subPredicateProperty,
                                                                 ImmutableMap<DescriptionBT, ObjectConstant> expressionIdMap) {
        return dag.stream()
                    .flatMap(supEq -> supEq.getMembers().stream()
                            .flatMap(sup -> dag.getSub(supEq).stream()
                                    .flatMap(subEq -> subEq.getMembers().stream()
                                            // Necessary to remove dag elements whose corresponding value from the
                                            // expressionIdMap were BNodes. ExpressionIdMap was filtered.
                                            .filter(sub -> !(isNull(expressionIdMap.get(sub)))
                                                    && !(isNull(expressionIdMap.get(sup))))
                                            .map(sub -> RDFFact.createTripleFact(
                                                    expressionIdMap.get(sub),
                                                    subPredicateProperty,
                                                    expressionIdMap.get(sup))))));
    }

    /**
     * Everything except subClassOf
     */
    private Stream<RDFFact> convertClassExpression(ClassExpression e,
                                                   ImmutableMap<DescriptionBT, ObjectConstant> expressionIdMap) {
        ObjectConstant classId = expressionIdMap.get(e);

        Stream<RDFFact> common = Stream.of(
                RDFFact.createTripleFact(classId, rdfType, rdfsClass),
                RDFFact.createTripleFact(classId, rdfType, owlClass));

        if (e instanceof OClass)
            return common;
        else if (e instanceof ObjectSomeValuesFrom) {
            return Stream.concat(
                    common,
                    extractFactsFromRestriction(
                            classId,
                            expressionIdMap.get(((ObjectSomeValuesFrom) e).getProperty())));
        }
        else if (e instanceof DataSomeValuesFrom) {
            return Stream.concat(
                    common,
                    extractFactsFromRestriction(
                            classId,
                            expressionIdMap.get(((DataSomeValuesFrom) e).getProperty())));
        }
        else
            throw new MinorOntopInternalBugException("Unexpected class expression");
    }

    private Stream<RDFFact> extractFactsFromRestriction(ObjectConstant classId, ObjectConstant propertyId) {
        return Stream.of(
                RDFFact.createTripleFact(classId, rdfType, owlRestriction),
                RDFFact.createTripleFact(classId, onProperty, propertyId),
                RDFFact.createTripleFact(classId, someValuesFrom, owlThing));
    }

    private Stream<RDFFact> convertDataPropertyExpression(DataPropertyExpression e,
                                                          ImmutableMap<DescriptionBT, ObjectConstant> expressionIdMap,
                                                          EquivalencesDAG<ClassExpression> classDag) {
        ObjectConstant propertyId = expressionIdMap.get(e);
        Stream<RDFFact> basics = Stream.of(
                RDFFact.createTripleFact(propertyId, rdfType, rdfProperty),
                RDFFact.createTripleFact(propertyId, rdfType, dataProperty));

        // TODO: consider range?
        return Stream.concat(
                basics,
                e.getAllDomainRestrictions().stream()
                        .flatMap(c -> extractSubDomainOrRange(propertyId, c, domain, expressionIdMap, classDag)));

    }

    /**
     * Everything except subPropertyOf
     */
    private Stream<RDFFact> extractSubDomainOrRange(ObjectConstant propertyId, ClassExpression classExpression,
                                                    IRIConstant rangeOrDomainProperty,
                                                    ImmutableMap<DescriptionBT, ObjectConstant> expressionIdMap,
                                                    EquivalencesDAG<ClassExpression> classDag) {
        Equivalences<ClassExpression> eq = classDag.getVertex(classExpression);

        if (settings.areSuperClassesOfDomainRangeInferred()) {
            return classDag.getSuper(eq).stream()
                    .flatMap(supEq -> supEq.stream()
                            .filter(sup -> sup instanceof OClass)
                            .map(sup -> RDFFact.createTripleFact(propertyId, rangeOrDomainProperty, expressionIdMap.get(sup))));
        } else {
            return classDag.getDirectSuper(eq).stream()
                    .flatMap(supEq -> supEq.stream()
                            .filter(sup -> sup instanceof OClass)
                            .map(sup -> RDFFact.createTripleFact(propertyId, rangeOrDomainProperty, expressionIdMap.get(sup))));
        }

    }

    /**
     * Everything except subPropertyOf
     */
    private Stream<RDFFact> convertObjectPropertyExpression(ObjectPropertyExpression e,
                                                            ImmutableMap<DescriptionBT, ObjectConstant> expressionIdMap,
                                                            EquivalencesDAG<ClassExpression> classDag) {
        ObjectConstant propertyId = expressionIdMap.get(e);

        return Stream.concat(
                Stream.concat(
                Stream.concat(
                Stream.concat(
                        Stream.of(
                            RDFFact.createTripleFact(propertyId, rdfType, rdfProperty),
                            RDFFact.createTripleFact(propertyId, rdfType, objectProperty)),
                extractSubDomainOrRange(propertyId, e.getDomain(), domain, expressionIdMap, classDag)),
                extractSubDomainOrRange(propertyId, e.getRange(), range, expressionIdMap, classDag)),
                extractFactsFromSubClassRestriction(propertyId, e.getRange(), expressionIdMap, classDag)),
                extractInverse(propertyId, e, expressionIdMap, classDag));
    }

    private Stream<RDFFact> extractFactsFromSubClassRestriction(ObjectConstant propertyId, ClassExpression classExpression,
                                                                ImmutableMap<DescriptionBT, ObjectConstant> expressionIdMap,
                                                                EquivalencesDAG<ClassExpression> classDag) {
        Equivalences<ClassExpression> eq = classDag.getVertex(classExpression);

        boolean restrictionPresent = (classDag.getDirectSub(eq).stream()
                .map(c -> c.toString())
                .anyMatch(c -> c.contains("urn:AUX.ROLE"))) &&
                // Avoid scenarios of functional property declaration by checking already defined domain/range
                // Only auxiliary object property connects restriction to graph
                (eq.getMembers().size()==1) &&
                (classDag.getDirectSuper(eq).size()==0);

        if (restrictionPresent) {
            BNode newBNode = termFactory.getConstantBNode(UUID.randomUUID().toString());
            Stream<RDFFact> restriction = Stream.of(RDFFact.createTripleFact(newBNode, rdfType, owlRestriction));
            Stream<RDFFact> restrictionOnProperty = Stream.of(RDFFact.createTripleFact(newBNode, onProperty, propertyId));
            Stream<RDFFact> restrictionSomeValuesFrom = classDag.getDirectSub(eq).stream()
                    .flatMap(sup -> classDag.getSuper(sup).stream()
                            .flatMap(supEq2 -> supEq2.stream()
                                    .filter(sup2 -> sup2 instanceof OClass)
                                    .map(sup3 -> RDFFact.createTripleFact(newBNode, someValuesFrom, expressionIdMap.get(sup3)))
                            )
                    );

            Stream<RDFFact> newBNodeclasses = Stream.of(
                    RDFFact.createTripleFact(newBNode, rdfType, rdfsClass),
                    RDFFact.createTripleFact(newBNode, rdfType, owlClass));

            Stream<RDFFact> restrictionSubClasses = classDag.getSub(eq).stream()
                    .flatMap(supEq -> supEq.stream()
                            .filter(sup -> sup instanceof OClass)
                            .map(sup -> RDFFact.createTripleFact(expressionIdMap.get(sup), subClassOf, newBNode)));

            return Stream.concat(restriction,
                    Stream.concat(restrictionOnProperty,
                            Stream.concat(restrictionSomeValuesFrom,
                                    Stream.concat(newBNodeclasses, restrictionSubClasses))));
        }

        return Stream.of();
    }

    private Stream<RDFFact> extractInverse(ObjectConstant propertyId, ObjectPropertyExpression e,
                                                                ImmutableMap<DescriptionBT, ObjectConstant> expressionIdMap,
                                                                EquivalencesDAG<ClassExpression> classDag) {
        Equivalences<ClassExpression> eq = classDag.getVertex(e.getInverse().getDomain());

        return eq.getMembers().stream()
                .filter(m -> m instanceof ObjectSomeValuesFrom)
                .map(m -> (ObjectSomeValuesFrom) m)
                .filter(m -> !m.getProperty().isInverse()) //Drop the inverse of itself generated by Ontop
                .map(m -> RDFFact.createTripleFact(propertyId, inverseOf, expressionIdMap.get(m.getProperty())));
    }
}
