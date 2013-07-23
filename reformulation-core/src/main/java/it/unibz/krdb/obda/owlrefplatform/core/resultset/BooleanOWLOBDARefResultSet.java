package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * The boolean result set returned by an OBDA statement.
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class BooleanOWLOBDARefResultSet implements TupleResultSet {

	private ResultSet set = null;
	private boolean isTrue = false;
	private int counter = 0;
	private OBDAStatement st;

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private ValueConstant valueConstant;

	public BooleanOWLOBDARefResultSet(ResultSet set, OBDAStatement st) {
		this.set = set;
		this.st = st;
		try {
			isTrue = set.next();
			valueConstant = this.fac.getValueConstant(String.valueOf(isTrue), COL_TYPE.BOOLEAN);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
		
	}

	public BooleanOWLOBDARefResultSet(boolean value, OBDAStatement st) {
		this.set = null;
		this.st = st;

		isTrue = value;

	}

	@Override
	public void close() throws OBDAException {
		if (set == null)
			return;
		try {
			set.close();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

//	/**
//	 * return 1 if true 0 otherwise
//	 */
//	@Override
//	public double getDouble(int column) throws OBDAException {
//		if (isTrue) {
//			return 1;
//		} else {
//			return 0;
//		}
//	}
//
//	/**
//	 * return 1 if true 0 otherwise
//	 */
//	@Override
//	public int getInt(int column) throws OBDAException {
//		if (isTrue) {
//			return 1;
//		} else {
//			return 0;
//		}
//	}
//
//	/**
//	 * returns the true value as object
//	 */
//	@Override
//	public Object getObject(int column) throws OBDAException {
//		if (isTrue) {
//			return "true";
//		} else {
//			return "false";
//		}
//	}
//
//	/**
//	 * returns the true value as string
//	 */
//	@Override
//	public String getString(int column) throws OBDAException {
//		if (isTrue) {
//			return "true";
//		} else {
//			return "false";
//		}
//	}

//	/**
//	 * returns the true value as URI
//	 */
//	@Override
//	public URI getURI(int column) throws OBDAException {
//		if (isTrue) {
//			return URI.create("true");
//		} else {
//			return URI.create("false");
//		}
//	}
//	
//	/**
//	 * returns the true value as URI
//	 */
//	@Override
//	public IRI getIRI(int column) throws OBDAException {
//		if (isTrue) {
//			return OBDADataFactoryImpl.getIRI("true");
//		} else {
//			return OBDADataFactoryImpl.getIRI("false");
//		}
//	}

	/**
	 * returns always 1
	 */
	@Override
	public int getColumCount() throws OBDAException {
		return 1;
	}

	/**
	 * returns the current fetch size. the default value is 100
	 */
	@Override
	public int getFetchSize() throws OBDAException {
		return 100;
	}

	@Override
	public List<String> getSignature() throws OBDAException {
		Vector<String> signature = new Vector<String>();
		if (set != null) {
			int i = getColumCount();

			for (int j = 1; j <= i; j++) {
				try {
					signature.add(set.getMetaData().getColumnLabel(j));
				} catch (SQLException e) {
					throw new OBDAException(e.getMessage());
				}
			}
		} else {
			signature.add("value");
		}
		return signature;
	}

	/**
	 * Note: the boolean result set has only 1 row
	 */
	@Override
	public boolean nextRow() throws OBDAException {
		if (!isTrue || counter > 0) {
			return false;
		} else {
			counter++;
			return true;
		}
	}

	@Override
	public OBDAStatement getStatement() {
		return st;
	}

	@Override
	public Constant getConstant(int column) throws OBDAException {
		
		return valueConstant;
	}

//	@Override
//	public ValueConstant getLiteral(int column) throws OBDAException {
//		return this.fac.getValueConstant(String.valueOf(isTrue), COL_TYPE.BOOLEAN);
//	}
//
//	@Override
//	public BNode getBNode(int column) throws OBDAException {
//		return null;
//	}

	@Override
	public Constant getConstant(String name) throws OBDAException {
		return valueConstant;
	}

//	@Override
//	public URI getURI(String name) throws OBDAException {
//		return null;
//	}
//	
//	@Override
//	public IRI getIRI(String name) throws OBDAException {
//		return null;
//	}
//
//	@Override
//	public ValueConstant getLiteral(String name) throws OBDAException {
//		return this.fac.getValueConstant(String.valueOf(isTrue), COL_TYPE.BOOLEAN);
//	}
//
//	@Override
//	public BNode getBNode(String name) throws OBDAException {
//		return null;
//	}

}
