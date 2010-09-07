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
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.obda.gui.swing.exception.NoDatasourceSelectedException;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingTreeModel;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MappingController implements TreeModelListener, DatasourcesControllerListener {

	private static MappingController						instance				= null;

	private ArrayList<MappingControllerListener>			listeners				= null;
	private Hashtable<String, ArrayList<OBDAMappingAxiom>>	mappings				= null;

	// private String currentsourceuri = null;

	private Hashtable<String, Boolean>						needsSyncwithReasoner	= null;

	private MappingTreeModel								treemodel				= null;

	private DatasourcesController							dscontroller			= null;

	private APIController									apic;

	Logger													log						= LoggerFactory.getLogger(MappingController.class);

	// /***************************************************************************
	// * get the global instance of the mapping controller for the currently
	// open
	// * project
	// *
	// * @return
	// */
	// public static MappingController getInstance() {
	// if (instance == null) {
	// instance = new MappingController();
	// }
	// return instance;
	// }

	public static void main(String args[]) {
		// String head = "HEAD1";
		// String body = "BODY1";
		// String mappingId = null;
		// String sourceuri = "sourceuri";
		//
		// // DataSourceMapping mapping1 = new
		// RDBMSDataSourceMapping(mappingId);
		//
		// DatasourcesController srcController = new DatasourcesController();
		// MappingController controller = new MappingController(srcController);
		//		
		// URI uri;
		// try {
		// uri = new URI("http://www.testuri.com");
		// } catch (URISyntaxException e2) {
		// return;
		// }
		//		
		// srcController.addDataSource(sourceuri, uri);
		// srcController.setCurrentDataSource(sourceuri);
		//
		// MappingTreeModel model = controller.getTreeModel();
		// model.addTreeModelListener(new TreeModelListener() {
		//
		// public void treeNodesChanged(TreeModelEvent e) {
		// System.out.println("Nodes change:" + e.toString());
		//
		// }
		//
		// public void treeNodesInserted(TreeModelEvent e) {
		// System.out.println("Nodes inserted:" + e.toString());
		//
		// }
		//
		// public void treeNodesRemoved(TreeModelEvent e) {
		// System.out.println("Nodes removed:" + e.toString());
		// }
		//
		// public void treeStructureChanged(TreeModelEvent e) {
		// System.out.println("Structure change:" + e.toString());
		//
		// }
		// });
		//
		// try {
		// mappingId = controller.insertMapping();
		// } catch (NoDatasourceSelectedException e1) {
		// e1.printStackTrace();
		// return;
		// } catch (DuplicateMappingException e1) {
		// e1.printStackTrace();
		// return;
		// }
		//
		// TargetQuery h = null;
		// try {
		// h = new ConjunctiveQuery(head, apic);
		// } catch (QueryParseException e1) {
		// e1.printStackTrace();
		// }
		// SourceQuery b = null;
		// try {
		// b = new RDBMSSQLQuery(body);
		// } catch (QueryParseException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// controller.updateMapping(sourceuri, mappingId, h);
		// controller.updateMapping(sourceuri, mappingId, b);
		// controller.updateMapping(sourceuri, mappingId, "newid");

	}

	public MappingController(DatasourcesController dscontroller, APIController apic) {
		this.apic = apic;
		this.dscontroller = dscontroller;
		mappings = new Hashtable<String, ArrayList<OBDAMappingAxiom>>();
		listeners = new ArrayList<MappingControllerListener>();
		treemodel = new MappingTreeModel(apic, dscontroller, this);
		needsSyncwithReasoner = new Hashtable<String, Boolean>();
		dscontroller.addDatasourceControllerListener(this);
		addMappingControllerListener(treemodel);
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
		String currentsourceName = null;
		if (currentsource != null) {
			currentsourceName = currentsource.getName();
		}
		String previousName = null;
		if (previous != null) {
			previousName = previous.getName();
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
		deleteMappings(source.getName());

	}

	/***************************************************************************
	 * Implementation of the DatasourceControllerListener interface. Does
	 * nothing unless the updated field is the URI, then it must update the key
	 * for all the mappings related to that source.
	 */
	public void datasourceUpdated(String oldname, DataSource currendata) {
		// TODO implement
	}

	/***************************************************************************
	 * Deletes the mapping with ID id
	 * 
	 * @param datasource_uri
	 * @param mapping_id
	 */
	public void deleteMapping(String datasource_uri, String mapping_id) {
		int index = indexOfMapping(datasource_uri, mapping_id);
		if (index == -1) {
			return;
		} else {
			ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
			current_mappings.remove(index);
		}

		setNeedsSyncWithReasoner(datasource_uri, true);

		fireMappingDeleted(datasource_uri, mapping_id);
	}

	/***************************************************************************
	 * Deletes all the mappings for a given datasource
	 * 
	 * @param datasource_uri
	 */
	public void deleteMappings(String datasource_uri) {
		ArrayList<OBDAMappingAxiom> mappings = getMappings(datasource_uri);
		while (!mappings.isEmpty()) {
			mappings.remove(0);
		}
		setNeedsSyncWithReasoner(datasource_uri, true);
		fireAllMappingsRemoved();
	}

	public void dumpMappingsToXML(Element root) {
		Enumeration<String> datasource_uris = mappings.keys();
		while (datasource_uris.hasMoreElements()) {
			dumpMappingsToXML(root, datasource_uris.nextElement());
		}
	}

	// TODO modify to allow modularization and independence from what type of
	// source it is
	public void dumpMappingsToXML(Element root, String datasource_uri) {
		// DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = root.getOwnerDocument();

		Element mappingsgroup = doc.createElement("mappings");

		mappingsgroup.setAttribute("sourceuri", datasource_uri);
		mappingsgroup.setAttribute("headclass", inf.unibz.it.ucq.domain.ConjunctiveQuery.class.toString());
		mappingsgroup.setAttribute("bodyclass", inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery.class.toString());

		root.appendChild(mappingsgroup);

		ArrayList<OBDAMappingAxiom> mappings = getMappings(datasource_uri);

		int mappingcount = mappings.size();
		for (int i = 0; i < mappingcount; i++) {
			try {
				OBDAMappingAxiom mapping = mappings.get(i);
				Element mappingelement = doc.createElement("mapping");
				// the new XML mapping
				mappingelement.setAttribute("id", mapping.getId());
				ConjunctiveQuery headquery = (ConjunctiveQuery) mapping.getTargetQuery();
				RDBMSSQLQuery bodyquery = (RDBMSSQLQuery) mapping.getSourceQuery();
				// the head XML child
				Element mappingheadelement = doc.createElement("CQ");
				if (headquery != null) {
					if (headquery.isInputQueryValid(apic)) {
						mappingheadelement.setAttribute("string", headquery.toString());
					} else {
						mappingheadelement.setAttribute("string", headquery.getInputQuString());
					}
				} else {
					mappingheadelement.setAttribute("string", "");
				}
				// the body XML child
				Element mappingbodyelement = doc.createElement("SQLQuery");
				if (bodyquery != null) {
					if (bodyquery.isInputQueryValid(apic)) {
						mappingbodyelement.setAttribute("string", bodyquery.toString());
					} else {
						mappingbodyelement.setAttribute("string", bodyquery.getInputQuString());
					}
				} else {
					mappingbodyelement.setAttribute("string", "");
				}

				mappingelement.appendChild(mappingheadelement);
				mappingelement.appendChild(mappingbodyelement);
				mappingsgroup.appendChild(mappingelement);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}

	}

	public void duplicateMapping(String srcuri, String id, String new_id) throws DuplicateMappingException {

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

	private void fireCurrentSourceChanged(String oldsrcuri, String newsrcuri) {
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
	private void fireMappigUpdated(String srcuri, String mapping_id, OBDAMappingAxiom mapping) {
		for (MappingControllerListener listener : listeners) {
			listener.mappingUpdated(srcuri, mapping_id, mapping);
		}
	}

	/**
	 * Announces to the listeners that a mapping was deleted.
	 * 
	 * @param mapping_id
	 */
	private void fireMappingDeleted(String srcuri, String mapping_id) {
		for (MappingControllerListener listener : listeners) {
			listener.mappingDeleted(srcuri, mapping_id);
		}
	}

	/**
	 * Announces to the listeners that a mapping was inserted.
	 * 
	 * @param mapping_id
	 */
	private void fireMappingInserted(String srcuri, String mapping_id) {
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
	public OBDAMappingAxiom getMapping(String source_uri, String mapping_id) {
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
	public Hashtable<String, ArrayList<OBDAMappingAxiom>> getMappings() {
		return mappings;
	}

	/***************************************************************************
	 * Returns all the mappings for a given datasource identified by its uri.
	 * 
	 * @param datasource_uri
	 * @return
	 */
	public ArrayList<OBDAMappingAxiom> getMappings(String datasource_uri) {
		if (datasource_uri == null)
			return null;
		ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
		if (current_mappings == null) {
			initMappingsArray(datasource_uri);
		}
		return mappings.get(datasource_uri);
	}

	private Boolean getNeedsSyncwithReasoner(String src) {
		Boolean value = needsSyncwithReasoner.get(src);
		if (value == null) {
			needsSyncwithReasoner.put(src, Boolean.TRUE);
			value = Boolean.TRUE;
		}
		return value;
	}

	public String getNextAvailableDuplicateIDforMapping(String source_uri, String originalid) {
		int new_index = -1;
		for (int index = 0; index < 999999999; index++) {
			if (indexOfMapping(source_uri, originalid + "(" + index + ")") == -1) {
				new_index = index;
				break;
			}
		}
		return originalid + "(" + new_index + ")";
	}

	public String getNextAvailableMappingID(String datasource_uri) {
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

	// public void setCurrentDataSource(String srcuri) {
	// currentsourceuri = srcuri;
	// }

	public MappingTreeModel getTreeModel() {
		return treemodel;
	}

	// TODO modify to allow modularization and independence from what type of
	// source it is
	public void importMappingsFromXML(Element mappings) throws QueryParseException {

		String source = mappings.getAttribute("sourceuri");
		String headclass = mappings.getAttribute("headclass");
		String bodyclass = mappings.getAttribute("bodyclass");

		NodeList childs = mappings.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			try {
				Node child = childs.item(i);
				if (!(child instanceof Element)) {
					continue;
				}
				Element mapping = (Element) child;
				String id = mapping.getAttribute("id");
				Element head = null;
				Element body = null;
				NodeList mappingchilds = mapping.getChildNodes();
				// Retrieving the child nodes avoiding empty nodes
				for (int j = 0; j < mappingchilds.getLength(); j++) {
					Node mappingchild = mappingchilds.item(j);
					if (!(mappingchild instanceof Element)) {
						continue;
					}
					if (head == null) {
						head = (Element) mappingchild;
						continue;
					}

					if (body == null) {
						body = (Element) mappingchild;
						continue;
					}
				}

				String CQstring = head.getAttribute("string");
				String SQLstring = body.getAttribute("string");

				ConjunctiveQuery headquery = null;
				try {
					headquery = new ConjunctiveQuery(CQstring, apic);
				} catch (QueryParseException e1) {
					// In case of error, reset the mapping controller to 0 rise
					// event
					deleteMappings(source);
					throw e1;

				}
				RDBMSSQLQuery bodyquery = new RDBMSSQLQuery(SQLstring, apic);
				RDBMSOBDAMappingAxiom newmapping = new RDBMSOBDAMappingAxiom(id);
				newmapping.setSourceQuery(bodyquery);
				newmapping.setTargetQuery(headquery);

				try {
					insertMapping(source, newmapping);
					// System.out.println("OBDAPlugin MappingManager: Inserted
					// mapping " + newmapping.getId() + " head: " +
					// headquery.toString()
					// + " bodyquery: " + bodyquery.toString());
				} catch (DuplicateMappingException e) {
					log.warn("duplicate mapping detected while trying to load mappings from file. Ignoring it. Datasource URI: " + source
							+ " Mapping ID: " + newmapping.getId());
				}
			} catch (Exception e) {
				try {
					log.warn("Error loading mapping with id: {}", ((Element) childs.item(i)).getAttribute("id"));
					log.debug(e.getMessage(), e);
				} catch (Exception e2) {
					log.warn("Error loading mapping");
					log.debug(e.getMessage(), e);
				}
			}

		}
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
	public int indexOfMapping(String datasource_uri, String mapping_id) {
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

	private void initMappingsArray(String datasource_uri) {
		mappings.put(datasource_uri, new ArrayList<OBDAMappingAxiom>());
	}

	// TODO: Remove NoDatasourceSelectedException, always pass datasource as
	// parameter
	/***************************************************************************
	 * Inserts a new mapping for the currently selected data source. If now
	 * source is selected an exception is thrown.
	 * 
	 * @deprecated Use the parameter instead
	 * 
	 */
	public String insertMapping() throws NoDatasourceSelectedException, DuplicateMappingException {

		DataSource currentsrc = dscontroller.getCurrentDataSource();

		if ((currentsrc == null)) {
			throw new NoDatasourceSelectedException("No datasource was selected");
		}
		String new_mapping_name = getNextAvailableMappingID(currentsrc.getName());
		try {
			insertMapping(currentsrc.getName(), new RDBMSOBDAMappingAxiom(new_mapping_name));
		} catch (QueryParseException e) {
			throw new RuntimeException("Error parsing one of the mappings queries... shouldn'nt happen");
		}
		return new_mapping_name;
	}

	/***************************************************************************
	 * Inserts a mappings into the mappings for a datasource. If the ID of the
	 * mapping already exits it throws an exception.
	 * 
	 * @param datasource_uri
	 * @param mapping
	 * @throws DuplicateMappingException
	 */
	public void insertMapping(String datasource_uri, OBDAMappingAxiom mapping) throws DuplicateMappingException {
		int index = indexOfMapping(datasource_uri, mapping.getId());
		if (index != -1)
			throw new DuplicateMappingException("ID " + mapping.getId());
		mappings.get(datasource_uri).add(mapping);

		setNeedsSyncWithReasoner(datasource_uri, true);

		fireMappingInserted(datasource_uri, mapping.getId());
	}

	/***************************************************************************
	 * True if the a data source is new, or mappings have been added, deleted or
	 * modified through the corresponding methods in the mapping controller.
	 * 
	 * @param srcuri
	 * @return
	 */
	public boolean needsSyncWithReasoner(String srcuri) {
		return getNeedsSyncwithReasoner(srcuri).booleanValue();
	}

	public void removeAllMappings() {
		mappings.clear();
		mappings = new Hashtable<String, ArrayList<OBDAMappingAxiom>>();
		needsSyncwithReasoner = new Hashtable<String, Boolean>();
		fireAllMappingsRemoved();

	}

	public void removeMappingControllerListener(MappingControllerListener listener) {
		listeners.remove(listener);
	}

	private void setNeedsSyncwithReasoner(String src, Boolean value) {
		this.needsSyncwithReasoner.put(src, value);
	}

	private void setNeedsSyncWithReasoner(String src_uri, boolean changed) {
		setNeedsSyncwithReasoner(src_uri, Boolean.valueOf(changed));

	}

	public void treeNodesChanged(TreeModelEvent e) {
		e.getSource();
		// TODO remove?

	}

	public void treeNodesInserted(TreeModelEvent e) {
		// TODO remove?

	}

	public void treeNodesRemoved(TreeModelEvent e) {
		// TODO remove?

	}

	public void treeStructureChanged(TreeModelEvent e) {
		// TODO remove?

	}

	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 * 
	 * @param datasource_uri
	 * @param mapping_id
	 * @param body
	 */
	public void updateMapping(String datasource_uri, String mapping_id, SourceQuery body) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		mapping.setSourceQuery(body);

		setNeedsSyncWithReasoner(datasource_uri, true);

		fireMappigUpdated(datasource_uri, mapping.getId(), mapping);
	}

	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 * 
	 * @param datasource_uri
	 * @param mapping_id
	 * @param new_mappingid
	 */
	public void updateMapping(String datasource_uri, String mapping_id, String new_mappingid) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		mapping.setId(new_mappingid);

		setNeedsSyncWithReasoner(datasource_uri, true);

		fireMappigUpdated(datasource_uri, mapping_id, mapping);
	}

	/***************************************************************************
	 * Updates the indicated mapping and fires the appropiate event.
	 * 
	 * @param datasource_uri
	 * @param mapping_id
	 * @param head
	 */
	public void updateMapping(String datasource_uri, String mapping_id, TargetQuery head) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		if (mapping == null) {
			return;
		}
		mapping.setTargetQuery(head);

		setNeedsSyncWithReasoner(datasource_uri, true);

		fireMappigUpdated(datasource_uri, mapping.getId(), mapping);
	}
	

	@Override
	public void datasourcParametersUpdated() {}


}
