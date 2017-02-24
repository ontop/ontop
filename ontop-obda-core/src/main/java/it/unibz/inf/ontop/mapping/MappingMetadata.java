package it.unibz.inf.ontop.mapping;

import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.UriTemplateMatcher;

public interface MappingMetadata {

    PrefixManager getPrefixManager();

    UriTemplateMatcher getUriTemplateMatcher();
}
