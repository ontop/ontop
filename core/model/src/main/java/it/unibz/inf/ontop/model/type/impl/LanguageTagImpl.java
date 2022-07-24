package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.LanguageTag;

import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class LanguageTagImpl implements LanguageTag {
    private final String prefix;
    private final Optional<String> optionalSuffix;
    private final String fullString;

    protected LanguageTagImpl(String fullString){
        this.fullString = Objects.requireNonNull(fullString).toLowerCase(Locale.ENGLISH);

        if (fullString.isEmpty())
            throw new IllegalArgumentException("A language tag cannot be empty");

        try {
            Locale locale = new Locale.Builder().setLanguageTag(fullString).build();
            this.prefix = locale.getLanguage();
            if (prefix.length() < 2 || prefix.length() > 3)
                // language not well-formed (required for RDF)
                throw new IllegalStateException("Invalid language tag found: " + fullString + ". The language code can only have 2 or 3 chars.");
            
            this.optionalSuffix = Optional.of(locale.getCountry())
                    .filter(v -> !v.isEmpty())
                    .map(v -> v.toLowerCase(Locale.ENGLISH));

        } catch (IllformedLocaleException ex) {
            throw new IllegalStateException("Invalid language tag found: " + fullString);
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
