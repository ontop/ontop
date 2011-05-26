/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.api.controller;

import inf.unibz.it.obda.api.controller.exception.DuplicateMappingException;
import inf.unibz.it.obda.codec.MappingXMLCodec;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.domain.Query;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingTreeModel;

import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MappingController {

	private ArrayList<MappingControllerListener>			listeners				= null;
	private Hashtable<URI, ArrayList<OBDAMappingAxiom>>	mappings				= null;

	private Hashtable<URI, Boolean>						needsSyncwithReasoner	= null;

	private final MappingTreeModel								treemodel				= null;

	private DatasourcesController							dscontroller			= null;

	private MappingXMLCodec									codec = null;

	Logger													log						= LoggerFactory.getLogger(MappingController.class);

	public MappingController(DatasourcesController dscontroller, APIController apic) {
		this.dscontroller = dscontroller;
		mappings = new Hashtable<URI, ArrayList<OBDAMappingAxiom>>();
		listeners = new ArrayList<MappingControllerListener>();
		codec = new MappingXMLCodec(apic);
		needsSyncwithReasoner = new Hashtable<URI, Boolean>();
	}

	public void addMappingControllerListener(MappingControllerListener listener) {
		listeners.add(listener);
	}

	public void alldatasourcesDeleted() {
		// TODO remove?

	}

	/***************************************************************************
	 * Implementation of the DatasourceControllerListener interface. Updates the
	 * local reference to the current datasource uri.
	 */
	public void currentDatasourceChange(DataSource previous, DataSource currentsource) {
		URI currentsourceName = null;
		if (currentsource != null) {
			currentsourceName = currentsource.getSourceID();
		}
		URI previousName = null;
		if (previous != null) {
			previousName = previous.getSourceID();
		}
		fireCurrentSourceChanged(previousName, currentsourceName);
	}

	/***************************************************************************
	 * Implementation of the DatasourceControllerListener interface. Does
	 * nothing
	 */
	public void datasourceAdded(DataSource source) {
	}

	/***************************************************************************
	 * Implementation of the DatasourceControllerListener interface. Should
	 * delete all mappings for the deleted source, remove any reference to it
	 * and finally tell the model to update itself if necessary.
	 */
	public void datasourceDeleted(DataSource source) {
		deleteMappings(source.getSourceID());
	}

	/***************************************************************************
	 * Implementation of the DatasourceControllerListener interface. Does
	 * nothing unless the updated field is the URI, then it must update the key
	 * for all the mappings related to that source.
	 */
	public void datasourceUpdated(String oldname, DataSource currendata) {
		ArrayList<OBDAMappingAxiom> axioms =mappings.get(oldname);
		mappings.remove(oldname);
		mappings.put(currendata.getSourceID(), axioms);
	}

	/***************************************************************************
	 * Deletes the mapping with ID id
	 *
	 * @param datasource_uri
	 * @param mapping_id
	 */
	public void deleteMapping(URI datasource_uri, String mapping_id) {
		int index = indexOfMapping(datasource_uri, mapping_id);
		if (index == -1) {
			return;
		} else {
			ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
			current_mappings.remove(index);
		}
fireMappingDeleted(datasource_uri, mapping_id);
	}

	/***************************************************************************
	 * Deletes all the mappings for a given datasource
	 *
	 * @param datasource_uri
	 */
	public void deleteMappings(URI datasource_uri) {
		ArrayList<OBDAMappingAxiom> mappings = getMappings(datasource_uri);
		while (!mappings.isEmpty()) {
			mappings.remove(0);
		}
		fireAllMappingsRemoved();
	}

	public void duplicateMapping(URI srcuri, String id, String new_id) throws DuplicateMappingException {
		OBDAMappingAxiom oldmapping = getMapping(srcuri, id);
		OBDAMappingAxiom newmapping = null;
		try {
			newmapping = (OBDAMappingAxiom) oldmapping.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		newmapping.setId(new_id);
		insertMapping(srcuri, newmapping);
	}

	private void fireAllMappingsRemoved() {
		for (MappingControllerListener listener : listeners) {
			listener.allMappingsRemoved();
		}
	}

	private void fireCurrentSourceChanged(URI oldsrcuri, URI newsrcuri) {
		for (MappingControllerListener listener : listeners) {
			listener.currentSourceChanged(oldsrcuri, newsrcuri);
		}
	}

	/***************************************************************************
	 * Announces that a mapping has been updated.
	 *
	 * @param srcuri
	 * @param mapping_id
	 * @param mapping
	 */
	private void fireMappigUpdated(URI srcuri, String mapping_id, OBDAMappingAxiom mapping) {
		for (MappingControllerListener listener : listeners) {
			listener.mappingUpdated(srcuri, mapping_id, mapping);
		}
	}

	/**
	 * Announces to the listeners that a mapping was deleted.
	 *
	 * @param mapping_id
	 */
	private void fireMappingDeleted(URI srcuri, String mapping_id) {
		for (MappingControllerListener listener : listeners) {
			listener.mappingDeleted(srcuri, mapping_id);
		}
	}

	/**
	 * Announces to the listeners that a mapping was inserted.
	 *
	 * @param mapping_id
	 */
	private void fireMappingInserted(URI srcuri, String mapping_id) {
		for (MappingControllerListener listener : listeners) {
			listener.mappingInserted(srcuri, mapping_id);
		}
	}

	/***************************************************************************
	 * Returns the object of the mapping with id ID for the datasource
	 * source_uri
	 *
	 * @param source_uri
	 * @param mapping_id
	 * @return
	 */
	public OBDAMappingAxiom getMapping(URI source_uri, String mapping_id) {
		int pos = indexOfMapping(source_uri, mapping_id);
		if (pos == -1) {
			return null;
		}
		ArrayList<OBDAMappingAxiom> mappings = getMappings(source_uri);
		return mappings.get(pos);
	}

	/***************************************************************************
	 * Returns all the mappings hold by the controller. Warning. do not modify
	 * this mappings manually, use the controllers methods.
	 *
	 * @return
	 */
	public Hashtable<URI, ArrayList<OBDAMappingAxiom>> getMappings() {
		return mappings;
	}

	/***************************************************************************
	 * Returns all the mappings for a given datasource identified by its uri.
	 *
	 * @param datasource_uri
	 * @return
	 */
	public ArrayList<OBDAMappingAxiom> getMappings(URI datasource_uri) {
		if (datasource_uri == null)
			return null;
		ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
		if (current_mappings == null) {
			initMappingsArray(datasource_uri);
		}
		return mappings.get(datasource_uri);
	}

	private Boolean getNeedsSyncwithReasoner(URI src) {
		Boolean value = needsSyncwithReasoner.get(src);
		if (value == null) {
			needsSyncwithReasoner.put(src, Boolean.TRUE);
			value = Boolean.TRUE;
		}
		return value;
	}

	public String getNextAvailableDuplicateIDforMapping(URI source_uri, String originalid) {
		int new_index = -1;
		for (int index = 0; index < 999999999; index++) {
			if (indexOfMapping(source_uri, originalid + "(" + index + ")") == -1) {
				new_index = index;
				break;
			}
		}
		return originalid + "(" + new_index + ")";
	}

	public String getNextAvailableMappingID(URI datasource_uri) {
		int index = 0;
		for (int i = 0; i < 99999999; i++) {
			index = indexOfMapping(datasource_uri, "M:" + Integer.toHexString(i));
			if (index == -1) {
				index = i;
				break;
			}
		}
		return "M:" + Integer.toHexString(index);
	}

	public MappingTreeModel getTreeModel() {
		return treemodel;
	}

	/***************************************************************************
	 * Retrives the position of the mapping identified by mapping_id in the
	 * array of mappings for the given datasource.
	 *
	 * @param datasource_uri
	 *            The datasource to whom the mapping belongs.
	 * @param mapping_id
	 *            The id of the mapping that is being searched.
	 * @return The position of the mapping in the array OR -1 if the mapping is
	 *         not found.
	 */
	public int indexOfMapping(URI datasource_uri, String mapping_id) {
		ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
		if (current_mappings == null) {
			initMappingsArray(datasource_uri);
			current_mappings = mappings.get(datasource_uri);
		}
		int position = -1;
		for (int i = 0; i < current_mappings.size(); i++) {
			if (current_mappings.get(i).getId().equals(mapping_id)) {
				position = i;
				break;
			}
		}
		return position;
	}

	private void initMappingsArray(URI datasource_uri) {
		mappings.put(datasource_uri, new ArrayList<OBDAMappingAxiom>());
	}

	/***************************************************************************
	 * Inserts a mappings into the mappings for a datasource. If the ID of the
	 * mapping already exits it throws an exception.
	 *
	 * @param datasource_uri
	 * @param mapping
	 * @throws DuplicateMappingException
	 */
	public void insertMapping(URI datasource_uri, OBDAMappingAxiom mapping) throws DuplicateMappingException {
		int index = indexOfMapping(datasource_uri, mapping.getId());
		if (index != -1)
			throw new DuplicateMappingException("ID " + mapping.getId());
		mappings.get(datasource_uri).add(mapping);
		fireMappingInserted(datasource_uri, mapping.getId());
	}

	/***************************************************************************
	 * True if the a data source is new, or mappings have been added, deleted or
	 * modified through the corresponding methods in the mapping controller.
	 *
	 * @param srcuri
	 * @return
	 */
	public boolean needsSyncWithReasoner(URI srcuri) {
		return getNeedsSyncwithReasoner(srcuri).booleanValue();
	}

	public void removeAllMappings() {
		mappings.clear();
		mappings = new Hashtable<URI, ArrayList<OBDAMappingAxiom>>();
		needsSyncwithReasoner = new Hashtable<URI, Boolean>();
		fireAllMappingsRemoved();
	}

	public void removeMappingControllerListener(MappingControllerListener listener) {
		listeners.remove(listener);
	}


	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 *
	 * @param datasource_uri
	 * @param mapping_id
	 * @param body
	 */
	public void updateSourceQueryMapping(URI datasource_uri, String mapping_id, Query sourceQuery) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		mapping.setSourceQuery(sourceQuery);
		fireMappigUpdated(datasource_uri, mapping.getId(), mapping);
	}

	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 *
	 * @param datasource_uri
	 * @param mapping_id
	 * @param new_mappingid
	 */
	public void updateMapping(URI datasource_uri, String mapping_id, String new_mappingid) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		mapping.setId(new_mappingid);
		fireMappigUpdated(datasource_uri, mapping_id, mapping);
	}

	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 *
	 * @param datasource_uri
	 * @param mapping_id
	 * @param head
	 */
	public void updateTargetQueryMapping(URI datasource_uri, String mapping_id, Query targetQuery) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		if (mapping == null) {
			return;
		}
		mapping.setTargetQuery(targetQuery);
		fireMappigUpdated(datasource_uri, mapping.getId(), mapping);
	}

//	public void activeOntologyChanged(){
//		for (MappingControllerListener listener : listeners) {
//			try {
//				listener.ontologyChanged();
//			} catch (Exception e) {
//				log.warn("Error while notifying listeners about an active ontology change.");
//			}
//		}
//	}
}
