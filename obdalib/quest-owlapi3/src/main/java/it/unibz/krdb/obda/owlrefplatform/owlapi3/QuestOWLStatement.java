package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlapi3.OWLConnection;
import it.unibz.krdb.obda.owlapi3.OWLResultSet;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLException;

public class QuestOWLStatement implements OWLStatement {

	private QuestStatement st;
	private QuestOWLConnection conn;


	public QuestOWLStatement(QuestStatement st, QuestOWLConnection conn) {
		this.conn = conn;
		this.st = st;
	}

	@Override
	public void cancel() throws OWLException {
		try {
			st.cancel();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public void close() throws OWLException {
		try {
			st.close();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public OWLResultSet execute(String query) throws OWLException {
		try {
			return new QuestOWLResultSet(st.execute(query), this);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public int executeUpdate(String query) throws OWLException {
		try {
			return st.executeUpdate(query);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public OWLConnection getConnection() throws OWLException {
		return conn;
	}

	@Override
	public int getFetchSize() throws OWLException {
		try {
			return st.getFetchSize();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public int getMaxRows() throws OWLException {
		try {
			return st.getMaxRows();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public void getMoreResults() throws OWLException {
		try {
			st.cancel();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public OWLResultSet getResultSet() throws OWLException {
		try {
			return new QuestOWLResultSet(st.getResultSet(), this);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public int getQueryTimeout() throws OWLException {
		try {
			return st.getQueryTimeout();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public void setFetchSize(int rows) throws OWLException {
		try {
			st.setFetchSize(rows);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public void setMaxRows(int max) throws OWLException {
		try {
			st.setMaxRows(max);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public boolean isClosed() throws OWLException {
		try {
			return st.isClosed();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public void setQueryTimeout(int seconds) throws Exception {
		try {
			st.setQueryTimeout(seconds);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}

	}

	@Override
	public int getTupleCount(String query) throws OWLException {
		try {
			return st.getTupleCount(query);
		} catch (Exception e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	public String getRewriting(String query) throws OWLException {
		try {
			return st.getRewriting(query);
		} catch (Exception e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	public String getUnfolding(String query) throws OWLException {
		try {
			return st.getUnfolding(query);
		} catch (Exception e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}
}
