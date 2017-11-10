package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.utils.UriTemplateMatcher;

public interface MappingMetadata {

    PrefixManager getPrefixManager();

    UriTemplateMatcher getUriTemplateMatcher();
}
