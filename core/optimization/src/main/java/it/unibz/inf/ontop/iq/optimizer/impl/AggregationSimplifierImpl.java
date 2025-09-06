package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.AggregationNode;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.optimizer.AggregationSimplifier;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
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

public class AggregationSimplifierImpl extends AbstractIQOptimizer implements AggregationSimplifier {

    private final OptimizationSingletons optimizationSingletons;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private AggregationSimplifierImpl(OptimizationSingletons optimizationSingletons) {
        // no equality check
        super(optimizationSingletons.getCoreSingletons().getIQFactory());
        this.optimizationSingletons = optimizationSingletons;
        CoreSingletons coreSingletons = optimizationSingletons.getCoreSingletons();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();

        this.transformer = IQTreeVariableGeneratorTransformer.of(
                IQTreeVariableGeneratorTransformer.of(AggregationSimplifyingTransformer::new),
                IQTree::normalizeForOptimization);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }

    /**
     * Recursive
     */
    private class AggregationSimplifyingTransformer extends RDFTypeDependentSimplifyingTransformer {

        AggregationSimplifyingTransformer(VariableGenerator variableGenerator) {
            super(optimizationSingletons, variableGenerator);
        }

        @Override
        public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
            // In case of aggregation nodes in the sub-tree
            IQTree normalizedChild = transformChild(child)
                    .normalizeForOptimization(variableGenerator);

            QueryNode newChildRoot = normalizedChild.getRootNode();

            // May need to renormalize the tree (RECURSIVE)
            if ((newChildRoot instanceof ConstructionNode) && (!child.getRootNode().equals(newChildRoot)))
                return transformChild(iqFactory.createUnaryIQTree(rootNode, normalizedChild)
                        .normalizeForOptimization(variableGenerator));

            Substitution<ImmutableFunctionalTerm> initialSubstitution = rootNode.getSubstitution();

            // With the GROUP BY clause, groups are never empty
            boolean hasGroupBy = !rootNode.getGroupingVariables().isEmpty();

            ImmutableMap<Variable, AggregationSimplification> simplificationMap = initialSubstitution.builder()
                            .toMapIgnoreOptional((v, t) -> simplifyAggregationFunctionalTerm(t, normalizedChild, hasGroupBy));

            Substitution<ImmutableFunctionalTerm> newAggregationSubstitution = initialSubstitution.builder()
                            .flatTransform(simplificationMap::get, d -> d.getDecomposition().getSubstitution())
                            .build();

            IQTree pushDownChildTree = pushDownDefinitions(
                    normalizedChild,
                    simplificationMap.values().stream()
                            .flatMap(s -> s.getPushDownRequests().stream()));

            // Substitution of the new parent construction node (containing typically the RDF function)
            Substitution<ImmutableTerm> parentSubstitution = initialSubstitution.builder()
                    .transformOrRemove(simplificationMap::get, d -> d.getDecomposition().getLiftableTerm())
                    .build();

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(iqTreeTools.createOptionalConstructionNode(rootNode::getVariables, parentSubstitution))
                    .append(iqFactory.createAggregationNode(rootNode.getGroupingVariables(), newAggregationSubstitution))
                    .build(pushDownChildTree);
        }

        private Optional<AggregationSimplification> simplifyAggregationFunctionalTerm(ImmutableFunctionalTerm aggregationFunctionalTerm,
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

        private boolean isRDFFunctionalTerm(ImmutableTerm term) {
            return (term instanceof ImmutableFunctionalTerm)
                    && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol);
        }

        private ImmutableTerm extractRDFTermTypeTerm(ImmutableTerm rdfTerm) {
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
