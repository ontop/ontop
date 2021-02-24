package it.unibz.inf.ontop.model.template;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.OntopModelTestingTools;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import it.unibz.inf.ontop.model.template.impl.TemplateParser;
import org.junit.Test;

import static org.junit.Assert.*;

public class TemplateComponentTest {

    @Test
    public void test_equality() {
        assertEquals(new Template.Component(0, "template"),
                        new Template.Component(0, "template22"));
    }

    @Test
    public void test_inequality() {
        assertNotEquals(new Template.Component(0, "template"),
                new Template.Component(1, "template"));
    }

    @Test
    public void test_hashcode() {
        assertEquals(ImmutableSet.of(new Template.Component(0, "template")),
                ImmutableSet.of(new Template.Component(0, "template"),
                        new Template.Component(0, "template22")));
    }

    @Test
    public void test_simple_string() {
        assertEquals(Template.builder().addSeparator("template").build(),
                TemplateParser.getComponents("template", false));
    }

    @Test
    public void test_single_column() {
        assertEquals(ImmutableList.of(new Template.Component(0, "template")),
                TemplateParser.getComponents("{template}", false));
    }

    @Test
    public void test_single_string_column() {
        assertEquals(ImmutableList.of(
                new Template.Component( "fish"),
                new Template.Component(0, "template")),
                TemplateParser.getComponents("fish{template}", false));
    }

    @Test
    public void test_single_column_string() {
        assertEquals(ImmutableList.of(
                new Template.Component(0, "template"),
                new Template.Component("fish")),
                TemplateParser.getComponents("{template}fish", false));
    }

    @Test
    public void test_two_columns() {
        assertEquals(ImmutableList.of(
                new Template.Component(0, "template"),
                new Template.Component(1, "fish")),
                TemplateParser.getComponents("{template}{fish}", false));
    }

    @Test
    public void test_column_string_column() {
        assertEquals(ImmutableList.of(
                new Template.Component(0, "template"),
                new Template.Component("fish"),
                new Template.Component(1, "carp")),
                TemplateParser.getComponents("{template}fish{carp}", false));
    }

    @Test
    public void test_string_column_string() {
        assertEquals(ImmutableList.of(
                new Template.Component("template"),
                new Template.Component(0, "fish"),
                new Template.Component("carp")),
                TemplateParser.getComponents("template{fish}carp", false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_empty_column() {
        TemplateParser.getComponents("template{}carp", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_no_closing_bracket() {
        TemplateParser.getComponents("template{fish}{carp", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_nested_brackets() {
        TemplateParser.getComponents("{template{fish}carp}", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_no_opening_bracket() {
        TemplateParser.getComponents("template}{fish}{carp", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_incomplete_escape() {
        TemplateParser.getComponents("template{fish}carp\\", false);
    }

    @Test
    public void test_simple_string_with_escape_backslash() {
        assertEquals(ImmutableList.of(new Template.Component("temp\\late")),
                TemplateParser.getComponents("temp\\\\late", false));
    }

    @Test
    public void test_simple_string_with_escape_backslash_and_other_escape() {
        assertEquals(ImmutableList.of(new Template.Component("temp\\\\nlate")),
                TemplateParser.getComponents("temp\\\\\\nlate", false));
    }

    @Test
    public void test_simple_string_with_escape_opening_bracket() {
        assertEquals(ImmutableList.of(new Template.Component("temp{late")),
                TemplateParser.getComponents("temp\\{late", false));
    }

    @Test
    public void test_simple_string_with_escape_closing_bracket() {
        assertEquals(ImmutableList.of(new Template.Component("temp}late")),
                TemplateParser.getComponents("temp\\}late", false));
    }
}
