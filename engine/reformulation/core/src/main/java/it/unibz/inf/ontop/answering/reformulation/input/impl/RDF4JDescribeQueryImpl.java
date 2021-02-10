package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JDescribeQuery;
import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.algebra.*;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


class RDF4JDescribeQueryImpl extends RDF4JInputQueryImpl<GraphResultSet> implements RDF4JDescribeQuery {

    private final ParsedQuery originalParsedQuery;

    // True if the pattern "?s ?p <describedIRI>" should also be considered while answering a DESCRIBE query.
    private final boolean isFixedObjectIncludedInDescribe;

    // LAZY
    private ConstructTemplate constructTemplate;

    RDF4JDescribeQueryImpl(ParsedQuery originalParsedQuery, String queryString, BindingSet bindings,
                           boolean isFixedObjectIncludedInDescribe) {
        super(queryString, bindings);
        this.originalParsedQuery = originalParsedQuery;
        this.isFixedObjectIncludedInDescribe = isFixedObjectIncludedInDescribe;
    }

    @Override
    public RDF4JDescribeQuery newBindings(BindingSet newBindings) {
        return new RDF4JDescribeQueryImpl(originalParsedQuery, getInputString(), newBindings, isFixedObjectIncludedInDescribe);
    }

    @Override
    protected ParsedQuery transformParsedQuery() throws OntopUnsupportedInputQueryException {
        ConstructQuerySplit split = convertIntoConstructionQuerySplit(originalParsedQuery, isFixedObjectIncludedInDescribe);
        constructTemplate = split.getConstructTemplate();

        return split.getSelectParsedQuery();
    }

    private static ConstructQuerySplit convertIntoConstructionQuerySplit(ParsedQuery originalParsedQuery,
                                                                         boolean isFixedObjectIncludedInDescribe)
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

        ProjectionElem describeVariable = extractProjectElement(projection);

        String p1 = generateUniqueVariableName("p");
        String o1 = generateUniqueVariableName("o");
        String s2 = generateUniqueVariableName("s");
        String p2 = generateUniqueVariableName("p");

        UnaryTupleOperator newProjection = createNewProjection(describeVariable, p1, o1, s2, p2, isFixedObjectIncludedInDescribe);
        ConstructTemplate constructTemplate = new RDF4JConstructTemplate(newProjection, null);

        TupleExpr initialSubQuery = projection.getArg().clone();

        ParsedTupleQuery selectQuery = createSelectQuery(initialSubQuery, describeVariable, p1, o1, s2, p2,
                isFixedObjectIncludedInDescribe);

        return new ConstructQuerySplit(constructTemplate, selectQuery);
    }

    private static String generateUniqueVariableName(String prefix) {
        return prefix + UUID.randomUUID().toString();
    }

    private static ProjectionElem extractProjectElement(Projection topProjection) throws OntopUnsupportedInputQueryException {
        List<ProjectionElem> projectionElements = topProjection.getProjectionElemList().getElements();

        if (projectionElements.size() == 1)
            return projectionElements.get(0);

        throw new OntopUnsupportedInputQueryException("DESCRIBE queries with more than one term (variable or IRI) are not supported");
    }

    private static UnaryTupleOperator createNewProjection(ProjectionElem describeTerm, String p1, String o1, String s2, String p2,
                                                          boolean isFixedObjectIncludedInDescribe) {
        ProjectionElemList projection1 = new ProjectionElemList(describeTerm, new ProjectionElem(p1), new ProjectionElem(o1));

        if (isFixedObjectIncludedInDescribe) {
            ProjectionElemList projection2 = new ProjectionElemList(new ProjectionElem(s2), new ProjectionElem(p2), describeTerm);

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

    private static ParsedTupleQuery createSelectQuery(TupleExpr initialSubQuery, ProjectionElem describeProjectionElem, String p1,
                                                      String o1, String s2, String p2, boolean isFixedObjectIncludedInDescribe) {
        Join joinTree = new Join(
                transformSubQuery(initialSubQuery, describeProjectionElem),
                createSPPOUnion(describeProjectionElem, p1, o1, s2, p2, isFixedObjectIncludedInDescribe));

        return new ParsedTupleQuery(joinTree);
    }

    private static TupleExpr transformSubQuery(TupleExpr initialSubQuery, ProjectionElem describeProjectionElem) {
        return new Distinct(
                new Projection(initialSubQuery, new ProjectionElemList(describeProjectionElem)));
    }

    private static TupleExpr createSPPOUnion(ProjectionElem describeProjectionElem, String p1,
                                             String o1, String s2, String p2, boolean isFixedObjectIncludedInDescribe) {
        Var describeVariable = new Var(describeProjectionElem.getTargetName());
        StatementPattern leftStatement = new StatementPattern(describeVariable, new Var(p1), new Var(o1));

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
