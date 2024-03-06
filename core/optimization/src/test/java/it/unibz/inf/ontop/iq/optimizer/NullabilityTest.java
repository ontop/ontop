package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import static it.unibz.inf.ontop.DependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertTrue;

public class NullabilityTest {


    @Test
    public void testNullabilityComplexSubstitution1() {
        var dataNode = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR2, ImmutableMap.of(0, A));
        var constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(
                                TERM_FACTORY.getDBStringConstant("0"),
                                TERM_FACTORY.getDBUpper(A)
                        )),
                        Y, TERM_FACTORY.getNullConstant()
                ));

        var iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode);

        var nullability = iqTree.getVariableNullability();

        assertTrue(nullability.isPossiblyNullable(Y));

    }
}
