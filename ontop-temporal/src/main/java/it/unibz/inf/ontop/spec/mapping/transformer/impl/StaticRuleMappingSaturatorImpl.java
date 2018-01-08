package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeTraverser;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.optimizer.ProjectionShrinkingOptimizer;
import it.unibz.inf.ontop.iq.optimizer.PushUpBooleanExpressionOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.PushUpBooleanExpressionOptimizerImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.reformulation.RuleUnfolder;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.transformer.StaticRuleMappingSaturator;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;

public class StaticRuleMappingSaturatorImpl implements StaticRuleMappingSaturator {

    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;
    private final DatalogProgram2QueryConverter datalogConverter;
    private final SpecificationFactory specificationFactory;
    private final RuleUnfolder ruleUnfolder;
    private final ImmutabilityTools immutabilityTools;
    private PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer;
    private ProjectionShrinkingOptimizer projectionShrinkingOptimizer;
    private final JoinLikeOptimizer joinLikeOptimizer;


    @Inject
    private StaticRuleMappingSaturatorImpl(TermFactory termFactory, DatalogFactory datalogFactory,
                                           DatalogProgram2QueryConverter datalogConverter,
                                           SpecificationFactory specificationFactory,
                                           RuleUnfolder ruleUnfolder, ImmutabilityTools immutabilityTools, JoinLikeOptimizer joinLikeOptimizer) {
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
        this.datalogConverter = datalogConverter;
        this.specificationFactory = specificationFactory;
        this.ruleUnfolder = ruleUnfolder;
        this.immutabilityTools = immutabilityTools;
        this.joinLikeOptimizer = joinLikeOptimizer;
        pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl(false, this.immutabilityTools);
        projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
    }

    private DatalogProgram convertStaticMTLRulesToDatalogProgram(DatalogMTLProgram datalogMTLProgram){
        ImmutableList<DatalogMTLRule> staticRuleList = datalogMTLProgram.getRules().stream()
                .filter(rule -> rule.getHead() instanceof StaticExpression)
                .collect(ImmutableCollectors.toList());

        DatalogProgram datalogProgram = datalogFactory.getDatalogProgram();
        datalogProgram.appendRule(staticRuleList.stream()
                .map(rule -> datalogFactory.getCQIE(termFactory.getFunction(rule.getHead().getPredicate(), rule.getHead().getTerms()),
                        getAtomicExpressions(rule).stream()
                                .map(sae -> termFactory.getFunction(sae.getPredicate(), sae.getTerms())).collect(ImmutableCollectors.toList())))
        .collect(ImmutableCollectors.toList()));

        return datalogProgram;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, DatalogMTLProgram datalogMTLProgram) {

        DatalogProgram datalogProgram = convertStaticMTLRulesToDatalogProgram(datalogMTLProgram);
        Queue<CQIE> queue = new LinkedList<>();
        queue.addAll(datalogProgram.getRules());
        Map<AtomPredicate, IntermediateQuery> mappingMap = new HashMap<>();
        mapping.getPredicates().forEach(atomPredicate -> mappingMap.put(atomPredicate, mapping.getDefinition(atomPredicate).get()));

        while(!queue.isEmpty()) {
            CQIE rule = queue.poll();
                if (areAllMappingsExist(ImmutableMap.copyOf(mappingMap), ImmutableList.copyOf(rule.getBody()))) {
                    try {
                        DatalogProgram dProg = datalogFactory.getDatalogProgram();
                        dProg.appendRule(rule);
                        IntermediateQuery intermediateQuery = datalogConverter.convertDatalogProgram(
                                dbMetadata, dProg, ImmutableList.of(), mapping.getExecutorRegistry());

                        intermediateQuery = ruleUnfolder.unfold(intermediateQuery, ImmutableMap.copyOf(mappingMap));
                        System.out.println(intermediateQuery.toString());
                        intermediateQuery = ruleUnfolder.optimize(intermediateQuery);
                        intermediateQuery = pushUpBooleanExpressionOptimizer.optimize(intermediateQuery);
                        intermediateQuery = projectionShrinkingOptimizer.optimize(intermediateQuery);
                        intermediateQuery = joinLikeOptimizer.optimize(intermediateQuery);
                        mappingMap.put(intermediateQuery.getProjectionAtom().getPredicate(), intermediateQuery);
                        System.out.println(intermediateQuery.toString());

                    } catch (EmptyQueryException e) {
                        e.printStackTrace();
                    }
                }else {
                    if (!queue.isEmpty()){
                        //TODO:Override compareTo for rule.getHead()
                        if (queue.stream().anyMatch(qe -> qe.getHead().equals(rule.getHead())))
                            queue.add(rule);
                    }
                }
        }


        return specificationFactory.createMapping(mapping.getMetadata(), ImmutableMap.copyOf(mappingMap), mapping.getExecutorRegistry());
    }

//    @Override
//    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, DatalogMTLProgram datalogMTLProgram) {
//        Queue<DatalogMTLRule> queue = new LinkedList<>();
//        queue.addAll(datalogMTLProgram.getRules());
//        IQ iq;
//        Map<AtomPredicate, IntermediateQuery> mappingMap = new HashMap<>();
//        mapping.getPredicates().forEach(atomPredicate -> mappingMap.put(atomPredicate, mapping.getDefinition(atomPredicate).get()));
//
//        while(!queue.isEmpty()){
//            DatalogMTLRule rule = queue.poll();
//            if ((rule.getBody() instanceof StaticExpression) ||
//                    ((rule.getBody() instanceof FilterExpression) &&
//                            (((FilterExpression) rule.getBody()).getExpression() instanceof StaticExpression))) {
//                ImmutableList<StaticAtomicExpression> staticAtomicExpressionsList = getAtomicExpressions(rule);
//                if (areAllMappingsExist(mapping, staticAtomicExpressionsList)) {
//                    iq = saturateRule(rule, mapping, dbMetadata);
//                    try {
//                        IntermediateQuery intermediateQuery = iqConverter.convert(iq, dbMetadata, mapping.getExecutorRegistry());
//                        mappingMap.put(iq.getProjectionAtom().getPredicate(), intermediateQuery);
//                        System.out.println(iq.toString());
//                    } catch (EmptyQueryException e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    if (!queue.isEmpty()){
//                        //TODO:Override compareTo for rule.getHead()
//                        if (queue.stream().anyMatch(qe -> qe.getHead().equals(rule.getHead())))
//                            queue.add(rule);
//                    }
//                }
//            }
//        }
//        return specificationFactory.createMapping(mapping.getMetadata(), ImmutableMap.copyOf(mappingMap), mapping.getExecutorRegistry());
//    }

//    private IQ saturateRule(DatalogMTLRule rule, Mapping mapping, DBMetadata dbMetadata){
//
//        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
//        Iterable<DatalogMTLExpression> it = treeTraverser.preOrderTraversal(rule.getBody());
//        Stack<DatalogMTLExpression> teStack = new Stack<>();
//        it.iterator().forEachRemaining(dMTLexp -> teStack.push(dMTLexp));
//        Stack<IQTree> iqTreeStack = new Stack<>();
//
//        if(!teStack.empty()) {
//            ImmutableMap<Variable, Term> varMap = retrieveMapForVariablesOccuringInTheHead(rule, mapping);
//            AtomicExpression atomicExpression;
//
//            if (rule.getHead() instanceof StaticAtomicExpression) {
//                atomicExpression = new StaticAtomicExpressionImpl(rule.getHead().getPredicate(), varMap.values().asList());
//
//
//                TargetAtom targetAtom = datalogConversionTools
//                        .convertFromDatalogDataAtom(termFactory.getFunction(atomicExpression.getPredicate(),  atomicExpression.getTerms()));
//                DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();
//                ConstructionNode constructionNode = IQFactory.createConstructionNode(projectionAtom.getVariables(),
//                        targetAtom.getSubstitution(), Optional.empty());
//
//                IQTree newTree;
//                while (!teStack.isEmpty()) {
//                    DatalogMTLExpression currentExpression = teStack.pop();
//
//                    if (currentExpression instanceof StaticExpression || currentExpression instanceof ComparisonExpression) {
//                        //TODO: Coalesce Node is missing, implement it.
//                        if (currentExpression instanceof AtomicExpression) {
//
//                            IntermediateQuery intermediateQuery;
//                            if (currentExpression instanceof ComparisonExpression) {
//                                continue;
//                            } else { //StaticAtomicExpression
//                                intermediateQuery = mapping.getDefinition(((StaticAtomicExpression) currentExpression).getPredicate()).get();
//                                newTree = ((UnaryIQTree) iqConverter.convert(intermediateQuery).getTree());
//                            }
//                        } else if (currentExpression instanceof StaticJoinExpression) {
//                            List<IQTree> iqtList = new ArrayList<>();
//                            for (int i = 0; i < ((StaticJoinExpression) currentExpression).getArity(); i++) {
//                                IQTree iqTree = iqTreeStack.pop();
//                                //if (!iqtList.contains(iqTree))
//                                    iqtList.add(iqTree);
//                            }
//                            InnerJoinNode innerJoinNode = IQFactory.createInnerJoinNode();
//                            newTree = IQFactory.createNaryIQTree(innerJoinNode, ImmutableList.copyOf(iqtList));
//
//                        } else { //FilterExpression)
//                            FilterNode filterNode = IQFactory
//                                    .createFilterNode(comparisonExpToFilterCondition(((FilterExpression) currentExpression).getComparisonExpression()));
//                            IQTree iqTree = iqTreeStack.pop();
//                            newTree = IQFactory.createUnaryIQTree(filterNode, iqTree);
//                        }
//
//                        if (newTree != null)
//                            iqTreeStack.push(newTree);
//                    } else {
//                        iqTreeStack.empty();
//                        break;
//                    }
//                }
//                if (!iqTreeStack.isEmpty())
//                    return IQFactory.createIQ(projectionAtom, IQFactory.createUnaryIQTree(constructionNode, iqTreeStack.pop()));
//            }
//        }else{
//            //TODO:????
//        }
//        return null;
//    }

    private boolean isContainedInTheTree(IQTree newTree, IQTree iqTree){

        boolean flag = false;
        if (!newTree.equals(iqTree)){
            for (IQTree subTree : iqTree.getChildren()){
                flag = flag || isContainedInTheTree(newTree, subTree);
            }
        }else return true;

        return false || flag;
    }

    private ImmutableList<StaticAtomicExpression> getAtomicExpressions(DatalogMTLRule rule) {

        if (TreeTraverser.using(DatalogMTLExpression::getChildNodes).postOrderTraversal(rule.getBody()).stream()
                .allMatch(dMTLExp -> dMTLExp instanceof StaticExpression)) {
            return TreeTraverser.using(DatalogMTLExpression::getChildNodes).postOrderTraversal(rule.getBody()).stream()
                    .filter(dMTLexp -> dMTLexp instanceof StaticAtomicExpression)
                    .map(dMTLexp -> (StaticAtomicExpression) dMTLexp)
                    .collect(ImmutableCollectors.toList());
        }
        return null;
    }

    private boolean areAllMappingsExist(ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap, ImmutableList<Function> bodyList){

        for (Function f : bodyList){
            if (!mappingMap.containsKey(f.getFunctionSymbol()))
                return false;
        }

        return true;
    }

//    private boolean areAllMappingsExist(ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap, ImmutableList<StaticAtomicExpression> atomicExpressionsList){
//
//        if (atomicExpressionsList.stream().filter(ae-> !(ae instanceof ComparisonExpression))
//                .allMatch(ae -> mappingMap.containsKey(ae.getPredicate())))
//            return true;
//
//        return false;
//    }

    private ImmutableMap<Variable, Term> retrieveMapForVariablesOccuringInTheHead(DatalogMTLRule rule, Mapping mapping){
        Map<Variable, Term> varMap = new HashMap<>();
        ImmutableList<StaticAtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
        for(Term term : rule.getHead().getImmutableTerms()){
            if(term instanceof Variable){
                for(AtomicExpression ae :atomicExpressionsList){
                    int varIdxInBody = 0;
                    for(Term t : ae.getImmutableTerms()){
                        if (t instanceof Variable) {
                            //TODO:Override compareTo for Variable
                            if(((Variable) t).equals(term)){
                                if(mapping.getPredicates().contains(ae.getPredicate())){
                                    int varIdxInSub = 0;
                                    Optional<IntermediateQuery> iq = mapping.getDefinition(ae.getPredicate());
                                    for(ImmutableTerm subTerm : ((ConstructionNode)iq.get().getRootNode()).getSubstitution().getImmutableMap().values()){
                                        if(varIdxInBody == varIdxInSub){
                                            if(varMap.containsKey((Variable) t)){
                                                if (!varMap.get(t).equals(subTerm)){
                                                    //TODO:throw exception
                                                }
                                            }
                                            else {
                                                varMap.put((Variable) t, (NonGroundFunctionalTerm) subTerm);
                                            }
                                        }
                                        varIdxInSub++;
                                    }
                                } else{
                                    //TODO:throw exception;
                                }
                            }
                        }
                        varIdxInBody++;
                    }
                }
            }
        }
        return ImmutableMap.copyOf(varMap);
    }

    private ImmutableExpression comparisonExpToFilterCondition(ComparisonExpression comparisonExpression){
        String operator = comparisonExpression.getPredicate().getName();
        ExpressionOperation expressionOperation = null;
        if(operator == ExpressionOperation.LT.getName())
            expressionOperation = ExpressionOperation.LT;
        else if(operator == ExpressionOperation.GT.getName())
            expressionOperation = ExpressionOperation.GT;
        else if(operator == ExpressionOperation.EQ.getName())
            expressionOperation = ExpressionOperation.EQ;
        else if(operator == ExpressionOperation.NEQ.getName())
            expressionOperation = ExpressionOperation.NEQ;

        return termFactory.getImmutableExpression(expressionOperation,comparisonExpression.getLeftOperand(), comparisonExpression.getRightOperand());
    }

}
