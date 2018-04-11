package it.unibz.inf.ontop.spec.datalogmtl.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLBaseVisitor;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLParser;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLVisitor;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.temporal.model.impl.AbstractUnaryTemporalExpressionWithRange;
import it.unibz.inf.ontop.temporal.model.impl.DatalogMTLFactoryImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatalogMTLVisitorImpl extends DatalogMTLBaseVisitor implements DatalogMTLVisitor {
    private final TermFactory termFactory;
    private final AtomFactory atomFactory;
    private final DatalogMTLFactory datalogMTLFactory;
    ImmutableMap<String, String> prefixes;
    ImmutableList<String> headsOfStaticRules;
    private static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public DatalogMTLVisitorImpl(TermFactory termFactory, AtomFactory atomFactory) {
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
        datalogMTLFactory = new DatalogMTLFactoryImpl();
    }

    private ImmutableList<String> collectStaticHeads(DatalogMTLParser.ParseContext ctx){

        List <String> stringList = ctx.dMTLProgram().dMTLRule().stream()
                .filter(dmtlRuleContext -> dmtlRuleContext.annotation() != null)
                .map(dmtlRuleContext -> visitTriple(dmtlRuleContext.head().triple(), false).getPredicate().getName())
                .collect(Collectors.toList());

        List <String> returnList = new ArrayList<>();
        for(String str : stringList){
            for(String pref : prefixes.values()){
                if(str.contains(pref))
                    returnList.add(str.replace(pref, ""));
            }
        }

        return ImmutableList.copyOf(returnList);
    }

    @Override
    public DatalogMTLProgram visitParse(DatalogMTLParser.ParseContext ctx) {

        prefixes = ctx.directiveStatement().prefixID().stream()
                .collect(ImmutableCollectors.toMap(
                        pid -> pid.PNAME_NS().getText(),
                        pid -> pid.IRIREF().getText().substring(1, pid.IRIREF().getText().length()-1)));

        //headsOfStaticRules = collectStaticHeads(ctx);
        ImmutableList<DatalogMTLRule> rules = visitDMTLProgram(ctx.dMTLProgram());

        return datalogMTLFactory.createProgram(prefixes, rules);
    }

    @Override
    public ImmutableList<DatalogMTLRule> visitDMTLProgram(DatalogMTLParser.DMTLProgramContext ctx){
        return ctx.dMTLRule().stream()
                .map(this::visitDMTLRule).collect(ImmutableCollectors.toList());
    }

    @Override
    public DatalogMTLRule visitDMTLRule(DatalogMTLParser.DMTLRuleContext ctx){

        Boolean isStatic = false;
        if(ctx.annotation() != null)
            isStatic = true;

        AtomicExpression headExpression = visitHead(ctx.head(), isStatic);
        DatalogMTLExpression bodyExpression = visitBody(ctx.body(), isStatic);

        return datalogMTLFactory.createRule(headExpression, bodyExpression);
    }

    public AtomicExpression visitHead(DatalogMTLParser.HeadContext ctx, boolean isHeadOfStaticRule){

        if(isHeadOfStaticRule){
            return visitTriple(ctx.triple(), true);
        }else {
            if (ctx.temporalOperator() == null) {
                if (ctx.temporalRange() == null) {
                    return visitTriple(ctx.triple(), false);
                } else {
                    throw new IllegalArgumentException("Invalid temporal expression. Temporal range is missing.");
                }
            }
        }

        //This part is commented out.
        //Because a specific interface should be defined for head expression
        //that only allows static expression, temporal atomic expression or
        //temporal atomic expression with temporal operator and range.
        //TODO: define interface for head expression
//        else {
//            if (ctx.temporalRange() == null){
//                throw new IllegalArgumentException("Invalid temporal expression. Temporal operator is missing.");
//            } else {
//                TemporalRange temporalRange = visitTemporalRange(ctx.temporalRange());
//                AtomicExpression temporalAtomicExpression = visitTriple(ctx.triple(), true);
//                return createUnaryTemporalExpression(ctx.temporalOperator(), temporalRange, (TemporalAtomicExpression) temporalAtomicExpression);
//            }
//        }
        return null;
    }

    public DatalogMTLExpression visitBody(DatalogMTLParser.BodyContext ctx, boolean isBodyOfStaticRule){

        while(ctx.body() != null) {
            return visitBody(ctx.body(), isBodyOfStaticRule);
        }

        DatalogMTLExpression expression;

        if(isBodyOfStaticRule){
            if (ctx.expression().size() > 1) {
                expression = datalogMTLFactory.createStaticJoinExpression(ctx.expression().stream()
                        .map(expCtx -> (StaticExpression)visitExpression(expCtx, isBodyOfStaticRule)).collect(Collectors.toList()));
            }else {
                expression = visitExpression(ctx.expression(0), isBodyOfStaticRule);
            }
        } else {
            if (ctx.expression().size() > 1) {
                List<DatalogMTLExpression> expList = ctx.expression().stream()
                        .map(expCtx -> visitExpression(expCtx, isBodyOfStaticRule)).collect(Collectors.toList());
                expression = datalogMTLFactory.createTemporalJoinExpression(expList);
            } else {
                expression = visitExpression(ctx.expression(0), isBodyOfStaticRule);
            }
        }
        return expression;
    }


    public DatalogMTLExpression visitExpression(DatalogMTLParser.ExpressionContext ctx, boolean isPartOfStaticRule){

        if (ctx.filterExpression() != null){
            return visitFilterExpression(ctx.filterExpression(), isPartOfStaticRule);
        } else if (ctx.triple_with_dot() != null){
            return visitTriple(ctx.triple_with_dot().triple(), isPartOfStaticRule);
        } else if (ctx.temporalExpression() != null){
            return visitTemporalExpression(ctx.temporalExpression());
        } else
            throw new IllegalArgumentException("Invalid expression " + ctx.getText());
    }

    @Override
    public TemporalExpression visitTemporalExpression(DatalogMTLParser.TemporalExpressionContext ctx){

        TemporalRange temporalRange = visitTemporalRange(ctx.temporalRange());
        if(ctx.triple_with_dot() != null){
            return createUnaryTemporalExpression(ctx.temporalOperator(), temporalRange, visitTriple(ctx.triple_with_dot().triple(), false));
        } else if (ctx.temporalExpression() != null){
            return createUnaryTemporalExpression(ctx.temporalOperator(), temporalRange, visitTemporalExpression(ctx.temporalExpression()));
        } else if(ctx.expression() != null){
            if(ctx.expression().size() > 1) {
                List<DatalogMTLExpression> expList = ctx.expression().stream().map(exp -> visitExpression(exp, false)).collect(Collectors.toList());
                TemporalJoinExpression temporalJoinExpression = datalogMTLFactory.createTemporalJoinExpression(expList);

                return createUnaryTemporalExpression(ctx.temporalOperator(), temporalRange, temporalJoinExpression);
            }else{
                DatalogMTLExpression expression = visitExpression(ctx.expression(0), false);
                return createUnaryTemporalExpression(ctx.temporalOperator(), temporalRange, expression);
            }
        }else
            throw new IllegalArgumentException("Invalid temporal expression " + ctx.getText());
    }

    private AbstractUnaryTemporalExpressionWithRange createUnaryTemporalExpression(
            DatalogMTLParser.TemporalOperatorContext ctx,  TemporalRange temporalRange, DatalogMTLExpression temporalExpression){

        if (ctx.always_in_future() != null)
            return (AbstractUnaryTemporalExpressionWithRange) datalogMTLFactory.createBoxPlusExpression(temporalRange, temporalExpression);
        else if  (ctx.always_in_past()!= null)
            return (AbstractUnaryTemporalExpressionWithRange) datalogMTLFactory.createBoxMinusExpression(temporalRange, temporalExpression);
        else if (ctx.sometime_in_future()!= null)
            return (AbstractUnaryTemporalExpressionWithRange) datalogMTLFactory.createDiamondPlusExpression(temporalRange, temporalExpression);
        else
            return (AbstractUnaryTemporalExpressionWithRange) datalogMTLFactory.createDiamondMinusExpression(temporalRange, temporalExpression);
    }

    public FilterExpression visitFilterExpression(DatalogMTLParser.FilterExpressionContext ctx, boolean isPartOfStaticRule){

        ComparisonExpression comparisonExpression = visitComparisonExpression(ctx.comparisonExpression());
        AtomicExpression atomicExpression = visitTriple(ctx.triple_with_dot().triple(), isPartOfStaticRule);

        return datalogMTLFactory.createFilterExpression(atomicExpression, comparisonExpression);
    }


    @Override
    public ComparisonExpression visitComparisonExpression(DatalogMTLParser.ComparisonExpressionContext ctx){

        VariableOrGroundTerm leftTerm = getTerm(ctx, 0);
        VariableOrGroundTerm rightTerm = getTerm(ctx, 2);
        AtomPredicate comparator = getComparator(ctx);

        return datalogMTLFactory.createComparisonExpression(comparator, leftTerm, rightTerm);
    }

    private VariableOrGroundTerm getTerm(DatalogMTLParser.ComparisonExpressionContext ctx, int idx){

        if (ctx.VARIABLE().contains(ctx.getChild(idx))){
            return termFactory.getVariable(ctx.getChild(idx).getText().substring(1));
        } else if (ctx.literal().equals(ctx.getChild(idx))) {
            return getTypedConstant(ctx.literal());
        } else
            throw  new IllegalArgumentException("illegal argument for comparison expression "+ ctx.getText());
    }

    private ValueConstant getTypedConstant(DatalogMTLParser.LiteralContext ctx){
        String value = ctx.getText();
        if(ctx.BOOLEAN() != null)
            return termFactory.getConstantLiteral(value, XSD.BOOLEAN);
        else if (ctx.DECIMAL() != null)
            return termFactory.getConstantLiteral(value, XSD.DECIMAL);
        else if (ctx.DOUBLE() != null)
            return termFactory.getConstantLiteral(value, XSD.DOUBLE);
        else if (ctx.INTEGER() != null)
            return termFactory.getConstantLiteral(value, XSD.INTEGER);
        else if (ctx.string() != null)
            return termFactory.getConstantLiteral(value, XSD.STRING);
        else
            throw new IllegalArgumentException("unrecognized constant type for " + ctx.getText());
    }

    private AtomPredicate getComparator(DatalogMTLParser.ComparisonExpressionContext ctx){

        if (ctx.COMPARATOR().getText().equals(">"))
            return  atomFactory.getAtomPredicate(ExpressionOperation.GT.getName(), 2);
        else if (ctx.COMPARATOR().getText().equals(">="))
            return  atomFactory.getAtomPredicate(ExpressionOperation.GTE.getName(), 2);
        else if (ctx.COMPARATOR().getText().equals("<"))
            return  atomFactory.getAtomPredicate(ExpressionOperation.LT.getName(), 2);
        else if (ctx.COMPARATOR().getText().equals("<="))
            return  atomFactory.getAtomPredicate(ExpressionOperation.LTE.getName(), 2);
        else if (ctx.COMPARATOR().getText().equals("="))
            return  atomFactory.getAtomPredicate(ExpressionOperation.EQ.getName(), 2);
        else if (ctx.COMPARATOR().getText().equals("<>"))
            return  atomFactory.getAtomPredicate(ExpressionOperation.NEQ.getName(), 2);
        else
            throw new IllegalArgumentException("Invalid comparator for the comparison expression " + ctx.getText());
    }

    private AtomicExpression visitTriple(DatalogMTLParser.TripleContext ctx, boolean isStatic){

        String subStr = ctx.tripleItem(0).getText();
        Variable subject;
        if (ctx.tripleItem(0).VARIABLE() != null)
            subject = termFactory.getVariable(subStr.substring(1, subStr.length()));
        else
            throw new IllegalArgumentException("Invalid subject " + subStr + " for the triple " + ctx.getText());

        String objStr = ctx.tripleItem(2).getText();
        if (ctx.tripleItem(2).VARIABLE() != null) {
            Variable object = termFactory.getVariable(objStr.substring(1, objStr.length()));
            if(ctx.tripleItem(1).predicate()!= null){
                if (!ctx.tripleItem(1).predicate().getText().equals("a") |
                        ((ctx.tripleItem(1).predicate().PNAME_NS() != null) &&
                                !isRDFType(ctx.tripleItem(1).predicate().PNAME_NS().getText(), ctx.tripleItem(1).predicate().WORD().getText()))){

                    String prefix = prefixes.get(ctx.tripleItem(1).predicate().PNAME_NS().getText());
                    String atomPredStr =  prefix + ctx.tripleItem(1).predicate().WORD();
                    AtomPredicate pred = atomFactory.getAtomPredicate(atomPredStr, 2);
                    if(isStatic)
                        return datalogMTLFactory.createStaticAtomicExpression(pred, subject, object);
                    else
                        return datalogMTLFactory.createTemporalAtomicExpression(pred, subject, object);
                }else
                    throw new IllegalArgumentException("Invalid triple " + ctx.getText());

            }
        } else if (ctx.tripleItem(2).predicate() != null){
            String atomPredStr = prefixes.get(ctx.tripleItem(2).predicate().PNAME_NS().getText()) + ctx.tripleItem(2).predicate().WORD();
            AtomPredicate pred = atomFactory.getAtomPredicate(atomPredStr, 1);
            if (ctx.tripleItem(1).predicate().getText().equals("a")){
                if(isStatic)
                    return datalogMTLFactory.createStaticAtomicExpression(pred, subject);
                else
                    return datalogMTLFactory.createTemporalAtomicExpression(pred, subject);

            } else if (ctx.tripleItem(1).predicate().PNAME_NS() != null){
                if(isRDFType(ctx.tripleItem(1).predicate().PNAME_NS().getText(), ctx.tripleItem(1).predicate().WORD().getText())) {
                    if (isStatic)
                        return datalogMTLFactory.createStaticAtomicExpression(pred, subject);
                    else
                        return datalogMTLFactory.createTemporalAtomicExpression(pred, subject);
                }
            } else
                throw new IllegalArgumentException("Invalid predicate for the triple " + ctx.getText());

        }else
            throw new IllegalArgumentException("Invalid object for the triple " + ctx.getText());

        return null;
    }

    private boolean isRDFType(String prefixName, String predicateName){
        if (prefixes.get(prefixName).equals(RDF) &&
                (predicateName.equals("type") || predicateName.equals("TYPE")))
            return true;

        return false;
    }

    @Override
    public TemporalRange visitTemporalRange(DatalogMTLParser.TemporalRangeContext ctx){

        boolean beginInc;
        boolean endInc;

        if (ctx.begin_inc().getText().equals("("))
            beginInc = false;
        else if (ctx.begin_inc().getText().equals("["))
            beginInc = true;
        else
            throw  new IllegalArgumentException("Begin inclusive argument is not valid! it should be either '(' or '[' .");

        if (ctx.end_inc().getText().equals(")"))
            endInc = false;
        else if (ctx.end_inc().getText().equals("]"))
            endInc = true;
        else
            throw  new IllegalArgumentException("End inclusive argument is not valid! it should be either ')' or ']' .");

        return datalogMTLFactory.createTemporalRange(beginInc, ctx.DURATION().get(0).getText(), ctx.DURATION().get(1).getText(), endInc);
    }

}