package it.unibz.inf.ontop.query.translation.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.OntopKGQuerySettings;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.query.aggregates.*;
import it.unibz.inf.ontop.query.translation.InsertClauseNormalizer;
import it.unibz.inf.ontop.query.translation.RDF4JQueryTranslator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.*;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedUpdate;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.CustomAggregateFunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

@Singleton
public class RDF4JQueryTranslatorImpl implements RDF4JQueryTranslator {

    private final CoreUtilsFactory coreUtilsFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TypeFactory typeFactory;
    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;
    private final RDF rdfFactory;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final InsertClauseNormalizer insertClauseNormalizer;

    private final IQTreeTools iqTreeTools;

    private static final Logger LOGGER = LoggerFactory.getLogger(RDF4JQueryTranslatorImpl.class);
    private static final boolean IS_DEBUG_ENABLED = LOGGER.isDebugEnabled();

    @Inject
    public RDF4JQueryTranslatorImpl(CoreUtilsFactory coreUtilsFactory, TermFactory termFactory, SubstitutionFactory substitutionFactory,
                                    TypeFactory typeFactory, IntermediateQueryFactory iqFactory, AtomFactory atomFactory, RDF rdfFactory,
                                    FunctionSymbolFactory functionSymbolFactory,
                                    InsertClauseNormalizer insertClauseNormalizer, IQTreeTools iqTreeTools, OntopKGQuerySettings settings) {
        this.coreUtilsFactory = coreUtilsFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.typeFactory = typeFactory;
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
        this.rdfFactory = rdfFactory;
        this.functionSymbolFactory = functionSymbolFactory;
        this.insertClauseNormalizer = insertClauseNormalizer;
        this.iqTreeTools = iqTreeTools;
        if(settings.isCustomSPARQLFunctionRegistrationEnabled()) {
            CustomAggregateFunctionRegistry registry = CustomAggregateFunctionRegistry.getInstance();
            registry.add(new VarianceSampAggregateFactory());
            registry.add(new VariancePopAggregateFactory());
            registry.add(new VarianceShortAggregateFactory());
            registry.add(new StdevSampAggregateFactory());
            registry.add(new StdevPopAggregateFactory());
            registry.add(new StdevShortAggregateFactory());
        }
    }

    @Override
    public IQ translateQuery(ParsedQuery pq, BindingSet bindings) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException {

        if (IS_DEBUG_ENABLED)
            LOGGER.debug("Parsed query:\n{}", pq);

        ImmutableMap<Variable, GroundTerm> externalBindings = convertExternalBindings(bindings);

        IQTree tree = getTranslator(externalBindings, pq.getDataset(), true).getTree(pq.getTupleExpr());

        ImmutableSet<Variable> vars = tree.getVariables();

        // Assumption: the binding names in the parsed query are in the desired order
        ImmutableList<Variable> projectedVars = pq.getTupleExpr().getBindingNames().stream()
                .map(termFactory::getVariable)
                // filter out the extra bindings generated by the rdf4j API for constants
                .filter(vars::contains)
                .collect(ImmutableCollectors.toList());
        if (IS_DEBUG_ENABLED)
            LOGGER.debug("IQTree (before normalization):\n{}", tree);

        return iqFactory.createIQ(
                atomFactory.getDistinctVariableOnlyDataAtom(
                        atomFactory.getRDFAnswerPredicate(projectedVars.size()),
                        projectedVars),
                tree)
                .normalizeForOptimization();
    }

    protected ImmutableMap<Variable, GroundTerm> convertExternalBindings(BindingSet bindings) {
        RDF4JValueTranslator translator = getValueTranslator();

        return bindings.getBindingNames().stream()
                .collect(ImmutableCollectors.toMap(
                        termFactory::getVariable,
                        n -> translator.getTermForLiteralOrIri(bindings.getValue(n))));
    }

    @Override
    public IQ translateAskQuery(ParsedQuery pq, BindingSet bindings) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {

        if (IS_DEBUG_ENABLED)
            LOGGER.debug("Parsed query:\n{}", pq);

        ImmutableMap<Variable, GroundTerm> externalBindings = convertExternalBindings(bindings);

        IQTree tree = getTranslator(externalBindings, pq.getDataset(), true).getTree(pq.getTupleExpr());

        if (IS_DEBUG_ENABLED)
            LOGGER.debug("IQTree (before normalization):\n{}", tree);
        return iqFactory.createIQ(
                atomFactory.getDistinctVariableOnlyDataAtom(
                        atomFactory.getRDFAnswerPredicate(0),
                        ImmutableList.of()
                ),
                projectOutAllVars(tree)
        ).normalizeForOptimization();
    }

    @Override
    public ImmutableSet<IQ> translateInsertOperation(ParsedUpdate parsedUpdate) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {
        ImmutableSet.Builder<IQ> iqsBuilder = ImmutableSet.builder();

        Map<UpdateExpr, Dataset> datasetMapping = parsedUpdate.getDatasetMapping();

        for (UpdateExpr expr: parsedUpdate.getUpdateExprs()) {
            if (expr instanceof Modify) {
                iqsBuilder.addAll(translateInsertExpression((Modify) expr, datasetMapping.get(expr)));
            }
            else
                throw new OntopUnsupportedKGQueryException("Unsupported update: " + expr);
        }
        return iqsBuilder.build();
    }

    protected ImmutableSet<IQ> translateInsertExpression(Modify expression, @Nullable Dataset dataset) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException {
        if (expression.getDeleteExpr() != null)
            throw new OntopUnsupportedKGQueryException("DELETE clauses are not supported");
        if (expression.getInsertExpr() == null)
            throw new OntopInvalidKGQueryException("Was expecting an INSERT clause");

        IQTree whereTree = expression.getWhereExpr() == null
                ? iqFactory.createTrueNode()
                : getTranslator(ImmutableMap.of(), dataset, true).getTree(expression.getWhereExpr());

        @Nullable Dataset insertDataset;
        if (dataset != null) {
            SimpleDataset newDataset = new SimpleDataset();
            IRI defaultInsertGraph = dataset.getDefaultInsertGraph();
            if (defaultInsertGraph != null)
                newDataset.addDefaultGraph(defaultInsertGraph);
            insertDataset = newDataset;
        }
        else
            insertDataset = null;

        IQTree insertTree = getTranslator(ImmutableMap.of(), insertDataset, false).getTree(expression.getInsertExpr());

        ImmutableSet.Builder<IQ> iqsBuilder = ImmutableSet.builder();
        ImmutableSet<IntensionalDataNode> dataNodes = extractIntensionalDataNodesFromHead(insertTree);


        InsertClauseNormalizer.Result normalization = insertClauseNormalizer.normalize(dataNodes, whereTree);
        IQTree normalizedSubTree = normalization.getConstructionNode()
                .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, whereTree))
                .orElse(whereTree);

        // NB: when there is no default graph
        if (normalizedSubTree.isDeclaredAsEmpty())
            return ImmutableSet.of();

        for (IntensionalDataNode dataNode : dataNodes) {
            iqsBuilder.add(createInsertIQ(dataNode, normalization.getBNodeVariableMap(), normalizedSubTree));
        }
        return iqsBuilder.build();
    }

    private ImmutableSet<IntensionalDataNode> extractIntensionalDataNodesFromHead(IQTree insertTree) throws OntopInvalidKGQueryException {
        if (insertTree instanceof IntensionalDataNode)
            return ImmutableSet.of((IntensionalDataNode) insertTree);

        QueryNode rootNode = insertTree.getRootNode();
        if ((rootNode instanceof InnerJoinNode) && !((InnerJoinNode) rootNode).getOptionalFilterCondition().isPresent()) {

            ImmutableSet.Builder<IntensionalDataNode> setBuilder = ImmutableSet.builder();
            for (IQTree child : insertTree.getChildren()) {
                setBuilder.addAll(extractIntensionalDataNodesFromHead(child));
            }
            return setBuilder.build();
        }

        throw new OntopInvalidKGQueryException("Invalid INSERT clause");
    }

    private IQ createInsertIQ(IntensionalDataNode dataNode, ImmutableMap<BNode, Variable> bnodeVariableMap, IQTree subTree) {

        DataAtom<AtomPredicate> dataNodeAtom = dataNode.getProjectionAtom();

        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Sets.union(subTree.getKnownVariables(), dataNode.getKnownVariables()));

        ImmutableList<VariableOrGroundTerm> normalizedArguments = dataNodeAtom.getArguments().stream()
                .map(a -> (a instanceof BNode)
                        ? Optional.ofNullable(bnodeVariableMap.get(a))
                        .orElseThrow(() -> new MinorOntopInternalBugException(
                                "BNodes from the INSERT clause should be replaced internally by variables generated out of templates"))
                        : a)
                .collect(ImmutableCollectors.toList());

        ImmutableMap<Variable, VariableOrGroundTerm> map = normalizedArguments.stream()
                .map(t -> Maps.immutableEntry(t instanceof Variable ? (Variable)t : variableGenerator.generateNewVariable(), t))
                .distinct()
                .collect(ImmutableCollectors.toMap());

        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(dataNodeAtom.getPredicate(),
                ImmutableList.copyOf(map.keySet()));

        ConstructionNode constructionNode = iqFactory.createConstructionNode(
                map.keySet(),
                map.entrySet().stream().collect(substitutionFactory.toSubstitutionSkippingIdentityEntries()));

        IQ newIQ = iqFactory.createIQ(
                projectionAtom,
                iqFactory.createUnaryIQTree(constructionNode, subTree));

        return newIQ.normalizeForOptimization();
    }

    private IQTree projectOutAllVars(IQTree tree) {
        if (tree.getRootNode() instanceof QueryModifierNode) {
            return iqFactory.createUnaryIQTree(
                    (UnaryOperatorNode) tree.getRootNode(),
                    projectOutAllVars(((UnaryIQTree) tree).getChild()));
        }

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(ImmutableSet.of()),
                tree);
    }

    private RDF4JTupleExprTranslator getTranslator(ImmutableMap<Variable, GroundTerm> externalBindings, @Nullable Dataset dataset, boolean treatBNodeAsVariable) {
        return new RDF4JTupleExprTranslator(externalBindings, dataset, treatBNodeAsVariable, coreUtilsFactory, substitutionFactory, iqFactory, atomFactory, termFactory, functionSymbolFactory, rdfFactory, typeFactory, iqTreeTools);
    }

    private RDF4JValueTranslator getValueTranslator() {
        return new RDF4JValueTranslator(termFactory, rdfFactory, typeFactory);
    }
}
