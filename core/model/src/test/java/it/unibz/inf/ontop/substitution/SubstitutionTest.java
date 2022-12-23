package it.unibz.inf.ontop.substitution;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubstitutionTest {

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
}
