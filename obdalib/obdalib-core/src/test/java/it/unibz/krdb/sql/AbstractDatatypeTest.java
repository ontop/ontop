package it.unibz.krdb.sql;

import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.utils.TypeMapper;
import it.unibz.krdb.sql.api.Attribute;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDatatypeTest extends TestCase {

	private static final String NUMERIC_TABLE = "Numeric";
	private static final String CHARACTER_TABLE = "Character";
	private static final String DATETIME_TABLE = "DateTime";
	private static final String BOOLEAN_TABLE = "Boolean";
	private static final String OTHERS_TABLE = "Others";
	
	private DBMetadata metadata;
	
	private static Logger log = LoggerFactory.getLogger(AbstractDatatypeTest.class);
	
	@Override
	public void setUp() {
		try {
			Class.forName(getDriverName());
		} 
		catch (ClassNotFoundException e) { /* NO-OP */ }
		
		try {
			Connection conn = DriverManager.getConnection(getConnectionString(), getConnectionUsername(), getConnectionPassword());
			metadata = JDBCConnectionManager.getMetaData(conn);
		} catch (SQLException e) { 
			e.printStackTrace();
		}
	}
	
	public void testNumericType() {
		final TypeMapper tm = TypeMapper.getInstance();
		DataDefinition dd = metadata.getDefinition(toString(NUMERIC_TABLE));
		
		boolean skipId = true;
		for (Attribute attr : dd.getAttributes()) {
			if (skipId) { skipId = false; continue; }
			int type = attr.getType();
			switch (type) {
				case Types.BIGINT: 
				case Types.INTEGER:
				case Types.SMALLINT:
				case Types.TINYINT: 
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "xsd:integer"); break;
				case Types.REAL: 
				case Types.FLOAT: 
				case Types.DOUBLE: 
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "xsd:double"); break;
				case Types.NUMERIC: 
				case Types.DECIMAL: 
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "xsd:decimal"); break;
				case Types.OTHER:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
				default:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
			}
		}
	}

	public void testCharacterType() {
		final TypeMapper tm = TypeMapper.getInstance();
		DataDefinition dd = metadata.getDefinition(toString(CHARACTER_TABLE));
		
		boolean skipId = true;
		for (Attribute attr : dd.getAttributes()) {
			if (skipId) { skipId = false; continue; }
			int type = attr.getType();
			switch (type) {
				case Types.CHAR: 
				case Types.VARCHAR:
				case Types.NCHAR:
				case Types.NVARCHAR: 
				case Types.LONGVARCHAR: 
				case Types.LONGNVARCHAR: 
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "xsd:string"); break;
				case Types.OTHER:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
				default:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
			}
		}
	}
	
	public void testDateTimeType() {
		final TypeMapper tm = TypeMapper.getInstance();
		DataDefinition dd = metadata.getDefinition(toString(DATETIME_TABLE));
		
		boolean skipId = true;
		for (Attribute attr : dd.getAttributes()) {
			if (skipId) { skipId = false; continue; }
			int type = attr.getType();
			switch (type) {
				case Types.DATE: 
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "xsd:date"); break;
				case Types.TIME:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "xsd:time"); break;
				case Types.TIMESTAMP:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "xsd:dateTime"); break;
				case Types.OTHER:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
				default:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
			}
		}
	}
	
	public void testBooleanType() {
		final TypeMapper tm = TypeMapper.getInstance();
		DataDefinition dd = metadata.getDefinition(toString(BOOLEAN_TABLE));
		
		boolean skipId = true;
		for (Attribute attr : dd.getAttributes()) {
			if (skipId) { skipId = false; continue; }
			int type = attr.getType();
			switch (type) {
				case Types.BIT: 
				case Types.BOOLEAN:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "xsd:boolean"); break;
				case Types.OTHER:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
				default:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
			}
		}
	}
	
	public void testOthersType() {
		final TypeMapper tm = TypeMapper.getInstance();
		DataDefinition dd = metadata.getDefinition(toString(OTHERS_TABLE));
		
		boolean skipId = true;
		for (Attribute attr : dd.getAttributes()) {
			if (skipId) { skipId = false; continue; }
			int type = attr.getType();
			switch (type) {
				case Types.OTHER:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
				default:
					writeLog(attr.getName(), type, tm.getPredicate(type).toString(), "rdf:plainLiteral"); break;
			}
		}
	}
	
	protected abstract String getDriverName();
	protected abstract String getConnectionString();
	protected abstract String getConnectionUsername();
	protected abstract String getConnectionPassword();
	protected abstract String toString(String name);
	
	private void writeLog(String attrName, int attrType, String questType, String xsdType) {
		log.info(String.format("Attribute \"%s\" SQL Type: %s, Quest Type: %s, XSD Type: %s", attrName, attrType, prefixed(questType), xsdType));
	}
	
	private String prefixed(String uri) {
		return uri.replace(OBDAVocabulary.NS_XSD, OBDAVocabulary.PREFIX_XSD);
	}
}
