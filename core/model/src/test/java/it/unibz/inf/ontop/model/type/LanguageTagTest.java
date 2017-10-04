package it.unibz.inf.ontop.model.type;


import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.model.type.COL_TYPE.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LanguageTagTest {

    @Test
    public void testDifferentLanguageRegions() {
        TermType type1 = TYPE_FACTORY.getTermType("en-us");
        TermType type2 = TYPE_FACTORY.getTermType("en-gb");

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator, TYPE_FACTORY.getTermType("en"));
        assertNotEquals(commonDenominator, type1);
        assertNotEquals(commonDenominator, TYPE_FACTORY.getTermType("it"));
    }

    @Test
    public void testSameLanguageRegions() {
        TermType type1 = TYPE_FACTORY.getTermType("en-gb");
        TermType type2 = TYPE_FACTORY.getTermType("en-gb");

        TermType commonDenominator = type1.getCommonDenominator(type2);

        assertEquals(commonDenominator, TYPE_FACTORY.getTermType("en-gb"));
        assertNotEquals(commonDenominator, TYPE_FACTORY.getTermType("en"));
    }

    @Test
    public void testSameTag() {
        TermType type1 = TYPE_FACTORY.getTermType("en-gb");

        TermType commonDenominator = type1.getCommonDenominator(type1);

        assertEquals(commonDenominator, TYPE_FACTORY.getTermType("en-gb"));
        assertEquals(commonDenominator, type1);
    }

    @Test
    public void testDifferentLanguagesWithRegionalTag() {
        TermType type1 = TYPE_FACTORY.getTermType("en-us");
        TermType type2 = TYPE_FACTORY.getTermType("fr-be");

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator.getColType(), STRING);
    }

    @Test
    public void testDifferentLanguages() {
        TermType type1 = TYPE_FACTORY.getTermType("en");
        TermType type2 = TYPE_FACTORY.getTermType("fr");

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator.getColType(), STRING);
    }

    @Test
    public void testLanguageString() {
        TermType type1 = TYPE_FACTORY.getTermType("en");
        TermType type2 = TYPE_FACTORY.getTermType(STRING);

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(STRING, commonDenominator.getColType());
    }

    @Test
    public void testLanguageAndLiteral() {
        TermType type1 = TYPE_FACTORY.getTermType("en");
        TermType type2 = TYPE_FACTORY.getTermType(LITERAL);

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Ignore("TODO: compare it to RDFTerm")
    @Test
    public void testLanguageAndURI() {
        TermType type1 = TYPE_FACTORY.getTermType("en");
        TermType type2 = TYPE_FACTORY.getTermType(OBJECT);

        TermType commonDenominator = type1.getCommonDenominator(type2);
        // TODO: compare it to RDFTerm
        //assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Test
    public void testString() {
        TermType type1 = TYPE_FACTORY.getTermType(STRING);
        TermType type2 = TYPE_FACTORY.getTermType(STRING);

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator.getColType(), STRING);
        assertEquals(commonDenominator, type1);
    }

    @Test
    public void testStringAndLiteral() {
        TermType type1 = TYPE_FACTORY.getTermType(STRING);
        TermType type2 = TYPE_FACTORY.getTermType(LITERAL);

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Test
    public void testLiterals() {
        TermType type1 = TYPE_FACTORY.getTermType(LITERAL);
        TermType type2 = TYPE_FACTORY.getTermType(LITERAL);

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Test
    public void testLangNumber() {
        TermType type1 = TYPE_FACTORY.getTermType("en");
        TermType type2 = TYPE_FACTORY.getTermType(DECIMAL);

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

}
