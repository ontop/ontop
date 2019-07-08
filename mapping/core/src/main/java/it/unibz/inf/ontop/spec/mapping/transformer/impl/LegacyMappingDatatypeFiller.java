package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.datalog.impl.DatalogRule2QueryConverter;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.iq.transform.impl.ChildTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.AND;

/**
 * Legacy code to infer datatypes not declared in the targets of mapping assertions.
 * Types are inferred from the DB metadata.
 * TODO: rewrite in a Datalog independent fashion
 */
public class LegacyMappingDatatypeFiller implements MappingDatatypeFiller {


    private final OntopMappingSettings settings;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final TermTypeInferenceTools termTypeInferenceTools;
    private final ImmutabilityTools immutabilityTools;
    private final QueryUnionSplitter unionSplitter;
    private final IQ2DatalogTranslator iq2DatalogTranslator;
    private final UnionFlattener unionNormalizer;
    private final IntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provMappingFactory;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final DatalogRule2QueryConverter datalogRule2QueryConverter;

    @Inject
    private LegacyMappingDatatypeFiller(OntopMappingSettings settings, Relation2Predicate relation2Predicate,
                                        TermFactory termFactory, TypeFactory typeFactory,
                                        TermTypeInferenceTools termTypeInferenceTools, ImmutabilityTools immutabilityTools, QueryUnionSplitter unionSplitter, IQ2DatalogTranslator iq2DatalogTranslator, UnionFlattener unionNormalizer, IntermediateQueryFactory iqFactory, ProvenanceMappingFactory provMappingFactory, NoNullValueEnforcer noNullValueEnforcer, DatalogRule2QueryConverter datalogRule2QueryConverter) {
        this.settings = settings;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.termTypeInferenceTools = termTypeInferenceTools;
        this.immutabilityTools = immutabilityTools;
        this.unionSplitter = unionSplitter;
        this.iq2DatalogTranslator = iq2DatalogTranslator;
        this.unionNormalizer = unionNormalizer;
        this.iqFactory = iqFactory;
        this.provMappingFactory = provMappingFactory;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.datalogRule2QueryConverter = datalogRule2QueryConverter;
    }

    /***
     * Infers missing data types.
     * For each rule, gets the type from the rule head, and if absent, retrieves the type from the metadata.
     *
     * The behavior of type retrieval is the following for each rule:
     * . build a "termOccurrenceIndex", which is a map from variables to body atoms + position.
     * For ex, consider the rule: C(x, y) <- A(x, y) \wedge B(y)
     * The "termOccurrenceIndex" map is {
     *  x \mapsTo [<A(x, y), 1>],
     *  y \mapsTo [<A(x, y), 2>, <B(y), 1>]
     *  }
     *  . then take the first occurrence each variable (e.g. take <A(x, y), 2> for variable y),
     *  and assign to the variable the corresponding column type in the DB
     *  (e.g. for y, the type of column 1 of table A).
     *  . then inductively infer the types of functions (e.g. concat, ...) from the variable types.
     *  Only the outermost expression is assigned a type.
     *
     *  Assumptions:
     *  .rule body atoms are extensional
     *  .the corresponding column types are compatible (e.g the types for column 1 of A and column 1 of B)
     */

    public static boolean PRINT_OUT = false;

    @Override
    public MappingWithProvenance inferMissingDatatypes(MappingWithProvenance mapping) throws UnknownDatatypeException {
        MappingDataTypeCompletion typeCompletion = new MappingDataTypeCompletion(
                settings.isDefaultDatatypeInferred(), termFactory, typeFactory, termTypeInferenceTools, immutabilityTools);

        if (PRINT_OUT)
            System.out.println("MAPPP: " + mapping.getProvenanceMap());

        try {
            ImmutableMap<IQ, PPMappingAssertionProvenance> iqMap = mapping.getProvenanceMap().entrySet().stream()
                    .filter(e -> !e.getKey().getTree().isDeclaredAsEmpty())
                    .flatMap(e -> (MappingTools.extractRDFPredicate(e.getKey()).isClass()
                            ? Stream.of(iqFactory.createIQ(e.getKey().getProjectionAtom(), e.getKey().getTree().acceptTransformer(new FilterChildNormalizer())))
                            : inferMissingDatatypes(e.getKey(), typeCompletion))
                                .map(iq -> new AbstractMap.SimpleEntry<>(iq, e.getValue())))
                    .collect(ImmutableCollectors.toMap());

            if (PRINT_OUT)
                System.out.println("IQMAP: " + iqMap);

            return provMappingFactory.create(iqMap, mapping.getMetadata());
        }
        catch (MappingDataTypeCompletion.UnknownDatatypeRuntimeException e) {
            throw new UnknownDatatypeException(e.getMessage());
        }
    }

    private Stream<IQ> inferMissingDatatypes(IQ iq0, MappingDataTypeCompletion typeCompletion) {
        return unionSplitter.splitUnion(unionNormalizer.optimize(iq0))
                .filter(iq -> !iq.getTree().isDeclaredAsEmpty())
                .flatMap(q -> iq2DatalogTranslator.translate(q).getRules().stream())
                .map(rule -> {
                    //CQIEs are mutable
                    Function atom = rule.getHead();
                    //case of data and object property
                    //if (!typeCompletion.isURIRDFType(atom.getTerm(1))) {
                        Term object = atom.getTerm(2); // the object, third argument only
                        ImmutableMultimap<Variable, Attribute> termOccurenceIndex = typeCompletion.createIndex(rule.getBody());
                        // Infer variable datatypes
                        typeCompletion.insertVariableDataTyping(object, atom, 2, termOccurenceIndex);
                        // Infer operation datatypes from variable datatypes
                        typeCompletion.insertOperationDatatyping(object, atom, 2);
                    //}
                    return rule;
                })
                .map(rule -> noNullValueEnforcer.transform(
                        datalogRule2QueryConverter.extractPredicatesAndConvertDatalogRule(
                                rule, iqFactory).liftBinding())).distinct();

    }


    // PINCHED FROM ExplicitEqualityTransformerImpl
    // TODO: extract as an independent class

    /**
     * Affects each outermost filter or (left) join n in the tree.
     * For each child of n, deletes its root if it is a filter node.
     * Then:
     * - if n is a join or filter: merge the boolean expressions
     * - if n is a left join: merge boolean expressions coming from the right, and lift the ones coming from the left.
     * This lift is only performed for optimization purposes: may save a subquery during SQL generation.
     */
    class FilterChildNormalizer extends DefaultNonRecursiveIQTreeTransformer {

        private final ChildTransformer childTransformer;

        public FilterChildNormalizer() {
            this.childTransformer = new ChildTransformer(iqFactory, this);
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {

            Optional<ImmutableExpression> leftChildChildExpression = getOptionalChildExpression(leftChild);
            Optional<ImmutableExpression> rightChildExpression = getOptionalChildExpression(rightChild);

            if (!leftChildChildExpression.isPresent() && !rightChildExpression.isPresent())
                return tree;

            IQTree leftJoinTree = iqFactory.createBinaryNonCommutativeIQTree(
                    rightChildExpression.isPresent()
                            ? iqFactory.createLeftJoinNode(getConjunction(
                            rootNode.getOptionalFilterCondition(),
                            ImmutableList.of(rightChildExpression.get())))
                            : rootNode,
                    trimRootFilter(leftChild),
                    trimRootFilter(rightChild));

            return leftChildChildExpression.isPresent()
                    ? iqFactory.createUnaryIQTree(iqFactory.createFilterNode(leftChildChildExpression.get()), leftJoinTree)
                    : leftJoinTree;
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ImmutableExpression> filterChildExpressions = getChildExpressions(children);
            if (filterChildExpressions.isEmpty())
                return tree;

            return iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(getConjunction(
                            rootNode.getOptionalFilterCondition(),
                            filterChildExpressions)),
                    children.stream()
                            .map(this::trimRootFilter)
                            .collect(ImmutableCollectors.toList()));
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            ImmutableList<ImmutableExpression> filterChildExpressions = getChildExpressions(ImmutableList.of(child));
            if (filterChildExpressions.isEmpty())
                return tree;

            return iqFactory.createUnaryIQTree(
                    iqFactory.createFilterNode(getConjunction(
                            Optional.of(rootNode.getFilterCondition()),
                            filterChildExpressions).get()),
                    trimRootFilter(child));
        }

        private ImmutableList<ImmutableExpression> getChildExpressions(ImmutableList<IQTree> children) {
            return children.stream()
                    .filter(t -> t.getRootNode() instanceof FilterNode)
                    .map(t -> ((FilterNode) t.getRootNode()).getFilterCondition())
                    .collect(ImmutableCollectors.toList());
        }

        private Optional<ImmutableExpression> getOptionalChildExpression(IQTree child) {
            QueryNode root = child.getRootNode();
            return root instanceof FilterNode
                    ? Optional.of(((FilterNode) root).getFilterCondition())
                    : Optional.empty();
        }

        private IQTree trimRootFilter(IQTree tree) {
            return tree.getRootNode() instanceof FilterNode
                    ? ((UnaryIQTree) tree).getChild()
                    : tree;
        }

        protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return childTransformer.transform(tree);
        }

        protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return childTransformer.transform(tree);
        }

        protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            return childTransformer.transform(tree);
        }

    }

    private Optional<ImmutableExpression> getConjunction(Optional<ImmutableExpression> optExpression, List<ImmutableExpression> expressions) {
        if (expressions.isEmpty())
            throw new IllegalArgumentException("Nonempty list of filters expected");

        ImmutableExpression result = (optExpression.isPresent()
                ? Stream.concat(Stream.of(optExpression.get()), expressions.stream())
                : expressions.stream())
                .reduce(null,
                        (a, b) -> (a == null) ? b : termFactory.getImmutableExpression(AND, a, b));
        return Optional.of(result);
    }


}
