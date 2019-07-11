package it.unibz.inf.ontop.datalog;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: enrich it
 */
public class Datalog2IQConversionTest {

    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
    private static final Constant TWO = TERM_FACTORY.getDBConstant("2",
            TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

    @Test
    public void testHeadConversion() throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
        AtomPredicate predicate = ATOM_FACTORY.getRDFAnswerPredicate(5);

        Function uriTemplate = (Function) IMMUTABILITY_TOOLS.convertToMutableTerm(
                TERM_FACTORY.getIRIFunctionalTerm("http://example.org/{}",
                ImmutableList.of(TERM_FACTORY.getVariable("z"))));

        Function datalogHead = TERM_FACTORY.getFunction(predicate,X,X,TWO, Y, uriTemplate);

        TargetAtom targetAtom = ((DatalogProgram2QueryConverterImpl)DATALOG_PROGRAM_2_QUERY_CONVERTER).convertFromDatalogDataAtom(datalogHead);

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
        assertEquals(IMMUTABILITY_TOOLS.convertToMutableTerm(bindings.get(fifthVariable)), uriTemplate);
    }
}
