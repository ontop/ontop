package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeTraverser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.TrueNodesRemovalOptimizer;
import it.unibz.inf.ontop.iq.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.iq.proposal.impl.QueryMergingProposalImpl;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.reformulation.RuleUnfolder;
import it.unibz.inf.ontop.reformulation.impl.RuleUnfolderImpl;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.impl.IntervalAndIntermediateQuery;
import it.unibz.inf.ontop.spec.mapping.transformer.DatalogMTLToIntermediateQueryConverter;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.temporal.datalog.impl.DatalogMTLConversionTools;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class TemporalMappingSaturatorImpl implements TemporalMappingSaturator {

    private final DatalogMTLToIntermediateQueryConverter dMTLConverter;
    private final RuleUnfolder ruleUnfolder;

    @Inject
    private TemporalMappingSaturatorImpl(DatalogMTLToIntermediateQueryConverter dMTLConverter,
                                         RuleUnfolder ruleUnfolder) {
        this.dMTLConverter = dMTLConverter;
        this.ruleUnfolder = ruleUnfolder;
    }

    @Override
    public TemporalMapping saturate(Mapping mapping, DBMetadata dbMetadata,
                                    TemporalMapping temporalMapping, DBMetadata temporalDBMetadata,
                                    DatalogMTLProgram datalogMTLProgram) {

        Queue<DatalogMTLRule> queue = new LinkedList<>();

        queue.addAll(datalogMTLProgram.getRules());
        Map <AtomPredicate, IntermediateQuery> mergedMap = mergeMappings(mapping,temporalMapping);

        while (!queue.isEmpty()) {
            DatalogMTLRule rule = queue.poll();
            if (!(rule.getBody() instanceof StaticJoinExpression) ||
                    ((rule.getBody() instanceof FilterExpression) &&
                            !(((FilterExpression) rule.getBody()).getExpression() instanceof StaticJoinExpression))) {
                IntermediateQuery intermediateQuery = dMTLConverter.dMTLToIntermediateQuery(rule,
                        temporalDBMetadata,temporalMapping.getExecutorRegistry());

                ImmutableList<AtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
                if (areAllMappingsExist(mergedMap, atomicExpressionsList)) {
                    try {
                        IntermediateQuery iq = ruleUnfolder.unfold(intermediateQuery, ImmutableMap.copyOf(mergedMap));
                        mergedMap.put(iq.getProjectionAtom().getPredicate(), iq);
                        System.out.println(iq.toString());
                    } catch (EmptyQueryException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!queue.isEmpty()) {
                        //TODO:Override compareTo for rule.getHead()
                        if (queue.stream().anyMatch(qe -> qe.getHead().equals(rule.getHead())))
                            queue.add(rule);
                    }
                }
            }
        }
        return null;
    }

    private Map<AtomPredicate, IntermediateQuery> mergeMappings(Mapping mapping, TemporalMapping temporalMapping){
        Map <AtomPredicate, IntermediateQuery> mergedMap = new HashMap<>();
        mergedMap.putAll(mapping.getPredicates().stream()
                .collect(Collectors.toMap(p-> p, p-> mapping.getDefinition(p).get())));
        mergedMap.putAll(temporalMapping.getPredicates().stream()
                .collect(Collectors.toMap(p-> p, p -> temporalMapping.getDefinition(p).get())));
        return mergedMap;
    }

    private boolean areAllMappingsExist(Map<AtomPredicate, IntermediateQuery> mergedMap,
                                        ImmutableList<AtomicExpression> atomicExpressionsList) {

        if (atomicExpressionsList.stream().filter(ae -> !(ae instanceof ComparisonExpression))
                .allMatch(ae -> mergedMap.containsKey(ae.getPredicate())))
            return true;

        return false;
    }

    private ImmutableList<AtomicExpression> getAtomicExpressions(DatalogMTLRule rule) {
        return TreeTraverser.using(DatalogMTLExpression::getChildNodes).postOrderTraversal(rule.getBody()).stream()
                .filter(dMTLexp -> dMTLexp instanceof AtomicExpression)
                .map(dMTLexp -> (AtomicExpression) dMTLexp)
                .collect(ImmutableCollectors.toList());
    }

//    @Override
//    public TemporalMapping saturate(Mapping mapping, DBMetadata dbMetadata,
//                                             TemporalMapping temporalMapping, DBMetadata temporalDBMetadata,
//                                             DatalogMTLProgram datalogMTLProgram) {
//        //QueryUnfolder queryUnfolder = translationFactory.create(mapping);
//
//        Map<AtomPredicate, IntervalAndIntermediateQuery> temporalMappingMap = new HashMap<>();
//        temporalMapping.getPredicates()
//                .forEach(p -> temporalMappingMap.put(p, temporalMapping.getIntervalAndIntermediateQuery(p)));
//        Queue<DatalogMTLRule> queue = new LinkedList<>();
//
//        queue.addAll(datalogMTLProgram.getRules());
//
//        SaturateRuleReturnType sType;
//        while (!queue.isEmpty()) {
//            DatalogMTLRule rule = queue.poll();
//            if (!(rule.getBody() instanceof StaticJoinExpression) ||
//                    ((rule.getBody() instanceof FilterExpression) &&
//                            !(((FilterExpression) rule.getBody()).getExpression() instanceof StaticJoinExpression))) {
//                IntermediateQuery intermediateQuery = dMTLConverter.dMTLToIntermediateQuery(rule,mapping,
//                        dbMetadata,temporalMappingMap,
//                        temporalDBMetadata,temporalMapping.getExecutorRegistry());
//                ImmutableList<AtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
//                if (areAllMappingsExist(mapping, temporalMappingMap, atomicExpressionsList)) {
//                    try {
//                        sType = saturateRule(rule, mapping, dbMetadata,
//                                ImmutableMap.copyOf(temporalMappingMap), temporalDBMetadata, temporalMapping.getExecutorRegistry());
//                        temporalMappingMap.put(sType.atomPredicate,sType.intervalAndIntermediateQuery);
//                        System.out.println(sType.intervalAndIntermediateQuery.getIntermediateQuery().toString());
//                    } catch (EmptyQueryException e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    if (!queue.isEmpty()) {
//                        //TODO:Override compareTo for rule.getHead()
//                        if (queue.stream().anyMatch(qe -> qe.getHead().equals(rule.getHead())))
//                            queue.add(rule);
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    private boolean areAllMappingsExist(Mapping mapping, Map<AtomPredicate, IntervalAndIntermediateQuery> temporalMappingMap,
//                                        ImmutableList<AtomicExpression> atomicExpressionsList) {
//
//        if (atomicExpressionsList.stream().filter(ae -> !(ae instanceof ComparisonExpression))
//                .allMatch(ae -> mapping.getDefinition(ae.getPredicate()).isPresent() || temporalMappingMap.containsKey(ae.getPredicate())))
//            return true;
//
//        return false;
//    }
//
//    private IQ saturateRule(DatalogMTLRule rule, Mapping mapping, DBMetadata dbMetadata,
//                            TemporalQuadrupleMapping temporalMapping, DBMetadata temporalDBMetadata) {
//
//        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
//        Iterable<DatalogMTLExpression> it = treeTraverser.preOrderTraversal(rule.getBody());
//        Stack<DatalogMTLExpression> teStack = new Stack<>();
//        it.iterator().forEachRemaining(dMTLexp -> teStack.push(dMTLexp));
//        Stack<IQTree> iqTreeStack = new Stack<>();
//
//        if (rule.getHead() instanceof TemporalAtomicExpression) {
//            if (!teStack.empty()) {
//                IntermediateQuery iq = dMTLToIntermediateQuery(rule,mapping,dbMetadata, temporalMapping, temporalDBMetadata);
//
//            } else {
//                //TODO:throw exception
//            }
//        }
//        return null;
//    }
//
//    private ImmutableMap<Variable, Term> retrieveMapForVariablesOccuringInTheHead(DatalogMTLRule rule, Mapping mapping, TemporalQuadrupleMapping temporalQuadrupleMapping) {
//        Map<Variable, Term> varMap = new HashMap<>();
//        ImmutableList<AtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
//        for (Term term : rule.getHead().getImmutableTerms()) {
//            if (term instanceof Variable) {
//                for (AtomicExpression ae : atomicExpressionsList) {
//                    int varIdxInBody = 0;
//                    for (Term t : ae.getImmutableTerms()) {
//                        if (t instanceof Variable) {
//                            //TODO:Override compareTo for Variable
//                            if (((Variable) t).equals(term)) {
//                                if (mapping.getPredicates().contains(ae.getPredicate())) {
//                                    int varIdxInSub = 0;
//                                    Optional<IntermediateQuery> iq = mapping.getDefinition(ae.getPredicate());
//                                    for (ImmutableTerm subTerm :
//                                            ((ConstructionNode) iq.get().getRootNode()).getSubstitution().getImmutableMap().values()) {
//                                        if (varIdxInBody == varIdxInSub) {
//                                            if (varMap.containsKey((Variable) t)) {
//                                                if (!varMap.get(t).equals(subTerm)) {
//                                                    //TODO:throw exception
//                                                }
//                                            } else {
//                                                varMap.put((Variable) t, (NonGroundFunctionalTerm) subTerm);
//                                            }
//                                        }
//                                        varIdxInSub++;
//                                    }
//                                } else if (temporalQuadrupleMapping.getPredicates().contains(ae.getPredicate())) {
//                                    int varIdxInSub = 0;
//                                    QuadrupleDefinition qd = temporalQuadrupleMapping.getDefinitions().get(ae.getPredicate());
//                                    for (ImmutableTerm subTerm : ((ConstructionNode) qd.getQuadruple()
//                                            .getIntermediateQuery().getRootNode()).getSubstitution().getImmutableMap().values()) {
//                                        if (varIdxInBody == varIdxInSub) {
//                                            varMap.put((Variable) t, (NonGroundFunctionalTerm) subTerm);
//                                        }
//                                        varIdxInSub++;
//                                    }
//
//                                } else {
//                                    //TODO:throw exception;
//                                }
//                            }
//
//                        }
//                        varIdxInBody++;
//                    }
//                }
//            }
//        }
//        return ImmutableMap.copyOf(varMap);
//    }
//
//    private class SaturateRuleReturnType{
//        AtomPredicate atomPredicate;
//        IntervalAndIntermediateQuery intervalAndIntermediateQuery;
//
//        public SaturateRuleReturnType(AtomPredicate atomPredicate,
//                                      IntervalAndIntermediateQuery intervalAndIntermediateQuery) {
//            this.atomPredicate = atomPredicate;
//            this.intervalAndIntermediateQuery = intervalAndIntermediateQuery;
//        }
//    }
//
//    private SaturateRuleReturnType saturateRule(DatalogMTLRule rule, Mapping mapping, DBMetadata dbMetadata,
//                                                ImmutableMap<AtomPredicate, IntervalAndIntermediateQuery> temporalMappingMap, DBMetadata temporalDBMetadata, ExecutorRegistry executorRegistry) throws EmptyQueryException {
//
//        return null;
//    }
//
//    private SaturateRuleReturnType saturateRule(DatalogMTLRule rule, Mapping mapping, DBMetadata dbMetadata,
//                            ImmutableMap<AtomPredicate, IntervalAndIntermediateQuery> temporalMappingMap, DBMetadata temporalDBMetadata, ExecutorRegistry executorRegistry) throws EmptyQueryException {
//
//
//
//        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
//        Iterable<DatalogMTLExpression> it = treeTraverser.preOrderTraversal(rule.getBody());
//        Stack<DatalogMTLExpression> teStack = new Stack<>();
//        it.iterator().forEachRemaining(dMTLexp -> teStack.push(dMTLexp));
//        Stack<IQTree> iqTreeStack = new Stack<>();
//
//        if (rule.getHead() instanceof TemporalAtomicExpression) {
//            if (!teStack.empty()) {
//                ImmutableMap<Variable, Term> varMap = retrieveMapForVariablesOccuringInTheHead(rule, mapping, temporalMappingMap);
//
//                //creating construction node for the head of the rule
//                TargetAtom targetAtom = datalogMTLConversionTools
//                        .convertFromDatalogDataAtom(termFactory.getFunction(rule.getHead().getPredicate(), varMap.values().asList()));
//                DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();
//                ConstructionNode constructionNode = TIQFactory.createConstructionNode(projectionAtom.getVariables(),
//                        targetAtom.getSubstitution(), Optional.empty());
//
//                //building the tree
//                IQTree newTree = null;
//                while (!teStack.isEmpty()) {
//                    DatalogMTLExpression currentExpression = teStack.pop();
//                    IQTree newCoalTree;
//                    //TODO: Coalesce Node is missing, implement it.
//                    if (currentExpression instanceof AtomicExpression) {
//
//                        IntermediateQuery intermediateQuery;
//                        if (currentExpression instanceof ComparisonExpression) {
//                            continue;
//                        } else if (currentExpression instanceof StaticAtomicExpression) {
//                            intermediateQuery = mapping.getDefinition(((StaticAtomicExpression) currentExpression).getPredicate()).get();
//                            newTree = ((UnaryIQTree) iqConverter.convert(intermediateQuery).getTree()).getChild();
//                        } else {//TemporalAtomicExpression
//                            intermediateQuery = temporalMappingMap.get(((TemporalAtomicExpression) currentExpression).getPredicate()).getIntermediateQuery();
//                            newCoalTree = ((UnaryIQTree) iqConverter.convert(intermediateQuery).getTree()).getChild();
//                            TemporalCoalesceNode coalesceNode = TIQFactory.createTemporalCoalesceNode();
//                            newTree = TIQFactory.createUnaryIQTree(coalesceNode, newCoalTree);
//                        }
//
//                    } else if (currentExpression instanceof TemporalJoinExpression) {
//                        IQTree iqTree1 = iqTreeStack.pop();
//                        IQTree iqTree2 = iqTreeStack.pop();
//
//                        TemporalJoinNode temporalJoinNode = TIQFactory.createTemporalJoinNode();
//                        newTree = TIQFactory.createNaryIQTree(temporalJoinNode, ImmutableList.of(iqTree1, iqTree2));
//
//                    } else if (currentExpression instanceof StaticJoinExpression) {
//                        List<IQTree> iqtList = new ArrayList<>();
//                        for (int i = 0; i < ((StaticJoinExpression) currentExpression).getArity(); i++) {
//                            iqtList.add(iqTreeStack.pop());
//                        }
//                        InnerJoinNode innerJoinNode = TIQFactory.createInnerJoinNode();
//                        newTree = TIQFactory.createNaryIQTree(innerJoinNode, ImmutableList.copyOf(iqtList));
//
//                    } else if (currentExpression instanceof FilterExpression) {
//                        FilterNode filterNode = TIQFactory.createFilterNode(comparisonExpToFilterCondition(((FilterExpression) currentExpression).getComparisonExpression()));
//                        IQTree iqTree = iqTreeStack.pop();
//                        newTree = TIQFactory.createUnaryIQTree(filterNode, iqTree);
//
//                    } else if (currentExpression instanceof UnaryTemporalExpression && currentExpression instanceof TemporalExpressionWithRange) {
//
//                        UnaryOperatorNode newNode;
//                        if (currentExpression instanceof BoxMinusExpression)
//                            newNode = TIQFactory.createBoxMinusNode(((BoxMinusExpression) currentExpression).getRange());
//
//                        else if (currentExpression instanceof BoxPlusExpression)
//                            newNode = TIQFactory.createBoxPlusNode(((BoxPlusExpression) currentExpression).getRange());
//
//                        else if (currentExpression instanceof DiamondMinusExpression)
//                            newNode = TIQFactory.createDiamondMinusNode(((DiamondMinusExpression) currentExpression).getRange());
//                        else
//                            newNode = TIQFactory.createDiamondPlusNode(((DiamondPlusExpression) currentExpression).getRange());
//
//                        IQTree iqTree = iqTreeStack.pop();
//
//                        TemporalCoalesceNode coalesceNode = TIQFactory.createTemporalCoalesceNode();
//                        newCoalTree = TIQFactory.createUnaryIQTree(coalesceNode, iqTree);
//
//                        newTree = TIQFactory.createUnaryIQTree(newNode, newCoalTree);
//
//                    } else if (currentExpression instanceof BinaryTemporalExpression && currentExpression instanceof TemporalExpressionWithRange) {
//
//                        BinaryNonCommutativeOperatorNode newNode;
//                        if (currentExpression instanceof SinceExpression) {
//                            newNode = TIQFactory.createSinceNode(((SinceExpression) currentExpression).getRange());
//                        } else { //UntilExpression
//                            newNode = TIQFactory.createUntilNode(((TemporalExpressionWithRange) currentExpression).getRange());
//                        }
//                        IQTree right = iqTreeStack.pop();
//                        TemporalCoalesceNode rightCoalesceNode = TIQFactory.createTemporalCoalesceNode();
//                        IQTree newRightCoalTree = TIQFactory.createUnaryIQTree(rightCoalesceNode, right);
//
//                        IQTree left = iqTreeStack.pop();
//                        TemporalCoalesceNode leftCoalesceNode = TIQFactory.createTemporalCoalesceNode();
//                        IQTree newLeftCoalTree = TIQFactory.createUnaryIQTree(leftCoalesceNode, right);
//
//                        newTree = TIQFactory.createBinaryNonCommutativeIQTree(newNode, newLeftCoalTree, newRightCoalTree);
//                    }
//                    if (newTree != null)
//                        iqTreeStack.push(newTree);
//                }
//
//                IQTree finalTree =iqTreeStack.pop();  ;
//
//                return new SaturateRuleReturnType(projectionAtom.getPredicate(),new IntervalAndIntermediateQuery(null,
//                        iqConverter.convert(TIQFactory.createIQ(projectionAtom,TIQFactory.createUnaryIQTree(constructionNode, finalTree)) , dbMetadata,executorRegistry)));
//                //return TIQFactory.createIQ(projectionAtom, TIQFactory.createUnaryIQTree(constructionNode, iqTreeStack.pop()));
//
//            } else {
//                //TODO:throw exception
//            }
//        }
//        return null;
//    }
}
