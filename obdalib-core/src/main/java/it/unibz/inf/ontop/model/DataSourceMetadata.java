package it.unibz.inf.ontop.model;

import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.List;

/**
 * Common abstraction for all sorts of Database (relational, etc.)x
 */
public interface DataSourceMetadata extends Serializable {

    String getDriverName();

    String getDriverVersion();
}
