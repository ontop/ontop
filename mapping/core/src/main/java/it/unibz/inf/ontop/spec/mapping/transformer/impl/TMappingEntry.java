package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
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

public class TMappingEntry {

    public static Collector<MappingAssertion, TMappingEntry, Optional<MappingAssertion>> toMappingAssertion(ExtensionalDataNodeListContainmentCheck cqc, CoreSingletons coreSingletons, UnionBasedQueryMerger queryMerger) {
        return Collector.of(
                () -> new TMappingEntry(cqc, coreSingletons, queryMerger), // Supplier
                TMappingEntry::add, // Accumulator
                (b1, b2) -> { throw new MinorOntopInternalBugException("no merge"); }, // Merger
                TMappingEntry::build, // Finisher
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

    public TMappingEntry(ExtensionalDataNodeListContainmentCheck cqc, CoreSingletons coreSingletons, UnionBasedQueryMerger queryMerger) {
        this.cqc = cqc;
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.homomorphismFactory = coreSingletons.getHomomorphismFactory();
        this.coreSingletons = coreSingletons;
        this.queryMerger = queryMerger;
    }

    public TMappingEntry add(MappingAssertion assertion) {
        Optional<ConjunctiveIQ> cq = extractConjunctiveIQ(assertion);
        cq.ifPresentOrElse(this::mergeMappingsWithCQC, () -> otherIqs.add(assertion.getQuery()));
        return this;
    }

    private class ConjunctiveIQ {
        private final DistinctVariableOnlyDataAtom projectionAtom;
        private final Substitution<ImmutableTerm> substitution;
        private final ImmutableList<ExtensionalDataNode> extensionalDataNodes;
        private final Optional<ValuesNode> valuesNode;
        private final ImmutableList<ImmutableList<ImmutableExpression>> filter;
        private final ImmutableList<ImmutableExpression> constantsFilter;

        ConjunctiveIQ(DistinctVariableOnlyDataAtom projectionAtom, ConstructionNode constructionNode, ImmutableList<ExtensionalDataNode> extensionalDataNodes, Optional<ValuesNode> valuesNode, ImmutableList<ImmutableList<ImmutableExpression>> filter) {
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
                    .collect(ImmutableCollectors.toList());

            if (constantsFilter.isEmpty())
                this.filter = filter;
            else
                this.filter = filter.isEmpty()
                        ? ImmutableList.of(constantsFilter)
                        : ImmutableList.of(Stream.concat(constantsFilter.stream(), filter.get(0).stream()).collect(ImmutableCollectors.toList()));
        }

        ConjunctiveIQ(ConjunctiveIQ other, ImmutableList<ImmutableList<ImmutableExpression>> filter) {
            this.projectionAtom = other.projectionAtom;
            this.substitution = other.substitution;
            this.extensionalDataNodes = other.extensionalDataNodes;
            this.valuesNode = other.valuesNode;

            this.filter = filter;
            this.constantsFilter = ImmutableList.of();
        }

        IQ asIQ() {
            return iqFactory.createIQ(projectionAtom,
                    iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(projectionAtom.getVariables(), substitution), getTree()));
        }

        IQTree getTree() {
            // assumes that filterAtoms is a possibly empty list of non-empty lists
            Optional<ImmutableExpression> mergedConditions = termFactory.getDisjunction(
                    filter.stream().map(termFactory::getConjunction));

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

        public ImmutableList<ImmutableList<ImmutableExpression>> getConditions() { return filter; }

        // there is a .contains() method call that relies on equals

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
            return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of(), Optional.empty(), ImmutableList.of()));
        }
        if (topTree instanceof ExtensionalDataNode) {
            return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of((ExtensionalDataNode) topTree), Optional.empty(), ImmutableList.of()));
        }
        if (topTree instanceof ValuesNode) {
            return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of(), Optional.of((ValuesNode) topTree), ImmutableList.of()));
        }

        QueryNode topNode = topTree.getRootNode();
        if (topNode instanceof FilterNode) {
            ImmutableExpression filter = ((FilterNode)topNode).getFilterCondition();
            IQTree childTree = ((UnaryIQTree)topTree).getChild();
            if (childTree instanceof ExtensionalDataNode)
                return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of((ExtensionalDataNode) childTree), Optional.empty(), getDisjunctionOfConjunctions(filter)));
            if (childTree instanceof ValuesNode)
                return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, ImmutableList.of(), Optional.of((ValuesNode) childTree), getDisjunctionOfConjunctions(filter)));
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
                ImmutableList<ImmutableList<ImmutableExpression>> filter = ((InnerJoinNode) topNode).getOptionalFilterCondition()
                        .map(this::getDisjunctionOfConjunctions)
                        .orElseGet(ImmutableList::of);

                return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, extensionalDataNodes, valuesNodes.stream().findFirst(), filter));
            }
        }

        return Optional.empty();
    }

    private ImmutableList<ImmutableList<ImmutableExpression>> getDisjunctionOfConjunctions(ImmutableExpression condition) {
        return ImmutableList.of(condition.flattenAND().collect(ImmutableCollectors.toList()));
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

    private void mergeMappingsWithCQC(ConjunctiveIQ assertion) {

        if (conjunctiveIqs.contains(assertion))
            return;

        if (assertion.getValuesNode().isPresent()) {
            conjunctiveIqs.add(assertion); // facts are just added
            return;
        }

        Iterator<ConjunctiveIQ> mappingIterator = conjunctiveIqs.iterator();
        while (mappingIterator.hasNext()) {

            ConjunctiveIQ current = mappingIterator.next();
            // to and from refer to "to assertion" and "from assertion"

            boolean couldIgnore = false;

            Optional<Homomorphism> to = getHomomorphismIterator(current, assertion)
                            .filter(Iterator::hasNext)
                            .map(Iterator::next);

            if (to.isPresent()) {
                if (current.getConditions().isEmpty() ||
                        (current.getConditions().size() == 1 &&
                                assertion.getConditions().size() == 1 &&
                                // rule1.getConditions().get(0) contains all images of rule2.getConditions.get(0)
                                current.getConditions().get(0).stream()
                                        .map(atom -> to.get().applyToBooleanExpression(atom, termFactory))
                                        .allMatch(atom -> assertion.getConditions().get(0).contains(atom)))) {

                    if (assertion.getDatabaseAtoms().size() < current.getDatabaseAtoms().size()) {
                        couldIgnore = true;
                    }
                    else {
                        // if the new mapping is redundant and there are no conditions then do not add anything
                        return;
                    }
                }
            }

            Optional<Homomorphism> from = getHomomorphismIterator(assertion, current)
                            .filter(Iterator::hasNext)
                            .map(Iterator::next);

            if (from.isPresent()) {
                if (assertion.getConditions().isEmpty() ||
                        (assertion.getConditions().size() == 1 &&
                                current.getConditions().size() == 1 &&
                                // rule1.getConditions().get(0) contains all images of rule2.getConditions.get(0)
                                assertion.getConditions().get(0).stream()
                                        .map(atom -> from.get().applyToBooleanExpression(atom, termFactory))
                                        .allMatch(atom -> current.getConditions().get(0).contains(atom)))) {

                    // The existing query is more specific than the new query, so we
                    // need to add the new query and remove the old
                    mappingIterator.remove();
                    continue;
                }
            }

            if (couldIgnore) {
                // if the new mapping is redundant and there are no conditions then do not add anything
                return;
            }

            if (to.isPresent() && from.isPresent()) {
                // We found an equivalence, we will try to merge the conditions of
                // newRule into the current
                // Here we can merge conditions of the new query with the one we have just found
                // new map always has just one set of filters  !!
                ImmutableList<ImmutableExpression> newf = assertion.getConditions().get(0).stream()
                        .map(atom -> from.get().applyToBooleanExpression(atom, termFactory))
                        .collect(ImmutableCollectors.toList());

                ImmutableSet<Variable> newfVars = newf.stream()
                        .flatMap(ImmutableTerm::getVariableStream)
                        .collect(ImmutableCollectors.toSet());
                ImmutableSet<Variable> ccVars = current.getDatabaseAtoms().stream()
                        .flatMap(a -> a.getVariables().stream())
                        .collect(ImmutableCollectors.toSet());
                if (ccVars.containsAll(newfVars)) {
                    // if each of the existing conditions in one of the filter groups
                    // is found in the new filter then the new filter is redundant
                    if (current.getConditions().stream().anyMatch(newf::containsAll))
                        return;

                    // REPLACE THE CURRENT RULE
                    mappingIterator.remove();
                    conjunctiveIqs.add(new ConjunctiveIQ(current, Stream.concat(
                            current.getConditions().stream()
                                    // if each of the new conditions is found among econd then the old condition is redundant
                                    .filter(f -> !f.containsAll(newf)),
                            Stream.of(newf))
                            .collect(ImmutableCollectors.toList())));
                    return;
                }
            }
        }
        conjunctiveIqs.add(assertion);
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
}
