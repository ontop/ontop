package it.unibz.inf.ontop.model.type;

import java.util.Optional;

/**
 * TODO: explain
 *
 * https://www.ietf.org/rfc/rfc4647.txt
 *
 */
public interface LanguageTag {

    /**
     * TODO: find a better name
     */
    String getPrefix();

    /**
     * TODO: find a better name
     *
     * TODO: explain
     */
    Optional<String> getOptionalSuffix();

    String getFullString();

    Optional<LanguageTag> getCommonDenominator(LanguageTag otherTag);

}
