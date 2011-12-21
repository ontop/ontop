package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.ViewDefinition;
import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.TablePrimary;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLQueryTranslator {

	private DBMetadata dbMetaData;
	
	private static int id_counter;
	
	private static Logger log = LoggerFactory.getLogger(SQLQueryTranslator.class);
	
	public SQLQueryTranslator(DBMetadata dbMetaData) {
		this.dbMetaData = dbMetaData;
		id_counter = 0;		
	}

	public QueryTree contructQueryTree(String query) {
		ANTLRStringStream inputStream = new ANTLRStringStream(query);
		SQL99Lexer lexer = new SQL99Lexer(inputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		SQL99Parser parser = new SQL99Parser(tokenStream);

		QueryTree queryTree = null;
		try {
			queryTree = parser.parse();
		} catch (RecognitionException e) {
			// Does nothing
		}
		
		if (parser.getNumberOfSyntaxErrors() != 0) {
			log.debug("Creating a view for query: {}", query);
			queryTree = createView(query);
		}		
		return queryTree;
	}
	
	private QueryTree createView(String query) {
		String viewName = String.format("view_%s", id_counter++);
		
		viewName = viewName.toUpperCase();
		
		ViewDefinition vd = createViewDefintion(viewName, query);
		dbMetaData.add(vd);
		
		QueryTree vt = createViewTree(viewName, query);
		return vt;
	}
		
	private ViewDefinition createViewDefintion(String viewName, String query) {
		int start = 6; // the keyword 'select'
		int end = query.toLowerCase().indexOf("from");		
		
		String projection = query.substring(start, end).trim();
		String[] columns = projection.split(",");
		
		ViewDefinition viewDefinition = new ViewDefinition();
		viewDefinition.setName(viewName);
		viewDefinition.copy(query);		
		for (int i = 0; i < columns.length; i++) {
			String columnName = columns[i].trim();
			if (columnName.contains(" as ")) {
				columnName = columnName.split(" as ")[1].trim();
			}
			viewDefinition.setAttribute(i+1, new Attribute(columnName)); // the attribute index always start at 1
		}
		return viewDefinition;
	}
	
	private QueryTree createViewTree(String viewName, String query) {		
		TablePrimary view = new TablePrimary(viewName);
		QueryTree queryTree = new QueryTree(new Relation(view));

		return queryTree;
	}
}
