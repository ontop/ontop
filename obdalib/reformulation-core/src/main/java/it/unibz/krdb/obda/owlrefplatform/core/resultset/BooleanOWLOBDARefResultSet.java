package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
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

public class BooleanOWLOBDARefResultSet implements OBDAResultSet {

	private ResultSet set = null;
	private boolean isTrue = false;
	private int counter = 0;
	private OBDAStatement st;

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	public BooleanOWLOBDARefResultSet(ResultSet set, OBDAStatement st) {
		this.set = set;
		this.st = st;
		try {
			isTrue = set.next();
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void close() throws OBDAException {
		try {
			set.close();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	/**
	 * return 1 if true 0 otherwise
	 */
	@Override
	public double getDouble(int column) throws OBDAException {
		if (isTrue) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * return 1 if true 0 otherwise
	 */
	@Override
	public int getInt(int column) throws OBDAException {
		if (isTrue) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * returns the true value as object
	 */
	@Override
	public Object getObject(int column) throws OBDAException {
		if (isTrue) {
			return "true";
		} else {
			return "false";
		}
	}

	/**
	 * returns the true value as string
	 */
	@Override
	public String getString(int column) throws OBDAException {
		if (isTrue) {
			return "true";
		} else {
			return "false";
		}
	}

	/**
	 * returns the true value as URI
	 */
	@Override
	public URI getURI(int column) throws OBDAException {
		if (isTrue) {
			return URI.create("true");
		} else {
			return URI.create("false");
		}
	}

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
		int i = getColumCount();
		Vector<String> signature = new Vector<String>();
		for (int j = 1; j <= i; j++) {
			try {
				signature.add(set.getMetaData().getColumnLabel(j));
			} catch (SQLException e) {
				throw new OBDAException(e.getMessage());
			}
		}
		return signature;
	}

	/**
	 * Note: the boolean result set has only 1 row
	 */
	@Override
	public boolean nextRow() throws OBDAException {
		if (counter > 0) {
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
		return this.fac.getValueConstant(String.valueOf(isTrue), COL_TYPE.BOOLEAN);
	}

	@Override
	public ValueConstant getLiteral(int column) throws OBDAException {
		return this.fac.getValueConstant(String.valueOf(isTrue), COL_TYPE.BOOLEAN);
	}

	@Override
	public BNode getBNode(int column) throws OBDAException {
		return null;
	}

	@Override
	public Constant getConstant(String name) throws OBDAException {
		return this.fac.getValueConstant(String.valueOf(isTrue), COL_TYPE.BOOLEAN);
	}

	@Override
	public URI getURI(String name) throws OBDAException {
		return null;
	}

	@Override
	public ValueConstant getLiteral(String name) throws OBDAException {
		return this.fac.getValueConstant(String.valueOf(isTrue), COL_TYPE.BOOLEAN);
	}

	@Override
	public BNode getBNode(String name) throws OBDAException {
		return null;
	}

}
