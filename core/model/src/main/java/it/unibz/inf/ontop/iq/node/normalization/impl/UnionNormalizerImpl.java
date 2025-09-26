package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.TrueNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.iq.node.normalization.UnionNormalizer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Singleton
public class UnionNormalizerImpl implements UnionNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final NotRequiredVariableRemover notRequiredVariableRemover;
    private final IQTreeTools iqTreeTools;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private UnionNormalizerImpl(IntermediateQueryFactory iqFactory, NotRequiredVariableRemover notRequiredVariableRemover, IQTreeTools iqTreeTools, TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.notRequiredVariableRemover = notRequiredVariableRemover;
        this.iqTreeTools = iqTreeTools;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQTree normalizeForOptimization(UnionNode unionNode, ImmutableList<IQTree> children, VariableGenerator variableGenerator, IQTreeCache treeCache) {

        var projectedVariables = unionNode.getVariables();

        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.normalizeForOptimization(variableGenerator))
                .filter(c -> !c.isDeclaredAsEmpty())
                .map(c -> notRequiredVariableRemover.optimize(c, projectedVariables, variableGenerator))
                .collect(ImmutableCollectors.toList());

        switch (liftedChildren.size()) {
            case 0:
                return iqFactory.createEmptyNode(projectedVariables);
            case 1:
                return liftedChildren.get(0);
            default:
                return tryToMergeSomeChildrenInAValuesNode(
                        liftBindingFromLiftedChildrenAndFlatten(unionNode, liftedChildren, variableGenerator, treeCache),
                        variableGenerator, treeCache);
        }
    }

    private IQTree tryToMergeSomeChildrenInAValuesNode(IQTree tree, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        var construction = IQTreeTools.UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
        if (construction.isPresent()) {
            IQTree newSubTree = tryToMergeSomeChildrenInAValuesNode(construction.getChild(), variableGenerator, treeCache, false);
            return (construction.getChild() == newSubTree)
                    ? tree
                    : iqFactory.createUnaryIQTree(construction.getNode(), newSubTree,
                    treeCache.declareAsNormalizedForOptimizationWithEffect());
        }

        return tryToMergeSomeChildrenInAValuesNode(tree, variableGenerator, treeCache, true);
    }


    private IQTree tryToMergeSomeChildrenInAValuesNode(IQTree tree, VariableGenerator variableGenerator, IQTreeCache treeCache,
                                                       boolean isRoot) {
        var union = NaryIQTreeTools.UnionDecomposition.of(tree);
        if (!union.isPresent())
            return tree;

        UnionNode unionNode = union.getNode();
        ImmutableList<IQTree> children = union.getChildren();

        ImmutableList<IQTree> nonMergedChildren = children.stream()
                .filter(t -> !isMergeableInValuesNode(t))
                .collect(ImmutableCollectors.toList());

        // At least 2 mergeable children are needed
        if (nonMergedChildren.size() >= children.size() - 1)
            return tree;

        // Tries to reuse the ordered value list of a values node
        ImmutableSet<Variable> valuesVariables = unionNode.getVariables();

        ImmutableList<ImmutableMap<Variable, Constant>> values = children.stream()
                .filter(this::isMergeableInValuesNode)
                .flatMap(c -> extractValues(c, valuesVariables))
                .collect(ImmutableCollectors.toList());

        IQTree mergedSubTree = iqFactory.createValuesNode(valuesVariables, values)
                // NB: some columns may be extracted and put into a construction node
                .normalizeForOptimization(variableGenerator);

        if (nonMergedChildren.isEmpty())
            return mergedSubTree;

        ImmutableList<IQTree> newChildren = Stream.concat(
                        Stream.of(mergedSubTree),
                        nonMergedChildren.stream())
                .collect(ImmutableCollectors.toList());

        return isRoot
                // Merging values nodes cannot trigger new binding lift opportunities
                ? iqFactory.createNaryIQTree(unionNode, newChildren,
                treeCache.declareAsNormalizedForOptimizationWithEffect())
                : iqFactory.createNaryIQTree(unionNode, newChildren);
    }

    /**
     * TODO: relax these constraints once we are sure non-DB constants in values nodes
     * are lifted or transformed properly in the rest of the code
     */
    private boolean isMergeableInValuesNode(IQTree tree) {
        if ((tree instanceof ValuesNode) || (tree instanceof TrueNode))
            return true;

        var construction = IQTreeTools.UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
        if (construction.isPresent()) {
            IQTree child = construction.getChild();
            return ((child instanceof ValuesNode) || (child instanceof TrueNode))
                    //NB: RDF constants are already expected to be decomposed
                    && construction.getNode().getSubstitution()
                    .rangeAllMatch(v -> (v instanceof DBConstant) || v.isNull());
        }
        return false;
    }

    private Stream<ImmutableMap<Variable, Constant>> extractValues(IQTree tree, ImmutableSet<Variable> outputVariables) {
        if (tree instanceof ValuesNode) {
            ValuesNode valuesNode = (ValuesNode) tree;
            if (valuesNode.getVariables().equals(outputVariables))
                return valuesNode.getValueMaps().stream();

            return valuesNode.getValueMaps().stream()
                    .map(m -> m.entrySet().stream()
                            .filter(e -> outputVariables.contains(e.getKey()))
                            .collect(ImmutableCollectors.toMap()));
        }

        if (tree instanceof TrueNode) // This can be allowed only if UNION has no variables!
            return Stream.of(ImmutableMap.of());

        var construction = IQTreeTools.UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
        if (construction.isPresent()) {
            Substitution<ImmutableTerm> substitution = construction.getNode().getSubstitution();
            IQTree child = construction.getChild();

            if (child instanceof ValuesNode) {
                return ((ValuesNode) child).getValueMaps().stream()
                        .map(m -> outputVariables.stream()
                                .collect(ImmutableCollectors.toMap(
                                        v -> v,
                                        v -> Optional.ofNullable(m.get(v)).orElseGet(() -> (Constant) substitution.get(v)))));
            }

            if (child instanceof TrueNode) {
                return Stream.of(outputVariables.stream()
                        .collect(ImmutableCollectors.toMap(
                                v -> v,
                                v -> (Constant) substitution.get(v))));
            }
        }
        throw new MinorOntopInternalBugException("Unexpected tree: " + tree);
    }

    /**
     * Has at least two children.
     * The returned tree may be opportunistically marked as "normalized" in case no further optimization is applied in this class.
     * Such a flag won't be taken seriously until leaving this class.
     *
     */
    private IQTree liftBindingFromLiftedChildrenAndFlatten(UnionNode unionNode, ImmutableList<IQTree> liftedChildren, VariableGenerator variableGenerator,
                                                           IQTreeCache treeCache) {

        var projectedVariables = unionNode.getVariables();

        /*
         * Cannot lift anything if some children do not have a construction node
         */
        ImmutableList<IQTreeTools.UnaryIQTreeDecomposition<ConstructionNode>> liftedChildrenDecompositions = IQTreeTools.UnaryIQTreeDecomposition.of(liftedChildren, ConstructionNode.class);

        if (liftedChildrenDecompositions.stream()
                .anyMatch(c -> !c.isPresent()))
            // Opportunistically flagged as normalized. May be discarded later on
            return iqFactory.createNaryIQTree(unionNode, flattenChildren(liftedChildren), treeCache.declareAsNormalizedForOptimizationWithEffect());

        ImmutableList<Substitution<ImmutableTerm>> tmpNormalizedChildSubstitutions = liftedChildrenDecompositions.stream()
                .map(IQTreeTools.UnaryIQTreeDecomposition::getNode)
                .map(ConstructionNode::getSubstitution)
                .map(s -> s.transform(this::normalizeNullAndRDFConstants))
                .collect(ImmutableCollectors.toList());

        Substitution<ImmutableTerm> mergedSubstitution = projectedVariables.stream()
                .map(v -> mergeDefinitions(v, tmpNormalizedChildSubstitutions, variableGenerator)
                        .map(d -> Maps.immutableEntry(v, d)))
                .flatMap(Optional::stream)
                .collect(substitutionFactory.toSubstitution());

        if (mergedSubstitution.isEmpty()) {
            // Opportunistically flagged as normalized. May be discarded later on
            return iqFactory.createNaryIQTree(unionNode, flattenChildren(liftedChildren), treeCache.declareAsNormalizedForOptimizationWithEffect());
        }
        ConstructionNode newRootNode = iqFactory.createConstructionNode(projectedVariables,
                // Cleans up the temporary "normalization"
                mergedSubstitution.transform(ImmutableTerm::simplify));

        ImmutableSet<Variable> unionVariables = newRootNode.getChildVariables();

        ImmutableList<IQTree> newChildrenStream = IntStream.range(0, liftedChildrenDecompositions.size())
                .mapToObj(i -> updateChild(
                        liftedChildrenDecompositions.get(i).getNode(),
                        liftedChildrenDecompositions.get(i).getChild(),
                        mergedSubstitution,
                        tmpNormalizedChildSubstitutions.get(i),
                        unionVariables,
                        variableGenerator))
                .flatMap(this::flattenChild)
                .collect(ImmutableCollectors.toList());

        IQTree unionIQ = iqTreeTools.createUnionTree(unionVariables,
                NaryIQTreeTools.transformChildren(newChildrenStream,
                        c -> iqTreeTools.unaryIQTreeBuilder(unionVariables).build(c)));

        return iqFactory.createUnaryIQTree(newRootNode, unionIQ)
                // TODO: see if needed or if we could opportunistically mark the tree as normalized
                .normalizeForOptimization(variableGenerator);
    }

    private ImmutableList<IQTree> flattenChildren(ImmutableList<IQTree> children) {
        ImmutableList<IQTree> flattenedChildren = children.stream()
                .flatMap(this::flattenChild)
                .collect(ImmutableCollectors.toList());
        return (children.size() == flattenedChildren.size())
                ? children
                : flattenedChildren;
    }

    private Stream<IQTree> flattenChild(IQTree child) {
        return (child.getRootNode() instanceof UnionNode)
                ? child.getChildren().stream()
                : Stream.of(child);
    }

    /**
     * RDF constants are transformed into RDF ground terms
     * Trick: NULL --> RDF(NULL,NULL)
     *
     * This "normalization" is temporary --> it will be "cleaned" but simplify the terms afterwards
     *
     */
    private ImmutableTerm normalizeNullAndRDFConstants(ImmutableTerm definition) {
        if (definition instanceof RDFConstant) {
            RDFConstant constant = (RDFConstant) definition;
            return termFactory.getRDFFunctionalTerm(
                    termFactory.getDBStringConstant(constant.getValue()),
                    termFactory.getRDFTermTypeConstant(constant.getType()));
        }
        else if (definition.isNull())
            return termFactory.getRDFFunctionalTerm(
                    termFactory.getNullConstant(), termFactory.getNullConstant());
        else
            return definition;
    }

    private Optional<ImmutableTerm> mergeDefinitions(
            Variable variable,
            ImmutableCollection<Substitution<ImmutableTerm>> childSubstitutions,
            VariableGenerator variableGenerator) {

        if (childSubstitutions.stream()
                .anyMatch(s -> !s.isDefining(variable)))
            return Optional.empty();

        return childSubstitutions.stream()
                .map(s -> s.get(variable))
                .map(this::normalizeNullAndRDFConstants)
                .map(Optional::of)
                .reduce((od1, od2) -> od1
                        .flatMap(d1 -> od2
                                .flatMap(d2 -> combineDefinitions(d1, d2, variableGenerator, true))))
                .flatMap(t -> t);
    }

    /**
     * Compare and combine the bindings, returning only the compatible (partial) values.
     *
     */
    private Optional<ImmutableTerm> combineDefinitions(ImmutableTerm d1, ImmutableTerm d2,
                                                       VariableGenerator variableGenerator,
                                                       boolean topLevel) {
        if (d1.equals(d2)) {
            return Optional.of(
                    // Top-level var-to-var must not be renamed since they are about projected variables
                    (d1.isGround() || (topLevel && (d1 instanceof Variable)))
                            ? d1
                            : replaceVariablesByFreshOnes((NonGroundTerm)d1, variableGenerator));
        }
        else if (d1 instanceof Variable)  {
            return topLevel
                    ? Optional.empty()
                    : Optional.of(variableGenerator.generateNewVariableFromVar((Variable) d1));
        }
        else if (d2 instanceof Variable)  {
            return topLevel
                    ? Optional.empty()
                    : Optional.of(variableGenerator.generateNewVariableFromVar((Variable) d2));
        }
        else if ((d1 instanceof ImmutableFunctionalTerm) && (d2 instanceof ImmutableFunctionalTerm)) {
            ImmutableFunctionalTerm functionalTerm1 = (ImmutableFunctionalTerm) d1;
            ImmutableFunctionalTerm functionalTerm2 = (ImmutableFunctionalTerm) d2;

            FunctionSymbol firstFunctionSymbol = functionalTerm1.getFunctionSymbol();

            /*
             * Different function symbols: stops the common part here.
             *
             * Same for functions that do not strongly type their arguments (unsafe to decompose). Example: STRICT_EQ
             */
            if ((!firstFunctionSymbol.equals(functionalTerm2.getFunctionSymbol()))
                    || (!firstFunctionSymbol.shouldBeDecomposedInUnion())) {
                return topLevel
                        ? Optional.empty()
                        : Optional.of(variableGenerator.generateNewVariable());
            }
            else {
                ImmutableList<? extends ImmutableTerm> arguments1 = functionalTerm1.getTerms();
                ImmutableList<? extends ImmutableTerm> arguments2 = functionalTerm2.getTerms();
                if (arguments1.size() != arguments2.size()) {
                    throw new IllegalStateException("Functions have different arities, they cannot be combined");
                }

                ImmutableList<ImmutableTerm> newArguments = IntStream.range(0, arguments1.size())
                        // RECURSIVE
                        .mapToObj(i -> combineDefinitions(arguments1.get(i), arguments2.get(i), variableGenerator, false)
                                .orElseGet(variableGenerator::generateNewVariable))
                        .collect(ImmutableCollectors.toList());

                return Optional.of(termFactory.getImmutableFunctionalTerm(firstFunctionSymbol, newArguments));
            }
        }
        else {
            return Optional.empty();
        }
    }

    private NonGroundTerm replaceVariablesByFreshOnes(NonGroundTerm term, VariableGenerator variableGenerator) {
        if (term instanceof Variable)
            return variableGenerator.generateNewVariableFromVar((Variable) term);

        NonGroundFunctionalTerm functionalTerm = (NonGroundFunctionalTerm) term;

        return termFactory.getNonGroundFunctionalTerm(functionalTerm.getFunctionSymbol(),
                functionalTerm.getTerms().stream()
                        .map(a -> a instanceof NonGroundTerm
                                // RECURSIVE
                                ? replaceVariablesByFreshOnes((NonGroundTerm) a, variableGenerator)
                                : a)
                        .collect(ImmutableCollectors.toList()));
    }

    /**
     * TODO: find a better name
     */
    private IQTree updateChild(ConstructionNode constructionNode, IQTree liftedGrandChild, Substitution<ImmutableTerm> mergedSubstitution,
                               Substitution<ImmutableTerm> tmpNormalizedSubstitution,
                               ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator) {

        ImmutableSet<Variable> formerV = constructionNode.getVariables();

        Substitution<ImmutableTerm> normalizedEta = substitutionFactory.onImmutableTerms().unifierBuilder(tmpNormalizedSubstitution)
                .unify(mergedSubstitution.stream(), Map.Entry::getKey, Map.Entry::getValue)
                .build()
                /*
                 * Normalizes eta so as to avoid projected variables to be substituted by non-projected variables.
                 *
                 * This normalization can be understood as a way to select a MGU (eta) among a set of equivalent MGUs.
                 * Such a "selection" is done a posteriori.
                 *
                 * Due to the current implementation of MGUS, the normalization should have no effect
                 * (already in a normal form). Here for safety.
                 */
                .map(eta -> substitutionFactory.getPrioritizingRenaming(eta, projectedVariables).compose(eta))
                .orElseThrow(() -> new QueryNodeSubstitutionException("The descending substitution " + mergedSubstitution
                        + " is incompatible with " + tmpNormalizedSubstitution));

        Substitution<ImmutableTerm> newTheta = normalizedEta.builder()
                .restrictDomainTo(projectedVariables)
                // Cleans up the temporary "normalization", in particular non-lifted RDF(NULL,NULL)
                .transform(ImmutableTerm::simplify)
                .build();

        Substitution<VariableOrGroundTerm> descendingSubstitution = normalizedEta.builder()
                .removeFromDomain(tmpNormalizedSubstitution.getDomain())
                .removeFromDomain(Sets.difference(newTheta.getDomain(), formerV))
                // NB: this is expected to be ok given that the expected compatibility of the merged substitution with
                // this construction node
                .transform(t -> (VariableOrGroundTerm)t)
                .build();

        DownPropagation dp = DownPropagation.of(descendingSubstitution, Optional.empty(), liftedGrandChild.getVariables(), variableGenerator, termFactory, iqFactory);
        IQTree newChild = dp.propagate(liftedGrandChild);

        var optionalConstructionNode = iqTreeTools.createOptionalConstructionNode(() -> projectedVariables, newTheta);
        return iqTreeTools.unaryIQTreeBuilder()
                .append(optionalConstructionNode)
                .build(newChild);
    }
}
