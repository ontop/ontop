package it.unibz.inf.ontop.dbschema;

import java.io.File;
import java.nio.file.Path;

public interface OntopViewMetaDataProvider extends MetadataProvider {

    void load(File file);

}
