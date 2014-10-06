package org.semanticweb.ontop.mongo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.io.SimplePrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.impl.DataSourceImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.parser.TargetQueryParserException;
import org.semanticweb.ontop.parser.TurtleOBDASyntaxParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Set;

public class MongoMappingParser implements MappingParser {

	private final static String prefixesKey = "prefixes";
	private final static String mappingsKey = "mappings";
	private final static String mappingIdKey = "mappingId";
	private final static String sourceKey = "source";
	private final static String targetKey = "target";
	private final static String collectionKey = "collection";
	private final static String criteriaKey = "criteria";

	private final static String databaseKey = "db";
	private static final String dbUrlKey = "url";
	private static final String dbUsernameKey = "username";
	private static final String dbPasswordKey = "password";
	private static final String dbDriverKey = "driver";
    
    private final String mappingDocument;
    
    private final PrefixManager prefixManager;
    
    
    // Error messages
    final static String INVALID_MAPPING_DOCUMENT = "The root of the mapping document is not a Json Object";
    
    final static String INVALID_PREFIXES_TYPE = "The " + prefixesKey + " element is not a Json Object";
    final static String INVALID_PREFIX_VALUE_TYPE = "The value of the key (%s) is not a String";
    
    final static String MISSING_MAPPINGS_KEY = "A mapping document must contain a " + mappingsKey + " key";
    final static String INVALID_MAPPINGS_TYPE = "The " + mappingsKey + " element is not a Json Array";
    
	static final String INVALID_MAPPING_TYPE = "The mapping element is not a Json Object";
	static final String MISSING_ONE_OF_MAPPING_KEYS = "A mapping object should contain " + mappingIdKey + ", " + sourceKey + " and " + targetKey + " keys";
	static final String INVALID_MAPPING_ID_TYPE = "The value of " + mappingIdKey + " is not a String";
	static final String INVALID_MAPPING_SOURCE_TYPE = "The value of " + sourceKey + " is not a Json Object";
	static final String INVALID_MAPPING_TARGET_TYPE = "The value of " + targetKey + " is not a String";
	
	static final String MISSING_ONE_OF_SOURCE_QUERY_KEYS = "A source query object should contain " + collectionKey + " and " + criteriaKey + " keys";
	static final String INVALID_SOURCE_COLLECTION_TYPE = "The value of " + collectionKey + " is not a String";
	static final String INVALID_SOURCE_CRITERIA_TYPE = "The value of " + criteriaKey + " is not a Json Object";

	static final String MISSING_DATABASE_KEY = "A mapping document must contain a " + databaseKey + " key";
	static final String INVALID_DATABASE_TYPE = "The database element is not a Json Object";
	static final String MISSING_ONE_OF_DATABASE_KEYS = "A database object should contain " + dbUrlKey + ", " + dbUsernameKey + ", " + dbPasswordKey + " and " + dbDriverKey + " keys";
	static final String INVALID_DATABASE_VALUE_TYPE = "The values of a database object should be Strings";
	
	
	
    public MongoMappingParser(String mappingDocument){
        this.mappingDocument = mappingDocument;
        this.prefixManager = new SimplePrefixManager(); 
    }

    public MongoMappingParser(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) { // while loop begins here
            sb.append(line).append("\n");
        }
        br.close();
        
        this.mappingDocument = sb.toString();
        this.prefixManager = new SimplePrefixManager();
    }


    public PrefixManager getPrefixManager(){
    	return this.prefixManager;
    }
    
    /*
     * parses the mappingDocument as a list of mapping axioms
     */
    @Override
    public List<OBDAMappingAxiom> parse() throws InvalidMongoMappingException  {
    	JsonParser parser = new JsonParser();
    	JsonElement root = parser.parse(mappingDocument);
  	
    	if (!root.isJsonObject()) {
    		throw new InvalidMongoMappingException(root, INVALID_MAPPING_DOCUMENT );
    	}
    	
    	return parse(root.getAsJsonObject());
    }
    
    // TODO: change the return type of parse
    private List<OBDAMappingAxiom>  parse(JsonObject rootObject) throws InvalidMongoMappingException {
    	// parse database connection parameters
    	if (!rootObject.has(databaseKey)) {
    		throw new InvalidMongoMappingException(rootObject, MISSING_DATABASE_KEY);
    	}
    	OBDADataSource dataSource = readDatabaseDeclaration(rootObject.get(databaseKey));
	    
    	// parse prefixes, if they are given
    	if (rootObject.has(prefixesKey)) {
    		readPrefixDeclaration(rootObject.get(prefixesKey));
    	}
    	
    	// check whether there is a mappings entry, if yes, parse them
    	if (!rootObject.has(mappingsKey)) {
			throw new InvalidMongoMappingException(rootObject, MISSING_MAPPINGS_KEY);
    	}    		
    	return readMappingsDeclaration(rootObject.get(mappingsKey));
    }

    private OBDADataSource readDatabaseDeclaration(JsonElement dbElement) throws InvalidMongoMappingException {
		if (!dbElement.isJsonObject()) {
			throw new InvalidMongoMappingException(dbElement, INVALID_DATABASE_TYPE);
		}
		
		JsonObject dbObject = dbElement.getAsJsonObject();
		
		// check that all keys (url, username, password and driver) are present
		if (! (dbObject.has(dbUrlKey) && dbObject.has(dbUsernameKey)) && dbObject.has(dbPasswordKey) && dbObject.has(dbDriverKey)) {
			throw new InvalidMongoMappingException(dbObject, MISSING_ONE_OF_DATABASE_KEYS);
		}

		// all values should be Strings
		if (!  (dbObject.get(dbUrlKey).isJsonPrimitive() && dbObject.get(dbUrlKey).getAsJsonPrimitive().isString() &&
				dbObject.get(dbUsernameKey).isJsonPrimitive() && dbObject.get(dbUsernameKey).getAsJsonPrimitive().isString() &&
				dbObject.get(dbPasswordKey).isJsonPrimitive() && dbObject.get(dbPasswordKey).getAsJsonPrimitive().isString() &&
				dbObject.get(dbDriverKey).isJsonPrimitive() && dbObject.get(dbDriverKey).getAsJsonPrimitive().isString()) ) {			
			throw new InvalidMongoMappingException(dbObject, INVALID_DATABASE_VALUE_TYPE);
		}
		
		String dbUrl = dbObject.get(dbUrlKey).getAsString();
		String userName= dbObject.get(dbUsernameKey).getAsString();
		String password= dbObject.get(dbPasswordKey).getAsString();
		String driver = dbObject.get(dbDriverKey).getAsString();

		// TODO: refactor this, do not use this factory
		OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();
		return dataFactory.getJDBCDataSource(dbUrl,userName, password, driver);
	}

	/*
     * parses the set of mappings
     */
	private List<OBDAMappingAxiom> readMappingsDeclaration(JsonElement mappingsElement) throws InvalidMongoMappingException {
		
		if (!mappingsElement.isJsonArray()) {
			throw new InvalidMongoMappingException(mappingsElement, INVALID_MAPPINGS_TYPE);
		}

		List<OBDAMappingAxiom> mappingsList = new ArrayList<>();

		JsonArray mappings = mappingsElement.getAsJsonArray();
		for (JsonElement mapping : mappings) {
			mappingsList.add( readMappingDeclaration(mapping) );
		}

		return mappingsList;
	}

	/*
	 * parses one mapping
	 */
	private OBDAMappingAxiom readMappingDeclaration(JsonElement mappingElement) throws InvalidMongoMappingException {
		// check that mappingElement is a Json Object (not a primitive)
		if (!mappingElement.isJsonObject()) {
			throw new InvalidMongoMappingException(mappingElement, INVALID_MAPPING_TYPE);
		}
		
		JsonObject mappingObject = mappingElement.getAsJsonObject();
		
		// check that all keys (mappingId, source and target) are present
		if (! (mappingObject.has(mappingIdKey) && mappingObject.has(sourceKey)) && mappingObject.has(targetKey)) {
			throw new InvalidMongoMappingException(mappingObject, MISSING_ONE_OF_MAPPING_KEYS);
		}

		// the value of mappingId should be a String
		if (! (mappingObject.get(mappingIdKey).isJsonPrimitive() && mappingObject.get(mappingIdKey).getAsJsonPrimitive().isString()) ) {			
			throw new InvalidMongoMappingException(mappingObject, INVALID_MAPPING_ID_TYPE);
		}
	
		// the value of source should be a JsonObject
		if (! mappingObject.get(sourceKey).isJsonObject() ) {
			throw new InvalidMongoMappingException(mappingObject, INVALID_MAPPING_SOURCE_TYPE);
		}

		// the value of target should be a String
		if (! (mappingObject.get(targetKey).isJsonPrimitive() && mappingObject.get(targetKey).getAsJsonPrimitive().isString()) ) {
			throw new InvalidMongoMappingException(mappingObject, INVALID_MAPPING_TARGET_TYPE);
		}
		
		String mappingId = mappingObject.get(mappingIdKey).getAsString();
		MongoQuery sourceQuery = readSourceQuery(mappingObject.get(sourceKey).getAsJsonObject());
		CQIE targetQuery = readTargetQuery(mappingObject.get(targetKey));
		OBDAMappingAxiom axiom = new MongoMappingAxiom(mappingId, sourceQuery, targetQuery);
		return axiom;
	}

	private static MongoQuery readSourceQuery(JsonObject sourceQuery) throws InvalidMongoMappingException {

		if (! (sourceQuery.has(collectionKey) && sourceQuery.has(criteriaKey)) ) {
			throw new InvalidMongoMappingException(sourceQuery, MISSING_ONE_OF_SOURCE_QUERY_KEYS);
		}
		
		// the value of collection should be a String
		if (! (sourceQuery.get(collectionKey).isJsonPrimitive() && sourceQuery.get(collectionKey).getAsJsonPrimitive().isString()) ) {			
			throw new InvalidMongoMappingException(sourceQuery, INVALID_SOURCE_COLLECTION_TYPE);
		}
			
		// the value of criteria should be a JsonObject
		if (! sourceQuery.get(criteriaKey).isJsonObject() ) {
			throw new InvalidMongoMappingException(sourceQuery, INVALID_SOURCE_CRITERIA_TYPE);
		}

		return new MongoQuery( sourceQuery.get(collectionKey).getAsString(), sourceQuery.get(criteriaKey).getAsJsonObject() );
	}

	/*
	 * Parses a target query string into an OBDAQuery by calling the parse method of TurtleOBDASyntaxParser
	 * 
	 * Here it is assumed that targetElement has been already checked to be a Primitive, which is a String
	 */
	private CQIE readTargetQuery(JsonElement targetElement) throws InvalidMongoMappingException {

		TurtleOBDASyntaxParser targetParser = new TurtleOBDASyntaxParser(prefixManager);
		try {
			return targetParser.parse(targetElement.getAsJsonPrimitive().getAsString());
		} catch (TargetQueryParserException e) {
			InvalidMongoMappingException mongoE = new InvalidMongoMappingException(targetElement, e.getMessage());
			mongoE.initCause(e);
			throw mongoE;
		}
	}

	private void readPrefixDeclaration(JsonElement prefixesElement) throws InvalidMongoMappingException {
		
		if (!prefixesElement.isJsonObject()) {
			throw new InvalidMongoMappingException(prefixesElement, INVALID_PREFIXES_TYPE);
		}
		
		Set<Entry<String, JsonElement>> prefixes = prefixesElement.getAsJsonObject().entrySet();
		for ( Entry<String, JsonElement> pair : prefixes) {
			if (! (pair.getValue().isJsonPrimitive() && pair.getValue().getAsJsonPrimitive().isString()) )
			{
				String message = String.format(INVALID_PREFIX_VALUE_TYPE, pair.getKey());
				throw new InvalidMongoMappingException(pair.getValue(), message);
			}
			prefixManager.addPrefix(pair.getKey(), pair.getValue().getAsString());
		}
	}
}
