package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public class LangDatatype extends AbstractRDFDatatype {

    private final LanguageTag langTag;
    private final TermTypeAncestry parentAncestry;
    private final TypeFactory typeFactory;

    private LangDatatype(LanguageTag langTag, TermTypeAncestry parentAncestry, TypeFactory typeFactory) {
        super(selectDatatypeIri(langTag), parentAncestry, DBTypeFactory::getDBStringType);
        this.langTag = langTag;
        this.parentAncestry = parentAncestry;
        this.typeFactory = typeFactory;
    }

    static RDFDatatype createLangDatatype(LanguageTag langTag, TermTypeAncestry parentAncestry,
                                          TypeFactory typeFactory) {
        return new LangDatatype(langTag, parentAncestry, typeFactory);
    }

    private static IRI selectDatatypeIri(LanguageTag langTag) {
        return langTag.getDirection().isPresent()
                ? RDF.DIRLANGSTRING
                : RDF.LANGSTRING;
    }


    @Override
    public TermType getCommonDenominator(TermType otherTermType) {

        if (otherTermType instanceof RDFDatatype) {
            RDFDatatype otherDatatype = ((RDFDatatype) otherTermType);

            Optional<LanguageTag> otherLanguageTag = otherDatatype.getLanguageTag();
            // Common denominator between two lang strings
            if (otherLanguageTag.isPresent())
                return getCommonDenominator(otherLanguageTag.get());
        }
        /*
         * Other term types --> standard test
         */
        return super.getCommonDenominator(otherTermType);
    }

    /**
     * Common denominator between two lang strings
     */
    private TermType getCommonDenominator(LanguageTag otherLanguageTag) {
        return langTag.getCommonDenominator(otherLanguageTag)
                .map(newLangTag -> newLangTag.equals(langTag)
                        ? (TermType) this
                        : new LangDatatype(newLangTag, parentAncestry, typeFactory))
                // Incompatible lang tags --> common denominator is xsd:string
                .orElseGet(typeFactory::getXsdStringDatatype);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof RDFDatatype) {
            RDFDatatype otherType = (RDFDatatype) other;
            return otherType.getLanguageTag()
                    .filter(langTag::equals)
                    .isPresent();
        }
        return false;
    }

    @Override
    public String toString() {
        return "@" + langTag.getFullString();
    }

    @Override
    public Optional<LanguageTag> getLanguageTag() {
        return Optional.of(langTag);
    }
}
