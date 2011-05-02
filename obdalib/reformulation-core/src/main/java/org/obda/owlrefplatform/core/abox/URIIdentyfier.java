package org.obda.owlrefplatform.core.abox;

import java.net.URI;

public class URIIdentyfier{
	
	private URI uri = null;	
	private URIType type = null;
	
	public URIIdentyfier(URI uri, URIType type){
		this.uri = uri;
		this.type = type;
	}

	public URI getUri() {
		return uri;
	}

	public URIType getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		
		if((obj instanceof URIIdentyfier)){	
			if(uri.equals(((URIIdentyfier) obj).getUri()) && type ==((URIIdentyfier) obj).getType()){
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) (Math.pow(uri.hashCode(), 2) * Math.pow(type.hashCode(),2));
	}
	
	
}