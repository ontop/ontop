package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.DummyVariableNullability;
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
        assertTrue(areCompatible("http://example.org/person/{}/{}", 2,
                "http://example.org/person/{}/2", 1));
    }

    @Test
    public void test2() {
        assertFalse(areCompatible("http://example.org/person/{}", 1,
                "http://example.org/person/{}/2", 1));
    }

    @Test
    public void test3() {
        assertTrue(areCompatible("http://example.org/person/{}", 1,
                "http://example.org/person/{}2", 1));
    }

    @Test
    public void test4() {
        assertFalse(areCompatible("http://example.org/car/{}", 1,
                "http://example.org/person/{}", 1));
    }

    @Test
    public void test5() {
        assertTrue(areCompatible("http://example.org/person/{}/address/{}", 2,
                "http://example.org/person/{}/{}/{}", 3));
    }

    @Test
    public void test6() {
        assertTrue(areCompatible("http://example.org/person/{}/address/{}", 2,
                "http://example.org/person/{}/{}/45", 2));
    }

    @Test
    public void test7() {
        assertFalse(areCompatible("http://example.org/person/{}/address/{}", 2,
                "http://example.org/person/{}/position/{}", 2));
    }

    @Test
    public void test8Same() {
        assertTrue(areCompatible("http://example.org/person/{}/address/{}", 2,
                "http://example.org/person/{}/address/{}", 2));
    }

    @Test
    public void test9() {
        assertTrue(areCompatible("http://example.org/person/{}", 1,
                "http://example.org/person/{}{}", 2));
    }

    @Test
    public void test10() {
        assertFalse(areCompatible("http://example.org/person/{}/address/{}", 2,
                "http://example.org/person/{}/address+{}", 2));
    }

    private boolean areCompatible(String template1, int arity1, String template2, int arity2) {
        ImmutableFunctionalTerm term1 = createIRIFunctionalTerm(template1, arity1, "x");
        ImmutableFunctionalTerm term2 = createIRIFunctionalTerm(template2, arity2, "y");

        ImmutableExpression condition1 = TERM_FACTORY.getStrictEquality(term1, term2);
        ImmutableExpression condition2 = TERM_FACTORY.getStrictEquality(term2, term1);
        DummyVariableNullability variableNullability = CORE_UTILS_FACTORY.createDummyVariableNullability(condition1);

        boolean res1 = !condition1.evaluate2VL(variableNullability).isEffectiveFalse();
        boolean res2 = !condition2.evaluate2VL(variableNullability).isEffectiveFalse();

        assertEquals("Symmetry issue: ", res1, res2);
        return res1;
    }

    private ImmutableFunctionalTerm createIRIFunctionalTerm(String template, int arity, String variablePrefix) {
        ImmutableList<Variable> arguments = IntStream.range(0, arity)
                .boxed()
                .map(i -> TERM_FACTORY.getVariable(variablePrefix + i))
                .collect(ImmutableCollectors.toList());

        return TERM_FACTORY.getIRIFunctionalTerm(template, arguments);
    }

}
