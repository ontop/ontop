package it.unibz.inf.ontop.spec.datalogmtl.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.temporal.model.impl.AbstractUnaryTemporalExpressionWithRange;
import it.unibz.inf.ontop.temporal.model.impl.DatalogMTLFactoryImpl;
import it.unibz.inf.ontop.temporal.model.impl.TemporalRangeImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.eclipse.rdf4j.query.algebra.Str;
import org.mapdb.Atomic;

import java.util.List;
import java.util.Map;

public class DatalogMTLVisitorImpl extends DatalogMTLBaseVisitor implements DatalogMTLVisitor {
    private final TermFactory termFactory;
    private final AtomFactory atomFactory;
    private final RDF rdfFactory;
    private final DatalogMTLFactory datalogMTLFactory;
    ImmutableMap<String, String> prefixes;
    private static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public DatalogMTLVisitorImpl(TermFactory termFactory, AtomFactory atomFactory) {
        this.rdfFactory = new SimpleRDF();
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
        datalogMTLFactory = new DatalogMTLFactoryImpl();
    }

    @Override
    public DatalogMTLProgram visitParse(DatalogMTLParser.ParseContext ctx) {

        prefixes = ctx.directiveStatement().prefixID().stream()
                .collect(ImmutableCollectors.toMap(pid -> pid.PNAME_NS().getText(), pid -> pid.IRIREF().getText()));

        ImmutableList<DatalogMTLRule> rules = ctx.dMTLProgram().dMTLRule().stream()
                .map(this::visitDMTLRule).collect(ImmutableCollectors.toList());

        return datalogMTLFactory.createProgram(prefixes, rules);
    }

    @Override
    public DatalogMTLRule visitDMTLRule(DatalogMTLParser.DMTLRuleContext ctx){
        AtomicExpression headExpression = visitHead(ctx.head());
        DatalogMTLExpression bodyExpression = visitBody(ctx.body());

        return datalogMTLFactory.createRule(headExpression, bodyExpression);
    }

    @Override
    public DatalogMTLExpression visitBody(DatalogMTLParser.BodyContext ctx){
        return null;
    }

    @Override
    public AtomicExpression visitHead(DatalogMTLParser.HeadContext ctx){
        if (ctx.temporalOperator() == null){
            if (ctx.temporalRange() == null){
                return  visitTriple(ctx.triple(), false);
            } else {
                throw new IllegalArgumentException("Invalid temporal expression. Temporal range is missing.");
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

    private AbstractUnaryTemporalExpressionWithRange createUnaryTemporalExpression(
            DatalogMTLParser.TemporalOperatorContext ctx,  TemporalRange temporalRange, TemporalAtomicExpression temporalAtomicExpression){
        if (ctx.always_in_future() != null)
            return (AbstractUnaryTemporalExpressionWithRange) datalogMTLFactory.createBoxPlusExpression(temporalRange, temporalAtomicExpression);
        else if  (ctx.always_in_past()!= null)
            return (AbstractUnaryTemporalExpressionWithRange) datalogMTLFactory.createBoxMinusExpression(temporalRange, temporalAtomicExpression);
        else if (ctx.sometime_in_future()!= null)
            return (AbstractUnaryTemporalExpressionWithRange) datalogMTLFactory.createDiamondPlusExpression(temporalRange, temporalAtomicExpression);
        else
            return (AbstractUnaryTemporalExpressionWithRange) datalogMTLFactory.createDiamondMinusExpression(temporalRange, temporalAtomicExpression);
    }

    public AtomicExpression visitTriple(DatalogMTLParser.TripleContext ctx, boolean isTemporal){

        String subStr = ctx.tripleItem(0).getText();
        Variable subject;
        if (ctx.tripleItem(0).VARIABLE() != null)
            subject = termFactory.getVariable(subStr.substring(1, subStr.length()));
        else
            throw new IllegalArgumentException("Invalid subject " + subStr + " for the triple " + ctx.getText());

        String objStr = ctx.tripleItem(2).getText();
        if (ctx.tripleItem(2).VARIABLE() != null) {
            Variable object = termFactory.getVariable(objStr.substring(1, subStr.length()));
            if(ctx.tripleItem(1).predicate()!= null){
                if (!isRDFType(ctx.tripleItem(1).predicate().PNAME_NS().getText(), ctx.tripleItem(1).predicate().WORD().getText())){
                    String prefix = prefixes.get(ctx.tripleItem(1).predicate().PNAME_NS().getText());
                    prefix = prefix.substring(1, prefix.length()-1);
                    String atomPredStr =  prefix + ctx.tripleItem(1).predicate().WORD();
                    AtomPredicate pred = atomFactory.getAtomPredicate(atomPredStr, 2);
                    if(isTemporal)
                        return datalogMTLFactory.createTemporalAtomicExpression(pred, subject, object);
                    else
                        return datalogMTLFactory.createStaticAtomicExpression(pred, subject, object);
                }else
                    throw new IllegalArgumentException("Invalid triple " + ctx.getText());

            }
        } else if (ctx.tripleItem(2).predicate() != null){
            String atomPredStr = prefixes.get(ctx.tripleItem(2).predicate().PNAME_NS().getText()) + ctx.tripleItem(2).predicate().WORD();
            AtomPredicate pred = atomFactory.getAtomPredicate(atomPredStr, 1);
            if (isRDFType(ctx.tripleItem(1).predicate().PNAME_NS().getText(), ctx.tripleItem(1).predicate().WORD().getText())){
                if(isTemporal)
                    return datalogMTLFactory.createTemporalAtomicExpression(pred, subject);
                else
                    return datalogMTLFactory.createStaticAtomicExpression(pred, subject);
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

        boolean beginInc = false;
        boolean endInc = false;

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

    @Override
    public Term visitTripleItem(DatalogMTLParser.TripleItemContext ctx){
        return null;
    }
}


