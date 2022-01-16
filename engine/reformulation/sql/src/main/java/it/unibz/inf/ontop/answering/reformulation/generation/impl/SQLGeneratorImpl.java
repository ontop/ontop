package it.unibz.inf.ontop.answering.reformulation.generation.impl;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.iq.transform.IQTree2NativeNodeGenerator;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.answering.reformulation.generation.PostProcessingProjectionSplitter;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.datalog.UnionFlattener;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.PostProcessableFunctionLifter;
import it.unibz.inf.ontop.iq.optimizer.TermTypeTermLifter;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.iq.transformer.EmptyRowsValuesNodeTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final OptimizerFactory optimizerFactory;
    private final PostProcessingProjectionSplitter projectionSplitter;
    private final TermTypeTermLifter rdfTypeLifter;
    private final PostProcessableFunctionLifter functionLifter;
    private final IQTree2NativeNodeGenerator defaultIQTree2NativeNodeGenerator;
    private final OntopReformulationSQLSettings settings;
    private final DialectExtraNormalizer extraNormalizer;
    private final BooleanExpressionPushDownTransformer pushDownTransformer;
    private final EmptyRowsValuesNodeTransformer valuesNodeTransformer;

    @AssistedInject
    private SQLGeneratorImpl(@Assisted DBParameters dbParameters,
                             IntermediateQueryFactory iqFactory,
                             UnionFlattener unionFlattener,
                             OptimizerFactory optimizerFactory,
                             PostProcessingProjectionSplitter projectionSplitter,
                             TermTypeTermLifter rdfTypeLifter, PostProcessableFunctionLifter functionLifter,
                             IQTree2NativeNodeGenerator defaultIQTree2NativeNodeGenerator,
                             DialectExtraNormalizer extraNormalizer, BooleanExpressionPushDownTransformer pushDownTransformer,
                             EmptyRowsValuesNodeTransformer valuesNodeTransformer,
                             OntopReformulationSQLSettings settings)
    {
        this.functionLifter = functionLifter;
        this.extraNormalizer = extraNormalizer;
        this.pushDownTransformer = pushDownTransformer;
        this.valuesNodeTransformer = valuesNodeTransformer;
        this.dbParameters = dbParameters;
        this.iqFactory = iqFactory;
        this.unionFlattener = unionFlattener;
        this.optimizerFactory = optimizerFactory;
        this.projectionSplitter = projectionSplitter;
        this.rdfTypeLifter = rdfTypeLifter;
        this.defaultIQTree2NativeNodeGenerator = defaultIQTree2NativeNodeGenerator;
        this.settings = settings;
    }

    @Override
    public IQ generateSourceQuery(IQ query) {
        return generateSourceQuery(query, settings.isPostProcessingAvoided());
    }

    @Override
    public IQ generateSourceQuery(IQ query, boolean avoidPostProcessing) {
        if (query.getTree().isDeclaredAsEmpty())
            return query;

        IQ rdfTypeLiftedIQ = rdfTypeLifter.optimize(query);
        LOGGER.debug("After lifting the RDF types:\n{}\n", rdfTypeLiftedIQ);

        IQ liftedIQ = functionLifter.optimize(rdfTypeLiftedIQ);
        LOGGER.debug("After lifting the post-processable function symbols:\n{}\n", liftedIQ);

        PostProcessingProjectionSplitter.PostProcessingSplit split = projectionSplitter.split(liftedIQ, avoidPostProcessing);

        IQTree normalizedSubTree = normalizeSubTree(split.getSubTree(), split.getVariableGenerator());
        // Late detection of emptiness
        if (normalizedSubTree.isDeclaredAsEmpty())
            return iqFactory.createIQ(query.getProjectionAtom(),
                    iqFactory.createEmptyNode(query.getProjectionAtom().getVariables()));

        NativeNode nativeNode = generateNativeNode(normalizedSubTree);

        UnaryIQTree newTree = iqFactory.createUnaryIQTree(split.getPostProcessingConstructionNode(), nativeNode);

        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    /**
     * TODO: what about the distinct?
     * TODO: move the distinct and slice lifting to the post-processing splitter
     */
    private IQTree normalizeSubTree(IQTree subTree, VariableGenerator variableGenerator) {

        IQTree sliceLiftedTree = liftSlice(subTree);
        LOGGER.debug("New query after lifting the slice:\n{}\n", sliceLiftedTree);

        // TODO: check if still needed
        IQTree flattenSubTree = unionFlattener.optimize(sliceLiftedTree, variableGenerator);
        LOGGER.debug("New query after flattening the union:\n{}\n", flattenSubTree);

        IQTree pushedDownSubTree = pushDownTransformer.transform(flattenSubTree);
        LOGGER.debug("New query after pushing down:\n{}\n", pushedDownSubTree);

        IQTree treeAfterPullOut = optimizerFactory.createEETransformer(variableGenerator).transform(pushedDownSubTree);
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
        if (subTree.getRootNode() instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) subTree.getRootNode();
            IQTree childTree = ((UnaryIQTree) subTree).getChild();
            if (childTree.getRootNode() instanceof SliceNode) {
                /*
                 * Swap the top construction node and the slice
                 */
                SliceNode sliceNode = (SliceNode) childTree.getRootNode();
                IQTree grandChildTree = ((UnaryIQTree) childTree).getChild();

                return iqFactory.createUnaryIQTree(sliceNode,
                        iqFactory.createUnaryIQTree(constructionNode, grandChildTree));
            }
        }
        return subTree;
    }

    private IQTree dropTopConstruct(IQTree subTree) {
        // Check if it starts with [LIMIT]
        if (subTree.getRootNode() instanceof SliceNode) {
            SliceNode sliceNode = (SliceNode) subTree.getRootNode();
            // Add slice node to trimmed childtree
            return iqFactory.createUnaryIQTree(sliceNode,
                    dropTopConstruct(((UnaryIQTree) subTree).getChild()));
        } else {
            // Check for pattern CONSTRUCT, DISTINCT, [CONSTRUCT], ORDER BY
            if (subTree.getRootNode() instanceof ConstructionNode) {
                ConstructionNode constructionNode = (ConstructionNode) subTree.getRootNode();
                // If there is variable substitution in the top construction do not normalize
                IQTree childTree = ((UnaryIQTree) subTree).getChild();
                if (childTree.getRootNode() instanceof DistinctNode && constructionNode.getSubstitution().isEmpty()) {
                    IQTree grandChildTree = ((UnaryIQTree) childTree).getChild();
                    // CASE 1: CONSTRUCT, DISTINCT, CONSTRUCT, ORDER BY
                    if (grandChildTree.getRootNode() instanceof ConstructionNode) {
                        IQTree grandGrandChildTree = ((UnaryIQTree) grandChildTree).getChild();
                        if (grandGrandChildTree.getRootNode() instanceof OrderByNode) {
                            /*
                             * Drop the top construction node
                             */
                            return childTree;
                        }
                    // CASE 2: CONSTRUCT, DISTINCT, ORDER BY
                    } else if (grandChildTree.getRootNode() instanceof OrderByNode) {
                        /*
                         * Drop the top construction node
                         */
                        return childTree;
                    }
                }
            }
            return subTree;
        }
    }

    private NativeNode generateNativeNode(IQTree normalizedSubTree) {
        return defaultIQTree2NativeNodeGenerator.generate(normalizedSubTree, dbParameters, false);
    }
}
