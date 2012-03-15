package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	 * @throws SQLException
	 */
	public OWLOBDARefResultSet(ResultSet set, List<String> signature, List<Term> signatureTyping, OBDAStatement st) throws SQLException {
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
	private COL_TYPE getType(int column) {
		Term type = signatureTyping.get(column);
		if (!(type instanceof Function)) {
			// The column is not typed
			return null;
		}
		Predicate function = ((Function) type).getFunctionSymbol();
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
		return COL_TYPE.OBJECT;
	}

	public double getDouble(int column) throws SQLException {
		return set.getDouble(signature.get(column - 1));
	}

	public int getInt(int column) throws SQLException {
		return set.getInt(signature.get(column - 1));
	}

	public Object getObject(int column) throws SQLException {
		return set.getObject(signature.get(column - 1));
	}

	public String getString(int column) throws SQLException {
		return set.getString(signature.get(column - 1));
	}

	public URI getURI(int column) throws SQLException {
		return getURI(signature.get(column-1));
	}

	public int getColumCount() throws SQLException {
		return signature.size();
	}

	public int getFetchSize() throws SQLException {
		return set.getFetchSize();
	}

	public List<String> getSignature() throws SQLException {
		return signature;
	}

	public boolean nextRow() throws SQLException {
		return set.next();
	}

	public void close() throws SQLException {
		set.close();
	}

	@Override
	public OBDAStatement getStatement() {
		return st;
	}

	@Override
	public Constant getConstant(int column) throws SQLException, URISyntaxException {
		return getConstant(signature.get(column-1));
	}

	@Override
	public ValueConstant getLiteral(int column) throws SQLException {
		return getLiteral(signature.get(column-1));
	}

	@Override
	public BNode getBNode(int column) throws SQLException {
		return getBNode(signature.get(column-1));
	}

	@Override
	public Constant getConstant(String name) throws SQLException, URISyntaxException {
		int index = columnMap.get(name);
		COL_TYPE type = getType(index);
		Constant result = null;

		if (type == COL_TYPE.OBJECT) {
			result = fac.getURIConstant(new URI(set.getString(name)));
		} else if (type == COL_TYPE.BNODE) {
			result = fac.getBNodeConstant(set.getString(name));
		} else {
			/*
			 * The constant is a literal, we need to find if its rdfs:Literal or
			 * a normal literal and construct it properly.
			 */
			if (type == COL_TYPE.LITERAL) {
				// Function ftype = (Function)typingMap.get(index);
				// Term valueterm = ftype.getTerms().get(0);
				// Term langterm = ftype.getTerms().get(1);
				String value = set.getString(name);
				String language = set.getString(name + "LitLang");
				result = fac.getValueConstant(value, language);

			} else {
				result = fac.getValueConstant(set.getString(name), type);
			}
		}

		return result;
	}

	@Override
	public URI getURI(String name) throws SQLException {
		return URI.create(set.getString(name));
	}

	@Override
	public ValueConstant getLiteral(String name) throws SQLException {
		Constant result;
		try {
			result = getConstant(name);
		} catch (URISyntaxException e) {
			// This should never happen
			log.error("Error casting column {}: {} ", name, e.getMessage());
			return null;
		}
		return (ValueConstant) result;
	}

	@Override
	public BNode getBNode(String name) throws SQLException {
		Constant result;
		try {
			result = getConstant(name);
		} catch (URISyntaxException e) {
			// This should never happen
			log.error("Error casting column {}: {} ", name, e.getMessage());
			return null;
		}
		return (BNode) result;
	}
}
