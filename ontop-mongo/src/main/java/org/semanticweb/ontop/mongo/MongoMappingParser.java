package org.semanticweb.ontop.mongo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.io.SimplePrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.parser.TargetQueryParserException;
import org.semanticweb.ontop.parser.TurtleOBDASyntaxParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
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
	
    private final String mappingDocument;
    
    private final PrefixManager prefixManager;

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
    		throw new InvalidMongoMappingException(root, "root of the mapping document is not a Json Object");
    	}
    	
    	return parse(root.getAsJsonObject());
    }
    
    private List<OBDAMappingAxiom>  parse(JsonObject rootObject) throws InvalidMongoMappingException {
    	// parse prefixes, if they are given
    	if (rootObject.has(prefixesKey)) {
    		readPrefixDeclaration(rootObject.get(prefixesKey));
    	}
    	
    	// check whether there is a mappings entry, if yes, parse them
    	if (!rootObject.has(mappingsKey)) {
			throw new InvalidMongoMappingException(rootObject, "A mapping document must contain a " + mappingsKey + " key");
    	}    		
    	return readMappingsDeclaration(rootObject.get(mappingsKey));
    }

    /*
     * parses the set of mappings
     */
	private List<OBDAMappingAxiom> readMappingsDeclaration(JsonElement mappingsElement) throws InvalidMongoMappingException {
		
		if (!mappingsElement.isJsonArray()) {
			throw new InvalidMongoMappingException(mappingsElement, "The " + mappingsKey + " element is not a Json Array");
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
			throw new InvalidMongoMappingException(mappingElement, "The mapping element is not a Json Object");
		}
		
		JsonObject mappingObject = mappingElement.getAsJsonObject();
		
		// check that all keys (mappingId, source and target) are present
		if (! (mappingObject.has(mappingIdKey) && mappingObject.has(sourceKey)) && mappingObject.has(targetKey)) {
			throw new InvalidMongoMappingException(mappingObject, "A mapping object should contain " + mappingIdKey + ", " + sourceKey + " and " + targetKey + " keys");
		}

		// the value of mappingId should be a String
		if (! (mappingObject.get(mappingIdKey).isJsonPrimitive() && mappingObject.get(mappingIdKey).getAsJsonPrimitive().isString()) ) {			
			throw new InvalidMongoMappingException(mappingObject, "The value of " + mappingIdKey + " is not a String");
		}
	
		// the value of source should be a JsonObject
		if (! mappingObject.get(sourceKey).isJsonObject() ) {
			throw new InvalidMongoMappingException(mappingObject, "The value of " + sourceKey + " is not a Json Object");
		}

		// the value of target should be a String
		if (! (mappingObject.get(targetKey).isJsonPrimitive() && mappingObject.get(targetKey).getAsJsonPrimitive().isString()) ) {
			throw new InvalidMongoMappingException(mappingObject, "The value of " + targetKey + " is not a String");
		}
		
		String mappingId = mappingObject.get(mappingIdKey).getAsString();
		OBDAMappingAxiom axiom = new MongoMappingAxiom(mappingId);
		axiom.setSourceQuery(readSourceQuery(mappingObject.get(sourceKey).getAsJsonObject()));
		axiom.setTargetQuery(readTargetQuery(mappingObject.get(targetKey)));
		return axiom;
	}

	private static OBDAQuery readSourceQuery(JsonObject sourceQuery) throws InvalidMongoMappingException {

		if (! (sourceQuery.has(collectionKey) && sourceQuery.has(criteriaKey)) ) {
			throw new InvalidMongoMappingException(sourceQuery, "A source query object should contain " + collectionKey + " and " + criteriaKey + " keys");
		}
		
		// the value of collection should be a String
		if (! (sourceQuery.get(collectionKey).isJsonPrimitive() && sourceQuery.get(collectionKey).getAsJsonPrimitive().isString()) ) {			
			throw new InvalidMongoMappingException(sourceQuery, "The value of " + collectionKey + " is not a String");
		}
			
		// the value of criteria should be a JsonObject
		if (! sourceQuery.get(criteriaKey).isJsonObject() ) {
			throw new InvalidMongoMappingException(sourceQuery, "The value of " + criteriaKey + " is not a Json Object");
		}

		return new MongoQuery( sourceQuery.get(collectionKey).getAsString(), sourceQuery.get(criteriaKey).getAsJsonObject() );
	}

	/*
	 * Parses a target query string into an OBDAQuery by calling the parse method of TurtleOBDASyntaxParser
	 * 
	 * Here it is assumed that targetElement has been already checked to be a Primitive, which is a String
	 */
	private OBDAQuery readTargetQuery(JsonElement targetElement) throws InvalidMongoMappingException {

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
			throw new InvalidMongoMappingException(prefixesElement, "The " + prefixesKey + " element is not a Json Object");
		}
		
		Set<Entry<String, JsonElement>> prefixes = prefixesElement.getAsJsonObject().entrySet();
		for ( Entry<String, JsonElement> pair : prefixes) {
			if (! (pair.getValue().isJsonPrimitive() && pair.getValue().getAsJsonPrimitive().isString()) )
			{
				throw new InvalidMongoMappingException(pair.getValue(), "The value of " + pair.getKey() + " key is not a String");
			}
			prefixManager.addPrefix(pair.getKey(), pair.getValue().getAsString());
		}
	}
}
