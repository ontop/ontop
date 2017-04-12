package it.unibz.inf.ontop.mapping.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.UriTemplateMatcher;

public class MappingMetadataImpl implements MappingMetadata {

    private final PrefixManager prefixManager;
    private final UriTemplateMatcher uriTemplateMatcher;

    @AssistedInject
    private MappingMetadataImpl(@Assisted PrefixManager prefixManager,
                                @Assisted UriTemplateMatcher uriTemplateMatcher) {
        this.prefixManager = prefixManager;
        this.uriTemplateMatcher = uriTemplateMatcher;
    }

    @Override
    public PrefixManager getPrefixManager() {
        return prefixManager;
    }

    @Override
    public UriTemplateMatcher getUriTemplateMatcher() {
        return uriTemplateMatcher;
    }
}
