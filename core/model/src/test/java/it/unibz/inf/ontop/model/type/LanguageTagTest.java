package it.unibz.inf.ontop.model.type;


import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE.*;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LanguageTagTest {

    @Test
    public void testDifferentLanguageRegions() {
        TermType type1 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en-us"));
        TermType type2 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en-gb"));

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        assertEquals(optionalCommonDenominator.get(), TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en")));
        assertNotEquals(optionalCommonDenominator.get(), type1);
        assertNotEquals(optionalCommonDenominator.get(), TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("it")));
    }

    @Test
    public void testSameLanguageRegions() {
        TermType type1 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en-gb"));
        TermType type2 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en-gb"));

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        assertEquals(optionalCommonDenominator.get(), TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en-gb")));
        assertNotEquals(optionalCommonDenominator.get(), TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en")));
    }

    @Test
    public void testSameTag() {
        TermType type1 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en-gb"));

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type1);
        assertTrue(optionalCommonDenominator.isPresent());

        assertEquals(optionalCommonDenominator.get(), TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en-gb")));
        assertEquals(optionalCommonDenominator.get(), type1);
    }

    @Test
    public void testDifferentLanguagesWithRegionalTag() {
        TermType type1 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en-us"));
        TermType type2 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("fr-be"));

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        TermType commonDenominator = optionalCommonDenominator.get();
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Test
    public void testDifferentLanguages() {
        TermType type1 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en"));
        TermType type2 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("fr"));

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        TermType commonDenominator = optionalCommonDenominator.get();
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Test
    public void testLanguageString() {
        TermType type1 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en"));
        TermType type2 = TYPE_FACTORY.getTermType(STRING);

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        TermType commonDenominator = optionalCommonDenominator.get();
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Test
    public void testLanguageAndLiteral() {
        TermType type1 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en"));
        TermType type2 = TYPE_FACTORY.getTermType(LITERAL);

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        TermType commonDenominator = optionalCommonDenominator.get();
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Test
    public void testLanguageAndURI() {
        TermType type1 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en"));
        TermType type2 = TYPE_FACTORY.getTermType(OBJECT);

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertFalse(optionalCommonDenominator.isPresent());
    }

    @Test
    public void testString() {
        TermType type1 = TYPE_FACTORY.getTermType(STRING);
        TermType type2 = TYPE_FACTORY.getTermType(STRING);

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        TermType commonDenominator = optionalCommonDenominator.get();
        assertEquals(commonDenominator.getColType(), STRING);
        assertEquals(commonDenominator, type1);
    }

    @Test
    public void testStringAndLiteral() {
        TermType type1 = TYPE_FACTORY.getTermType(STRING);
        TermType type2 = TYPE_FACTORY.getTermType(LITERAL);

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        TermType commonDenominator = optionalCommonDenominator.get();
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Test
    public void testLiterals() {
        TermType type1 = TYPE_FACTORY.getTermType(LITERAL);
        TermType type2 = TYPE_FACTORY.getTermType(LITERAL);

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        TermType commonDenominator = optionalCommonDenominator.get();
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

    @Test
    public void testLangNumber() {
        TermType type1 = TYPE_FACTORY.getTermType(TERM_FACTORY.getConstantLiteral("en"));
        TermType type2 = TYPE_FACTORY.getTermType(DECIMAL);

        Optional<TermType> optionalCommonDenominator = type1.getCommonDenominator(type2);
        assertTrue(optionalCommonDenominator.isPresent());

        TermType commonDenominator = optionalCommonDenominator.get();
        assertEquals(commonDenominator.getColType(), LITERAL);
    }

}
