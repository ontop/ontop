package it.unibz.inf.ontop.model.impl;


import it.unibz.inf.ontop.model.LanguageTag;
import org.openrdf.model.util.language.LanguageTagSyntaxException;

import java.util.Optional;

public class LanguageTagImpl implements LanguageTag {
    private final String prefix;
    private final Optional<String> optionalSuffix;
    private final String fullString;

    protected LanguageTagImpl(String fullString){
        this.fullString = fullString;

        try {
            org.openrdf.model.util.language.LanguageTag tag = new org.openrdf.model.util.language.LanguageTag(fullString);
            prefix = tag.getLanguage().twoCharCode;
            optionalSuffix = Optional.ofNullable(tag.getVariant());
        }
        catch (LanguageTagSyntaxException e) {
            // TODO: find a better exception
            throw new IllegalStateException("Invalid language tag found: " + fullString
                    + " (should have been detected before)");
        }
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Optional<String> getOptionalSuffix() {
        return optionalSuffix;
    }

    @Override
    public String getFullString() {
        return fullString;
    }

    @Override
    public Optional<LanguageTag> getCommonDenominator(LanguageTag otherTag) {
        if (equals(otherTag)) {
            return Optional.of(this);
        }
        else if (prefix.equals(otherTag.getPrefix())) {
            return Optional.of(new LanguageTagImpl(prefix));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public int hashCode() {
        return fullString.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof LanguageTag) {
            return fullString.equals(((LanguageTag) other).getFullString());
        }
        return false;
    }

    @Override
    public String toString() {
        return fullString;
    }
}
