package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.ontology.Assertion;

import java.util.List;

public interface GraphResultSet {

	public boolean hasNext() throws OBDAException;

	public List<Assertion> next() throws OBDAException;

	public void close() throws OBDAException;
}