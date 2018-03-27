package it.unibz.inf.ontop.spec.datalogmtl.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLNormalizer;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Collectors;

public class DatalogMTLNormalizerImpl implements DatalogMTLNormalizer{

    private final DatalogMTLFactory datalogMTLFactory;
    private final AtomFactory atomFactory;
    private int auxCounter = 0;

    @Inject
    public DatalogMTLNormalizerImpl(DatalogMTLFactory datalogMTLFactory, AtomFactory atomFactory){
        this.datalogMTLFactory = datalogMTLFactory;
        this.atomFactory = atomFactory;
    }

    private List<DatalogMTLRule> getStaticRules(DatalogMTLProgram datalogMTLProgram){
        return datalogMTLProgram.getRules().stream().filter(r -> r.getHead() instanceof StaticExpression).collect(Collectors.toList());
    }

    private AtomPredicate createFreshAtomPredicate(int arity){
        auxCounter++;
        return atomFactory.getAtomPredicate("aux_" + (auxCounter-1), arity);
    }
    public DatalogMTLProgram normalize(DatalogMTLProgram program, Mapping staticMapping){
        List<DatalogMTLRule> staticRules = getStaticRules(program);
        List<DatalogMTLRule> rules = correctStaticExpressionsInRules(program.getRules(), staticMapping, staticRules);
        rules = pullOutStaticSubRules(rules);
        rules = convertNaryTemporalJoinsToBinary(rules);
        return datalogMTLFactory.createProgram(program.getPrefixes(), rules);
    }

    private List<DatalogMTLRule> convertNaryTemporalJoinsToBinary(List<DatalogMTLRule> rules){
        List<DatalogMTLRule> newRules = new ArrayList<>();
        for (DatalogMTLRule rule : rules){
            DatalogMTLRule newRule = datalogMTLFactory.createRule(rule.getHead(), naryToBinary(rule.getBody()));
            newRules.add(newRule);
        }
        return newRules;
    }
    private DatalogMTLExpression naryToBinary(DatalogMTLExpression currentExpression){
        if (currentExpression instanceof AtomicExpression) {
            return currentExpression;
        }else if(currentExpression instanceof TemporalJoinExpression){
            DatalogMTLExpression firstExp = null;
            for (DatalogMTLExpression childExp : ((TemporalJoinExpression) currentExpression).getOperands()) {
                childExp = naryToBinary(childExp);
                if (firstExp != null) {
                    firstExp = datalogMTLFactory.createTemporalJoinExpression(firstExp, childExp);
                }else {
                    firstExp = childExp;
                }
            }
            return firstExp;
        }else if(currentExpression instanceof StaticJoinExpression){
            List <StaticExpression> opList = new ArrayList<>();
            for (DatalogMTLExpression exp :currentExpression.getChildNodes()){
                opList.add((StaticExpression)exp);
            }
            return datalogMTLFactory.createStaticJoinExpression(opList);
        }else {
            List <DatalogMTLExpression> opList = new ArrayList<>();
            for (DatalogMTLExpression exp :currentExpression.getChildNodes()){
                opList.add(naryToBinary(exp));
            }
            return createNewExpression(currentExpression, opList);
        }
    }

    private DatalogMTLExpression createNewExpression(DatalogMTLExpression currentExp, List<DatalogMTLExpression> operands){
        if (currentExp instanceof InnerJoinExpression){
            return datalogMTLFactory.createInnerJoinExpression(operands);
        } else if(currentExp instanceof BoxPlusExpression){
            return datalogMTLFactory.createBoxPlusExpression(((BoxPlusExpression) currentExp).getRange(), operands.get(0));
        } else if(currentExp instanceof BoxMinusExpression){
            return datalogMTLFactory.createBoxMinusExpression(((BoxMinusExpression) currentExp).getRange(), operands.get(0));
        } else if(currentExp instanceof DiamondPlusExpression){
            return datalogMTLFactory.createDiamondPlusExpression(((DiamondPlusExpression) currentExp).getRange(), operands.get(0));
        } else if(currentExp instanceof DiamondMinusExpression){
            return datalogMTLFactory.createDiamondMinusExpression(((DiamondMinusExpression) currentExp).getRange(), operands.get(0));
        } else if(currentExp instanceof SinceExpression){
            return datalogMTLFactory.createSinceExpression(((SinceExpression) currentExp).getRange(),operands.get(0), operands.get(1));
        } else if(currentExp instanceof UntilExpression){
            return datalogMTLFactory.createUntilExpression(((UntilExpression) currentExp).getRange(),operands.get(0), operands.get(1));
        } else if(currentExp instanceof FilterExpression){
            return datalogMTLFactory.createFilterExpression(operands.get(0), ((FilterExpression) currentExp).getComparisonExpression());
        } else {
            throw new UnsupportedOperationException("Unknown DatalogMTL expression");
        }
    }

    private List<DatalogMTLRule> pullOutStaticSubRules(List<DatalogMTLRule> rules) {
        List<DatalogMTLRule> newRules = new ArrayList<>();
        for (DatalogMTLRule rule : rules){
            if (rule.getBody() instanceof TemporalJoinExpression){
                TemporalJoinExpression body = rule.getBody();
                DatalogMTLExpression staticExp;
                ImmutableList <StaticExpression> staticOperands = body.getOperands().stream()
                        .filter(o -> o instanceof StaticExpression)
                        .map(o -> ((StaticExpression)o))
                        .collect(ImmutableCollectors.toList());
                if (!staticOperands.isEmpty()) {
                    if (staticOperands.size() > 1) {
                        DatalogMTLRule newRule = createStaticRule(staticOperands);
                        newRules.add(newRule);
                        staticExp = newRule.getHead();
                    } else {
                        staticExp = staticOperands.get(0);
                    }

                    DatalogMTLExpression temporalExp;
                    ImmutableList<DatalogMTLExpression> temporalOperands = body.getOperands().stream()
                            .filter(o -> o instanceof TemporalExpression)
                            .map(o -> ((TemporalExpression) o))
                            .collect(ImmutableCollectors.toList());
                    if (temporalOperands.size() > 1) {
                        DatalogMTLRule newRule = createTemporalRule(temporalOperands);
                        newRules.add(newRule);
                        temporalExp = newRule.getHead();
                    } else {
                        temporalExp = temporalOperands.get(0);
                    }

                    DatalogMTLExpression newBody = datalogMTLFactory.createInnerJoinExpression(staticExp, temporalExp);
                    newRules.add(datalogMTLFactory.createRule(rule.getHead(), newBody));
                }else{
                    newRules.add(rule);
                }
            }else{
                newRules.add(rule);
            }
        }
        return ImmutableList.copyOf(newRules);
    }

    private DatalogMTLRule createStaticRule(ImmutableList <StaticExpression> staticOperands){
        Set<VariableOrGroundTerm> newSet = new HashSet<>();
        for (StaticExpression se : staticOperands) {
            newSet.addAll(se.getAllVariableOrGroundTerms());
        }
        AtomPredicate newStaticHeadPredicate = createFreshAtomPredicate(newSet.size());
        AtomicExpression newStaticHead = datalogMTLFactory.createStaticAtomicExpression(newStaticHeadPredicate, new ArrayList<>(newSet));
        DatalogMTLExpression newStaticJoinBody = datalogMTLFactory.createStaticJoinExpression(staticOperands);
        return datalogMTLFactory.createRule(newStaticHead, newStaticJoinBody);
    }

    private DatalogMTLRule createTemporalRule(ImmutableList <DatalogMTLExpression> temporalOperands){
        Set<VariableOrGroundTerm> newSet = new HashSet<>();
        for (DatalogMTLExpression te : temporalOperands) {
            newSet.addAll(te.getAllVariableOrGroundTerms());
        }
        AtomPredicate newTemporalHeadPredicate = createFreshAtomPredicate(newSet.size());
        AtomicExpression newTemporalHead = datalogMTLFactory.createTemporalAtomicExpression(newTemporalHeadPredicate, new ArrayList<>(newSet));
        DatalogMTLExpression newTemporalJoinBody = datalogMTLFactory.createTemporalJoinExpression(temporalOperands);
        return datalogMTLFactory.createRule(newTemporalHead, newTemporalJoinBody);
    }

    private List<DatalogMTLRule> correctStaticExpressionsInRules(List<DatalogMTLRule> rules, Mapping staticMapping, List<DatalogMTLRule> staticRules){
        List<DatalogMTLRule> newRules = new ArrayList<>();
        for (DatalogMTLRule rule : rules){
            DatalogMTLExpression body = correctStaticExpressions(rule.getBody(), staticMapping, staticRules);
            if (body instanceof StaticExpression){
                AtomicExpression head = datalogMTLFactory.createStaticAtomicExpression(rule.getHead().getPredicate(), rule.getHead().getVariableOrGroundTerms());
                newRules.add(datalogMTLFactory.createRule(head, body));
            } else {
                newRules.add(datalogMTLFactory.createRule(rule.getHead(), body));
            }
        }
        return newRules;
    }

    private DatalogMTLExpression correctStaticExpressions(DatalogMTLExpression currentExpression, Mapping staticMapping, List <DatalogMTLRule> staticRules){
        if (currentExpression instanceof AtomicExpression) {
            if(currentExpression instanceof TemporalAtomicExpression) {
                for(AtomPredicate atomPredicate : staticMapping.getPredicates()){
                    if(atomPredicate.getName().equals(((TemporalAtomicExpression) currentExpression).getPredicate().getName())){
                       return datalogMTLFactory.createStaticAtomicExpression(((TemporalAtomicExpression) currentExpression).getPredicate(),
                               ((TemporalAtomicExpression) currentExpression).getVariableOrGroundTerms());
                    }
                }
                for(DatalogMTLRule rule : staticRules){
                    if (rule.getHead().getPredicate().getName().equals(((TemporalAtomicExpression) currentExpression).getPredicate().getName())){
                        return datalogMTLFactory.createStaticAtomicExpression(((TemporalAtomicExpression) currentExpression).getPredicate(),
                                ((TemporalAtomicExpression) currentExpression).getVariableOrGroundTerms());
                    }
                }
                return currentExpression;

            } else {
                return currentExpression;
            }
        } else if(currentExpression instanceof TemporalJoinExpression){
            List<DatalogMTLExpression> newChildren = ((TemporalJoinExpression)currentExpression).getOperands().stream()
                    .map(child -> correctStaticExpressions(child, staticMapping, staticRules)).collect(Collectors.toList());

            List<StaticExpression> newStaticChildren = new ArrayList<>();
            for(DatalogMTLExpression child : newChildren){
                if(child instanceof StaticExpression)
                    newStaticChildren.add((StaticExpression)child);
            }
            if (newStaticChildren.size() == newChildren.size()){
                return datalogMTLFactory.createStaticJoinExpression(newStaticChildren);
            } else {
                return datalogMTLFactory.createTemporalJoinExpression(newChildren);
            }
        } else if(currentExpression instanceof StaticJoinExpression){
            return currentExpression;
        } else if (currentExpression instanceof FilterExpression){
            return datalogMTLFactory.createFilterExpression(correctStaticExpressions(((FilterExpression) currentExpression).getExpression(), staticMapping, staticRules),
                    ((FilterExpression) currentExpression).getComparisonExpression());
        } else if(currentExpression instanceof UnaryTemporalExpression
                && currentExpression instanceof TemporalExpressionWithRange){

            if (currentExpression instanceof BoxMinusExpression){
                return datalogMTLFactory.createBoxMinusExpression(((BoxMinusExpression) currentExpression).getRange(),
                        correctStaticExpressions(((BoxMinusExpression) currentExpression).getOperand(), staticMapping, staticRules));
            } else if (currentExpression instanceof BoxPlusExpression){
                return datalogMTLFactory.createBoxPlusExpression(((BoxPlusExpression) currentExpression).getRange(),
                        correctStaticExpressions(((BoxPlusExpression) currentExpression).getOperand(), staticMapping, staticRules));
            } else if (currentExpression instanceof DiamondMinusExpression) {
                return datalogMTLFactory.createDiamondMinusExpression(((DiamondMinusExpression) currentExpression).getRange(),
                        correctStaticExpressions(((DiamondMinusExpression) currentExpression).getOperand(), staticMapping, staticRules));
            } else { //diamond plus
                return datalogMTLFactory.createDiamondPlusExpression(((DiamondPlusExpression) currentExpression).getRange(),
                        correctStaticExpressions(((DiamondPlusExpression) currentExpression).getOperand(), staticMapping, staticRules));

            }
        } else if (currentExpression instanceof BinaryTemporalExpression
                && currentExpression instanceof TemporalExpressionWithRange) {

            if (currentExpression instanceof SinceExpression) {
                return datalogMTLFactory.createSinceExpression(((SinceExpression) currentExpression).getRange(),
                        correctStaticExpressions(((SinceExpression) currentExpression).getLeftOperand(), staticMapping, staticRules),
                        correctStaticExpressions(((SinceExpression) currentExpression).getRightOperand(), staticMapping, staticRules));

            } else { //UntilExpression
                return datalogMTLFactory.createUntilExpression(((UntilExpression) currentExpression).getRange(),
                        correctStaticExpressions(((UntilExpression) currentExpression).getLeftOperand(), staticMapping, staticRules),
                        correctStaticExpressions(((UntilExpression) currentExpression).getRightOperand(), staticMapping, staticRules));

            }
        } else throw new IllegalArgumentException("Invalid agument " + currentExpression.toString());
    }
}
