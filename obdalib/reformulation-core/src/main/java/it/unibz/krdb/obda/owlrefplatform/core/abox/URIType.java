package it.unibz.krdb.obda.owlrefplatform.core.abox;

public enum URIType {
	CONCEPT,OBJECTPROPERTY,DATAPROPERTY, INDIVIDUAL;
	
	public static URIType getURIType(String s) throws Exception{
		if(s.toLowerCase().equals("concept")){
			return CONCEPT;
		}else if(s.toLowerCase().equals("objectproperty")){
			return OBJECTPROPERTY;
		}else if(s.toLowerCase().equals("dataproperty")){
			return DATAPROPERTY;
		}else if(s.toLowerCase().equals("individual")){
			return INDIVIDUAL;
		}else{
			throw new Exception( s + " is not a valid URI type");
		}
	}
}
