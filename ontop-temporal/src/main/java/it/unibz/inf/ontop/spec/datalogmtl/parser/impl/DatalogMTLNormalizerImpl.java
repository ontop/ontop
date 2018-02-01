package it.unibz.inf.ontop.spec.datalogmtl.parser.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLNormalizer;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.temporal.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatalogMTLNormalizerImpl implements DatalogMTLNormalizer{

    private final DatalogMTLFactory datalogMTLFactory;

    @Inject
    public DatalogMTLNormalizerImpl(DatalogMTLFactory datalogMTLFactory){
        this.datalogMTLFactory = datalogMTLFactory;
    }

    private List<DatalogMTLRule> getStaticRules(DatalogMTLProgram datalogMTLProgram){
        return datalogMTLProgram.getRules().stream().filter(r -> r.getHead() instanceof StaticExpression).collect(Collectors.toList());
    }

    public DatalogMTLProgram normalize(DatalogMTLProgram program, Mapping staticMapping){
        List<DatalogMTLRule> staticRules = getStaticRules(program);
        List<DatalogMTLRule> rules = correctStaticExpressionsInRules(program.getRules(), staticMapping, staticRules);
        return datalogMTLFactory.createProgram(program.getPrefixes(), rules);
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
