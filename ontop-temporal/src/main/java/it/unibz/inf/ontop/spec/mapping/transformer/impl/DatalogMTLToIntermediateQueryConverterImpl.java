package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeTraverser;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.TargetAtom;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalQuadrupleMapping;
import it.unibz.inf.ontop.spec.mapping.impl.IntervalAndIntermediateQuery;
import it.unibz.inf.ontop.spec.mapping.transformer.DatalogMTLToIntermediateQueryConverter;
import it.unibz.inf.ontop.temporal.datalog.impl.DatalogMTLConversionTools;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.iq.node.TemporalCoalesceNode;
import it.unibz.inf.ontop.temporal.iq.node.TemporalJoinNode;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Collectors;

public class DatalogMTLToIntermediateQueryConverterImpl implements DatalogMTLToIntermediateQueryConverter{

    private final TemporalIntermediateQueryFactory TIQFactory;
    private final TermFactory termFactory;
    private final AtomFactory atomFactory;
    private final DatalogMTLConversionTools datalogMTLConversionTools;

    @Inject
    public DatalogMTLToIntermediateQueryConverterImpl(TemporalIntermediateQueryFactory tiqFactory, TermFactory termFactory, AtomFactory atomFactory, DatalogMTLConversionTools datalogMTLConversionTools) {
        TIQFactory = tiqFactory;
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
        this.datalogMTLConversionTools = datalogMTLConversionTools;
    }

    @Override
    public IntermediateQuery dMTLToIntermediateQuery(DatalogMTLRule rule, DBMetadata temporalDBMetadata, ExecutorRegistry executorRegistry) {

        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
        Iterable<DatalogMTLExpression> it = treeTraverser.postOrderTraversal(rule.getBody());
        Stack<DatalogMTLExpression> teStack = new Stack<>();
        it.iterator().forEachRemaining(teStack::push);

        if (rule.getHead() instanceof TemporalAtomicExpression) {
            if (!teStack.empty()) {
                //creating construction node for the head of the rule
                TargetAtom targetAtom = datalogMTLConversionTools
                        .convertFromDatalogDataAtom(termFactory.getFunction(rule.getHead()
                                .getPredicate(), rule.getHead().getTerms()));
                AtomPredicate newPredicate = atomFactory.getAtomPredicate(targetAtom.getProjectionAtom().getPredicate().getName(),
                        targetAtom.getProjectionAtom().getPredicate().getArity() + 4);
                DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(newPredicate,
                        appendTemporalComponentsToHead(targetAtom.getProjectionAtom().getArguments()));
                ConstructionNode constructionNode = TIQFactory.createConstructionNode(projectionAtom.getVariables());
                TemporalIntermediateQueryBuilder TIQBuilder = TIQFactory.createTemporalIQBuilder(temporalDBMetadata, executorRegistry);
                TIQBuilder.init(projectionAtom, constructionNode);
                TIQBuilder = getBuilder(rule.getBody(), constructionNode, TIQBuilder);
                return TIQBuilder.build();
            }
        }
        return null;
    }

    private ImmutableExpression comparisonExpToFilterCondition(ComparisonExpression comparisonExpression) {
        String operator = comparisonExpression.getPredicate().getName();
        ExpressionOperation expressionOperation = null;
        if (operator == ExpressionOperation.LT.getName())
            expressionOperation = ExpressionOperation.LT;
        else if (operator == ExpressionOperation.GT.getName())
            expressionOperation = ExpressionOperation.GT;
        else if (operator == ExpressionOperation.EQ.getName())
            expressionOperation = ExpressionOperation.EQ;
        else if (operator == ExpressionOperation.NEQ.getName())
            expressionOperation = ExpressionOperation.NEQ;

        return termFactory.getImmutableExpression(expressionOperation, comparisonExpression.getLeftOperand(), comparisonExpression.getRightOperand());
    }

    private ImmutableList<Variable> appendTemporalComponentsToHead(ImmutableList<Variable> projectionVariables){

        List<Variable> newArglist = new ArrayList<>(projectionVariables);
        newArglist.add(termFactory.getVariable("beginInc"));
        newArglist.add(termFactory.getVariable("begin"));
        newArglist.add(termFactory.getVariable("end"));
        newArglist.add(termFactory.getVariable("endInc"));

        return ImmutableList.copyOf(newArglist);
    }

    private ImmutableList<VariableOrGroundTerm> appendTemporalComponents(ImmutableList<VariableOrGroundTerm> projectionVariables){

            List<VariableOrGroundTerm> newArglist = new ArrayList<>(projectionVariables);
            newArglist.add(termFactory.getVariable("beginInc"));
            newArglist.add(termFactory.getVariable("begin"));
            newArglist.add(termFactory.getVariable("end"));
            newArglist.add(termFactory.getVariable("endInc"));

            return ImmutableList.copyOf(newArglist);
    }

    private ImmutableList<NonGroundTerm> extractVariablesInTheSubTree(DatalogMTLExpression root){
        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
        Iterable<DatalogMTLExpression> it = treeTraverser.postOrderTraversal(root);
        List<NonGroundTerm> varList = new ArrayList<>();
        for (DatalogMTLExpression expression : it) {
            if (expression instanceof AtomicExpression){
                varList.addAll(((AtomicExpression) expression).extractVariables());
            }
        }
        return ImmutableList.copyOf(varList);
    }

    private TemporalIntermediateQueryBuilder getBuilder(DatalogMTLExpression currentExpression,
                                                        QueryNode parentNode, TemporalIntermediateQueryBuilder TIQBuilder){
        if (currentExpression instanceof AtomicExpression) {
            if(currentExpression instanceof TemporalAtomicExpression) {
                AtomPredicate newPred = atomFactory.getAtomPredicate(((AtomicExpression) currentExpression).getPredicate().getName(),
                        ((AtomicExpression) currentExpression).getPredicate().getArity() + 4);
                IntensionalDataNode intensionalDataNode =
                        TIQFactory.createIntensionalDataNode(atomFactory
                                .getDataAtom(newPred,
                                        appendTemporalComponents(((AtomicExpression)currentExpression).getVariableOrGroundTerms())));
                if (!(parentNode instanceof TemporalCoalesceNode)) {
                    TemporalCoalesceNode coalesceNode = TIQFactory
                            .createTemporalCoalesceNode(extractVariablesInTheSubTree(currentExpression));
                           // .createTemporalCoalesceNode(intensionalDataNode.getVariables()
                           //         .stream().map(v->(NonGroundTerm)v).collect(ImmutableCollectors.toList()));
                    TIQBuilder.addChild(parentNode, coalesceNode);
                    TIQBuilder.addChild(coalesceNode, intensionalDataNode);
                } else {
                    TIQBuilder.addChild(parentNode, intensionalDataNode);
                }
            }else{
                IntensionalDataNode intensionalDataNode =
                        TIQFactory.createIntensionalDataNode(atomFactory
                                .getDataAtom(((AtomicExpression) currentExpression).getPredicate(),
                                        ((AtomicExpression)currentExpression).getVariableOrGroundTerms()));
                TIQBuilder.addChild(parentNode, intensionalDataNode);
            }
        }else if(currentExpression instanceof TemporalJoinExpression){
            TemporalJoinNode temporalJoinNode = TIQFactory.createTemporalJoinNode();
            TIQBuilder.addChild(parentNode, temporalJoinNode);
            ((TemporalJoinExpression)currentExpression).getChildNodes().forEach(exp ->{
                getBuilder(exp, temporalJoinNode, TIQBuilder);
            });
        }else if(currentExpression instanceof StaticJoinExpression){
            InnerJoinNode innerJoinNode = TIQFactory.createInnerJoinNode();
            TIQBuilder.addChild(parentNode,innerJoinNode);
            ((StaticJoinExpression)currentExpression).getChildNodes().forEach(exp ->{
                getBuilder(exp, innerJoinNode, TIQBuilder);
            });
        }else if (currentExpression instanceof FilterExpression){
            FilterNode filterNode = TIQFactory
                    .createFilterNode(comparisonExpToFilterCondition(((FilterExpression) currentExpression)
                            .getComparisonExpression()));
            TIQBuilder.addChild(parentNode, filterNode);
            getBuilder(((FilterExpression) currentExpression).getExpression(), filterNode,TIQBuilder);

        }else if(currentExpression instanceof UnaryTemporalExpression
                && currentExpression instanceof TemporalExpressionWithRange){

            UnaryOperatorNode newNode;
            if (currentExpression instanceof BoxMinusExpression)
                newNode = TIQFactory.createBoxMinusNode(((BoxMinusExpression) currentExpression).getRange());

            else if (currentExpression instanceof BoxPlusExpression)
                newNode = TIQFactory.createBoxPlusNode(((BoxPlusExpression) currentExpression).getRange());

            else if (currentExpression instanceof DiamondMinusExpression)
                newNode = TIQFactory.createDiamondMinusNode(((DiamondMinusExpression) currentExpression).getRange());
            else
                newNode = TIQFactory.createDiamondPlusNode(((DiamondPlusExpression) currentExpression).getRange());

            TIQBuilder.addChild(parentNode, newNode);

            // to avoid redundant coalesce nodes
            if(((UnaryTemporalExpression)currentExpression).getOperand() instanceof UnaryTemporalExpression){
                getBuilder(((UnaryTemporalExpression)currentExpression).getOperand(),newNode,TIQBuilder);
            }else {
                DatalogMTLExpression childExpression = ((UnaryTemporalExpression) currentExpression).getOperand();
                TemporalCoalesceNode coalesceNode = TIQFactory.createTemporalCoalesceNode(extractVariablesInTheSubTree(childExpression));
                TIQBuilder.addChild(newNode, coalesceNode);
                getBuilder(childExpression, coalesceNode, TIQBuilder);
            }

        }else if (currentExpression instanceof BinaryTemporalExpression
                && currentExpression instanceof TemporalExpressionWithRange) {

            BinaryNonCommutativeOperatorNode newNode;
            if (currentExpression instanceof SinceExpression) {
                newNode = TIQFactory.createSinceNode(((SinceExpression) currentExpression).getRange());
            } else { //UntilExpression
                newNode = TIQFactory.createUntilNode(((TemporalExpressionWithRange) currentExpression).getRange());
            }
            TIQBuilder.addChild(parentNode, newNode);

            DatalogMTLExpression rightChildExpression = ((BinaryTemporalExpression)currentExpression).getRightOperand();
            TemporalCoalesceNode rightCoalesceNode = TIQFactory.createTemporalCoalesceNode(extractVariablesInTheSubTree(rightChildExpression));
            TIQBuilder.addChild(newNode, rightCoalesceNode);
            getBuilder(((BinaryTemporalExpression)currentExpression).getRightOperand(), rightCoalesceNode, TIQBuilder);

            DatalogMTLExpression leftChildExpression = ((BinaryTemporalExpression)currentExpression).getLeftOperand();
            TemporalCoalesceNode leftCoalesceNode = TIQFactory.createTemporalCoalesceNode(extractVariablesInTheSubTree(leftChildExpression));
            TIQBuilder.addChild(newNode, leftCoalesceNode);
            getBuilder(((BinaryTemporalExpression)currentExpression).getLeftOperand(), leftCoalesceNode, TIQBuilder);
        }
        return TIQBuilder;
    }

    //    private ImmutableMap<Variable, Variable> retrieveMapForVariablesOccuringInTheHead(
//            DatalogMTLRule rule, Mapping mapping, Map<AtomPredicate,
//            IntervalAndIntermediateQuery> temporalMappingMap) {
//        Map<Variable, Variable> varMap = new HashMap<>();
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
//                                    for (Variable subTerm : iq.get().getProjectionAtom().getVariables()) {
//                                        if (varIdxInBody == varIdxInSub) {
//                                            if (varMap.containsKey((Variable) t)) {
//                                                if (!varMap.get(t).equals(subTerm)) {
//                                                    //TODO:throw exception
//                                                }
//                                            } else {
//                                                varMap.put((Variable) t, subTerm);
//                                            }
//                                        }
//                                        varIdxInSub++;
//                                    }
//                                } else if (temporalMappingMap.containsKey(ae.getPredicate())) {
//                                    int varIdxInSub = 0;
//                                    for (Variable subTerm : temporalMappingMap.get(ae.getPredicate())
//                                            .getIntermediateQuery().getProjectionAtom().getVariables()) {
//                                        if (varIdxInBody == varIdxInSub) {
//                                            varMap.put((Variable) t, subTerm);
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
//    private ImmutableList<AtomicExpression> getAtomicExpressions(DatalogMTLRule rule) {
//        return TreeTraverser.using(DatalogMTLExpression::getChildNodes).postOrderTraversal(rule.getBody()).stream()
//                .filter(dMTLexp -> dMTLexp instanceof AtomicExpression)
//                .map(dMTLexp -> (AtomicExpression) dMTLexp)
//                .collect(ImmutableCollectors.toList());
//    }
//
//    private IntermediateQuery dMTLToIntermediateQuery(DatalogMTLRule rule, Mapping mapping, DBMetadata dbMetadata,
//                                                      TemporalQuadrupleMapping temporalQuadrupleMapping, DBMetadata temporalDBMetadata) {
//
//        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
//        Iterable<DatalogMTLExpression> it = treeTraverser.postOrderTraversal(rule.getBody());
//        Stack<DatalogMTLExpression> teStack = new Stack<>();
//        it.iterator().forEachRemaining(dMTLexp -> teStack.push(dMTLexp));
//
//        if (rule.getHead() instanceof TemporalAtomicExpression) {
//            if (!teStack.empty()) {
//                ImmutableMap<Variable, Term> varMap = retrieveMapForVariablesOccuringInTheHead(rule, mapping, temporalQuadrupleMapping);
//
//                //creating construction node for the head of the rule
//                TargetAtom targetAtom = datalogMTLConversionTools
//                        .convertFromDatalogDataAtom(termFactory.getFunction(rule.getHead().getPredicate(), varMap.values().asList()));
//                DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();
//                ConstructionNode constructionNode = TIQFactory.createConstructionNode(projectionAtom.getVariables(),
//                        targetAtom.getSubstitution(), Optional.empty());
//                TemporalIntermediateQueryBuilder TIQBuilder = TIQFactory.createTemporalIQBuilder(temporalDBMetadata, temporalQuadrupleMapping.getExecutorRegistry());
//                TIQBuilder.init(projectionAtom, constructionNode);
//                TIQBuilder = getBuilder(rule.getBody(), constructionNode, TIQBuilder);
//                IntermediateQuery intermediateQuery = TIQBuilder.build();
//                System.out.println(intermediateQuery.toString());
//                return intermediateQuery;
//            }
//        }
//
//        return null;
//    }
}
