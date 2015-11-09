package it.unibz.krdb.obda.parser;

import it.unibz.krdb.sql.QuotedIDFactory;
import it.unibz.krdb.sql.RelationID;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLQueryShallowParser {

	private static Logger log = LoggerFactory.getLogger(SQLQueryShallowParser.class);	
		
	private static int id_counter;
	
	/**
	 * Called from ParsedMapping. Returns the query, even if there were 
	 * parsing errors.
	 * 
	 * @param query The sql query to be parsed
	 * @return A ParsedSQLQuery (possible with null values)
	 */
	public static ParsedSQLQuery parse(QuotedIDFactory idfac, String query) {
    	
		ParsedSQLQuery parsedQuery = null;
		try {
			parsedQuery = new ParsedSQLQuery(query, false, idfac);
		} 
		catch (JSQLParserException e) {
			if (e.getCause() instanceof ParseException)
				log.warn("Parse exception, check no SQL reserved keywords have been used "+ e.getCause().getMessage());
		}
		
		if (parsedQuery == null) {
			log.warn("The following query couldn't be parsed. " +
					"This means Quest will need to use nested subqueries (views) to use this mappings. " +
					"This is not good for SQL performance, specially in MySQL. " + 
					"Try to simplify your query to allow Quest to parse it. " + 
					"If you think this query is already simple and should be parsed by Quest, " +
					"please contact the authors. \nQuery: '{}'", query);
			
			RelationID viewId = idfac.createRelationID(null, String.format("view_%s", id_counter++));
			parsedQuery = SQLQueryDeepParser.createParsedSqlForGeneratedView(idfac, viewId);	
		}
		return parsedQuery;
	}
}
