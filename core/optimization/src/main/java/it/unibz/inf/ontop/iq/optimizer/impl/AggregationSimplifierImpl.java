package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.AggregationNode;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.optimizer.AggregationSimplifier;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transformer.impl.RDFTypeDependentSimplifyingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol.AggregationSimplification;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import java.util.Optional;

public class AggregationSimplifierImpl implements AggregationSimplifier {

    private final IntermediateQueryFactory iqFactory;
    private final OptimizationSingletons optimizationSingletons;

    private final IQTreeTools iqTreeTools;

    @Inject
    private AggregationSimplifierImpl(IntermediateQueryFactory iqFactory, OptimizationSingletons optimizationSingletons, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.optimizationSingletons = optimizationSingletons;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQ optimize(IQ query) {
        VariableGenerator variableGenerator = query.getVariableGenerator();
        IQTreeTransformer transformer = createTransformer(variableGenerator);
        IQTree newTree = transformer.transform(query.getTree())
                .normalizeForOptimization(variableGenerator);
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected IQTreeTransformer createTransformer(VariableGenerator variableGenerator) {
        return new AggregationSimplifyingTransformer(variableGenerator, optimizationSingletons, iqTreeTools);
    }

    /**
     * Recursive
     */
    protected static class AggregationSimplifyingTransformer extends RDFTypeDependentSimplifyingTransformer {

        private final VariableGenerator variableGenerator;
        private final TermFactory termFactory;
        private final IQTreeTools iqTreeTools;

        protected AggregationSimplifyingTransformer(VariableGenerator variableGenerator,
                                                    OptimizationSingletons optimizationSingletons, IQTreeTools iqTreeTools) {
            super(optimizationSingletons);
            this.variableGenerator = variableGenerator;
            CoreSingletons coreSingletons = optimizationSingletons.getCoreSingletons();
            this.termFactory = coreSingletons.getTermFactory();
            this.iqTreeTools = iqTreeTools;
        }

        @Override
        public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
            // In case of aggregation nodes in the sub-tree
            IQTree normalizedChild = child.acceptTransformer(this)
                    .normalizeForOptimization(variableGenerator);

            QueryNode newChildRoot = normalizedChild.getRootNode();

            // May need to renormalize the tree (RECURSIVE)
            if ((newChildRoot instanceof ConstructionNode) && (!child.getRootNode().equals(newChildRoot)))
                return transform(
                        iqFactory.createUnaryIQTree(rootNode, normalizedChild).normalizeForOptimization(variableGenerator));

            Substitution<ImmutableFunctionalTerm> initialSubstitution = rootNode.getSubstitution();

            // With the GROUP BY clause, groups are never empty
            boolean hasGroupBy = !rootNode.getGroupingVariables().isEmpty();

            ImmutableMap<Variable, AggregationSimplification> simplificationMap = initialSubstitution.builder()
                            .toMapIgnoreOptional((v, t) -> simplifyAggregationFunctionalTerm(t, normalizedChild, hasGroupBy));

            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution = initialSubstitution.builder()
                            .flatTransform(simplificationMap::get, d -> d.getDecomposition().getSubstitution())
                            .build();

            AggregationNode newNode = iqFactory.createAggregationNode(rootNode.getGroupingVariables(), newAggregationSubstitution);

            IQTree pushDownChildTree = pushDownDefinitions(
                    normalizedChild,
                    simplificationMap.values().stream()
                            .flatMap(s -> s.getPushDownRequests().stream()));
            UnaryIQTree newAggregationTree = iqFactory.createUnaryIQTree(newNode, pushDownChildTree);

            // Substitution of the new parent construction node (containing typically the RDF function)
            Substitution<ImmutableTerm> parentSubstitution = initialSubstitution.builder()
                    .transformOrRemove(simplificationMap::get, d -> d.getDecomposition().getLiftableTerm())
                    .build();

            return iqTreeTools.createConstructionNodeTreeIfNontrivial(newAggregationTree, parentSubstitution, rootNode::getVariables);
        }

        protected Optional<AggregationSimplification> simplifyAggregationFunctionalTerm(ImmutableFunctionalTerm aggregationFunctionalTerm,
                                                                                        IQTree child, boolean hasGroupBy) {
            FunctionSymbol functionSymbol = aggregationFunctionalTerm.getFunctionSymbol();

            /*
             * Focuses on SPARQLAggregationFunctionSymbol
             */
            if (functionSymbol instanceof SPARQLAggregationFunctionSymbol) {
                SPARQLAggregationFunctionSymbol aggregationFunctionSymbol = (SPARQLAggregationFunctionSymbol) functionSymbol;
                ImmutableList<? extends ImmutableTerm> subTerms = aggregationFunctionalTerm.getTerms();

                /*
                 * Needs the sub-terms to be RDF(...) functional terms or RDF constants
                 */
                if (subTerms.stream().allMatch(t -> isRDFFunctionalTerm(t)
                        || (t instanceof RDFConstant))) {
                    ImmutableList<Optional<ImmutableSet<RDFTermType>>> extractedRDFTypes = aggregationFunctionalTerm.getTerms().stream()
                            .map(this::extractRDFTermTypeTerm)
                            .map(this::unwrapIfElseNull)
                            .map(t -> extractPossibleTypes(t, child))
                            .collect(ImmutableCollectors.toList());

                    /*
                     * If the RDF types of a sub-term cannot be determined, aborts the simplification
                     */
                    if (extractedRDFTypes.stream().anyMatch(t -> !t.isPresent())) {
                        return Optional.empty();
                    }

                    ImmutableList<ImmutableSet<RDFTermType>> possibleRDFTypes = extractedRDFTypes.stream()
                            .map(Optional::get)
                            .collect(ImmutableCollectors.toList());

                    /*
                     * Delegates the simplification to the function symbol
                     */
                    return aggregationFunctionSymbol.decomposeIntoDBAggregation(subTerms, possibleRDFTypes,
                            hasGroupBy, child.getVariableNullability(), variableGenerator, termFactory);
                }
            }
            /*
             * By default, does not optimize
             */
            return Optional.empty();
        }

        protected boolean isRDFFunctionalTerm(ImmutableTerm term) {
            return (term instanceof ImmutableFunctionalTerm)
                    && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol);
        }

        protected ImmutableTerm extractRDFTermTypeTerm(ImmutableTerm rdfTerm) {
            if (isRDFFunctionalTerm(rdfTerm))
                return ((ImmutableFunctionalTerm)rdfTerm).getTerm(1);
            else if (rdfTerm instanceof RDFConstant)
                return termFactory.getRDFTermTypeConstant(((RDFConstant) rdfTerm).getType());
            else if (rdfTerm.isNull())
                return termFactory.getNullConstant();

            throw new IllegalArgumentException("Was expecting a isRDFFunctionalTerm or an RDFConstant or NULL");
        }
    }
}
