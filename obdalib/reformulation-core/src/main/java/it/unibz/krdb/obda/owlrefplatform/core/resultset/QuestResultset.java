package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

public class QuestResultset implements TupleResultSet {

	private boolean isSemIndex = false;
	private ResultSet set = null;
	private OBDAStatement st;
	private Vector<String> signature;

	private HashMap<String, Integer> columnMap = new HashMap<String, Integer>();

	private HashMap<String, String> bnodeMap = new HashMap<String, String>();

	private LinkedHashSet<String> uriRef = new LinkedHashSet<String>();
	private int bnodeCounter = 0;

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private LinkedHashMap<Integer, String> uriMap = new LinkedHashMap<Integer, String>();

	/***
	 * Constructs an OBDA statement from an SQL statement, a signature described
	 * by terms and a statement. The statement is maintained only as a reference
	 * for closing operations.
	 * 
	 * @param set
	 * @param signature
	 *            A list of terms that determines the type of the columns of
	 *            this results set.
	 * @param st
	 * @throws OBDAException
	 */
	public QuestResultset(ResultSet set, List<String> signature,
			OBDAStatement st) throws OBDAException {
		this(set, signature, st, null);

	}

//	// This ResultSet is used for semantic index case
//	// It encodes URIs as Integers for faster query performance
//	// Later in the results we need to replace URI references with actual URIs
//	public QuestResultset(ResultSet set, List<String> signature,
//			OBDAStatement st, LinkedHashSet<String> uriRef) throws OBDAException {
//		this.set = set;
//		this.st = st;
//		this.uriRef = uriRef;
//		if (uriRef != null)
//			this.isSemIndex = true;
//		else
//			this.isSemIndex = false;
//
//		this.signature = new Vector<String>(signature);
//		for (int j = 1; j <= signature.size(); j++) {
//			columnMap.put(signature.get(j - 1), j - 1);
//		}
//
//	}

	public QuestResultset(ResultSet set, List<String> signature,
			OBDAStatement st, LinkedHashMap<Integer, String> uriMap) throws OBDAException {
		this.set = set;
		this.st = st;
		this.uriMap = uriMap;
		if (uriRef != null)
			this.isSemIndex = true;
		else
			this.isSemIndex = false;

		this.signature = new Vector<String>(signature);
		for (int j = 1; j <= signature.size(); j++) {
			columnMap.put(signature.get(j - 1), j - 1);
		}

	}
	
	public double getDouble(int column) throws OBDAException {
		try {
			return set.getDouble(signature.get(column - 1));
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	public int getInt(int column) throws OBDAException {
		try {
			return set.getInt(signature.get(column - 1));
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	public Object getObject(int column) throws OBDAException {
		try {
			return set.getObject(signature.get(column - 1));
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	public String getString(int column) throws OBDAException {
		try {
			return set.getString(signature.get(column - 1));
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	public URI getURI(int column) throws OBDAException {
		return getURI(signature.get(column - 1));
	}

	public IRI getIRI(int column) throws OBDAException {
		return getIRI(signature.get(column - 1));
	}

	public int getColumCount() throws OBDAException {
		return signature.size();
	}

	public int getFetchSize() throws OBDAException {
		try {
			return set.getFetchSize();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	public List<String> getSignature() throws OBDAException {
		return signature;
	}

	public boolean nextRow() throws OBDAException {
		try {
			return set.next();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	public void close() throws OBDAException {
		try {
			set.close();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public OBDAStatement getStatement() {
		return st;
	}

	@Override
	public Constant getConstant(int column) throws OBDAException {
		return getConstant(signature.get(column - 1));
	}

	@Override
	public ValueConstant getLiteral(int column) throws OBDAException {
		return getLiteral(signature.get(column - 1));
	}

	@Override
	public BNode getBNode(int column) throws OBDAException {
		return getBNode(signature.get(column - 1));
	}

	@Override
	public Constant getConstant(String name) throws OBDAException {
		Constant result = null;
		String realValue = "";
		try {	
			realValue = set.getString(name);
			COL_TYPE type = getQuestType((byte) set.getInt(name + "QuestType"));
			
			if (type == null || realValue == null) {
				return null;
			} else {
				if (type == COL_TYPE.OBJECT) {
					// URI value = getURI(name);
					IRI irivalue = getIRI(name);
					// result = fac.getURIConstant(value);
					result = fac.getURIConstant(irivalue);

				} else if (type == COL_TYPE.BNODE) {
					String rawLabel = set.getString(name);
					String scopedLabel = this.bnodeMap.get(rawLabel);
					if (scopedLabel == null) {
						scopedLabel = "b" + bnodeCounter;
						bnodeCounter += 1;
						bnodeMap.put(rawLabel, scopedLabel);
					}
					result = fac.getBNodeConstant(scopedLabel);
				} else {
					/*
					 * The constant is a literal, we need to find if its
					 * rdfs:Literal or a normal literal and construct it
					 * properly.
					 */
					if (type == COL_TYPE.LITERAL) {
						String value = set.getString(name);
						String language = set.getString(name + "Lang");
						if (language == null || language.trim().equals("")) {
							result = fac.getValueConstant(value);
						} else {
							result = fac.getValueConstant(value, language);
						}
					} else if (type == COL_TYPE.BOOLEAN) {
						boolean value = set.getBoolean(name);
						if (value) {
							result = fac.getValueConstant("true", type);
						} else {
							result = fac.getValueConstant("false", type);
						}
					} else if (type == COL_TYPE.DOUBLE) {
						double d = set.getDouble(name);
						// format name into correct double representation
						DecimalFormat formatter = new DecimalFormat("0.0###E0");
						DecimalFormatSymbols symbol = DecimalFormatSymbols
								.getInstance();
						symbol.setDecimalSeparator('.');
						formatter.setDecimalFormatSymbols(symbol);
						String s = formatter.format(d);
						result = fac.getValueConstant(s, type);

					} else if (type == COL_TYPE.DATETIME) {
						Timestamp value = set.getTimestamp(name);
						result = fac.getValueConstant(
								value.toString().replace(' ', 'T'), type);
					} else {
						result = fac.getValueConstant(realValue, type);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			Throwable cause = e.getCause();
			if (cause instanceof URISyntaxException) {
				OBDAException ex = new OBDAException(
						"Error creating an object's URI. This is often due to mapping with URI templates that refer to " +
						"columns in which illegal values may appear, e.g., white spaces and special characters.\n" +
						"To avoid this error do not use these columns for URI templates in your mappings, or process " +
						"them using SQL functions (e.g., string replacement) in the SQL queries of your mappings.\n\n" +
						"Note that this last option can be bad for performance, future versions of Quest will allow to " +
						"string manipulation functions in URI templates to avoid these performance problems.\n\n" +
						"Detailed message: " + cause.getMessage());
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			} else {
				OBDAException ex = new OBDAException(
						"Quest couldn't parse the data value to Java object: " + realValue + "\n" +
						"Please review the mapping rules to have the datatype assigned properly.");
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
		return result;
	}

	private COL_TYPE getQuestType(byte sqltype) {
		if (sqltype == 1) {
			return COL_TYPE.OBJECT;
		} else if (sqltype == 2) {
			return COL_TYPE.BNODE;
		} else if (sqltype == 3) {
			return COL_TYPE.LITERAL;
		} else if (sqltype == 4) {
			return COL_TYPE.INTEGER;
		} else if (sqltype == 5) {
			return COL_TYPE.DECIMAL;
		} else if (sqltype == 6) {
			return COL_TYPE.DOUBLE;
		} else if (sqltype == 7) {
			return COL_TYPE.STRING;
		} else if (sqltype == 8) {
			return COL_TYPE.DATETIME;
		} else if (sqltype == 9) {
			return COL_TYPE.BOOLEAN;
		} else if (sqltype == 0) {
			return null;
		} else {
			throw new RuntimeException("COLTYPE unknown: " + sqltype);
		}
	}

	@Override
	public URI getURI(String name) throws OBDAException {
		String result = "";
		try {
			result = set.getString(name);

			return URI.create(result);// .replace(' ', '_'));

		} catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public IRI getIRI(String name) throws OBDAException {
		String result = "";
		int id = 0;
		try {
			result = set.getString(name);
			if (isSemIndex) {
				try {
					id = Integer.parseInt(result);
				} catch (NumberFormatException e) {
					// its not a number - its a URI
					IRIFactory irif = new IRIFactory();
					IRI iri = irif.create(result);
					return iri;
				}

				if (id == 0) result = "0";
				else result = (String) uriMap.get(new Integer(id));
			}
			IRIFactory irif = new IRIFactory();
			IRI iri = irif.create(result);
			return iri;
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public ValueConstant getLiteral(String name) throws OBDAException {
		Constant result;

		result = getConstant(name);

		return (ValueConstant) result;
	}

	@Override
	public BNode getBNode(String name) throws OBDAException {
		Constant result;
		result = getConstant(name);
		return (BNode) result;
	}
}