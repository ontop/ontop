package it.unibz.inf.ontop.owlapi.resultset.impl;

import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.semanticweb.owlapi.model.*;

import java.util.List;
import java.util.NoSuchElementException;

/***
 * A wrapper for TupleResultSet that presents the results as OWLAPI objects.
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 * 
 */
public class OntopTupleOWLResultSet implements TupleOWLResultSet {

	private final TupleResultSet res;
	private final byte[] salt;

	public OntopTupleOWLResultSet(TupleResultSet res, byte[] salt) {
		this.salt = salt;
		if (res == null)
			throw new IllegalArgumentException("The result set must not be null");
		this.res = res;
	}

	@Override
	public int getColumnCount() throws OWLException {
		try {
			return res.getColumnCount();
		} catch (Exception e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public List<String> getSignature() throws OWLException {
		try {
			return res.getSignature();
		} catch (Exception e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public int getFetchSize() throws OWLException {
		try {
			return res.getFetchSize();
		} catch (Exception e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
    public void close() throws OWLException {
		try {
			res.close();
		} catch (Exception e) {
			throw new OntopOWLException(e);
		}

	}

	@Override
	public boolean hasNext() throws OWLException {
		try {
			return res.hasNext();
		} catch (Exception e) {
			throw new OntopOWLException(e);
		}
	}

    @Override
    public OWLBindingSet next() throws OWLException {
        try {
            return new OntopOWLBindingSet(res.next(), salt);
        } catch (OntopConnectionException | OntopResultConversionException | NoSuchElementException e) {
            throw new OntopOWLException(e);
		}
	}
}
