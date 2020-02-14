package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import it.unibz.inf.ontop.spec.dbschema.tools.DBMetadataExtractorAndSerializer;

public class FakeDBMetadataExtractorAndSerializer implements DBMetadataExtractorAndSerializer {

    @Override
    public String extractAndSerialize() {
        throw new UnsupportedOperationException("Fake implementation. Please use a real implementation");
    }
}
