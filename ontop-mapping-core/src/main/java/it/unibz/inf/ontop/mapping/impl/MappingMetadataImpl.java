package it.unibz.inf.ontop.mapping.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.MappingMetadata;

public class MappingMetadataImpl implements MappingMetadata {

    private final PrefixManager prefixManager;

    @AssistedInject
    private MappingMetadataImpl(@Assisted PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    @Override
    public PrefixManager getPrefixManager() {
        return prefixManager;
    }
}
