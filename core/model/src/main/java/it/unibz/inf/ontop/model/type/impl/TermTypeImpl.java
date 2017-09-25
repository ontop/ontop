package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE.LITERAL;
import static it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE.LITERAL_LANG;
import static it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE.STRING;

/**
 * TODO: integrate into a factory
 */
public class TermTypeImpl implements TermType {

    private final Predicate.COL_TYPE colType;
    private final Optional<ImmutableTerm> optionalLangTagTerm;
    private static final Optional<TermType> OPTIONAL_LITERAL_TERM_TYPE = Optional.of(
            TYPE_FACTORY.getTermType(LITERAL));
    private final Optional<LanguageTag> optionalLangTagConstant;

    /**
     * Only for langString.
     *
     * It may indeed appear that the languageTag is not known
     * at query reformulation time because this information
     * is stored in a DB column.
     *
     */
    protected TermTypeImpl(ImmutableTerm languageTagTerm) {
        this.colType = LITERAL_LANG;
        this.optionalLangTagTerm = Optional.of(languageTagTerm);
        this.optionalLangTagConstant = extractLanguageTagConstant(languageTagTerm);
    }

    protected TermTypeImpl(Predicate.COL_TYPE colType) {
        this.colType = colType;
        this.optionalLangTagTerm = Optional.empty();
        this.optionalLangTagConstant = Optional.empty();
    }

    @Override
    public Predicate.COL_TYPE getColType() {
        return colType;
    }

    @Override
    public Optional<ImmutableTerm> getLanguageTagTerm() {
        return optionalLangTagTerm;
    }

    @Override
    public boolean isFullyDefined() {
        return (colType != LITERAL_LANG) || optionalLangTagTerm.isPresent();
    }

    /**
     * TODO: refactor
     */
    @Override
    public boolean isCompatibleWith(TermType moreGeneralType) {
        return TermTypeInferenceTools.getCommonDenominatorType(colType, moreGeneralType.getColType())
                .map(moreGeneralType::equals)
                .orElse(false);
    }

    @Override
    public Optional<TermType> getCommonDenominator(TermType otherTermType) {

        /*
         * TODO: explain
         */
        if (colType == LITERAL_LANG && otherTermType.getColType() == LITERAL_LANG) {

            if (optionalLangTagConstant.isPresent()) {
                LanguageTag langTag = optionalLangTagConstant.get();

                Optional<LanguageTag> newOptionalLangTag = otherTermType.getLanguageTagTerm()
                        .flatMap(TermTypeImpl::extractLanguageTagConstant)
                        .flatMap(langTag::getCommonDenominator);

                if (newOptionalLangTag.isPresent()) {
                    LanguageTag newLangTag = newOptionalLangTag.get();
                    return Optional.of(newLangTag.equals(langTag)
                            ? this
                            : new TermTypeImpl(TERM_FACTORY.getConstantLiteral(newLangTag.getFullString(), STRING)));
                }
            }
            else if (optionalLangTagTerm.isPresent()) {
                ImmutableTerm langTagTerm = optionalLangTagTerm.get();
                if (otherTermType.getLanguageTagTerm()
                        .filter(langTagTerm::equals)
                        .isPresent()) {
                    return Optional.of(this);
                }
            }
            // Default: (non-tagged) LITERAL
            return OPTIONAL_LITERAL_TERM_TYPE;
        }
        else {
            return TermTypeInferenceTools.getCommonDenominatorType(colType, otherTermType.getColType());
        }
    }

    private static Optional<LanguageTag> extractLanguageTagConstant(ImmutableTerm term) {
        return Optional.of(term)
                .filter(t -> t instanceof Constant)
                .map(c -> (Constant) c)
                .map(c -> new LanguageTagImpl(c.getValue()));
    }

    @Override
    public int hashCode() {
        return colType.hashCode() + optionalLangTagTerm.hashCode();
    }

    /**
     * TODO: refactor
     */
    @Override
    public boolean equals(Object other) {
        return Optional.ofNullable(other)
                .filter(o -> (o instanceof TermType))
                .map(o -> (TermType) o)
                .filter(o -> colType == o.getColType())
                .filter(o -> optionalLangTagTerm
                        .map(tag1 -> o.getLanguageTagTerm()
                                .map(tag1::equals)
                                .orElse(false))
                        .orElseGet(() -> !o.getLanguageTagTerm().isPresent()))
                .isPresent();
    }

    @Override
    public String toString() {
        String tagSuffix = optionalLangTagTerm.isPresent()
                    ? "@" +  optionalLangTagTerm.get().toString()
                    : "";

        // TODO: Should we print the IRI of the datatypes instead (when possible)?
        return colType + tagSuffix;
    }
}
