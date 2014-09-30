package org.semanticweb.ontop.mongo;

import org.semanticweb.ontop.exception.InvalidMappingException;

import com.google.gson.JsonElement;

public class InvalidMongoMappingException extends InvalidMappingException{

	private final JsonElement jsonElement;
	
	public InvalidMongoMappingException(JsonElement jsonElement){
		this(jsonElement, "");
	}
	
	public InvalidMongoMappingException(JsonElement jsonElement, String message){
		super(message);
		this.jsonElement = jsonElement;
	}
	
	@Override
	public String toString(){
		return getMessage() + " at " + jsonElement.toString() ;
		
	}
}
