package it.unibz.inf.ontop.model.template;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.OntopModelTestingTools;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class TemplateComponentTest {

    private final TemplateFactory factory = new IRITemplateFactory(OntopModelTestingTools.TERM_FACTORY);

    @Test
    public void test_equality() {
        assertEquals(new TemplateComponent(0, "template"),
                        new TemplateComponent(0, "template22"));
    }

    @Test
    public void test_inequality() {
        assertNotEquals(new TemplateComponent(0, "template"),
                new TemplateComponent(1, "template"));
    }

    @Test
    public void test_hashcode() {
        assertEquals(ImmutableSet.of(new TemplateComponent(0, "template")),
                ImmutableSet.of(new TemplateComponent(0, "template"),
                        new TemplateComponent(0, "template22")));
    }

    @Test
    public void test_simple_string() {
        assertEquals(ImmutableList.of(new TemplateComponent("template")),
                factory.getComponents("template"));
    }

    @Test
    public void test_single_column() {
        assertEquals(ImmutableList.of(new TemplateComponent(0, "template")),
                factory.getComponents("{template}"));
    }

    @Test
    public void test_single_string_column() {
        assertEquals(ImmutableList.of(
                new TemplateComponent( "fish"),
                new TemplateComponent(0, "template")),
                factory.getComponents("fish{template}"));
    }

    @Test
    public void test_single_column_string() {
        assertEquals(ImmutableList.of(
                new TemplateComponent(0, "template"),
                new TemplateComponent("fish")),
                factory.getComponents("{template}fish"));
    }

    @Test
    public void test_two_columns() {
        assertEquals(ImmutableList.of(
                new TemplateComponent(0, "template"),
                new TemplateComponent(1, "fish")),
                factory.getComponents("{template}{fish}"));
    }

    @Test
    public void test_column_string_column() {
        assertEquals(ImmutableList.of(
                new TemplateComponent(0, "template"),
                new TemplateComponent("fish"),
                new TemplateComponent(1, "carp")),
                factory.getComponents("{template}fish{carp}"));
    }

    @Test
    public void test_string_column_string() {
        assertEquals(ImmutableList.of(
                new TemplateComponent("template"),
                new TemplateComponent(0, "fish"),
                new TemplateComponent("carp")),
                factory.getComponents("template{fish}carp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_empty_column() {
        factory.getComponents("template{}carp");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_no_closing_bracket() {
        factory.getComponents("template{fish}{carp");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_nested_brackets() {
        factory.getComponents("{template{fish}carp}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_no_opening_bracket() {
        factory.getComponents("template}{fish}{carp");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_incomplete_escape() {
        factory.getComponents("template{fish}carp\\");
    }

    @Test
    public void test_simple_string_with_escape_backslash() {
        assertEquals(ImmutableList.of(new TemplateComponent("temp\\late")),
                factory.getComponents("temp\\\\late"));
    }

    @Test
    public void test_simple_string_with_escape_backslash_and_other_escape() {
        assertEquals(ImmutableList.of(new TemplateComponent("temp\\\\nlate")),
                factory.getComponents("temp\\\\\\nlate"));
    }

    @Test
    public void test_simple_string_with_escape_opening_bracket() {
        assertEquals(ImmutableList.of(new TemplateComponent("temp{late")),
                factory.getComponents("temp\\{late"));
    }

    @Test
    public void test_simple_string_with_escape_closing_bracket() {
        assertEquals(ImmutableList.of(new TemplateComponent("temp}late")),
                factory.getComponents("temp\\}late"));
    }
}
