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
    private final Optional<String> direction;

    protected LanguageTagImpl(String fullString){
        String provided = Objects.requireNonNull(fullString);
        if (provided.isEmpty())
            throw new IllegalArgumentException("A language tag cannot be empty");

        String normalized = provided.toLowerCase(Locale.ENGLISH);
        int directionSeparator = normalized.indexOf("--");
        String baseTag = directionSeparator >= 0 ? normalized.substring(0, directionSeparator) : normalized;

        if (baseTag.isEmpty())
            throw new IllegalStateException("Invalid language tag found: " + fullString);

        Optional<String> parsedDirection = Optional.empty();
        if (directionSeparator >= 0) {
            String directionToken = normalized.substring(directionSeparator + 2);
            if (directionToken.isEmpty())
                throw new IllegalStateException("Invalid language direction found in tag: " + fullString);

            String normalizedDirection = directionToken.toLowerCase(Locale.ENGLISH);
            if (!normalizedDirection.equals("ltr") && !normalizedDirection.equals("rtl"))
                throw new IllegalStateException("Invalid language direction '" + directionToken + "' found in tag: " + fullString);

            parsedDirection = Optional.of(normalizedDirection);
            this.fullString = normalized;
        }
        else {
            this.fullString = normalized;
        }

        try {
            Locale locale = new Locale.Builder().setLanguageTag(baseTag).build();
            this.prefix = locale.getLanguage();
            if (prefix.length() < 2 || prefix.length() > 3)
                // language not well-formed (required for RDF)
                throw new IllegalStateException("Invalid language tag found: " + fullString + ". The language code can only have 2 or 3 chars.");

            this.optionalSuffix = Optional.of(locale.getCountry())
                    .filter(v -> !v.isEmpty())
                    .map(v -> v.toLowerCase(Locale.ENGLISH));
            this.direction = parsedDirection;

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
    public Optional<String> getDirection() {
        return direction;
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
