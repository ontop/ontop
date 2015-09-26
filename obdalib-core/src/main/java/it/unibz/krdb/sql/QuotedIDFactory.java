package it.unibz.krdb.sql;

public interface QuotedIDFactory {

	QuotedID createFromString(String s);
	
	QuotedID createFromDatabaseRecord(String s);
	
	RelationID createRelationFromString(String s);

	RelationID createRelationFromString(String schema, String table);
	
	RelationID createRelationFromDatabaseRecord(String schema, String table);
	
}
