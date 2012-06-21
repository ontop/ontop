package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OWLOBDARefResultSet implements OBDAResultSet {

	private ResultSet set = null;
	private OBDAStatement st;
	private Vector<String> signature;

	private HashMap<String, Integer> columnMap = new HashMap<String, Integer>();
	private List<Term> signatureTyping;

	private HashMap<String, Term> typingMap = new HashMap<String, Term>();

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private static final Logger log = LoggerFactory.getLogger(OWLOBDARefResultSet.class);

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
	public OWLOBDARefResultSet(ResultSet set, List<String> signature, List<Term> signatureTyping, OBDAStatement st) throws OBDAException {
		this.set = set;
		this.st = st;

		this.signatureTyping = new ArrayList<Term>();
		this.signatureTyping.addAll(signatureTyping);

		int i = signatureTyping.size();
		this.signature = new Vector<String>(signature);
		for (int j = 1; j <= i; j++) {
			columnMap.put(signature.get(j - 1), j - 1);
			typingMap.put(signature.get(j - 1), signatureTyping.get(j - 1));
		}

	}

	/***
	 * Returns a COL_TYPE that describes the type of the given column in this
	 * result set. The type is determined by the mapping of RDF Datatypes to
	 * COL_TYPE see in OBDAVocabulary.
	 * 
	 * @param column
	 * @return
	 */
	public COL_TYPE getType(int column) {
		Term term = signatureTyping.get(column);
		if (term instanceof Variable) {
			// Variables without a data-type function have no data-type!
			return null;
		} else if (term instanceof Constant) {
			Constant constant = (Constant) term;
			if (constant instanceof ValueConstant) {
				return ((ValueConstant) constant).getType();
			} else if (constant instanceof URIConstant) {
				return COL_TYPE.OBJECT;
			} else if (constant instanceof BNode) {
				return COL_TYPE.BNODE;
			}
		} else if (term instanceof Function) {
			Predicate function = ((Function) term).getFunctionSymbol();
			if (function == OBDAVocabulary.XSD_BOOLEAN) {
				return COL_TYPE.BOOLEAN;
			} else if (function == OBDAVocabulary.XSD_DATETIME) {
				return COL_TYPE.DATETIME;
			} else if (function == OBDAVocabulary.XSD_DECIMAL) {
				return COL_TYPE.DECIMAL;
			} else if (function == OBDAVocabulary.XSD_DOUBLE) {
				return COL_TYPE.DOUBLE;
			} else if (function == OBDAVocabulary.XSD_INTEGER) {
				return COL_TYPE.INTEGER;
			} else if (function == OBDAVocabulary.XSD_STRING) {
				return COL_TYPE.STRING;
			} else if (function == OBDAVocabulary.RDFS_LITERAL) {
				return COL_TYPE.LITERAL;
			} else if (function.getName().equals(OBDAVocabulary.QUEST_URI)) {
				return COL_TYPE.OBJECT;
			} else if (function.getName().equals(OBDAVocabulary.QUEST_BNODE)) {
				return COL_TYPE.BNODE;
			}
		}
		// For other kind of term class.
		return null;
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
			throw new OBDAException(e.getMessage());
		}
	}

	public void close() throws OBDAException {
		try {
			set.close();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
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
		int index = columnMap.get(name);
		COL_TYPE type = getType(index);
		Constant result = null;

		try {
			if (type == COL_TYPE.OBJECT || type == null) {
				String value = set.getString(name);
				result = fac.getURIConstant(URI.create(value));
			} else if (type == COL_TYPE.BNODE) {
				result = fac.getBNodeConstant(set.getString(name));
			} else {
				/*
				 * The constant is a literal, we need to find if its
				 * rdfs:Literal or a normal literal and construct it properly.
				 */
				if (type == COL_TYPE.LITERAL) {
					String value = set.getString(name);
					String language = set.getString(name + "LitLang");
					if (language == null || language.trim().equals("")) {
						result = fac.getValueConstant(value);
					} else {
						result = fac.getValueConstant(value, language);
					}

				} else if (type == COL_TYPE.BOOLEAN) {
					boolean value = set.getBoolean(name);
					if (value)
						result = fac.getValueConstant("true", type);
					else
						result = fac.getValueConstant("false", type);

				} else if (type == COL_TYPE.DATETIME) {
					Timestamp value = set.getTimestamp(name);
					result = fac.getValueConstant(value.toString().replace(' ', 'T'), type);
					
//					if (value)
//						result = fac.getValueConstant("true", type);
//					else
//						result = fac.getValueConstant("false", type);

				} else {

					result = fac.getValueConstant(set.getString(name), type);
				}
			}
		} catch (IllegalArgumentException e) {
			Throwable cause = e.getCause();
			if (cause instanceof URISyntaxException) {
				throw new OBDAException("Error creating an object's URI. This is often due to mapping with URI templates that refer to columns in which illegal values may appear, e.g., white spaces and special characters. To avoid this error do not use these columns for URI templates in your mappings, or process them using SQL functions (e.g., string replacement) in the SQL queries of your mappings. Note that this last option can be bad for performance, future versions of Quest will allow to string manipulation functions in URI templates to avoid these performance problems." + "\n\nDetailed message: " + cause.getMessage());
			}
			throw e;			
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
		return result;
	}

	@Override
	public URI getURI(String name) throws OBDAException {
		try {
			return URI.create(set.getString(name));
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
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
