package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

/**
 * Metadata about the configuration of the data source.
 *
 * This information is typically extracted by connecting to the data source.
 *
 * DOES NOT CONTAIN information about the SCHEMA
 *
 * TODO: move it to be more general package
 */
public interface DBParameters {

    QuotedIDFactory getQuotedIDFactory();

    DBTypeFactory getDBTypeFactory();

    CoreSingletons getCoreSingletons();

    String getDriverName();

    String getDriverVersion();

    String getDbmsProductName();

    String getDbmsVersion();
}
