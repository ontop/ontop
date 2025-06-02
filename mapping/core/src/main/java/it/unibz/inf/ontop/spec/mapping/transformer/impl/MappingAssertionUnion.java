package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.constraints.HomomorphismFactory;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeHomomorphismIteratorImpl;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeListContainmentCheck;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
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
    private final IQTreeTools iqTreeTools;

    public MappingAssertionUnion(ExtensionalDataNodeListContainmentCheck cqc, CoreSingletons coreSingletons, UnionBasedQueryMerger queryMerger) {
        this.cqc = cqc;
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.homomorphismFactory = coreSingletons.getHomomorphismFactory();
        this.coreSingletons = coreSingletons;
        this.queryMerger = queryMerger;
        this.iqTreeTools = coreSingletons.getIQTreeTools();
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

        ConjunctiveIQ(DistinctVariableOnlyDataAtom projectionAtom, ConstructionNode constructionNode, ImmutableList<ExtensionalDataNode> extensionalDataNodes, Optional<ValuesNode> valuesNode, DisjunctionOfConjunctions filter) {

            VariableGenerator variableGenerator = coreSingletons.getCoreUtilsFactory().createVariableGenerator(
                    Stream.concat(constructionNode.getVariables().stream(),
                                    Stream.concat(extensionalDataNodes.stream().flatMap(n -> n.getVariables().stream()),
                                            valuesNode.stream().flatMap(n -> n.getVariables().stream())))
                            .collect(ImmutableCollectors.toSet()));
            ;
            this.projectionAtom = projectionAtom;

            // replaces constant IRI in the object position of properties with a ValueNode
            RDFAtomPredicate rdfAtomPredicate = (RDFAtomPredicate) projectionAtom.getPredicate();
            ImmutableList<ImmutableTerm> args = constructionNode.getSubstitution().apply(projectionAtom.getArguments());

            ImmutableMap<IRIConstant, Variable> map  = Stream.of(
                            Optional.of(rdfAtomPredicate.getSubject(args)),
                            rdfAtomPredicate.getPropertyIRI(args)
                                    .filter(i -> !i.equals(RDF.TYPE))
                                    .map(i -> rdfAtomPredicate.getObject(args)),
                            rdfAtomPredicate.getGraph(args))
                    .flatMap(Optional::stream)
                    .filter(c -> c instanceof IRIConstant)
                    .map(c -> (IRIConstant)c)
                    .distinct()
                    .collect(ImmutableCollectors.toMap(c -> c, c -> variableGenerator.generateNewVariable()));

            this.substitution = constructionNode.getSubstitution()
                    .transform(t -> Optional.ofNullable(map.get(t)).<ImmutableTerm>map(termFactory::getIRIFunctionalTerm).orElse(t));

            ImmutableMap<Variable, Constant> constantSubstitutionEntries = map.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getValue,
                            e -> termFactory.getDBStringConstant(e.getKey().getIRI().getIRIString())));

            // replaces constants in extensional data nodes with a ValueNode
            ImmutableMap<Integer, ImmutableMap<Integer, Variable>> variableMap = IntStream.range(0, extensionalDataNodes.size())
                    .boxed()
                    .collect(ImmutableCollectors.toMap(i -> i, i -> extensionalDataNodes.get(i).getArgumentMap().entrySet().stream()
                            .filter(e -> e.getValue() instanceof Constant)
                            .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> variableGenerator.generateNewVariable()))));

            this.extensionalDataNodes = variableMap.isEmpty()
                    ? extensionalDataNodes
                    : IntStream.range(0, extensionalDataNodes.size())
                        .mapToObj(i -> variableMap.get(i).isEmpty()
                                ? extensionalDataNodes.get(i)
                                : iqFactory.createExtensionalDataNode(
                                    extensionalDataNodes.get(i).getRelationDefinition(),
                                    extensionalDataNodes.get(i).getArgumentMap().entrySet().stream()
                                        .collect(ImmutableCollectors.toMap(
                                                Map.Entry::getKey,
                                                e -> Optional.<VariableOrGroundTerm>ofNullable(variableMap.get(i).get(e.getKey()))
                                                        .orElseGet(e::getValue)))))
                        .collect(ImmutableCollectors.toList());

            Optional<ImmutableMap<Variable, Constant>> constantsMap = Optional.of(Stream.concat(
                            constantSubstitutionEntries.entrySet().stream(),
                            variableMap.entrySet().stream()
                                    .flatMap(me -> me.getValue().entrySet().stream()
                                            .map(e -> Maps.immutableEntry(
                                                    e.getValue(),
                                                    (Constant)extensionalDataNodes.get(me.getKey()).getArgumentMap().get(e.getKey())))))
                    .collect(ImmutableCollectors.toMap()))
                    .filter(cm -> !cm.isEmpty());

            this.valuesNode = valuesNode
                    .map(v -> constantsMap
                            .map(cm -> iqFactory.createValuesNode(
                                    Sets.union(v.getVariables(), cm.keySet()).immutableCopy(),
                                    v.getValueMaps().stream()
                                            .map(m -> Stream.concat(m.entrySet().stream(), cm.entrySet().stream())
                                                    .collect(ImmutableCollectors.toMap()))
                                            .collect(ImmutableCollectors.toList())))
                            .orElse(v))
                    .or(() -> constantsMap
                            .map(cm -> iqFactory.createValuesNode(cm.keySet(), ImmutableList.of(cm))));

            this.filter = filter;
        }

        ConjunctiveIQ(ConjunctiveIQ other, DisjunctionOfConjunctions filter, Optional<ValuesNode> valuesNode) {
            this.projectionAtom = other.projectionAtom;
            this.substitution = other.substitution;
            this.extensionalDataNodes = other.extensionalDataNodes;

            this.filter = filter;
            this.valuesNode = valuesNode;
        }


        IQ asIQ() {
            return iqTreeTools.createMappingIQ(projectionAtom, substitution, getTree());
        }

        IQTree getTree() {
            // assumes that filter is a possibly empty list of non-empty lists
            Optional<ImmutableExpression> mergedConditions = translate(filter);

            ImmutableList<IQTree> children = Stream.concat(
                            extensionalDataNodes.stream(), valuesNode.stream())
                    .collect(ImmutableCollectors.toList());

            return iqTreeTools.createOptionalInnerJoinTree(mergedConditions, children)
                    .orElseGet(iqFactory::createTrueNode);
        }

        Optional<ImmutableExpression> translate(DisjunctionOfConjunctions filter) {

            switch (filter.getNumberOfConjunctions()) {
                case 0:
                    return Optional.empty();
                case 1:
                    return termFactory.getDisjunction(filter.stream()
                            .map(e -> termFactory.getConjunction(ImmutableList.copyOf(e))));
                default:
                    ImmutableSet<ImmutableExpression> sharedAtoms = filter.stream().findFirst()
                            .map(e -> e.stream().filter(c -> filter.stream().allMatch(e2 -> e2.contains(c))).collect(ImmutableCollectors.toSet()))
                            .get();

                    return termFactory.getConjunction(Stream.concat(
                            sharedAtoms.stream(),
                            termFactory.getDisjunction(filter.stream()
                                    .map(e -> termFactory.getConjunction(ImmutableList.copyOf(Sets.difference(e, sharedAtoms))))).stream()));
            }
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

    private class ConjunctiveIQExtractor implements IQVisitor<Optional<ConjunctiveIQ>> {
        private final DistinctVariableOnlyDataAtom projectionAtom;
        private final ConstructionNode constructionNode;

        private ConjunctiveIQExtractor(DistinctVariableOnlyDataAtom projectionAtom, ConstructionNode constructionNode) {
            this.projectionAtom = projectionAtom;
            this.constructionNode = constructionNode;
        }

        private Optional<ConjunctiveIQ> newConjunctiveIQ(ImmutableList<ExtensionalDataNode> extensionalDataNodes, Optional<ValuesNode> valuesNode, DisjunctionOfConjunctions filter) {
            return Optional.of(new ConjunctiveIQ(projectionAtom, constructionNode, extensionalDataNodes, valuesNode, filter));
        }

        @Override
        public Optional<ConjunctiveIQ> transformIntensionalData(IntensionalDataNode dataNode) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformExtensionalData(ExtensionalDataNode dataNode) {
            return newConjunctiveIQ(ImmutableList.of(dataNode), Optional.empty(), DisjunctionOfConjunctions.getTrue());
        }

        @Override
        public Optional<ConjunctiveIQ> transformEmpty(EmptyNode node) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformTrue(TrueNode node) {
            return newConjunctiveIQ(ImmutableList.of(), Optional.empty(), DisjunctionOfConjunctions.getTrue());
        }

        @Override
        public Optional<ConjunctiveIQ> transformNative(NativeNode nativeNode) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformValues(ValuesNode valuesNode) {
            return newConjunctiveIQ(ImmutableList.of(), Optional.of(valuesNode), DisjunctionOfConjunctions.getTrue());
        }

        @Override
        public Optional<ConjunctiveIQ> transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            ImmutableExpression filter = rootNode.getFilterCondition();
            if (child instanceof ExtensionalDataNode)
                return newConjunctiveIQ(ImmutableList.of((ExtensionalDataNode) child), Optional.empty(), DisjunctionOfConjunctions.of(filter));
            if (child instanceof ValuesNode)
                return newConjunctiveIQ(ImmutableList.of(), Optional.of((ValuesNode) child), DisjunctionOfConjunctions.of(filter));
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<ExtensionalDataNode> extensionalDataNodes = children.stream()
                    .filter(n -> n instanceof ExtensionalDataNode)
                    .map(n -> (ExtensionalDataNode)n)
                    .collect(ImmutableCollectors.toList());

            ImmutableList<ValuesNode> valuesNodes = children.stream()
                    .filter(n -> n instanceof ValuesNode)
                    .map(n -> (ValuesNode)n)
                    .collect(ImmutableCollectors.toList());

            if (extensionalDataNodes.size() + valuesNodes.size() == children.size() && valuesNodes.size() <= 1) {
                DisjunctionOfConjunctions filter = rootNode.getOptionalFilterCondition()
                        .map(DisjunctionOfConjunctions::of)
                        .orElseGet(DisjunctionOfConjunctions::getTrue);

                return newConjunctiveIQ(extensionalDataNodes, valuesNodes.stream().findFirst(), filter);
            }
            return Optional.empty();
        }

        @Override
        public Optional<ConjunctiveIQ> transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            return Optional.empty();
        }
    }

    private Optional<ConjunctiveIQ> extractConjunctiveIQ(MappingAssertion assertion) {
        DistinctVariableOnlyDataAtom projectionAtom = assertion.getProjectionAtom();
        ConstructionNode constructionNode = (ConstructionNode) assertion.getQuery().getTree().getRootNode();
        return assertion.getTopChild().acceptVisitor(new ConjunctiveIQExtractor(projectionAtom, constructionNode));
    }

    public Optional<MappingAssertion> build() {
        Optional<IQ> query = queryMerger.mergeDefinitions(
                        Stream.concat(conjunctiveIqs.stream().map(ConjunctiveIQ::asIQ), otherIqs.stream())
                                .collect(ImmutableCollectors.toList()))
                .map(IQ::normalizeForOptimization);

        //if (query.toString().contains("UNION"))
        //    System.out.println("MAU-UNION: " + query);

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

        //System.out.println("PROCESSING: " + newCIQ);

        if (conjunctiveIqs.contains(newCIQ))
            return;

        Iterator<ConjunctiveIQ> iterator = conjunctiveIqs.iterator();
        while (iterator.hasNext()) {

            ConjunctiveIQ currentCIQ = iterator.next();

            boolean currentCiqContainsNewCiqButIsLonger = false;

            Optional<Homomorphism> fromCurrentCIQ = getHomomorphismIterator(currentCIQ, newCIQ)
                    .filter(Iterator::hasNext)
                    .map(Iterator::next);
            Optional<DisjunctionOfConjunctions> currentCIQConditionsImage = fromCurrentCIQ
                    .map(h -> applyHomomorphism(h, currentCIQ.getConditions()));
;
            if (fromCurrentCIQ.isPresent() && contains(currentCIQConditionsImage.get(), newCIQ.getConditions())) {
                if (contains(fromCurrentCIQ.get(), currentCIQ.getValuesNode(), newCIQ.getValuesNode())) {
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

            if (fromNewCIQ.isPresent() && contains(newCIQConditionsImage.get(), currentCIQ.getConditions())) {
                if (contains(fromNewCIQ.get(), newCIQ.getValuesNode(), currentCIQ.getValuesNode())) {
                    iterator.remove();
                    continue;
                }
            }

            if (currentCiqContainsNewCiqButIsLonger) {
                return;
            }

            if (fromCurrentCIQ.isPresent() && fromNewCIQ.isPresent()) {
                if (currentCIQ.getConditions().isTrue() && newCIQ.getConditions().isTrue() || newCIQConditionsImage.get().equals(currentCIQ.getConditions())) {
                    ValuesNode currentCIQValuesNode = currentCIQ.getValuesNode().get();
                    Optional<ValuesNode> optionalNewCIQValuesNodeImage = applyHomomorphism(fromNewCIQ.get(), newCIQ.getValuesNode().get());
                    if (optionalNewCIQValuesNodeImage.isPresent()) {
                        ValuesNode newCIQValuesNodeImage = optionalNewCIQValuesNodeImage.get();
                        if (newCIQValuesNodeImage.getVariables().equals(currentCIQValuesNode.getVariables())) {
                            iterator.remove();
                            conjunctiveIqs.add(new ConjunctiveIQ(currentCIQ,
                                    currentCIQ.getConditions(),
                                    Optional.of(iqFactory.createValuesNode(
                                            currentCIQValuesNode.getVariables(),
                                            Stream.concat(
                                                            newCIQValuesNodeImage.getValueMaps().stream(),
                                                            currentCIQValuesNode.getValueMaps().stream())
                                                    .distinct()
                                                    .collect(ImmutableCollectors.toList())))));
                            //System.out.println("MAU-MERGE-VALUES: " + currentCIQ + " AND " + newCIQ);
                            return;
                        }
                    }
                }
                // We found an equivalence, we will try to merge the *non-empty* conditions of newCIQ into currentCIQ
                ImmutableSet<Variable> currentCIQDatabaseAtomVariables = currentCIQ.getDatabaseAtoms().stream()
                        .flatMap(a -> a.getVariables().stream())
                        .collect(ImmutableCollectors.toSet());

                DisjunctionOfConjunctions newCIQCombinedConditionsImage = DisjunctionOfConjunctions.getAND(
                        newCIQConditionsImage.get(),
                        applyHomomorphism(fromNewCIQ.get(), translate(newCIQ.getValuesNode())));

                if (currentCIQDatabaseAtomVariables.containsAll(newCIQCombinedConditionsImage.getVariables())) {
                    iterator.remove();
                    conjunctiveIqs.add(
                            new ConjunctiveIQ(
                                    currentCIQ,
                                    DisjunctionOfConjunctions.getOR(
                                            DisjunctionOfConjunctions.getAND(currentCIQ.getConditions(), translate(currentCIQ.getValuesNode())),
                                            newCIQCombinedConditionsImage),
                                    Optional.empty()));
                    //System.out.println("MAU-MERGE-CONDITION: " + currentCIQ + " AND " + newCIQ);
                    return;
                }
                // one reason for non-merge is R(x,_), x = 1 and R(_,x), x = 2 (see TMappingConstantPositionsTest)
                // second reason for non-merge is variables in ValueNode that do not occur in data atoms (but occur in the ConstructionNode instead)
                //System.out.println("MAU-CANT-MERGE-45: " + currentCIQ + " AND " + newCIQ + " " + currentCIQDatabaseAtomVariables + " " + newCIQCombinedConditionsImage.getVariables());
            }
        }
        conjunctiveIqs.add(newCIQ);
    }

    DisjunctionOfConjunctions translate(Optional<ValuesNode> valuesNode) {
        if (valuesNode.isEmpty())
            return DisjunctionOfConjunctions.getTrue();

        return DisjunctionOfConjunctions.of(termFactory.getDisjunction(valuesNode.get().getValueMaps().stream()
                .map(m -> termFactory.getConjunction(m.entrySet().stream()
                        .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue()))
                        .collect(ImmutableCollectors.toList())))
                .collect(ImmutableCollectors.toList())));
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

    private Optional<ValuesNode> applyHomomorphism(Homomorphism h, ValuesNode n) {
        ImmutableSet<Variable> newVariables = n.getVariables().stream()
                .map(h::apply)
                .map(v -> (Variable)v)
                .collect(ImmutableCollectors.toSet());

        if (newVariables.size() < n.getVariables().size())
            return Optional.empty();

        return Optional.of(iqFactory.createValuesNode(
                newVariables,
                n.getValueMaps().stream()
                        .map(m -> m.entrySet().stream()
                                .collect(ImmutableCollectors.toMap(e -> (Variable)h.apply(e.getKey()), Map.Entry::getValue)))
                        .collect(ImmutableCollectors.toList())));
    }

    private boolean contains(Homomorphism h, Optional<ValuesNode> v1, Optional<ValuesNode> v2) {
        if (v1.isEmpty())
            return true;

        Optional<ValuesNode> v1Image = applyHomomorphism(h, v1.get());
        return v1Image
                .filter(valuesNode -> v2.isPresent() && v2.get().getValueMaps().stream()
                        .allMatch(c -> valuesNode.getValueMaps().stream().anyMatch(m -> c.entrySet().containsAll(m.entrySet()))))
                .isPresent();

    }
}
