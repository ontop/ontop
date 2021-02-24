package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JConstructQuery;
import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.algebra.*;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

import java.util.Optional;


class RDF4JConstructQueryImpl extends RegularRDF4JInputImpl<GraphResultSet> implements RDF4JConstructQuery {
    private final ConstructTemplate template;

    RDF4JConstructQueryImpl(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        this(split(parsedQuery), queryString, bindings);
    }

    RDF4JConstructQueryImpl(ConstructQuerySplit split, String queryString, BindingSet bindings) {
        this(split.getConstructTemplate(), split.getSelectParsedQuery(), queryString, bindings);
    }

    private RDF4JConstructQueryImpl(ConstructTemplate template, ParsedQuery selectParsedQuery, String queryString,
                                    BindingSet bindings) {
        super(selectParsedQuery, queryString, bindings);
        this.template = template;
    }

    @Override
    public ConstructTemplate getConstructTemplate() {
        return template;
    }

    @Override
    public RDF4JConstructQuery newBindings(BindingSet newBindings) {
        return new RDF4JConstructQueryImpl(template, parsedQuery, getInputString(), newBindings);
    }

    private static ConstructQuerySplit split(ParsedQuery parsedQuery) {
        TupleExpr root = parsedQuery.getTupleExpr();
        TupleExpr topNonSliceExpression = getFirstNonSliceExpression(root);
        UnaryTupleOperator constructionProjection = getFirstProjection(topNonSliceExpression);
        Optional<Extension> constructionExtension = getExtension(constructionProjection);

        ConstructTemplate constructTemplate = constructionExtension
                .map(e -> new RDF4JConstructTemplate(constructionProjection, e))
                .orElseGet(() -> new RDF4JConstructTemplate(constructionProjection, null));

        TupleExpr selectSubTree = constructionExtension
                .map(Extension::getArg)
                .orElseGet(constructionProjection::getArg);

        ParsedTupleQuery selectQuery = computeSelectQuery(root, selectSubTree);

        return new ConstructQuerySplit(constructTemplate, selectQuery);
    }

    private static TupleExpr getFirstNonSliceExpression(TupleExpr expr) {
        // NB: the slice is not relevant for the construct template
        // (will be taken into account in the SELECT query fragment)
        return expr instanceof Slice ?
                getFirstNonSliceExpression(((Slice) expr).getArg()):
                expr;
    }

    private static Optional<Extension> getExtension(UnaryTupleOperator proj) {
        return Optional.of(proj.getArg())
                .filter(t -> t instanceof Extension)
                .map(t -> (Extension) t);
    }

    private static UnaryTupleOperator getFirstProjection(TupleExpr expr) {
        if(expr instanceof Projection || expr instanceof MultiProjection)
            return (UnaryTupleOperator) expr;
        if (expr instanceof Reduced)
            return getFirstProjection(((Reduced) expr).getArg());

        throw new MinorOntopInternalBugException("Unexpected SPARQL query (after parsing): "+
                "an instance of "+
                Projection.class+
                " or "+
                Reduced.class+
                " is expected, instead of\n"+
                expr
        );
    }

    private static ParsedTupleQuery computeSelectQuery(TupleExpr root, TupleExpr selectSubTree) {
        TupleExpr selectTree;
        if (root instanceof Slice) {
            Slice newSliceTree = (Slice) root.clone();
            newSliceTree.setArg(selectSubTree.clone());
            selectTree = newSliceTree;
        }
        else
            selectTree = selectSubTree;

        return new ParsedTupleQuery(selectTree);
    }

}
