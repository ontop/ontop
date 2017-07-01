package it.unibz.inf.ontop.mapping;

import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;

public interface MappingMetadata {

    PrefixManager getPrefixManager();

    UriTemplateMatcher getUriTemplateMatcher();
}
