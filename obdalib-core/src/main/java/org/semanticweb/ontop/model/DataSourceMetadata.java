package org.semanticweb.ontop.model;

import com.google.common.collect.Multimap;

import java.util.List;

/**
 * Common abstraction for all sorts of Database (relational, etc.)x
 */
public interface DataSourceMetadata {
    String printKeys();

    Multimap<Predicate,List<Integer>> extractPKs(List<CQIE> datalogProgram);

    String getDriverName();

    String getDriverVersion();
}
