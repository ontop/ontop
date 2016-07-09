package it.unibz.inf.ontop.model;

import java.io.Serializable;

/**
 * Common abstraction for all sorts of Database (relational, etc.)x
 */
public interface DataSourceMetadata extends Serializable {

    String getDriverName();

    String getDriverVersion();

    String printKeys();
}
