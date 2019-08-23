package it.unibz.inf.ontop.answering.reformulation.input;


import com.google.common.collect.ImmutableList;
import org.eclipse.rdf4j.query.algebra.Extension;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;

/**
 * TODO: make it independent of RDF4J
 */
public interface ConstructTemplate {

    ImmutableList<ProjectionElemList> getProjectionElemList();

    Extension getExtension();
}
