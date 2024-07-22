package it.unibz.inf.ontop.model.template;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.template.impl.TemplateParser;
import org.junit.Test;

import static org.junit.Assert.*;

public class TemplateComponentTest {

    @Test
    public void test_equality() {
        assertEquals(Template.builder().column("template").build(),
                Template.builder().column("template22").build());
    }

    @Test
    public void test_inequality() {
        assertNotEquals(Template.builder().column("template").build().get(0),
                Template.builder().column("a").column("template").build().get(1));
    }

    @Test
    public void test_hashcode() {
        assertEquals(ImmutableSet.of(Template.builder().column("template").build().get(0)),
                ImmutableSet.of(Template.builder().column("template").build().get(0),
                        Template.builder().column("template22").build().get(0)));
    }

    @Test
    public void test_simple_string() {
        assertEquals(Template.builder().string("template").build(),
                TemplateParser.getComponents("template", false));
    }

    @Test
    public void test_single_column() {
        assertEquals(Template.builder().column("template").build(),
                TemplateParser.getComponents("{template}", false));
    }

    @Test
    public void test_single_string_column() {
        assertEquals(Template.builder().string("fish").column("template").build(),
                TemplateParser.getComponents("fish{template}", false));
    }

    @Test
    public void test_single_column_string() {
        assertEquals(Template.builder().column("template").string("fish").build(),
                TemplateParser.getComponents("{template}fish", false));
    }

    @Test
    public void test_two_columns() {
        assertEquals(Template.builder().column("template").column("fish").build(),
                TemplateParser.getComponents("{template}{fish}", false));
    }

    @Test
    public void test_column_string_column() {
        assertEquals(Template.builder().column("template").string("fish").column("carp").build(),
                TemplateParser.getComponents("{template}fish{carp}", false));
    }

    @Test
    public void test_string_column_string() {
        assertEquals(Template.builder().string("template").column("fish").string("carp").build(),
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
        assertEquals(Template.builder().string("temp\\late").build(),
                TemplateParser.getComponents("temp\\\\late", false));
    }

    @Test
    public void test_simple_string_with_escape_backslash_and_other_escape() {
        assertEquals(Template.builder().string("temp\\\\nlate").build(),
                TemplateParser.getComponents("temp\\\\\\nlate", false));
    }

    @Test
    public void test_simple_string_with_escape_opening_bracket() {
        assertEquals(Template.builder().string("temp{late").build(),
                TemplateParser.getComponents("temp\\{late", false));
    }

    @Test
    public void test_simple_string_with_escape_closing_bracket() {
        assertEquals(Template.builder().string("temp}late").build(),
                TemplateParser.getComponents("temp\\}late", false));
    }
}
