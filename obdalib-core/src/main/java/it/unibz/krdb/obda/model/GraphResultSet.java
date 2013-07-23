package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.ontology.Assertion;

import java.util.List;

import com.hp.hpl.jena.sparql.syntax.Template;

public interface GraphResultSet extends ResultSet {

	public boolean hasNext() throws OBDAException;

	public List<Assertion> next() throws OBDAException;

	public void close() throws OBDAException;

	TupleResultSet getTupleResultSet();

	void addNewResultSet(List<Assertion> result);
	
	Template getTemplate();
	
}