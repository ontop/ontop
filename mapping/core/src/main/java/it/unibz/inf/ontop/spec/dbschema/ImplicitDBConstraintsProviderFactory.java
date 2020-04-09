package it.unibz.inf.ontop.spec.dbschema;

import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.File;
import java.util.Optional;

public interface ImplicitDBConstraintsProviderFactory {

    MetadataProvider extract(Optional<File> constraintFile, MetadataProvider baseMetadataProvider)
            throws MetadataExtractionException;
}
