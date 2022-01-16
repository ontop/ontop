package it.unibz.inf.ontop.substitution;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubstitutionTest {

    private static final Variable W = TERM_FACTORY.getVariable("w");
    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
    private static final Variable Z = TERM_FACTORY.getVariable("z");
    private static final Constant ONE = TERM_FACTORY.getDBConstant("1",
            TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

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

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(X,W, Y,Z);

        ImmutableSubstitution<ImmutableTerm> expectedSubstitution = SUBSTITUTION_FACTORY.getSubstitution(W,X, Z,Y);

        runTests(priorityVariables, initialSubstitution, expectedSubstitution);
    }

    @Test
    public void testOrientate5() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(X,W, Z,Y);

        ImmutableSubstitution<ImmutableTerm> expectedSubstitution = SUBSTITUTION_FACTORY.getSubstitution(W,X, Z,Y);

        runTests(priorityVariables, initialSubstitution, expectedSubstitution);
    }


    @Test
    public void testOrientate6() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of();

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(X,W, Z,Y);

        runTests(priorityVariables, initialSubstitution, initialSubstitution);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOrientate7() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(X, Y, Y, ONE);

        runTestsWithExpectedRejection(priorityVariables, initialSubstitution);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOrientate8() {
        ImmutableList<Variable> priorityVariables = ImmutableList.of(X, Y, Z);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBIsNotNull(Y), Y, ONE);

        runTestsWithExpectedRejection(priorityVariables, initialSubstitution);
    }

    @Test
    public void testUnify1() {
        Variable x = TERM_FACTORY.getVariable("x");
        Variable a = TERM_FACTORY.getVariable("a");
        Variable b = TERM_FACTORY.getVariable("b");
        Variable c = TERM_FACTORY.getVariable("c");

        ImmutableList<Template.Component> template = Template.of("http://example.org/", 0, "/", 1);

        ImmutableList<ImmutableTerm> firstArguments = ImmutableList.of(x, x);

        ImmutableList<ImmutableTerm> secondArguments = ImmutableList.of(
                TERM_FACTORY.getIRIFunctionalTerm(template, ImmutableList.of(a, a)),
                TERM_FACTORY.getIRIFunctionalTerm(template, ImmutableList.of(b, c)));

        checkUnification(firstArguments, secondArguments);
        checkUnification(firstArguments, secondArguments.reverse());
    }

    @Test
    public void testUnify2() {
        Variable x = TERM_FACTORY.getVariable("x");
        Variable a = TERM_FACTORY.getVariable("a");
        Variable b = TERM_FACTORY.getVariable("b");
        Variable c = TERM_FACTORY.getVariable("c");

        ImmutableList<Template.Component> template = Template.of("http://example.org/", 0, "/", 1);

        ImmutableList<ImmutableTerm> firstArguments = ImmutableList.of(x, x);

        ImmutableList<ImmutableTerm> secondArguments = ImmutableList.of(
                TERM_FACTORY.getIRIFunctionalTerm(template, ImmutableList.of(TERM_FACTORY.getDBUpper(a), TERM_FACTORY.getDBUpper(a))),
                TERM_FACTORY.getIRIFunctionalTerm(template, ImmutableList.of(TERM_FACTORY.getDBUpper(b), TERM_FACTORY.getDBUpper(c))));

        checkUnification(firstArguments, secondArguments);
        checkUnification(firstArguments, secondArguments.reverse());
    }

    private void checkUnification(ImmutableList<ImmutableTerm> firstArguments, ImmutableList<ImmutableTerm> secondArguments) {
        Optional<ImmutableSubstitution<ImmutableTerm>> optionalUnifier = UNIFICATION_TOOLS.computeMGU(firstArguments, secondArguments);
        assertTrue(optionalUnifier.isPresent());
        ImmutableSubstitution<ImmutableTerm> unifier = optionalUnifier.get();

        for(int i = 0; i < firstArguments.size(); i++) {
            assertEquals(unifier.apply(firstArguments.get(i)), unifier.apply(secondArguments.get(i)));
        }
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
