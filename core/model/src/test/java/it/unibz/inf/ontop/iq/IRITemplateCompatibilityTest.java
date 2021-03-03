package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.stream.IntStream;

import static it.unibz.inf.ontop.OntopModelTestingTools.CORE_UTILS_FACTORY;
import static it.unibz.inf.ontop.OntopModelTestingTools.TERM_FACTORY;
import static org.junit.Assert.*;

public class IRITemplateCompatibilityTest {

    @Test
    public void test1() {
        assertTrue(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/").addColumn().build(), 2,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/2").build(), 1));
    }

    @Test
    public void test2() {
        assertFalse(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().build(), 1,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/2").build(), 1));
    }

    @Test
    public void test3() {
        assertTrue(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().build(), 1,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("2").build(), 1));
    }

    @Test
    public void test4() {
        assertFalse(areCompatible(Template.builder().addSeparator("http://example.org/car/").addColumn().build(), 1,
                Template.builder().addSeparator("http://example.org/person/").addColumn().build(), 1));
    }

    @Test
    public void test5() {
        assertTrue(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/address/").addColumn().build(), 2,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/").addColumn().addSeparator("/").addColumn().build(), 3));
    }

    @Test
    public void test6() {
        assertTrue(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/address/").addColumn().build(), 2,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/").addColumn().addSeparator("/45").build(), 2));
    }

    @Test
    public void test7() {
        assertFalse(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/address/").addColumn().build(), 2,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/position/").addColumn().build(), 2));
    }

    @Test
    public void test8Same() {
        assertTrue(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/address/").addColumn().build(), 2,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/address/").addColumn().build(), 2));
    }

    @Test
    public void test8splitbug() {
        assertFalse(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("//").build(), 1,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/ / ").addColumn().build(), 2));
    }

    @Test
    public void test9() {
        assertTrue(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().build(), 1,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addColumn().build(), 2));
    }

    @Test
    public void test10() {
        assertFalse(areCompatible(Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/address/").addColumn().build(), 2,
                Template.builder().addSeparator("http://example.org/person/").addColumn().addSeparator("/address+").addColumn().build(), 2));
    }

    @Test
    public void test11() {
        assertTrue(areCompatible(Template.builder().addColumn().addColumn().build(), 2,
                Template.builder().addColumn().build(), 1));
    }

    @Test
    public void test12() {
        assertFalse(areCompatible(Template.builder().addColumn().addSeparator("/").addColumn().build(), 2,
                Template.builder().addColumn().build(), 1));
    }

    @Test
    public void test13() {
        assertFalse(areCompatible(Template.builder().addSeparator("A").addColumn().addSeparator("/").addColumn().addSeparator("A").build(), 2,
                Template.builder().addSeparator("A").addColumn().addSeparator("/").addColumn().addSeparator("B").build(), 2));
    }

    @Test
    public void test14() {
        assertFalse(areCompatible(Template.builder().addSeparator("A").addColumn().addSeparator("/").addColumn().addSeparator("A").build(), 2,
                Template.builder().addSeparator("A").addColumn().addSeparator("/").addColumn().addSeparator("BAB").build(), 2));
    }

    @Test
    public void test15() {
        assertTrue(areCompatible(Template.builder().addSeparator("A").addColumn().addSeparator("/").addColumn().addSeparator("AB").build(), 2,
                Template.builder().addSeparator("A").addColumn().addSeparator("/").addColumn().addSeparator("BAB").build(), 2));
    }

    @Test
    public void test16() {
        assertTrue(areCompatible(
                Template.builder().addSeparator("http://example.org/level").addColumn().addSeparator("/").addColumn().build(), 2,
                Template.builder().addSeparator("http://example.org/level0/").addColumn().build(), 1));
    }

    private boolean areCompatible(ImmutableList<Template.Component> template1, int arity1, ImmutableList<Template.Component> template2, int arity2) {
        ImmutableFunctionalTerm term1 = createIRIFunctionalTerm(template1, arity1, "x");
        ImmutableFunctionalTerm term2 = createIRIFunctionalTerm(template2, arity2, "y");

        ImmutableExpression condition1 = TERM_FACTORY.getStrictEquality(term1, term2);
        ImmutableExpression condition2 = TERM_FACTORY.getStrictEquality(term2, term1);
        VariableNullability variableNullability = CORE_UTILS_FACTORY.createSimplifiedVariableNullability(condition1);

        boolean res1 = !condition1.evaluate2VL(variableNullability).isEffectiveFalse();
        boolean res2 = !condition2.evaluate2VL(variableNullability).isEffectiveFalse();

        assertEquals("Symmetry issue: ", res1, res2);
        return res1;
    }

    private ImmutableFunctionalTerm createIRIFunctionalTerm(ImmutableList<Template.Component> template, int arity, String variablePrefix) {
        ImmutableList<Variable> arguments = IntStream.range(0, arity)
                .mapToObj(i -> TERM_FACTORY.getVariable(variablePrefix + i))
                .collect(ImmutableCollectors.toList());

        return TERM_FACTORY.getIRIFunctionalTerm(template, arguments);
    }

}
