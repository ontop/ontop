package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.constraints.HomomorphismFactory;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeHomomorphismIteratorImpl;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeListContainmentCheck;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MappingAssertionUnion {

    public static Collector<MappingAssertion, MappingAssertionUnion, Optional<MappingAssertion>> toMappingAssertion(ExtensionalDataNodeListContainmentCheck cqc, CoreSingletons coreSingletons, UnionBasedQueryMerger queryMerger) {
        return Collector.of(
                () -> new MappingAssertionUnion(cqc, coreSingletons, queryMerger), // Supplier
                MappingAssertionUnion::add, // Accumulator
                (b1, b2) -> { throw new MinorOntopInternalBugException("no merge"); }, // Merger
                MappingAssertionUnion::build, // Finisher
                Collector.Characteristics.UNORDERED);
    }

    private final List<ConjunctiveIQ> conjunctiveIqs = new ArrayList<>();
    private final List<IQ> otherIqs = new ArrayList<>();
    private final ExtensionalDataNodeListContainmentCheck cqc;
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final HomomorphismFactory homomorphismFactory;
    private final CoreSingletons coreSingletons;
    private final UnionBasedQueryMerger queryMerger;

    public MappingAssertionUnion(ExtensionalDataNodeListContainmentCheck cqc, CoreSingletons coreSingletons, UnionBasedQueryMerger queryMerger) {
        this.cqc = cqc;
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.homomorphismFactory = coreSingletons.getHomomorphismFactory();
        this.coreSingletons = coreSingletons;
        this.queryMerger = queryMerger;
    }

    public MappingAssertionUnion add(MappingAssertion assertion) {
        Optional<ConjunctiveIQ> cq = extractConjunctiveIQ(assertion);
        cq.ifPresentOrElse(this::mergeMappingsWithCQC, () -> otherIqs.add(assertion.getQuery()));
        return this;
    }

    private class ConjunctiveIQ {
        private final DistinctVariableOnlyDataAtom projectionAtom;
        private final Substitution<ImmutableTerm> substitution;
        private final ImmutableList<ExtensionalDataNode> extensionalDataNodes;
        private final Optional<ValuesNode> valuesNode;
        private final DisjunctionOfConjunctions filter;
        private final ImmutableSet<ImmutableExpression> constantsFilter;

        ConjunctiveIQ(DistinctVariableOnlyDataAtom projectionAtom, ConstructionNode constructionNode, ImmutableList<ExtensionalDataNode> extensionalDataNodes, Optional<ValuesNode> valuesNode, DisjunctionOfConjunctions filter) {
            this.projectionAtom = projectionAtom;
            this.substitution = constructionNode.getSubstitution();
            this.valuesNode = valuesNode;

            VariableGenerator variableGenerator = coreSingletons.getCoreUtilsFactory().createVariableGenerator(
                    Stream.concat(constructionNode.getVariables().stream(),
                                    Stream.concat(extensionalDataNodes.stream().flatMap(n -> n.getVariables().stream()),
                                            valuesNode.stream().flatMap(n -> n.getVariables().stream())))
                            .collect(ImmutableCollectors.toSet()));
                    ;
            ImmutableMap<Integer, ImmutableMap<Integer, Variable>> variableMap = IntStream.range(0, extensionalDataNodes.size())
                    .boxed()
                    .collect(ImmutableCollectors.toMap(i -> i, i -> extensionalDataNodes.get(i).getArgumentMap().entrySet().stream()
                            .filter(e -> !(e.getValue() instanceof Variable))
                            .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> variableGenerator.generateNewVariable()))));

            this.extensionalDataNodes = IntStream.range(0, extensionalDataNodes.size())
                    .mapToObj(i -> iqFactory.createExtensionalDataNode(
                            extensionalDataNodes.get(i).getRelationDefinition(),
                            extensionalDataNodes.get(i).getArgumentMap().entrySet().stream()
                                    .collect(ImmutableCollectors.toMap(
                                            Map.Entry::getKey,
                                            e -> Optional.<VariableOrGroundTerm>ofNullable(variableMap.get(i).get(e.getKey())).orElseGet(e::getValue)))))
                    .collect(ImmutableCollectors.toList());


            this.constantsFilter = variableMap.entrySet().stream()
                    .flatMap(me -> me.getValue().entrySet().stream()
                            .map(e -> Maps.immutableEntry(e.getValue(), extensionalDataNodes.get(me.getKey()).getArgumentMap().get(e.getKey()))))
                    .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue()))
                    .collect(ImmutableCollectors.toSet());

            this.filter = DisjunctionOfConjunctions.getAND(filter, constantsFilter);
        }

        ConjunctiveIQ(ConjunctiveIQ other, DisjunctionOfConjunctions filter) {
            this.projectionAtom = other.projectionAtom;
            this.substitution = other.substitution;
            this.extensionalDataNodes = other.extensionalDataNodes;
            this.valuesNode = other.valuesNode;

            this.filter = filter;
            this.constantsFilter = ImmutableSet.of();
        }

        IQ asIQ() {
            return iqFactory.createIQ(projectionAtom,
                    iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(projectionAtom.getVariables(), substitution), getTree()));
        }

        IQTree getTree() {
            // assumes that filter is a possibly empty list of non-empty lists
            Optional<ImmutableExpression> mergedConditions = termFactory.getDisjunction(
                    filter.stream().map(e -> termFactory.getConjunction(ImmutableList.copyOf(e))));

            if (extensionalDataNodes.isEmpty() && valuesNode.isEmpty())
                    return iqFactory.createTrueNode();
            else if (valuesNode.isEmpty() && extensionalDataNodes.size() == 1)
                    return mergedConditions
                            .<IQTree>map(c -> iqFactory.createUnaryIQTree(iqFactory.createFilterNode(c), extensionalDataNodes.get(0)))
                            .orElseGet(() -> extensionalDataNodes.get(0));
            else if (valuesNode.isPresent() && extensionalDataNodes.isEmpty())
                return mergedConditions
                        .<IQTree>map(c -> iqFactory.createUnaryIQTree(iqFactory.createFilterNode(c), valuesNode.get()))
                        .orElseGet(valuesNode::get);
            else return iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(mergedConditions),
                            Stream.concat(extensionalDataNodes.stream(), valuesNode.stream()).collect(ImmutableCollectors.toList()));
        }

        public ImmutableList<ImmutableTerm> getHeadTerms() { return substitution.applyToTerms(projectionAtom.getArguments());  }

        public Substitution<ImmutableTerm> getSubstitution() { return substitution; }

        public ImmutableList<ExtensionalDataNode> getDatabaseAtoms() { return extensionalDataNodes; }

        public Optional<ValuesNode> getValuesNode() { return valuesNode; }

        public DisjunctionOfConjunctions getConditions() { return filter; }

        @Override
        public int hashCode() {
            return Objects.hash(substitution, extensionalDataNodes, valuesNode, filter);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ConjunctiveIQ) {
                ConjunctiveIQ other = (ConjunctiveIQ)o;
                return (projectionAtom.equals(other.projectionAtom) &&
                        substitution.equals(other.substitution) &&
                        extensionalDataNodes.equals(other.extensionalDataNodes) &&
                        valuesNode.equals(other.valuesNode) &&
                        filter.equals(other.filter));
            }
            return false;
        }

        @Override
        public String toString() {
            return projectionAtom.getPredicate() + "(" + getHeadTerms() + ") <- " + extensionalDataNodes + " FILTER " + filter + " " + valuesNode;
        }

    }

    private Optional<ConjunctiveIQ> extractConjunctiveIQ(MappingAssertion assertion) {
        DistinctVariableOnlyDataAtom projectionAtom = assertion.getProjectionAtom();
        ConstructionNode constructionNode = (ConstructionNode) assertion.getQuery().getTree().getRootNode();
        IQTree topTree = assertion.getTopChild();
        if (topTree instanceof TrueNode) {
            return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of(), Optional.empty(), DisjunctionOfConjunctions.getTrue()));
        }
        if (topTree instanceof ExtensionalDataNode) {
            return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of((ExtensionalDataNode) topTree), Optional.empty(), DisjunctionOfConjunctions.getTrue()));
        }
        if (topTree instanceof ValuesNode) {
            return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of(), Optional.of((ValuesNode) topTree), DisjunctionOfConjunctions.getTrue()));
        }

        QueryNode topNode = topTree.getRootNode();
        if (topNode instanceof FilterNode) {
            ImmutableExpression filter = ((FilterNode)topNode).getFilterCondition();
            IQTree childTree = ((UnaryIQTree)topTree).getChild();
            if (childTree instanceof ExtensionalDataNode)
                return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of((ExtensionalDataNode) childTree), Optional.empty(), DisjunctionOfConjunctions.of(filter)));
            if (childTree instanceof ValuesNode)
                return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of(), Optional.of((ValuesNode) childTree), DisjunctionOfConjunctions.of(filter)));
        }

        if (topNode instanceof InnerJoinNode) {
            ImmutableList<IQTree> childrenTrees = topTree.getChildren();
            ImmutableList<ExtensionalDataNode> extensionalDataNodes = childrenTrees.stream()
                    .filter(n -> n instanceof ExtensionalDataNode)
                    .map(n -> (ExtensionalDataNode)n)
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ValuesNode> valuesNodes = childrenTrees.stream()
                    .filter(n -> n instanceof ValuesNode)
                    .map(n -> (ValuesNode)n)
                    .collect(ImmutableCollectors.toList());

            if (extensionalDataNodes.size() + valuesNodes.size() == childrenTrees.size() && valuesNodes.size() <= 1) {
                DisjunctionOfConjunctions filter = ((InnerJoinNode) topNode).getOptionalFilterCondition()
                        .map(DisjunctionOfConjunctions::of)
                        .orElseGet(DisjunctionOfConjunctions::getTrue);

                return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, extensionalDataNodes, valuesNodes.stream().findFirst(), filter));
            }
        }

        return Optional.empty();
    }

    public Optional<MappingAssertion> build() {
        Optional<IQ> query = queryMerger.mergeDefinitions(
                        Stream.concat(conjunctiveIqs.stream().map(ConjunctiveIQ::asIQ), otherIqs.stream())
                                .collect(ImmutableCollectors.toList()))
                .map(IQ::normalizeForOptimization);

        return query.map(q -> new MappingAssertion(q, null));
    }




    /***
     *
     * This is an optimization mechanism that allows T-mappings to reduce
     * the number of mapping assertions. The unfolding will then produce fewer queries.
     *
     * The method
     *    (1) removes a mapping assertion from rules if it is subsumed by the given assertion
     *
     *    (2) does not add the assertion if it is subsumed by one of the rules
     *
     *    (3) merges the given assertion into an existing assertion if their database atoms
     *        are homomorphically equivalent
     *
     * For example, if we are given
     *     S(x,z) :- R(x,y,z), y = 2
     * and rules contains
     *     S(x,z) :- R(x,y,z), y > 7
     * then this method will modify the existing assertion into
     *     S(x,z) :- R(x,y,z), OR(y > 7, y = 2)
     */

    private void mergeMappingsWithCQC(ConjunctiveIQ newCIQ) {

        if (conjunctiveIqs.contains(newCIQ))
            return;

        if (newCIQ.getValuesNode().isPresent()) {
            conjunctiveIqs.add(newCIQ); // facts are just added
            return;
        }

        Iterator<ConjunctiveIQ> iterator = conjunctiveIqs.iterator();
        while (iterator.hasNext()) {

            ConjunctiveIQ currentCIQ = iterator.next();

            boolean currentCiqContainsNewCiqButIsLonger = false;

            Optional<Homomorphism> fromCurrentCIQ = getHomomorphismIterator(currentCIQ, newCIQ)
                    .filter(Iterator::hasNext)
                    .map(Iterator::next);
            Optional<DisjunctionOfConjunctions> currentCIQConditionsImage = fromCurrentCIQ
                    .map(h -> applyHomomorphism(h, currentCIQ.getConditions()));

            if (fromCurrentCIQ.isPresent()) {
                if (contains(currentCIQConditionsImage.get(), newCIQ.getConditions())) {
                    if (newCIQ.getDatabaseAtoms().size() >= currentCIQ.getDatabaseAtoms().size()) {
                        return;
                    }
                    currentCiqContainsNewCiqButIsLonger = true;
                }
            }

            Optional<Homomorphism> fromNewCIQ = getHomomorphismIterator(newCIQ, currentCIQ)
                    .filter(Iterator::hasNext)
                    .map(Iterator::next);
            Optional<DisjunctionOfConjunctions> newCIQConditionsImage = fromNewCIQ
                    .map(h -> applyHomomorphism(h, newCIQ.getConditions()));

            if (fromNewCIQ.isPresent()) {
                if (contains(newCIQConditionsImage.get(), currentCIQ.getConditions())) {
                    iterator.remove();
                    continue;
                }
            }

            if (currentCiqContainsNewCiqButIsLonger) {
                return;
            }

            if (fromCurrentCIQ.isPresent() && fromNewCIQ.isPresent()) {
                // We found an equivalence, we will try to merge the non-empty conditions of newCIQ into currentCIQ

                DisjunctionOfConjunctions mergedConditions = DisjunctionOfConjunctions.getOR(currentCIQ.getConditions(), newCIQConditionsImage.get());
                if (mergedConditions.equals(currentCIQ.getConditions())) {
                    return;
                }

                ImmutableSet<Variable> currentCIQDatabaseAtomVariables = currentCIQ.getDatabaseAtoms().stream()
                        .flatMap(a -> a.getVariables().stream())
                        .collect(ImmutableCollectors.toSet());

                if (currentCIQDatabaseAtomVariables.containsAll(newCIQConditionsImage.get().getVariables())) {
                    iterator.remove();
                    conjunctiveIqs.add(new ConjunctiveIQ(currentCIQ, mergedConditions));
                    return;
                }
                // otherwise, need to merge data atoms - can be tricky
            }
        }
        conjunctiveIqs.add(newCIQ);
    }

    private Optional<Iterator<Homomorphism>> getHomomorphismIterator(ConjunctiveIQ from, ConjunctiveIQ to) {
        Homomorphism.Builder builder = homomorphismFactory.getHomomorphismBuilder();
        if (!builder.extend(from.getHeadTerms(), to.getHeadTerms()).isValid())
                return Optional.empty();

        Homomorphism h = builder.build();
        return Optional.of(new ExtensionalDataNodeHomomorphismIteratorImpl(
                h,
                from.getDatabaseAtoms(),
                cqc.chase(to.getDatabaseAtoms())));
    }

    private DisjunctionOfConjunctions applyHomomorphism(Homomorphism h, DisjunctionOfConjunctions f) {
        return f.stream()
                .map(d -> d.stream().map(atom -> h.applyToBooleanExpression(atom, termFactory))
                        .collect(ImmutableCollectors.toSet()))
                .collect(DisjunctionOfConjunctions.toDisjunctionOfConjunctions());
    }

    private boolean contains(DisjunctionOfConjunctions f1, DisjunctionOfConjunctions f2) {
        return f1.isTrue()
                || !f2.isTrue() && f2.stream().allMatch(c -> f1.stream().anyMatch(c::containsAll));
    }
}
