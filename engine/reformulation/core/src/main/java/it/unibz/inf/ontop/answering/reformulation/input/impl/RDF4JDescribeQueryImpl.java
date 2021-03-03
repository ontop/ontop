package it.unibz.inf.ontop.answering.reformulation.input.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructQuery;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JDescribeQuery;
import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.answering.reformulation.input.translation.InputQueryTranslator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.algebra.*;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

import java.util.Optional;


class RDF4JDescribeQueryImpl implements RDF4JDescribeQuery {

    private static final String DESCRIBE_VARIABLE = "d";
    private static final String P1 = "p1";
    private static final String O1 = "o1";
    private static final String S2 = "s2";
    private static final String P2 = "p2";

    private final ParsedQuery originalParsedQuery;

    private final String queryString;
    private final BindingSet bindings;

    // True if the pattern "?s ?p <describedIRI>" should also be considered while answering a DESCRIBE query.
    private final boolean isFixedObjectIncludedInDescribe;

    RDF4JDescribeQueryImpl(ParsedQuery originalParsedQuery, String queryString, BindingSet bindings,
                           boolean isFixedObjectIncludedInDescribe) {
        this.originalParsedQuery = originalParsedQuery;
        this.queryString = queryString;
        this.bindings = bindings;
        this.isFixedObjectIncludedInDescribe = isFixedObjectIncludedInDescribe;
    }

    @Override
    public RDF4JDescribeQuery newBindings(BindingSet newBindings) {
        return new RDF4JDescribeQueryImpl(originalParsedQuery, getInputString(), newBindings, isFixedObjectIncludedInDescribe);
    }

    @Override
    public String getInputString() {
        return queryString;
    }

    @Override
    public IQ translate(InputQueryTranslator translator) throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {
        throw new UnsupportedOperationException("DESCRIBE queries cannot be translated in one step.");
    }

    @Override
    public SelectQuery getSelectQuery() {
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

        return new RDF4JSelectQueryImpl(new ParsedTupleQuery(projection), "# SELECT QUERY OF THE FOLLOWING: \n"
                + queryString, bindings);
    }

    @Override
    public ImmutableCollection<ConstructQuery> computeConstructQueries(ImmutableSet<IRI> resourcesToDescribe) {
        ConstructQuerySplit split = createConstructionQuerySplit(isFixedObjectIncludedInDescribe);
        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

        return resourcesToDescribe.stream()
                .map(d -> new RDF4JConstructQueryImpl(split, "# Construct for describing " + d,
                        new ListBindingSet(ImmutableList.of(DESCRIBE_VARIABLE), valueFactory.createIRI(d.getIRIString()))))
                .collect(ImmutableCollectors.toList());
    }

    private static ConstructQuerySplit createConstructionQuerySplit(boolean isFixedObjectIncludedInDescribe) {
        ParsedTupleQuery selectQuery = new ParsedTupleQuery(createSPPOUnion(isFixedObjectIncludedInDescribe));

        UnaryTupleOperator newProjection = createNewProjection(isFixedObjectIncludedInDescribe);
        ConstructTemplate constructTemplate = new RDF4JConstructTemplate(newProjection, null);

        return new ConstructQuerySplit(constructTemplate, selectQuery);
    }

    private static UnaryTupleOperator createNewProjection(boolean isFixedObjectIncludedInDescribe) {
        ProjectionElem describeProjectElem = new ProjectionElem(DESCRIBE_VARIABLE);
        ProjectionElemList projection1 = new ProjectionElemList(describeProjectElem, new ProjectionElem(P1), new ProjectionElem(O1));

        if (isFixedObjectIncludedInDescribe) {
            ProjectionElemList projection2 = new ProjectionElemList(new ProjectionElem(S2), new ProjectionElem(P2), describeProjectElem);

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

    private static TupleExpr createSPPOUnion(boolean isFixedObjectIncludedInDescribe) {
        Var describeVariable = new Var(DESCRIBE_VARIABLE);
        StatementPattern leftStatement = new StatementPattern(describeVariable, new Var(P1), new Var(O1));

        ProjectionElem describeProjectionElem = new ProjectionElem(DESCRIBE_VARIABLE);

        Projection left = new Projection(leftStatement,
                new ProjectionElemList(describeProjectionElem, new ProjectionElem(P1), new ProjectionElem(O1)));

        if (isFixedObjectIncludedInDescribe) {
            StatementPattern rightStatement = new StatementPattern(new Var(S2), new Var(P2), describeVariable);
            Projection right = new Projection(rightStatement,
                    new ProjectionElemList(new ProjectionElem(S2), new ProjectionElem(P2), describeProjectionElem));

            return new Union(left, right);
        }
        else
            return left;
    }
}
