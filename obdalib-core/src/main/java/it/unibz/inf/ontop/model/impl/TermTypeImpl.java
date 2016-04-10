package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.LanguageTag;
import it.unibz.inf.ontop.model.TermType;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceTools;

import java.util.Optional;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.LITERAL;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.LITERAL_LANG;

/**
 * TODO: integrate into a factory
 */
public class TermTypeImpl implements TermType {

    private final Predicate.COL_TYPE colType;
    private final Optional<LanguageTag> optionalLangTagConstant;
    private final Optional<Variable> optionalLangTagVariable;
    private static final Optional<TermType> OPTIONAL_LITERAL_TERM_TYPE = Optional.of(
            OBDADataFactoryImpl.getInstance().getTermType(LITERAL));

    /**
     * Only for langString WHEN the languageTag is known.
     *
     * It may indeed appear that the languageTag is not known
     * at query reformulation time because this information
     * is stored in a DB column.
     *
     */
    protected TermTypeImpl(LanguageTag languageTag) {
        this.colType = LITERAL_LANG;
        this.optionalLangTagConstant = Optional.of(languageTag);
        this.optionalLangTagVariable = Optional.empty();
    }

    /**
     * Only for langString WHEN the languageTag is known.
     *
     * It may indeed appear that the languageTag is not known
     * at query reformulation time because this information
     * is stored in a DB column.
     *
     */
    protected TermTypeImpl(Variable languageTagVariable) {
        this.colType = LITERAL_LANG;
        this.optionalLangTagConstant = Optional.empty();
        this.optionalLangTagVariable = Optional.of(languageTagVariable);
    }

    /**
     * Do not use this construction for LITERAL_LANG!.
     */
    protected TermTypeImpl(Predicate.COL_TYPE colType) {
        if (colType == LITERAL_LANG) {
            throw new IllegalArgumentException("A Literal lang must have a language tag (constant or variable)!");
        }
        this.colType = colType;
        this.optionalLangTagConstant = Optional.empty();
        this.optionalLangTagVariable = Optional.empty();
    }

    @Override
    public Predicate.COL_TYPE getColType() {
        return colType;
    }

    @Override
    public Optional<LanguageTag> getLanguageTagConstant() {
        return optionalLangTagConstant;
    }

    @Override
    public Optional<Variable> getLanguageTagVariable() {
        return optionalLangTagVariable;
    }

    @Override
    public boolean isCompatibleWith(Predicate.COL_TYPE moreGeneralType) {
        return TermTypeInferenceTools.getCommonDenominatorType(colType, moreGeneralType)
                .map(t -> t == moreGeneralType)
                .orElse(false);
    }

    @Override
    public Optional<TermType> getCommonDenominator(TermType otherTermType) {

        /**
         * TODO: explain
         */
        if (colType == LITERAL_LANG && otherTermType.getColType() == LITERAL_LANG) {

            if (optionalLangTagConstant.isPresent()) {
                LanguageTag langTag = optionalLangTagConstant.get();

                Optional<LanguageTag> newOptionalLangTag = otherTermType.getLanguageTagConstant()
                        .flatMap(langTag::getCommonDenominator);

                if (newOptionalLangTag.isPresent()) {
                    LanguageTag newLangTag = newOptionalLangTag.get();
                    return Optional.of(newLangTag.equals(langTag)
                            ? this
                            : new TermTypeImpl(newOptionalLangTag.get()));
                }
            }
            else if (optionalLangTagVariable.isPresent()) {
                Variable langTagVar = optionalLangTagVariable.get();
                if (otherTermType.getLanguageTagVariable()
                        .filter(langTagVar::equals)
                        .isPresent()) {
                    return Optional.of(this);
                }
            }
            // Default: (non-tagged) LITERAL
            return OPTIONAL_LITERAL_TERM_TYPE;
        }
        else {
            return TermTypeInferenceTools.getCommonDenominatorType(colType, otherTermType.getColType())
                    .map(TermTypeImpl::new);
        }
    }

    @Override
    public int hashCode() {
        return colType.hashCode() + optionalLangTagConstant.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return Optional.ofNullable(other)
                .filter(o -> (o instanceof TermType))
                .map(o -> (TermType) o)
                .filter(o -> colType == o.getColType())
                .filter(o -> optionalLangTagConstant
                        .map(tag1 -> o.getLanguageTagConstant()
                                .map(tag1::equals)
                                .orElse(false))
                        .orElseGet(() -> !o.getLanguageTagConstant().isPresent()))
                .filter(o -> optionalLangTagVariable
                        .map(tag1 -> o.getLanguageTagVariable()
                                .map(tag1::equals)
                                .orElse(false))
                        .orElseGet(() -> !o.getLanguageTagVariable().isPresent()))
                .isPresent();
    }

    @Override
    public String toString() {
        String tagSuffix = optionalLangTagConstant.isPresent()
                ? "@" + optionalLangTagConstant.get().toString()
                : optionalLangTagVariable.isPresent()
                    ? "@" +  optionalLangTagVariable.get().toString()
                    : "";

        // TODO: Should we print the IRI of the datatypes instead (when possible)?
        return colType + tagSuffix;
    }
}
