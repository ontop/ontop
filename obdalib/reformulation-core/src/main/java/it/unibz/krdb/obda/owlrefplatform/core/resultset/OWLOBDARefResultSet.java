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

	private HashMap<Integer, COL_TYPE> typingMap = new HashMap<Integer, Predicate.COL_TYPE>();

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
	public OWLOBDARefResultSet(ResultSet set, List<Term> signatureTyping, OBDAStatement st) throws SQLException {
		this.set = set;
		this.st = st;

		this.signatureTyping = new ArrayList<Term>();
		this.signatureTyping.addAll(signatureTyping);
		
		int i = getColumCount();
		signature = new Vector<String>();
		for (int j = 1; j <= i; j++) {
			String columnLabel = set.getMetaData().getColumnLabel(j);
			signature.add(columnLabel);
			columnMap.put(columnLabel, j);
			// Initializing the coltype buffer
			getType(j);
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
		Term type = signatureTyping.get(column - 1);
		if (!(type instanceof Function)) {
			// The column is not typed
			return null;
		}
		Function function = (Function) type;
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
		} else if (function.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_URI)) {
			return COL_TYPE.OBJECT;
		} else if (function.getFunctionSymbol().getName().equals(OBDAVocabulary.QUEST_BNODE)) {
			return COL_TYPE.BNODE;
		}
		return COL_TYPE.OBJECT;
	}

	public double getDouble(int column) throws SQLException {
		return set.getDouble(column);
	}

	public int getInt(int column) throws SQLException {
		return set.getInt(column);
	}

	public Object getObject(int column) throws SQLException {
		return set.getObject(column);
	}

	public String getString(int column) throws SQLException {
		return set.getString(column);
	}

	public URI getURI(int column) throws SQLException {
		return URI.create(set.getString(column));
	}

	public int getColumCount() throws SQLException {
		return set.getMetaData().getColumnCount();
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

		COL_TYPE type = typingMap.get(column);
		Constant result = null;

		if (type == COL_TYPE.OBJECT) {
			result = fac.getURIConstant(new URI(set.getString(column)));
		} else if (type == COL_TYPE.BNODE) {
			result = fac.getBNodeConstant(set.getString(column));
		} else {
			result = fac.getValueConstant(set.getString(column), type);
		}

		return result;
	}

	@Override
	public ValueConstant getLiteral(int column) throws SQLException {
		Constant result;
		try {
			result = getConstant(column);
		} catch (URISyntaxException e) {
			// This should never happen
			log.error("Error casting column {}: {} ", column, e.getMessage());
			return null;
		}
		return (ValueConstant) result;
	}

	@Override
	public BNode getBNode(int column) throws SQLException {
		Constant result;
		try {
			result = getConstant(column);
		} catch (URISyntaxException e) {
			// This should never happen
			log.error("Error casting column {}: {} ", column, e.getMessage());
			return null;
		}
		return (BNode) result;
	}

	@Override
	public Constant getConstant(String name) throws SQLException, URISyntaxException {
		return getConstant(this.columnMap.get(name));
	}

	@Override
	public URI getURI(String name) throws SQLException {
		return getURI(this.columnMap.get(name));
	}

	@Override
	public ValueConstant getLiteral(String name) throws SQLException {
		return getLiteral(this.columnMap.get(name));
	}

	@Override
	public BNode getBNode(String name) throws SQLException {
		return getBNode(this.columnMap.get(name));
	}
}
