package it.unibz.inf.ontop.rdf4j.query.impl;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.query.resultset.TupleResultSet;

import java.util.List;
import java.util.Objects;

import it.unibz.inf.ontop.exception.OntopResultConversionException;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;

public class OntopTupleQueryResult implements TupleQueryResult {

	private final byte[] salt;
	private final TupleResultSet res;
	private final List<String> signature;

	public OntopTupleQueryResult(TupleResultSet res, List<String> signature, byte[] salt) {
		this.salt = salt;
		this.res = Objects.requireNonNull(res);
		this.signature = signature;
	}
	
	@Override
	public void close() throws QueryEvaluationException {
		try {
			res.close();
		} catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public boolean hasNext() throws QueryEvaluationException {
		try {
			return res.hasNext();
		} catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public BindingSet next() throws QueryEvaluationException {
        try {
            return new OntopRDF4JBindingSet(res.next(), salt);
        } catch (OntopConnectionException | OntopResultConversionException e) {
            throw new QueryEvaluationException(e);
		}
	}

	@Override
	public void remove() throws QueryEvaluationException {
		throw new QueryEvaluationException("The query result is read-only. Elements cannot be removed");
	}
	

	@Override
	public List<String> getBindingNames() throws QueryEvaluationException {
		return this.signature;
	}

}
