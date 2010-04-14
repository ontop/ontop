/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.dl.domain;

import java.net.URI;

public abstract class NamedEntity implements Cloneable, Entity {

	private URI	uri	= null; // This should be an absolute URI in the form of prefix:name
	
	//TODO implement prefix:name mechanism

	public NamedEntity(URI uri) {
		this.setUri(uri);
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
	}
	
	public String getNamespacePrefix() {
		return "";
	}
	
	public String getName() {
		String uristr = uri.toString();
		String[] split = uristr.split("#");
		String name = null;
		if (split.length == 1) {
			//No # symbol, looking for a URI of the form prefix:Name or :Name or Name
			name = split[0];
		} else if (split.length == 2){
			//it is a full URI as jsdf://asdfl;kasdlkfj/asdf#name
			name = split[1].trim();
		} else {
			//invalid URL
		}
		return name;
	}
	
//	public abstract Entity clone();
	
	
	public String toString() {
		return uri.toString();
	}
	
}
