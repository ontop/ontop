package it.unibz.inf.ontop.utils;

import it.unibz.inf.ontop.exception.InvalidPrefixWritingException;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;

public class IRIPrefixes {

    public static String getUriTemplateString(ImmutableFunctionalTerm iriTemplateFunctionalTerm, PrefixManager prefixmng) {
        String template = URITemplates.getUriTemplateString(iriTemplateFunctionalTerm);
        try {
            template = prefixmng.getExpandForm(template);
        } catch (InvalidPrefixWritingException ex) {
            // in this case, the we do not need to expand
        }
        return template;
    }
}
