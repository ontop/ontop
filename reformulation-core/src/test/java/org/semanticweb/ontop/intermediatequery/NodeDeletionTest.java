package org.semanticweb.ontop.intermediatequery;

import com.google.common.base.Optional;
import org.junit.Test;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.AtomPredicateImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.optimization.BasicJoinOptimizer;
import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.ConstructionNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.TableNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;

/**
 * TODO: test
 */
public class NodeDeletionTest {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();


    @Test(expected = EmptyQueryException.class)
    public void testSimpleJoin() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        ConstructionNode rootNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans1", 1), x));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder();
        queryBuilder.init(rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableBooleanExpression falseCondition = DATA_FACTORY.getImmutableBooleanExpression(OBDAVocabulary.AND, falseValue, falseValue);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(falseCondition));
        queryBuilder.addChild(rootNode, joinNode);

        TableNode table1 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table1", 1), x));
        queryBuilder.addChild(joinNode, table1);

        TableNode table2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table2", 1), x));
        queryBuilder.addChild(joinNode, table2);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        IntermediateQueryOptimizer joinOptimizer = new BasicJoinOptimizer();

        /**
         * Should throw the EmptyQueryException
         */
        IntermediateQuery optimizedQuery = joinOptimizer.optimize(initialQuery);
        System.err.println("Optimized query (should have been rejected): " + optimizedQuery.toString());
    }
}
