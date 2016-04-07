package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.LanguageTag;

import java.util.Objects;
import java.util.Optional;

public class LanguageTagImpl implements LanguageTag {
    private final String prefix;
    private final Optional<String> optionalSuffix;
    private final String fullString;

    protected LanguageTagImpl(String fullString){
        this.fullString = fullString;
        throw new RuntimeException("TODO: complete");
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
        throw new RuntimeException("TODO: implement it");
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
