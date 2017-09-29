package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE.LITERAL_LANG;
import static it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE.STRING;

public class LangDatatype extends AbstractRDFDatatype {

    private final LanguageTag langTag;

    protected LangDatatype(LanguageTag langTag) {
        super(LITERAL_LANG);
        this.langTag = langTag;
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
                        : new LangDatatype(newLangTag))
                // Incompatible lang tags --> common denominator is xsd:string
                .orElseGet(() -> TYPE_FACTORY.getTermType(STRING));
    }

    /**
     * TODO: refactor
     */
    @Override
    public boolean equals(Object other) {
        // Should check if it is a language tag
        if (!super.equals(other))
            return false;

        RDFDatatype otherType = (RDFDatatype) other;
        return otherType.getLanguageTag()
                .filter(langTag::equals)
                .isPresent();
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
