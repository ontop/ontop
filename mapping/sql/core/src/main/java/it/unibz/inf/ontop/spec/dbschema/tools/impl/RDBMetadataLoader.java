package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import java.io.IOException;

public interface RDBMetadataLoader {
    Metadata loadAndDeserialize() throws IOException;
}
