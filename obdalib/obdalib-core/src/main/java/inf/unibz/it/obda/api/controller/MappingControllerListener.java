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
package inf.unibz.it.obda.api.controller;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;

import java.net.URI;


public interface MappingControllerListener {
	
	/***
	 * Called when a mapping has been inserted into the currently selected datasource.
	 * @param mapping_id
	 */
	public void mappingInserted(URI srcid, String mapping_id);

	/***
	 * Called when a mapping has been deleted into the currently selected datasource.
	 * @param mapping_id
	 */
	public void mappingDeleted(URI srcid, String mapping_id);
	
	//public void mappingIdUpdated(String src_uri, String mapping_id, String new_mapping_id);
	
	/***
	 * Called when a mapping has been updated into the currently selected datasource.
	 */
	public void mappingUpdated(URI srcid, String mapping_id, OBDAMappingAxiom mapping);
	

	/***
	 * Called when the current data sources has changed.
	 * @param oldsrcuri
	 * @param newsrcuri
	 */
	public void currentSourceChanged(URI oldsrcid, URI newsrcid);
	
	
	/***
	 * Called when all mappings were removed, for all datasources.
	 */
	public void allMappingsRemoved();
	
	/**
	 * Called when the active ontology is changed. 
	 */
	public void ontologyChanged();
}
