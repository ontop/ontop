package it.unibz.inf.ontop.pivotalrepr.datalog;


import com.google.common.collect.ImmutableList;
import fj.P2;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.pivotalrepr.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.pivotalrepr.mapping.TargetAtom;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.pivotalrepr.datalog.impl.DatalogConversionTools.convertFromDatalogDataAtom;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: enrich it
 */
public class Datalog2IQConversionTest {

    private static final Variable X = DATA_FACTORY.getVariable("x");
    private static final Variable Y = DATA_FACTORY.getVariable("y");
    private static final Constant TWO = DATA_FACTORY.getConstantLiteral("2", INTEGER);
    private static final Function URI_TEMPLATE = DATA_FACTORY.getUriTemplate(
            DATA_FACTORY.getConstantLiteral("http://example.org/"),
            DATA_FACTORY.getVariable("z"));

    @Test
    public void testHeadConversion() throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
        AtomPredicate predicate = new AtomPredicateImpl("ans", 5);

        Function datalogHead = DATA_FACTORY.getFunction(predicate,X,X,TWO, Y, URI_TEMPLATE);

        TargetAtom targetAtom = convertFromDatalogDataAtom(datalogHead);

        DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();
        ImmutableSubstitution<ImmutableTerm> bindings = targetAtom.getSubstitution();

        assertEquals(projectionAtom.getPredicate(), predicate);

        ImmutableList<Variable> projectedArguments = projectionAtom.getArguments();
        assertEquals(projectedArguments.size(), datalogHead.getTerms().size());

        assertEquals(projectedArguments.get(0), X);

        Variable secondVariable = projectedArguments.get(1);
        assertNotEquals(secondVariable, X);

        assertTrue(bindings.isDefining(secondVariable));
        assertEquals(bindings.get(secondVariable), X);

        Variable thirdVariable = projectedArguments.get(2);
        assertTrue(bindings.isDefining(thirdVariable));
        assertEquals(bindings.get(thirdVariable), TWO);

        assertEquals(projectedArguments.get(3), Y);

        Variable fifthVariable = projectedArguments.get(4);
        assertTrue(bindings.isDefining(fifthVariable));
        assertEquals(bindings.get(fifthVariable), URI_TEMPLATE);
    }
}
