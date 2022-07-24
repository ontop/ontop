package it.unibz.inf.ontop.answering.logging.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
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
import java.util.stream.Stream;


/**
 * TODO: move it away from the query logging package if also used for other purposes
 */
@Singleton
public class QueryTemplateExtractor {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final OntopModelSettings settings;

    @Inject
    protected QueryTemplateExtractor(CoreSingletons coreSingletons, OntopModelSettings settings) {
        this.coreSingletons = coreSingletons;
        iqFactory = coreSingletons.getIQFactory();
        this.settings = settings;
    }

    Optional<QueryTemplateExtraction> extract(IQ iq) {

        IQTree initialIQTree = iq.getTree();
        QueryTemplateTransformer transformer = new QueryTemplateTransformer(coreSingletons,
                initialIQTree.getKnownVariables(), settings);

        IQTree newTree = transformer.transform(initialIQTree);
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
    protected static class QueryTemplateTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final CoreSingletons coreSingletons;
        private final VariableGenerator variableGenerator;

        // Mutable
        private final Map<GroundTerm, Variable> parameterMap;
        private final AtomFactory atomFactory;
        private final OntopModelSettings settings;
        private final SPARQLFunctionSymbol sparqlEqFunctionSymbol;
        private final TermFactory termFactory;
        private final BooleanFunctionSymbol rdf2BoolFunctionsymbol;

        protected QueryTemplateTransformer(CoreSingletons coreSingletons, ImmutableSet<Variable> knownVariables,
                                           OntopModelSettings settings) {
            super(coreSingletons);
            this.coreSingletons = coreSingletons;
            atomFactory = coreSingletons.getAtomFactory();
            this.settings = settings;
            this.variableGenerator = coreSingletons.getCoreUtilsFactory()
                    .createVariableGenerator(knownVariables);
            this.parameterMap = Maps.newLinkedHashMap();
            this.termFactory = coreSingletons.getTermFactory();
            FunctionSymbolFactory functionSymbolFactory = coreSingletons.getFunctionSymbolFactory();
            this.sparqlEqFunctionSymbol = functionSymbolFactory.getRequiredSPARQLFunctionSymbol(SPARQL.EQ, 2);
            rdf2BoolFunctionsymbol = functionSymbolFactory.getRDF2DBBooleanFunctionSymbol();
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
                                .filter(ImmutableTerm::isGround)
                                .map(t -> (GroundTerm) t)
                                .map(t -> Maps.immutableEntry(i, t))
                                .map(Stream::of)
                                .orElseGet(Stream::empty))
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
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            IQTree newChild = child.acceptTransformer(this);
            Optional<ImmutableExpression> newCondition = transformFilterCondition(rootNode.getFilterCondition());

            FilterNode newRootNode = newCondition
                    .map(rootNode::changeFilterCondition)
                    .orElse(rootNode);

            return iqFactory.createUnaryIQTree(newRootNode, newChild);
        }


        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeft = leftChild.acceptTransformer(this);
            IQTree newRight = rightChild.acceptTransformer(this);

            Optional<ImmutableExpression> newCondition = rootNode.getOptionalFilterCondition()
                    .flatMap(this::transformFilterCondition);

            LeftJoinNode newRootNode = newCondition
                    .map(c -> rootNode.changeOptionalFilterCondition(newCondition))
                    .orElse(rootNode);

            return iqFactory.createBinaryNonCommutativeIQTree(newRootNode, newLeft, newRight);
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            Optional<ImmutableExpression> newCondition = rootNode.getOptionalFilterCondition()
                    .flatMap(this::transformFilterCondition);

            InnerJoinNode newRootNode = newCondition
                    .map(c -> rootNode.changeOptionalFilterCondition(newCondition))
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
            if (expression.getFunctionSymbol().equals(rdf2BoolFunctionsymbol)) {
                ImmutableTerm subTerm = expression.getTerm(0);
                if (subTerm instanceof ImmutableFunctionalTerm) {
                    ImmutableFunctionalTerm subFunctionalTerm = (ImmutableFunctionalTerm) subTerm;
                    if (subFunctionalTerm.getFunctionSymbol().equals(sparqlEqFunctionSymbol)) {

                        ImmutableList<? extends ImmutableTerm> initialTerms = subFunctionalTerm.getTerms();
                        ImmutableList<ImmutableTerm> newTerms = initialTerms.stream()
                                .map(t -> t.isGround()
                                        ? parameterMap.computeIfAbsent(
                                        (GroundTerm) t, g -> variableGenerator.generateNewVariable())
                                        : t)
                                .collect(ImmutableCollectors.toList());

                        return newTerms.equals(initialTerms)
                                ? expression
                                : termFactory.getImmutableExpression(
                                        rdf2BoolFunctionsymbol,
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
