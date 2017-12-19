package it.unibz.inf.ontop.model.type;


import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OntopModelTestingTools.TYPE_FACTORY;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LanguageTagTest {

    @Test
    public void testDifferentLanguageRegions() {
        TermType type1 = TYPE_FACTORY.getLangTermType("en-us");
        TermType type2 = TYPE_FACTORY.getLangTermType("en-gb");

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator, TYPE_FACTORY.getLangTermType("en"));
        assertNotEquals(commonDenominator, type1);
        assertNotEquals(commonDenominator, TYPE_FACTORY.getLangTermType("it"));
    }

    @Test
    public void testSameLanguageRegions() {
        TermType type1 = TYPE_FACTORY.getLangTermType("en-gb");
        TermType type2 = TYPE_FACTORY.getLangTermType("en-gb");

        TermType commonDenominator = type1.getCommonDenominator(type2);

        assertEquals(commonDenominator, TYPE_FACTORY.getLangTermType("en-gb"));
        assertNotEquals(commonDenominator, TYPE_FACTORY.getLangTermType("en"));
    }

    @Test
    public void testRegularLanguageTag() {
        RDFDatatype type = TYPE_FACTORY.getLangTermType("en-gb");

        Optional<LanguageTag> optionalLanguageTag = type.getLanguageTag();
        assertTrue(optionalLanguageTag.isPresent());

        LanguageTag langTag = optionalLanguageTag.get();
        assertEquals("en-gb", langTag.getFullString());
        assertEquals("en", langTag.getPrefix());

        Optional<String> optionalSuffix = langTag.getOptionalSuffix();
        assertTrue(optionalSuffix.isPresent());
        assertEquals("gb", optionalSuffix.get());
    }

    @Test
    public void testUpperCaseLanguageTag() {
        RDFDatatype type = TYPE_FACTORY.getLangTermType("EN-GB");

        Optional<LanguageTag> optionalLanguageTag = type.getLanguageTag();
        assertTrue(optionalLanguageTag.isPresent());

        LanguageTag langTag = optionalLanguageTag.get();
        assertEquals("en-gb", langTag.getFullString());
        assertEquals("en", langTag.getPrefix());

        Optional<String> optionalSuffix = langTag.getOptionalSuffix();
        assertTrue(optionalSuffix.isPresent());
        assertEquals("gb", optionalSuffix.get());
    }

    @Test
    public void testSameTag() {
        TermType type1 = TYPE_FACTORY.getLangTermType("en-gb");

        TermType commonDenominator = type1.getCommonDenominator(type1);

        assertEquals(commonDenominator, TYPE_FACTORY.getLangTermType("en-gb"));
        assertEquals(commonDenominator, type1);
    }

    @Test
    public void testDifferentLanguagesWithRegionalTag() {
        TermType type1 = TYPE_FACTORY.getLangTermType("en-us");
        TermType type2 = TYPE_FACTORY.getLangTermType("fr-be");

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator, TYPE_FACTORY.getXsdStringDatatype());
    }

    @Test
    public void testDifferentLanguages() {
        TermType type1 = TYPE_FACTORY.getLangTermType("en");
        TermType type2 = TYPE_FACTORY.getLangTermType("fr");

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator, TYPE_FACTORY.getXsdStringDatatype());
    }

    @Test
    public void testLanguageString() {
        TermType type1 = TYPE_FACTORY.getLangTermType("en");
        TermType type2 = TYPE_FACTORY.getXsdStringDatatype();

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(type2, commonDenominator);
    }

    @Test
    public void testLanguageAndURI() {
        TermType type1 = TYPE_FACTORY.getLangTermType("en");
        TermType type2 = TYPE_FACTORY.getIRITermType();

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator, TYPE_FACTORY.getAbstractRDFTermType());
    }

    @Test
    public void testString() {
        TermType type1 = TYPE_FACTORY.getXsdStringDatatype();
        TermType type2 = TYPE_FACTORY.getXsdStringDatatype();

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator, type1);
    }

    @Test
    public void testLangNumber() {
        TermType type1 = TYPE_FACTORY.getLangTermType("en");
        TermType type2 = TYPE_FACTORY.getXsdDecimalDatatype();

        TermType commonDenominator = type1.getCommonDenominator(type2);
        assertEquals(commonDenominator, TYPE_FACTORY.getAbstractRDFSLiteral());
    }

}
