package it.unibz.inf.ontop.spec.dbschema;

import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Optional;

public interface ImplicitDBConstraintsProviderFactory {

    MetadataProvider extract(Optional<File> constraintFile, QuotedIDFactory idFactory)
            throws DBMetadataExtractionException;
}
