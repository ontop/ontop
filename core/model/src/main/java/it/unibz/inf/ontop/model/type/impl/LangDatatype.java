package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.model.vocabulary.RDF;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

public class LangDatatype extends AbstractRDFDatatype {

    private final LanguageTag langTag;
    private final TermTypeAncestry parentAncestry;

    private LangDatatype(LanguageTag langTag, TermTypeAncestry parentAncestry) {
        super(RDF.LANGSTRING, parentAncestry, false);
        this.langTag = langTag;
        this.parentAncestry = parentAncestry;
    }

    static RDFDatatype createLangDatatype(LanguageTag langTag, TermTypeAncestry parentAncestry) {
        return new LangDatatype(langTag, parentAncestry);
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
                        : new LangDatatype(newLangTag, parentAncestry))
                // Incompatible lang tags --> common denominator is xsd:string
                .orElseGet(TYPE_FACTORY::getXsdStringDatatype);
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
        return super.toString() + "@" + langTag.getFullString();
    }

    @Override
    public Optional<LanguageTag> getLanguageTag() {
        return Optional.of(langTag);
    }
}
