package inf.unibz.it.obda.owlapi;


import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.io.DataManager;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.QueryAtom;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeListener;
import org.semanticweb.owl.model.RemoveAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Due to the refactoring mechanism implemented in the owl api, 
 * that allows only to add and remove axioms and the fact that there is no common
 * policy of how those atoms are ordered, at this point we have no reliable
 * method to establish which is the actual entity to update. Therefore
 * for the moment this synchronizer only handles Remove events.
 * 
 * @author obda
 *
 */


public class SynchronizedMappingController extends MappingController implements OWLOntologyChangeListener{

	/**
	 * the current data source controller
	 */
	private DatasourcesController	dscontroller			= null;
	/**
	 * current api controller
	 */
	private APIController 			apic					= null;
	
	
	/**
	 * The constructor. Creates a new instance of the SynchronizedMappingController
	 * @param dsc the data source controller	
	 * @param ac the api controller
	 */
	public SynchronizedMappingController(DatasourcesController dsc,
			APIController ap) {
		super(dsc, ap);
		dscontroller = dsc;
		apic = ap;
	}

	
	/**
	 * Handles the ontology change event, which is thrown whenever something
	 * changes in the ontology. Attention: the implementation is strongly coupled
	 * to the event structure of protege. E.g a renaming is done be adding a new 
	 * entity and deleting the old, etc.   
	 */
	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
			throws OWLException {
		
		((OWLAPICoupler)apic.getCoupler()).synchWithOntology(changes.get(0).getOntology());
		String ontoprefix = apic.getCoupler().getPrefixForUri(changes.get(0).getOntology().getURI());
		List<RemoveAxiom> vec = getRemoveAxioms(changes);
		if(vec != null){
			Set<OWLEntity> entities = getInvolvedEntities(vec);
			Iterator<OWLEntity> nit = entities.iterator();
			while(nit.hasNext()){
				OWLEntity ent = nit.next();
				if(!stillExists(ent)){
					removeAxiom(ontoprefix+":"+ent.toString());
				}
			}
		}
	}
	
	/**
	 * Private method that checks whether all changes involve a remove axiom. In
	 * that case we can be sure we have to remove an entity. Otherwise the 
	 * Change Event is a candidate for a renaming but some further checks
	 * have to be done. 
	 */
	private List<RemoveAxiom> getRemoveAxioms(List<? extends OWLOntologyChange> changes){
		
		Vector<RemoveAxiom> vec = new Vector<RemoveAxiom>();
		Iterator<? extends OWLOntologyChange>it = changes.iterator();
		while(it.hasNext()){
			OWLOntologyChange ch = it.next();
			if(ch instanceof RemoveAxiom){
				vec.add((RemoveAxiom)ch);
			}
		}
		return vec;
	}
	
	/**
	 * private method that is used to check whether an Entity is still in the 
	 * ontology or not.
	 */
	private boolean stillExists(OWLEntity ent){
		
		APICoupler coupler = apic.getCoupler(); 
		boolean extists = true;
		if(ent instanceof OWLClass){
			extists = coupler.isNamedConcept(apic.getCurrentOntologyURI(),ent.getURI());
		}else if(ent instanceof OWLDataProperty){
			extists = coupler.isDatatypeProperty(apic.getCurrentOntologyURI(),ent.getURI());
		}else{
			extists = coupler.isObjectProperty(apic.getCurrentOntologyURI(),ent.getURI());
		}
		
		if(extists){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * private method that retrieves all involved entities in an Change Event.
	 */
	private Set<OWLEntity> getInvolvedEntities(List<RemoveAxiom> changes){
		Set<OWLEntity> set = new HashSet<OWLEntity>();
		
		Iterator<RemoveAxiom> it = changes.iterator();
		while(it.hasNext()){
			OWLAxiom ch = it.next().getAxiom();
			// filter out datatypes like int, string, etc
			if(ch instanceof OWLDataPropertyRangeAxiom){
				OWLDataPropertyRangeAxiom ax = (OWLDataPropertyRangeAxiom)ch;
				Set<OWLEntity> s = ax.getReferencedEntities();
				String prop = ax.getProperty().toString();
				Iterator<OWLEntity> sit = s.iterator();
				while(sit.hasNext()){
					OWLEntity ent = sit.next();
					if(prop.equals(ent.toString())){
						set.add(ent);
					}
				}
				
			}else{
				Set<OWLEntity> entities = ch.getReferencedEntities();
				Iterator<OWLEntity> eit = entities.iterator();
				while(eit.hasNext()){
					OWLEntity ent = eit.next();
					if(!ent.toString().equals("XMLLiteral") &&!ent.toString().equals("Thing") ){
						set.add(ent);
					}
				}
			}
		}
		return set;
	}
		
	/**
	 * Removes all entities from the mappings that where involved in the 
	 * Remove Event.
	 */
	private void removeAxiom(String name){
		
		HashMap<URI, DataSource> datasources = dscontroller.getAllSources();
		Set<URI> keys = datasources.keySet();
		Iterator<URI> kit = keys.iterator();
		while(kit.hasNext()){
			DataSource ds = datasources.get(kit.next());
			ArrayList<OBDAMappingAxiom> maps = getMappings(ds.getSourceID());
			Iterator<OBDAMappingAxiom> it = maps.iterator();
			Vector<String> mappingsToRemove = new Vector<String>();
			Map<String, ArrayList<QueryAtom>> mappingsToUpdate = new HashMap<String, ArrayList<QueryAtom>>();
			while(it.hasNext()){
				OBDAMappingAxiom map = it.next();
				ConjunctiveQuery cq = (ConjunctiveQuery) map.getTargetQuery();
				ArrayList<QueryAtom> atoms = cq.getAtoms();
				Iterator<QueryAtom> it2 = atoms.iterator();
				ArrayList<QueryAtom> newList = new ArrayList<QueryAtom>();
				boolean update = false;
				while(it2.hasNext()){
					QueryAtom atom = it2.next();
					String n = apic.getEntityNameRenderer().getPredicateName(atom);
					if(n.equals(name)){
						update = true;
					}else{
						newList.add(atom);
					}
				}
				if(update){
					if(newList.size()==0){
						mappingsToRemove.add(map.getId());
					}else{
						mappingsToUpdate.put(map.getId(), newList);
					}
				}
			}
			
			Iterator<String> it3 = mappingsToRemove.iterator();
			while(it3.hasNext()){
				String mapID = it3.next();
				deleteMapping(ds.getSourceID(), mapID);
			}
			
			Iterator<String> it4 = mappingsToUpdate.keySet().iterator();
			while(it4.hasNext()){
				String key = it4.next();
				ArrayList<QueryAtom> body =mappingsToUpdate.get(key);
				ConjunctiveQuery cq = new ConjunctiveQuery();
				cq.addQueryAtom(body);
				updateMapping(ds.getSourceID(), key, cq);
			}
		}
	}
}

