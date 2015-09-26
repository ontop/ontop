package it.unibz.krdb.sql;

/**
 * Creates QuotedIdentifiers following the rules of SQL standard:<br>
 *    - unquoted identifiers are converted into upper case<br>
 *    - quoted identifiers are preserved
 * 
 * @author Roman Kontchakov
 *
 */

public class QuotedIDFactoryStandardSQL implements QuotedIDFactory {

	private static final String QUOTATION = "\"";
	
	@Override
	public QuotedID createFromString(String s) {
		if (s == null)
			return new QuotedID(s, QuotedID.NO_QUOTATION);
		
		if (s.startsWith("\"") && s.endsWith("\"")) 
			return new QuotedID(s.substring(1, s.length() - 1), QUOTATION);

		return new QuotedID(s.toUpperCase(), QuotedID.NO_QUOTATION);
	}

	@Override
	public QuotedID createFromDatabaseRecord(String s) {
		return new QuotedID(s, QUOTATION);
	}

	@Override
	public RelationID createRelationFromString(String s) {
		String[] names = s.split("\\.");
		if (names.length == 1)
			return new RelationID(createFromString(null), createFromString(s));
		else
			return new RelationID(createFromString(names[0]), createFromString(names[1]));			
	}

	@Override
	public RelationID createRelationFromDatabaseRecord(String schema, String table) {
		return new RelationID(createFromDatabaseRecord(schema), createFromDatabaseRecord(table));
	}

	@Override
	public RelationID createRelationFromString(String schema, String table) {
		return new RelationID(createFromString(schema), createFromString(table));			
	}

	// TODO: add check for the symbols in the id
	
}
