package org.semanticweb.ontop.model;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.net.URI;

public interface OBDAMappingListener extends Serializable {
	
	/**
	 * Called when a mapping has been inserted into the currently selected data source.
	 */
	public void mappingInserted(URI srcid, String mapping_id);
	
	/**
	 * Called when a mapping has been deleted into the currently selected data source.
	 */
	public void mappingDeleted(URI srcid, String mapping_id);
		
	/**
	 * Called when a mapping has been updated into the currently selected datasource.
	 */
	public void mappingUpdated(URI srcid, String mapping_id, OBDAMappingAxiom mapping);

	/**
	 * Called when the current data sources has changed.
	 */
	public void currentSourceChanged(URI oldsrcid, URI newsrcid);
	
	/**
	 * Called when all mappings were removed, for all datasources.
	 */
	public void allMappingsRemoved();
}
