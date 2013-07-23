package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.owlapi3.OWLAPI3IndividualTranslator;
import it.unibz.krdb.obda.owlapi3.OWLResultSet;
import it.unibz.krdb.obda.owlapi3.OWLStatement;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

public class QuestOWLResultSet implements OWLResultSet {

	private final TupleResultSet res;

	private final OWLStatement owlst;

	public QuestOWLResultSet(TupleResultSet res, QuestOWLStatement owlst) {
		if (res == null)
			throw new IllegalArgumentException("The result set must not be null");
		this.res = res;
		this.owlst = owlst;
	}

	@Override
	public int getColumCount() throws OWLException {
		try {
			return res.getColumCount();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public List<String> getSignature() throws OWLException {
		try {
			return res.getSignature();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public int getFetchSize() throws OWLException {
		try {
			return res.getFetchSize();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public void close() throws OWLException {
		try {
			res.close();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}

	}

	@Override
	public OWLStatement getStatement() {
		return owlst;
	}

	@Override
	public boolean nextRow() throws OWLException {
		try {
			return res.nextRow();
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	private OWLAPI3IndividualTranslator translator = new OWLAPI3IndividualTranslator();

	@Override
	public OWLPropertyAssertionObject getOWLPropertyAssertionObject(int column) throws OWLException {
		try {
			return translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e + " Column: " + column) {
			};
		}
	}

	@Override
	public OWLPropertyAssertionObject getOWLPropertyAssertionObject(String column) throws OWLException {
		try {
			return translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e + " Column: " + column) {
			};
		}
	}

	@Override
	public OWLIndividual getOWLIndividual(int column) throws OWLException {
		try {
			return (OWLIndividual) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public OWLIndividual getOWLIndividual(String column) throws OWLException {
		try {
			return (OWLIndividual) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public OWLNamedIndividual getOWLNamedIndividual(int column) throws OWLException {
		try {
			return (OWLNamedIndividual) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public OWLNamedIndividual getOWLNamedIndividual(String column) throws OWLException {
		try {
			return (OWLNamedIndividual) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public OWLAnonymousIndividual getOWLAnonymousIndividual(int column) throws OWLException {
		try {
			return (OWLAnonymousIndividual) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public OWLAnonymousIndividual getOWLAnonymousIndividual(String column) throws OWLException {
		try {
			return (OWLAnonymousIndividual) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public OWLLiteral getOWLLiteral(int column) throws OWLException {
		try {
			return (OWLLiteral) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public OWLLiteral getOWLLiteral(String column) throws OWLException {
		try {
			return (OWLLiteral) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public OWLObject getOWLObject(int column) throws OWLException {
		try {
			return (OWLObject) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

	@Override
	public OWLObject getOWLObject(String column) throws OWLException {
		try {
			return (OWLObject) translator.translate(res.getConstant(column));
		} catch (OBDAException e) {
			throw new OWLException(e) {
			};
		}
	}

}
