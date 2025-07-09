package it.unibz.inf.ontop.answering.logging.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;


/**
 * TODO: move it away from the query logging package if also used for other purposes
 */
@Singleton
public class QueryTemplateExtractor {

    private final IntermediateQueryFactory iqFactory;
    private final OntopModelSettings settings;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;
    private final SPARQLFunctionSymbol sparqlEqFunctionSymbol;
    private final BooleanFunctionSymbol rdf2BoolFunctionSymbol;


    @Inject
    protected QueryTemplateExtractor(CoreSingletons coreSingletons, OntopModelSettings settings) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.settings = settings;
        this.atomFactory = coreSingletons.getAtomFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.termFactory = coreSingletons.getTermFactory();
        FunctionSymbolFactory functionSymbolFactory = coreSingletons.getFunctionSymbolFactory();
        this.sparqlEqFunctionSymbol = functionSymbolFactory.getRequiredSPARQLFunctionSymbol(SPARQL.EQ, 2);
        this.rdf2BoolFunctionSymbol = functionSymbolFactory.getRDF2DBBooleanFunctionSymbol();
    }

    Optional<QueryTemplateExtraction> extract(IQ iq) {

        IQTree initialIQTree = iq.getTree();
        QueryTemplateTransformer transformer = new QueryTemplateTransformer(iq.getVariableGenerator());

        IQTree newTree = initialIQTree.acceptVisitor(transformer);
        ImmutableMap<GroundTerm, Variable> parameterMap = transformer.getParameterMap();

        if (parameterMap.isEmpty())
            return Optional.empty();

        return Optional.of(new QueryTemplateExtraction(
                iqFactory.createIQ(iq.getProjectionAtom(), newTree),
                parameterMap));
    }


    /**
     * Specific to an IQ
     *
     * Extracts from filter/LJ/joins and intensional data nodes.
     */
    private class QueryTemplateTransformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

        // Mutable
        private final Map<GroundTerm, Variable> parameterMap;

        QueryTemplateTransformer(VariableGenerator variableGenerator) {
            super(QueryTemplateExtractor.this.iqFactory, variableGenerator);
            this.parameterMap = Maps.newLinkedHashMap();
        }

        public ImmutableMap<GroundTerm, Variable> getParameterMap() {
            return ImmutableMap.copyOf(parameterMap);
        }

        @Override
        public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
            DataAtom<AtomPredicate> atom = dataNode.getProjectionAtom();

            if (atom.getPredicate() instanceof RDFAtomPredicate) {
                RDFAtomPredicate rdfAtomPredicate = (RDFAtomPredicate) atom.getPredicate();

                ImmutableList<? extends VariableOrGroundTerm> arguments = atom.getArguments();

                ImmutableList<Integer> indexes = rdfAtomPredicate.getClassIRI(arguments)
                        .map(iri -> ImmutableList.of(0))
                        .orElseGet(() -> ImmutableList.of(0, 2));

                ImmutableMap<Integer, GroundTerm> groundTermIndex = indexes.stream()
                        .flatMap(i -> Optional.of(arguments.get(i))
                                .filter(t -> t instanceof GroundTerm)
                                .map(t -> (GroundTerm) t)
                                .map(t -> Maps.immutableEntry(i, t))
                                .stream())
                        .collect(ImmutableCollectors.toMap());

                if (groundTermIndex.isEmpty())
                    return dataNode;

                // Adds new ground terms to the parameter map
                groundTermIndex.values()
                        .forEach(t -> parameterMap.computeIfAbsent(t, g -> variableGenerator.generateNewVariable()));

                ImmutableList<VariableOrGroundTerm> newArguments = IntStream.range(0, arguments.size())
                        .mapToObj(i -> Optional.ofNullable(groundTermIndex.get(i))
                                .map(parameterMap::get)
                                .map(t -> (VariableOrGroundTerm)t)
                                .orElseGet(() -> arguments.get(i)))
                        .collect(ImmutableCollectors.toList());
                DataAtom<AtomPredicate> newAtom = atomFactory.getDataAtom(atom.getPredicate(), newArguments);

                return iqFactory.createIntensionalDataNode(newAtom);
            }

            return dataNode;
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            Optional<ImmutableExpression> newCondition = transformFilterCondition(rootNode.getFilterCondition());

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(iqTreeTools.createOptionalFilterNode(newCondition))
                    .build(transformChild(child));
        }


        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            Optional<ImmutableExpression> newCondition = rootNode.getOptionalFilterCondition()
                    .flatMap(this::transformFilterCondition);

            return iqTreeTools.createLeftJoinTree(
                    newCondition,
                    transformChild(leftChild),
                    transformChild(rightChild));
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                    this::transformChild);

            Optional<ImmutableExpression> newCondition = rootNode.getOptionalFilterCondition()
                    .flatMap(this::transformFilterCondition);

            InnerJoinNode newRootNode = newCondition
                    .map(c -> iqFactory.createInnerJoinNode(newCondition))
                    .orElse(rootNode);

            return iqFactory.createNaryIQTree(newRootNode, newChildren);
        }

        /**
         * NB: produces an invalid IQ with unbound variables, so the test mode needs to be disabled
         * for extracting parameters.
         */
        protected Optional<ImmutableExpression> transformFilterCondition(ImmutableExpression filterCondition) {
            if (settings.isTestModeEnabled())
                return Optional.empty();

            return termFactory.getConjunction(filterCondition.flattenAND()
                    .map(this::transformSubExpression))
                    .filter(e -> !e.equals(filterCondition));
        }

        /**
         * Transforms ground terms in SPARQL equalities
         */
        private ImmutableExpression transformSubExpression(ImmutableExpression expression) {
            if (expression.getFunctionSymbol().equals(rdf2BoolFunctionSymbol)) {
                ImmutableTerm subTerm = expression.getTerm(0);
                if (subTerm instanceof ImmutableFunctionalTerm) {
                    ImmutableFunctionalTerm subFunctionalTerm = (ImmutableFunctionalTerm) subTerm;
                    if (subFunctionalTerm.getFunctionSymbol().equals(sparqlEqFunctionSymbol)) {

                        ImmutableList<? extends ImmutableTerm> initialTerms = subFunctionalTerm.getTerms();
                        ImmutableList<ImmutableTerm> newTerms = initialTerms.stream()
                                .map(t -> t instanceof GroundTerm
                                        ? parameterMap.computeIfAbsent((GroundTerm) t, g -> variableGenerator.generateNewVariable())
                                        : t)
                                .collect(ImmutableCollectors.toList());

                        return newTerms.equals(initialTerms)
                                ? expression
                                : termFactory.getImmutableExpression(
                                        rdf2BoolFunctionSymbol,
                                        termFactory.getImmutableFunctionalTerm(sparqlEqFunctionSymbol, newTerms));
                    }
                }
            }
            return expression;
        }
    }



    public static class QueryTemplateExtraction {

        private final IQ iq;
        private final ImmutableMap<GroundTerm, Variable> parameterMap;

        public QueryTemplateExtraction(IQ iq, ImmutableMap<GroundTerm, Variable> parameterMap) {
            this.iq = iq;
            this.parameterMap = parameterMap;
        }

        public IQ getIq() {
            return iq;
        }

        public ImmutableMap<GroundTerm, Variable> getParameterMap() {
            return parameterMap;
        }
    }
}
