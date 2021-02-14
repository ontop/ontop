package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JDescribeQuery;
import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.algebra.*;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


class RDF4JDescribeQueryImpl extends RDF4JInputQueryImpl<GraphResultSet> implements RDF4JDescribeQuery {

    private final ParsedQuery originalParsedQuery;

    // True if the pattern "?s ?p <describedIRI>" should also be considered while answering a DESCRIBE query.
    private final boolean isFixedObjectIncludedInDescribe;
    // Same hash for the same query string
    private final String hash;

    // LAZY
    private ConstructTemplate constructTemplate;

    RDF4JDescribeQueryImpl(ParsedQuery originalParsedQuery, String queryString, BindingSet bindings,
                           boolean isFixedObjectIncludedInDescribe) {
        super(queryString, bindings);
        this.originalParsedQuery = originalParsedQuery;
        this.isFixedObjectIncludedInDescribe = isFixedObjectIncludedInDescribe;
        hash = DigestUtils.sha1Hex(queryString);
    }

    @Override
    public RDF4JDescribeQuery newBindings(BindingSet newBindings) {
        return new RDF4JDescribeQueryImpl(originalParsedQuery, getInputString(), newBindings, isFixedObjectIncludedInDescribe);
    }

    @Override
    protected ParsedQuery transformParsedQuery() throws OntopUnsupportedInputQueryException {
        ConstructQuerySplit split = convertIntoConstructionQuerySplit(originalParsedQuery, isFixedObjectIncludedInDescribe, hash);
        constructTemplate = split.getConstructTemplate();

        return split.getSelectParsedQuery();
    }

    private static ConstructQuerySplit convertIntoConstructionQuerySplit(ParsedQuery originalParsedQuery,
                                                                         boolean isFixedObjectIncludedInDescribe,
                                                                         String hash)
            throws OntopUnsupportedInputQueryException {
        TupleExpr root = originalParsedQuery.getTupleExpr();
        TupleExpr topNonDescribeExpression = Optional.of(root)
                .filter(t -> t instanceof DescribeOperator)
                .map(t -> ((DescribeOperator)t).getArg())
                .orElseThrow(() -> new MinorOntopInternalBugException("The describe query was expected to start " +
                        "with a DescribeOperator"));

        Projection projection = Optional.of(topNonDescribeExpression)
                .filter(t -> t instanceof Projection)
                .map(t -> (Projection) t)
                .orElseThrow(() -> new MinorOntopInternalBugException("The describe query was expected to have a Project " +
                        "after the DescribeOperator"));

        ProjectionElem initialDescribeVariable = extractProjectElement(projection);
        // Introduced for reproducing the describe variable name across multiple executions.
        // Needed due to the presence of IQ caching.
        ProjectionElem describeVariableRenaming = new ProjectionElem(initialDescribeVariable.getTargetName(), "d" + hash);

        String p1 = "p" + hash;
        String o1 = "o" + hash;
        String s2 = "s" + hash;
        String p2 = "p" + hash;

        UnaryTupleOperator newProjection = createNewProjection(describeVariableRenaming.getTargetName(), p1, o1, s2, p2, isFixedObjectIncludedInDescribe);
        ConstructTemplate constructTemplate = new RDF4JConstructTemplate(newProjection, null);

        TupleExpr initialSubQuery = projection.getArg().clone();

        ParsedTupleQuery selectQuery = createSelectQuery(initialSubQuery, describeVariableRenaming, p1, o1, s2, p2,
                isFixedObjectIncludedInDescribe);

        return new ConstructQuerySplit(constructTemplate, selectQuery);
    }

    private static ProjectionElem extractProjectElement(Projection topProjection) throws OntopUnsupportedInputQueryException {
        List<ProjectionElem> projectionElements = topProjection.getProjectionElemList().getElements();

        if (projectionElements.size() == 1)
            return projectionElements.get(0);

        throw new OntopUnsupportedInputQueryException("DESCRIBE queries with more than one term (variable or IRI) are not supported");
    }

    private static UnaryTupleOperator createNewProjection(String describeTerm, String p1, String o1, String s2, String p2,
                                                          boolean isFixedObjectIncludedInDescribe) {
        ProjectionElem describeProjectElem = new ProjectionElem(describeTerm);
        ProjectionElemList projection1 = new ProjectionElemList(describeProjectElem, new ProjectionElem(p1), new ProjectionElem(o1));

        if (isFixedObjectIncludedInDescribe) {
            ProjectionElemList projection2 = new ProjectionElemList(new ProjectionElem(s2), new ProjectionElem(p2), describeProjectElem);

            MultiProjection multiProjection = new MultiProjection();
            multiProjection.addProjection(projection1);
            multiProjection.addProjection(projection2);

            return multiProjection;
        }
        else {
            Projection projection = new Projection();
            projection.setProjectionElemList(projection1);

            return projection;
        }
    }

    private static TupleExpr renameSubQuery(TupleExpr subQuery, ProjectionElem describeVariable) {
        return new Projection(subQuery, new ProjectionElemList(describeVariable));
    }

    private static ParsedTupleQuery createSelectQuery(TupleExpr initialSubQuery, ProjectionElem describeRenaming, String p1,
                                                      String o1, String s2, String p2, boolean isFixedObjectIncludedInDescribe) {
        Join joinTree = new Join(
                transformSubQuery(initialSubQuery, describeRenaming),
                createSPPOUnion(describeRenaming.getTargetName(), p1, o1, s2, p2, isFixedObjectIncludedInDescribe));

        return new ParsedTupleQuery(joinTree);
    }

    private static TupleExpr transformSubQuery(TupleExpr initialSubQuery, ProjectionElem describeRenaming) {
        return new Distinct(
                new Projection(initialSubQuery, new ProjectionElemList(describeRenaming)));
    }

    private static TupleExpr createSPPOUnion(String newDescribeVariableName, String p1,
                                             String o1, String s2, String p2, boolean isFixedObjectIncludedInDescribe) {
        Var describeVariable = new Var(newDescribeVariableName);
        StatementPattern leftStatement = new StatementPattern(describeVariable, new Var(p1), new Var(o1));

        ProjectionElem describeProjectionElem = new ProjectionElem(newDescribeVariableName);

        Projection left = new Projection(leftStatement,
                new ProjectionElemList(describeProjectionElem, new ProjectionElem(p1), new ProjectionElem(o1)));

        if (isFixedObjectIncludedInDescribe) {
            StatementPattern rightStatement = new StatementPattern(new Var(s2), new Var(p2), describeVariable);
            Projection right = new Projection(rightStatement,
                    new ProjectionElemList(new ProjectionElem(s2), new ProjectionElem(p2), describeProjectionElem));

            return new Union(left, right);
        }
        else
            return left;
    }

    @Override
    public ConstructTemplate getConstructTemplate() {

        // May happen due to the caching of the IQ
        if (constructTemplate == null) {
            try {
                transformParsedQuery();
                return constructTemplate;
            } catch (OntopUnsupportedInputQueryException e) {
                throw new IllegalStateException("The fact that this query is not supported should have been detected " +
                        "while reformulating the query.");
            }
        }

        return constructTemplate;
    }
}
