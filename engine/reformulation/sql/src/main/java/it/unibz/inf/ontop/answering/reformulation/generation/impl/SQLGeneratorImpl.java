package it.unibz.inf.ontop.answering.reformulation.generation.impl;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.NotFullyTranslatableToNativeQueryException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.optimizer.splitter.ProjectionSplitter;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.iq.transform.IQTree2NativeNodeGenerator;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.generation.PostProcessingProjectionSplitter;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.datalog.UnionFlattener;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.PostProcessableFunctionLifter;
import it.unibz.inf.ontop.iq.optimizer.TermTypeTermLifter;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.iq.transformer.EmptyRowsValuesNodeTransformer;
import it.unibz.inf.ontop.iq.transformer.ExplicitEqualityTransformer;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIfElseNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryIQTreeDecomposition;

/**
 * TODO: explain
 *
 * See TranslationFactory for creating a new instance.
 *
 */
public class SQLGeneratorImpl implements NativeQueryGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLGeneratorImpl.class);
    private final DBParameters dbParameters;
    private final IntermediateQueryFactory iqFactory;
    private final UnionFlattener unionFlattener;
    private final PostProcessingProjectionSplitter projectionSplitter;
    private final TermTypeTermLifter rdfTypeLifter;
    private final PostProcessableFunctionLifter functionLifter;
    private final IQTree2NativeNodeGenerator defaultIQTree2NativeNodeGenerator;
    private final OntopReformulationSQLSettings settings;
    private final DialectExtraNormalizer extraNormalizer;
    private final BooleanExpressionPushDownTransformer pushDownTransformer;
    private final EmptyRowsValuesNodeTransformer valuesNodeTransformer;
    private final ExplicitEqualityTransformer equalityTransformer;
    private final IQTreeTools iqTreeTools;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @AssistedInject
    private SQLGeneratorImpl(@Assisted DBParameters dbParameters,
                             UnionFlattener unionFlattener,
                             PostProcessingProjectionSplitter projectionSplitter,
                             TermTypeTermLifter rdfTypeLifter, PostProcessableFunctionLifter functionLifter,
                             IQTree2NativeNodeGenerator defaultIQTree2NativeNodeGenerator,
                             DialectExtraNormalizer extraNormalizer, BooleanExpressionPushDownTransformer pushDownTransformer,
                             EmptyRowsValuesNodeTransformer valuesNodeTransformer,
                             OntopReformulationSQLSettings settings, ExplicitEqualityTransformer equalityTransformer,
                             CoreSingletons coreSingletons)
    {
        this.functionLifter = functionLifter;
        this.extraNormalizer = extraNormalizer;
        this.pushDownTransformer = pushDownTransformer;
        this.valuesNodeTransformer = valuesNodeTransformer;
        this.dbParameters = dbParameters;
        this.iqFactory = coreSingletons.getIQFactory();
        this.unionFlattener = unionFlattener;
        this.projectionSplitter = projectionSplitter;
        this.rdfTypeLifter = rdfTypeLifter;
        this.defaultIQTree2NativeNodeGenerator = defaultIQTree2NativeNodeGenerator;
        this.settings = settings;
        this.equalityTransformer = equalityTransformer;
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

    @Override
    public IQ generateSourceQuery(IQ query) {
        return generateSourceQuery(query, settings.isPostProcessingAvoided());
    }

    @Override
    public IQ generateSourceQuery(boolean forNativeConsumption, IQ query) throws OntopReformulationException {
        return generateSourceQuery(forNativeConsumption, query, forNativeConsumption || settings.isPostProcessingAvoided());
    }

    @Override
    public IQ generateSourceQuery(IQ query, boolean avoidPostProcessing) {
        return generateSourceQuery(query, avoidPostProcessing, false, Function.identity());
    }

    @Override
    public IQ generateSourceQuery(IQ query, boolean avoidPostProcessing, boolean tolerateUnknownTypes) {
        return generateSourceQuery(query, avoidPostProcessing, tolerateUnknownTypes, Function.identity());
    }

    protected IQ generateSourceQuery(boolean forNativeConsumption, IQ query, boolean avoidPostProcessing)
            throws NotFullyTranslatableToNativeQueryException {
        if ((!avoidPostProcessing) && forNativeConsumption)
            throw new MinorOntopInternalBugException("The avoidPostProcessing option cannot be false when forNativeConsumption is true");

        return forNativeConsumption
                ? generateSourceQueryForNativeConsumption(query)
                : generateSourceQuery(query, avoidPostProcessing, false, Function.identity());
    }

    protected IQ generateSourceQuery(IQ query, boolean avoidPostProcessing, boolean tolerateUnknownTypes,
                                     Function<ImmutableSet<Variable>, ImmutableSet<Variable>> signatureExtractor) {
        IQTree initialTree = query.getTree();
        if (initialTree.isDeclaredAsEmpty())
            return query;

        VariableGenerator variableGenerator = query.getVariableGenerator();

        IQTree rdfTypeLiftedTree = rdfTypeLifter.transform(initialTree, variableGenerator);
        LOGGER.debug("After lifting the RDF types:\n{}\n", rdfTypeLiftedTree);

        IQTree liftedTree = functionLifter.transform(rdfTypeLiftedTree, variableGenerator);
        LOGGER.debug("After lifting the post-processable function symbols:\n{}\n", liftedTree);

        ProjectionSplitter.ProjectionSplit split = projectionSplitter.split(liftedTree, variableGenerator, avoidPostProcessing);

        IQTree normalizedSubTree = normalizeSubTree(split.getSubTree(), variableGenerator);
        // Late detection of emptiness
        if (normalizedSubTree.isDeclaredAsEmpty())
            return iqFactory.createIQ(query.getProjectionAtom(),
                    iqFactory.createEmptyNode(query.getProjectionAtom().getVariables()));

        NativeNode nativeNode = defaultIQTree2NativeNodeGenerator.generate(normalizedSubTree,
                signatureExtractor.apply(normalizedSubTree.getVariables()),
                dbParameters, tolerateUnknownTypes);

        UnaryIQTree newTree = iqFactory.createUnaryIQTree(split.getConstructionNode(), nativeNode);

        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    /**
     * TODO: what about the distinct?
     * TODO: move the distinct and slice lifting to the post-processing splitter
     */
    private IQTree normalizeSubTree(IQTree subTree, VariableGenerator variableGenerator) {

        IQTree sliceLiftedTree = liftSlice(subTree);
        LOGGER.debug("New query after lifting the slice:\n{}\n", sliceLiftedTree);

        // ORDER BY lifting
        // pattern CONSTRUCTION, DISTINCT, ORDER BY becomes CONSTRUCTION, ORDER BY, DISTINCT
        IQTree treeAfterOrderByLifting = liftOrderByAboveDistinct(sliceLiftedTree);
        LOGGER.debug("New query after lifting order by above distinct:\n{}\n", treeAfterOrderByLifting);

        // TODO: check if still needed
        IQTree flattenSubTree = unionFlattener.transform(treeAfterOrderByLifting, variableGenerator);
        LOGGER.debug("New query after flattening the union:\n{}\n", flattenSubTree);

        IQTree pushedDownSubTree = pushDownTransformer.transform(flattenSubTree);
        LOGGER.debug("New query after pushing down:\n{}\n", pushedDownSubTree);

        IQTree treeAfterPullOut = equalityTransformer.transform(pushedDownSubTree, variableGenerator);
        LOGGER.debug("Query tree after pulling out equalities:\n{}\n", treeAfterPullOut);

        // Top construction elimination when it causes problems
        // Pattern: [LIMIT], CONSTRUCTION, DISTINCT, [CONSTRUCTION] and ORDER BY
        IQTree treeAfterTopConstructionNormalization = dropTopConstruct(treeAfterPullOut);
        LOGGER.debug("New query after top construction elimination in order by cases: \n" + treeAfterTopConstructionNormalization);

        // Handle VALUES [] () () edge case
        IQTree treeAfterEmptyRowsValuesNodeNormalization = valuesNodeTransformer.transform(
                treeAfterTopConstructionNormalization, variableGenerator);
        LOGGER.debug("New query after empty rows values node transformation:\n{}\n", treeAfterEmptyRowsValuesNodeNormalization);

        // Dialect specific
        IQTree afterDialectNormalization = extraNormalizer.transform(treeAfterEmptyRowsValuesNodeNormalization, variableGenerator);
        LOGGER.debug("New query after the dialect-specific extra normalization:\n{}\n", afterDialectNormalization);

        return afterDialectNormalization;
    }

    private IQTree liftSlice(IQTree subTree) {
        var construction = UnaryIQTreeDecomposition.of(subTree, ConstructionNode.class);
        var slice = UnaryIQTreeDecomposition.of(construction, SliceNode.class);
        if (construction.isPresent() && slice.isPresent()) {
            return iqTreeTools.unaryIQTreeBuilder()
                    .append(slice.getNode())
                    .append(construction.getNode())
                    .build(slice.getChild());
        }
        return subTree;
    }

    private IQTree dropTopConstruct(IQTree subTree) {
        // Check for the pattern [LIMIT] CONSTRUCT DISTINCT [CONSTRUCT2] ORDER BY
        var slice = UnaryIQTreeDecomposition.of(subTree, SliceNode.class);
        var construction = UnaryIQTreeDecomposition.of(slice, ConstructionNode.class);
        var distinct = UnaryIQTreeDecomposition.of(construction, DistinctNode.class);
        var construction2 = UnaryIQTreeDecomposition.of(distinct, ConstructionNode.class);
        var orderBy = UnaryIQTreeDecomposition.of(construction2, OrderByNode.class);
        // If there is variable substitution in the top construction do not normalize
        if (construction.isPresent() && construction.getNode().getSubstitution().isEmpty()) {
            if (distinct.isPresent()  && orderBy.isPresent()) {
                // Drop the top construction node
                return iqTreeTools.unaryIQTreeBuilder()
                        .append(slice.getOptionalNode())
                        .build(construction.getChild());
            }
        }
        return subTree;
    }

    private IQTree liftOrderByAboveDistinct(IQTree subTree) {
        var construction = UnaryIQTreeDecomposition.of(subTree, ConstructionNode.class);
        var distinct = UnaryIQTreeDecomposition.of(construction, DistinctNode.class);
        var orderBy = UnaryIQTreeDecomposition.of(distinct, OrderByNode.class);

        if (construction.isPresent() && distinct.isPresent() && orderBy.isPresent()) {
            return iqTreeTools.unaryIQTreeBuilder()
                    .append(construction.getNode())
                    .append(orderBy.getNode())
                    .append(distinct.getNode())
                    .build(orderBy.getChild());
        }
        return subTree;
    }

    protected IQ generateSourceQueryForNativeConsumption(IQ query) throws NotFullyTranslatableToNativeQueryException {

        DistinctVariableOnlyDataAtom initialProjectionAtom = query.getProjectionAtom();
        IQTree initialTree = query.getTree();

        Substitution<ImmutableTerm> definitions = extractDefinitions(initialTree);
        ImmutableMap<Variable, RDFTermType> rdfTypes = extractRDFTypes(definitions);

        IQTree dbTree = replaceRDFByDBTerms(initialTree, rdfTypes);

        LOGGER.debug("Producing the native query string...");

        IQ dbIQ = iqFactory.createIQ(initialProjectionAtom, dbTree);

        IQTree nativeTree = generateSourceQuery(dbIQ, true, false,
                // Preserves the variable order from the input query
                vs -> initialProjectionAtom.getVariables())
                .normalizeForOptimization().getTree();

        if (!(nativeTree instanceof NativeNode))
            throw new NotFullyTranslatableToNativeQueryException("the post-processing step could not be eliminated");

        NativeNode nativeNode = (NativeNode) nativeTree;

        ImmutableMap<Variable, DBTermType> dbTypeMap = nativeNode.getTypeMap();

        /*
         * HACK! The construction node reuses the same variables for the constructed RDF terms and the DB ones.
         * This is illegal but does the trick, as the IQTree does not get normalized anymore at that stage.
         *
         * TODO: find an alternative to that hack while preserving good variable names in the native query.
         *
         */

        ConstructionNode postProcessingToRDFNode = iqFactory.createConstructionNodeForNativeQuery(
                nativeTree.getVariables().stream()
                        .collect(substitutionFactory.toSubstitution(
                                v -> termFactory.getRDFFunctionalTerm(
                                        termFactory.getConversion2RDFLexical(
                                                Optional.ofNullable(dbTypeMap.get(v))
                                                        .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting a type from the native node")),
                                                v,
                                                Optional.ofNullable(rdfTypes.get(v))
                                                        .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting an RDF type"))),
                                        termFactory.getRDFTermTypeConstant(rdfTypes.get(v))))));

        IQTree executableTree = iqFactory.createUnaryIQTree(
                postProcessingToRDFNode,
                nativeTree);

        IQ executableQuery = iqFactory.createIQ(dbIQ.getProjectionAtom(), executableTree);

        LOGGER.debug("Resulting native query:\n{}\n", executableQuery);

        return executableQuery;
    }

    private IQTree replaceRDFByDBTerms(IQTree tree, ImmutableMap<Variable, RDFTermType> rdfTypes) {
        if (rdfTypes.isEmpty())
            return tree;

        var slice = UnaryIQTreeDecomposition.of(tree, SliceNode.class);
        var construction = UnaryIQTreeDecomposition.of(slice, ConstructionNode.class);
        if (!construction.isPresent())
            throw new MinorOntopInternalBugException("Unexpected tree shape (proper exception should have already been thrown)");

        ConstructionNode newConstructionNode = iqTreeTools.replaceSubstitution(
                construction.getNode(),
                s -> s.builder()
                        .transform(rdfTypes::get, this::replaceRDFByDBTerm)
                        .build());

        return iqTreeTools.unaryIQTreeBuilder()
                .append(slice.getOptionalNode())
                .append(newConstructionNode)
                .build(construction.getTail());
    }

    private Substitution<ImmutableTerm> extractDefinitions(IQTree rdfTree) throws NotFullyTranslatableToNativeQueryException {
        if (rdfTree.getVariables().isEmpty())
            return substitutionFactory.getSubstitution();

        var slice = UnaryIQTreeDecomposition.of(rdfTree, SliceNode.class);
        var construction = UnaryIQTreeDecomposition.of(slice, ConstructionNode.class);
        if (!construction.isPresent())
            throw new NotFullyTranslatableToNativeQueryException("was expected to have an extended projection at the top. IQ: " + rdfTree);

        Substitution<ImmutableTerm> substitution = construction.getNode().getSubstitution();
        // NB: should not include any non-projected variable (illegal IQ)
        Set<Variable> missingVariables = Sets.difference(rdfTree.getVariables(), substitution.getDomain());
        if (!missingVariables.isEmpty())
            throw new NotFullyTranslatableToNativeQueryException(String.format(
                    "the variables %s are missing an independent definition",
                    missingVariables));

        return substitution;
    }


    private ImmutableTerm replaceRDFByDBTerm(ImmutableTerm definition,
                                             RDFTermType rdfType) {
        if (definition instanceof Variable)
            return definition;
        if (definition instanceof RDFConstant)
            return termFactory.getConversionFromRDFLexical2DB(
                    termFactory.getDBStringConstant(((RDFConstant) definition).getValue()), rdfType);
        if ((definition instanceof ImmutableFunctionalTerm)
                && ((ImmutableFunctionalTerm) definition).getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
            return termFactory.getConversionFromRDFLexical2DB(
                    ((ImmutableFunctionalTerm) definition).getTerm(0), rdfType);
        }
        throw new MinorOntopInternalBugException("BI connector: unexpected tree shape " +
                "(proper exception should have already been thrown)");
    }

    private ImmutableMap<Variable, RDFTermType> extractRDFTypes(Substitution<ImmutableTerm> definitions)
            throws NotFullyTranslatableToNativeQueryException {

        try {
            return definitions.builder()
                    .toMap((v, t) -> extractRDFType(v, t, definitions));
        }
        catch (NotFullyTranslatableToNativeQueryRuntimeException e) {
            throw new NotFullyTranslatableToNativeQueryException(e.getMessage());
        }
    }

    private RDFTermType extractRDFType(Variable variable, ImmutableTerm definition, Substitution<ImmutableTerm> definitions)  {
        if (definition instanceof Variable) {
            Variable otherVariable = (Variable) definition;
            // recursively unravel definitions
            return extractRDFType(otherVariable, definitions.get(otherVariable), definitions);
        }
        else if (definition instanceof RDFConstant) {
            return ((RDFConstant) definition).getType();
        }
        else if ((definition instanceof ImmutableFunctionalTerm) &&
                ((ImmutableFunctionalTerm) definition).getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
            ImmutableTerm termTypeTerm = ((ImmutableFunctionalTerm) definition).getTerms().get(1);
            if (termTypeTerm instanceof RDFTermTypeConstant) {
                return ((RDFTermTypeConstant) termTypeTerm).getRDFTermType();
            }
            else if (termTypeTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm termTypeFunctionalTerm = (ImmutableFunctionalTerm) termTypeTerm;
                if ((termTypeFunctionalTerm.getFunctionSymbol() instanceof DBIfElseNullFunctionSymbol)
                        && (termTypeFunctionalTerm.getTerm(1) instanceof RDFTermTypeConstant))
                    return ((RDFTermTypeConstant) termTypeFunctionalTerm.getTerm(1)).getRDFTermType();
            }
            throw new NotFullyTranslatableToNativeQueryRuntimeException(String.format(
                    "the variable %s is not guaranteed to be uniquely typed.\nConsider imposing a datatype through a FILTER in the input query", variable));
        }
        throw new NotFullyTranslatableToNativeQueryRuntimeException(String.format(
                "could not infer the unique type of the variable %s", variable));
    }

    /**
     *  Exception required only for handling streams
     */
    private static class NotFullyTranslatableToNativeQueryRuntimeException extends RuntimeException {
        private NotFullyTranslatableToNativeQueryRuntimeException(String message) {
            super(message);
        }
    }
}
