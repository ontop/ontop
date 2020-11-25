package it.unibz.inf.ontop.spec.mapping.parser;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.parser.impl.TemplateComponent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TemplateComponentTest {

    @Test
    public void test_simple_string() {
        assertEquals(ImmutableList.of(new TemplateComponent(false, "template")),
                TemplateComponent.getComponents("template"));
    }

    @Test
    public void test_single_column() {
        assertEquals(ImmutableList.of(new TemplateComponent(true, "template")),
                TemplateComponent.getComponents("{template}"));
    }

    @Test
    public void test_single_string_column() {
        assertEquals(ImmutableList.of(
                new TemplateComponent(false, "fish"),
                new TemplateComponent(true, "template")),
                TemplateComponent.getComponents("fish{template}"));
    }

    @Test
    public void test_single_column_string() {
        assertEquals(ImmutableList.of(
                new TemplateComponent(true, "template"),
                new TemplateComponent(false, "fish")),
                TemplateComponent.getComponents("{template}fish"));
    }

    @Test
    public void test_two_columns() {
        assertEquals(ImmutableList.of(
                new TemplateComponent(true, "template"),
                new TemplateComponent(true, "fish")),
                TemplateComponent.getComponents("{template}{fish}"));
    }

    @Test
    public void test_column_string_column() {
        assertEquals(ImmutableList.of(
                new TemplateComponent(true, "template"),
                new TemplateComponent(false, "fish"),
                new TemplateComponent(true, "carp")),
                TemplateComponent.getComponents("{template}fish{carp}"));
    }

    @Test
    public void test_string_column_string() {
        assertEquals(ImmutableList.of(
                new TemplateComponent(false, "template"),
                new TemplateComponent(true, "fish"),
                new TemplateComponent(false, "carp")),
                TemplateComponent.getComponents("template{fish}carp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_empty_column() {
        TemplateComponent.getComponents("template{}carp");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_no_closing_bracket() {
        TemplateComponent.getComponents("template{fish}{carp");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_nested_brackets() {
        TemplateComponent.getComponents("{template{fish}carp}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_no_opening_bracket() {
        TemplateComponent.getComponents("template}{fish}{carp");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_incomplete_escape() {
        TemplateComponent.getComponents("template{fish}carp\\");
    }

    @Test
    public void test_simple_string_with_escape_backslash() {
        assertEquals(ImmutableList.of(new TemplateComponent(false, "temp\\\\late")),
                TemplateComponent.getComponents("temp\\\\late"));
    }

    @Test
    public void test_simple_string_with_escape_backslash_and_other_escape() {
        assertEquals(ImmutableList.of(new TemplateComponent(false, "temp\\\\\\nlate")),
                TemplateComponent.getComponents("temp\\\\\\nlate"));
    }

    @Test
    public void test_simple_string_with_escape_opening_bracket() {
        assertEquals(ImmutableList.of(new TemplateComponent(false, "temp\\{late")),
                TemplateComponent.getComponents("temp\\{late"));
    }

    @Test
    public void test_simple_string_with_escape_closing_bracket() {
        assertEquals(ImmutableList.of(new TemplateComponent(false, "temp\\}late")),
                TemplateComponent.getComponents("temp\\}late"));
    }
}
