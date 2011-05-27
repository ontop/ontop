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
package inf.unibz.it.obda.model;
import java.net.URI;


/****
 * Defines the methods that an API coupler should provide
 * @author Mariano Rodriguez Muro
 *
 */
public interface APICoupler {

	
	/**
	 * Checks whether the given data property belongs to the given ontology
	 * 
	 * @param ontouri the ontology URI
	 * @param propertyURI the data property URI
	 * @return true if the data property belongs to the given ontolgoy, false otherwise
	 */
	
//	public boolean isDatatypeProperty(URI ontouri,URI propertyURI);
	
	public boolean isDatatypeProperty(URI propertyURI);
	
	/**
	 * Checks whether the given object property belongs to the given ontology
	 * 
	 * @param ontouri the ontology URI
	 * @param propertyURI the object property URI
	 * @return true if the object property belongs to the given ontolgoy, false otherwise
	 */
//	public boolean isObjectProperty(URI ontouri,URI propertyURI);
	public boolean isObjectProperty(URI propertyURI);
	
	/**
	 * Checks whether the given concept belongs to the given ontology
	 * 
	 * @param ontouri the ontology URI
	 * @param propertyURI the concept URI
	 * @return true if the concept belongs to the given ontolgoy, false otherwise
	 */
//	public boolean isNamedConcept(URI ontouri,URI propertyURI);
	public boolean isNamedConcept(URI classURI);
	
//	/**
//	 * Returns the the assigned prefix for the given Ontology URI.
//	 * @param uri the ontology RUI
//	 * @return the assigned prefix or an empty String if no prefix is registered
//	 */
//	public String getPrefixForUri(URI uri);
//	
//	/**
//	 * Returns the ontology URI to which the given prefix was assigned
//	 * @param prefix the prefix
//	 * @return the ontology URI or null if the prefix has no assigned ontology
//	 */
//	public String getUriForPrefix(String prefix);
//	
//	/**
//	 * Removes the given onotlogy from the coupler
//	 * @param ontouri the ontolgy URI of the removed ontology.
//	 */
//	public void removeOntology(URI ontouri);
}
