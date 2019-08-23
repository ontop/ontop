package it.unibz.inf.ontop.substitution;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.IS_NOT_NULL;
import static org.junit.Assert.assertEquals;

public class SubstitutionTest {

    private static final Variable W = TERM_FACTORY.getVariable("w");
    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
    private static final Variable Z = TERM_FACTORY.getVariable("z");
    private static final Constant ONE = TERM_FACTORY.getConstantLiteral("1", TYPE_FACTORY.getXsdIntegerDatatype());

    @Test
    public void testOrientate1() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                        X,Y,
                        Z,Y
                );

        ImmutableSubstitution<ImmutableTerm> expectedSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                        Y,X,
                        Z,X
                );

        runTests(priorityVariables, initialSubstitution, expectedSubstitution);
    }

    @Test
    public void testOrientate2() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                        X,Z,
                        Y,Z);

        ImmutableSubstitution<ImmutableTerm> expectedSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                        Z,X,
                        Y,X);

        runTests(priorityVariables, initialSubstitution, expectedSubstitution);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOrientate3() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                        X,Y,
                        Y,Z);
//        ImmutableSubstitution<ImmutableTerm> expectedSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
//                ImmutableMap.of(
//                        Y,X,
//                        Z,X
//                ));

        runTestsWithExpectedRejection(priorityVariables, initialSubstitution);
    }

    @Test
    public void testOrientate4() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(
                        X,W,
                        Y,Z
                ));

        ImmutableSubstitution<ImmutableTerm> expectedSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(
                        W,X,
                        Z,Y
                ));

        runTests(priorityVariables, initialSubstitution, expectedSubstitution);
    }

    @Test
    public void testOrientate5() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(
                        X,W,
                        Z,Y
                ));

        ImmutableSubstitution<ImmutableTerm> expectedSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(
                        W,X,
                        Z,Y
                ));

        runTests(priorityVariables, initialSubstitution, expectedSubstitution);
    }


    @Test
    public void testOrientate6() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of();

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(
                        X,W,
                        Z,Y
                ));

        runTests(priorityVariables, initialSubstitution, initialSubstitution);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOrientate7() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(
                        X, Y,
                        Y, ONE
                ));

        runTestsWithExpectedRejection(priorityVariables, initialSubstitution);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOrientate8() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(
                        X, TERM_FACTORY.getImmutableFunctionalTerm(IS_NOT_NULL, Y),
                        Y, ONE
                ));

        runTestsWithExpectedRejection(priorityVariables, initialSubstitution);
    }


    private static <T extends ImmutableTerm> void runTests(ImmutableList<Variable> priorityVariables,
                                                           ImmutableSubstitution<T> initialSubstitution,
                                                           ImmutableSubstitution<T> expectedSubstitution) {
        System.out.println("Priority variables: " + priorityVariables + "\n");
        System.out.println("Initial substitution: " + initialSubstitution + "\n");
        System.out.println("Expected substitution: " + expectedSubstitution + "\n");

        ImmutableSubstitution<T> obtainedSubstitution = initialSubstitution.orientate(priorityVariables);
        System.out.println("Obtained substitution: " + obtainedSubstitution + "\n");

        assertEquals("Wrong substitution obtained",obtainedSubstitution, expectedSubstitution);
    }

    private static <T extends ImmutableTerm> void runTestsWithExpectedRejection(ImmutableList<Variable> priorityVariables,
                                                           ImmutableSubstitution<T> initialSubstitution) {
        System.out.println("Priority variables: " + priorityVariables + "\n");
        System.out.println("Initial substitution: " + initialSubstitution + "\n");

        initialSubstitution.orientate(priorityVariables);
    }

}
