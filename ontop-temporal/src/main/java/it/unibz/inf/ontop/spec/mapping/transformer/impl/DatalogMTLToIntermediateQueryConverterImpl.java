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
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalQuadrupleMapping;
import it.unibz.inf.ontop.spec.mapping.impl.IntervalAndIntermediateQuery;
import it.unibz.inf.ontop.spec.mapping.transformer.DatalogMTLToIntermediateQueryConverter;
import it.unibz.inf.ontop.temporal.datalog.impl.DatalogMTLConversionTools;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.iq.node.TemporalCoalesceNode;
import it.unibz.inf.ontop.temporal.iq.node.TemporalJoinNode;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
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
        it.iterator().forEachRemaining(dMTLexp -> teStack.push(dMTLexp));

        if (rule.getHead() instanceof TemporalAtomicExpression) {
            if (!teStack.empty()) {
                //ImmutableMap<Variable, Variable> varMap = retrieveMapForVariablesOccuringInTheHead(rule, mapping,temporalMappingMap);

                //creating construction node for the head of the rule
                TargetAtom targetAtom = datalogMTLConversionTools
                        .convertFromDatalogDataAtom(termFactory.getFunction(rule.getHead()
                                .getPredicate(), rule.getHead().getTerms()));
                //varMap.values().stream().map(v->(Term)v).collect(Collectors.toList())
                DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();
                ConstructionNode constructionNode = TIQFactory.createConstructionNode(projectionAtom.getVariables());
                TemporalIntermediateQueryBuilder TIQBuilder = TIQFactory.createTemporalIQBuilder(temporalDBMetadata, executorRegistry);
                TIQBuilder.init(projectionAtom, constructionNode);
                TIQBuilder = getBuilder(rule.getBody(), constructionNode, TIQBuilder);
                IntermediateQuery intermediateQuery = TIQBuilder.build();
                System.out.println(intermediateQuery.toString());
                return intermediateQuery;
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

    private TemporalIntermediateQueryBuilder getBuilder(DatalogMTLExpression currentExpression, QueryNode parentNode, TemporalIntermediateQueryBuilder TIQBuilder){
        if (currentExpression instanceof AtomicExpression) {
            IntensionalDataNode intensionalDataNode =
                    TIQFactory.createIntensionalDataNode(atomFactory.getDataAtom(((AtomicExpression) currentExpression).getPredicate(),
                            ((AtomicExpression)currentExpression).getVariableOrGroundTerms()));
            TemporalCoalesceNode coalesceNode = TIQFactory.createTemporalCoalesceNode();
            TIQBuilder.addChild(parentNode, coalesceNode);
            TIQBuilder.addChild(coalesceNode,intensionalDataNode);
        }else if(currentExpression instanceof TemporalJoinExpression){
            TemporalJoinNode temporalJoinNode = TIQFactory.createTemporalJoinNode();
            TIQBuilder.addChild(parentNode, temporalJoinNode);
            getBuilder(((TemporalJoinExpression) currentExpression).getOperands().get(0), ((QueryNode) temporalJoinNode), TIQBuilder);
            getBuilder(((TemporalJoinExpression) currentExpression).getOperands().get(1), ((QueryNode) temporalJoinNode), TIQBuilder);
        }else if(currentExpression instanceof StaticJoinExpression){
            InnerJoinNode innerJoinNode = TIQFactory.createInnerJoinNode();
            TIQBuilder.addChild(parentNode,innerJoinNode);
            ((StaticJoinExpression)currentExpression).getChildNodes().forEach(exp ->{
                getBuilder(exp, ((QueryNode) innerJoinNode), TIQBuilder);
            });
        }else if (currentExpression instanceof FilterExpression){
            FilterNode filterNode = TIQFactory.createFilterNode(comparisonExpToFilterCondition(((FilterExpression) currentExpression).getComparisonExpression()));
            TIQBuilder.addChild(parentNode, filterNode);
            getBuilder(((FilterExpression) currentExpression).getExpression(), filterNode,TIQBuilder);

        }else if(currentExpression instanceof UnaryTemporalExpression && currentExpression instanceof TemporalExpressionWithRange){

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
            TemporalCoalesceNode coalesceNode = TIQFactory.createTemporalCoalesceNode();
            TIQBuilder.addChild(newNode, coalesceNode);
            getBuilder(((UnaryTemporalExpression)currentExpression).getOperand(),coalesceNode,TIQBuilder);

        }else if (currentExpression instanceof BinaryTemporalExpression && currentExpression instanceof TemporalExpressionWithRange) {

            BinaryNonCommutativeOperatorNode newNode;
            if (currentExpression instanceof SinceExpression) {
                newNode = TIQFactory.createSinceNode(((SinceExpression) currentExpression).getRange());
            } else { //UntilExpression
                newNode = TIQFactory.createUntilNode(((TemporalExpressionWithRange) currentExpression).getRange());
            }
            TIQBuilder.addChild(parentNode, newNode);

            TemporalCoalesceNode rightCoalesceNode = TIQFactory.createTemporalCoalesceNode();
            TIQBuilder.addChild(newNode, rightCoalesceNode);
            getBuilder(((BinaryTemporalExpression)currentExpression).getRightOperand(), rightCoalesceNode, TIQBuilder);

            TemporalCoalesceNode leftCoalesceNode = TIQFactory.createTemporalCoalesceNode();
            TIQBuilder.addChild(newNode, leftCoalesceNode);
            getBuilder(((BinaryTemporalExpression)currentExpression).getLeftOperand(), leftCoalesceNode, TIQBuilder);
        }
        return TIQBuilder;
    }

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
