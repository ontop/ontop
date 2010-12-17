package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.reformulation.domain.imp.PredicateImp;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeListener;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.SetOntologyURI;

/**
 * The synchronized mapping controller is an extension of the origianl mapping
 * controller of the obda api. The main difference between them is that the
 * synchronized mapping controller forwards all updates done on the entities in the
 * ontology to the mappings, e.g. if a entity is renamed in the ontology the
 * synchronized mapping controller will immediately reflect those changes in the mappings.
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
			APIController ac) {
		super(dsc, ac);
		dscontroller = dsc;
		apic = ac;
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

		((OWLAPICoupler)apic.getCoupler()).updateOntologies();
		URI u = changes.get(0).getOntology().getURI();
		String ontoprefix= null;
		if(!u.toString().endsWith("#")){
			String aux = u.toString()+"#";
			ontoprefix = apic.getCoupler().getPrefixForUri(URI.create(aux));
			if(aux.equals(ontoprefix)){
				ontoprefix = apic.getCoupler().getPrefixForUri(u);
			}
		}
		boolean replace = allRemoveAxioms(changes);
		if(replace){
			if(changes.size()>=2){
				OWLOntologyChange rem = changes.get(0);
				OWLOntologyChange add = changes.get(1);
				if(rem instanceof RemoveAxiom && add instanceof AddAxiom){
					Set<OWLEntity> r =rem.getAxiom().getReferencedEntities();
					Set<OWLEntity> a =add.getAxiom().getReferencedEntities();
					Iterator<OWLEntity> it1 = r.iterator();
					Iterator<OWLEntity> it2 = a.iterator();
					String old = "";
					while(it1.hasNext()){
						String aux = it1.next().toString();
						boolean found = false;
						while(it2.hasNext() && !found){
							String aux2 = it2.next().toString();
							if(aux.equals(aux2)){
								found = true;
							}
						}
						if(!found){
							old = ontoprefix+":"+aux;
							break;
						}
					}

					it1 = r.iterator();
					it2 = a.iterator();
					URI newUri = null;
					String neu = "";
					while(it2.hasNext()){
						OWLEntity ent = it2.next();
						String aux = ent.toString();
						boolean found = false;
						while(it1.hasNext() && !found){
							String aux2 = it1.next().toString();
							if(aux.equals(aux2)){
								found = true;
							}
						}
						if(!found){
							neu = ontoprefix+":"+aux;
							newUri = ent.getURI();
							break;
						}
					}
					if(!neu.equals("") && !old.equals("")){
						replaceAxiom(old, neu, newUri);
					}
				}
			}
		}else if (changes.size()==1 && changes.get(0) instanceof SetOntologyURI){
			DatasourcesController con = apic.getDatasourcesController();
			SetOntologyURI ch =  (SetOntologyURI) changes.get(0);
			Set<DataSource> sources =con.getDatasources(ch.getOriginalURI());
			Iterator<DataSource> it = sources.iterator();
			String neu = ch.getNewURI().toString();
			String old = ch.getOriginalURI().toString();
			while(it.hasNext()){
				DataSource ds = it.next();
				String ontouri = ds.getParameter(RDBMSsourceParameterConstants.ONTOLOGY_URI);
				if(ontouri.equals(old)){
					ds.setParameter(RDBMSsourceParameterConstants.ONTOLOGY_URI, neu);
				}
			}

		}else if (changes.size()==1 && changes.get(0) instanceof AddAxiom){

			OWLOntologyChange ch = changes.get(0);
			if(ch.getAxiom() instanceof OWLImportsDeclaration){
				OWLImportsDeclaration imp = (OWLImportsDeclaration) ch.getAxiom();
				if(apic instanceof OBDAPluginController){
					OBDAPluginController c =(OBDAPluginController)apic;
					c.addOntologyToCoupler(imp.getImportedOntologyURI());
				}

				URI fileuri = apic.getPhysicalURIOfOntology(imp.getImportedOntologyURI());
				fileuri = apic.getIOManager().getOBDAFile(fileuri);
				apic.setCurrentOntologyURI(imp.getImportedOntologyURI());
				apic.getIOManager().loadOBDADataFromURI(fileuri);
				apic.markAsLoaded(imp.getImportedOntologyURI());
			}
		}else if (changes.size()==1 && changes.get(0) instanceof RemoveAxiom){

			OWLOntologyChange ch = changes.get(0);
			if(ch.getAxiom() instanceof OWLImportsDeclaration){
				OWLImportsDeclaration imp = (OWLImportsDeclaration) ch.getAxiom();
				apic.unloaded(imp.getImportedOntologyURI());
			}
		}else{
			Set<OWLEntity> entities = getInvolvedEntities(changes);
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
	 * private method that is used to check whether an Entity is still in the
	 * ontology or not.
	 */
	private boolean stillExists(OWLEntity ent){

		APICoupler coupler = apic.getCoupler();
		URI uri = apic.getCurrentOntologyURI();
		boolean extists = true;
		if(ent instanceof OWLClass){
			extists = coupler.isNamedConcept(uri,ent.getURI());
		}else if(ent instanceof OWLDataProperty){
			extists = coupler.isDatatypeProperty(uri,ent.getURI());
		}else{
			extists = coupler.isObjectProperty(uri,ent.getURI());
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
	private Set<OWLEntity> getInvolvedEntities(List<? extends OWLOntologyChange> changes){
		Set<OWLEntity> set = new HashSet<OWLEntity>();

		Iterator<? extends OWLOntologyChange> it = changes.iterator();
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
	 * Private method that checks whether all changes involve a remove axiom. In
	 * that case we can be sure we have to remove an entity. Otherwise the
	 * Change Event is a candidate for a renaming but some further checks
	 * have to be done.
	 */
	private boolean allRemoveAxioms(List<? extends OWLOntologyChange> changes){

		if(changes.size()<2){
			return false;
		}
		Iterator<? extends OWLOntologyChange> it = changes.iterator();
		while(it.hasNext()){
			OWLOntologyChange ch = it.next();
			if(ch instanceof AddAxiom){
				return true;
			}
		}
		return false;

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
			Map<String, CQIE> mappingsToUpdate = new HashMap<String, CQIE>();
			while(it.hasNext()){
				OBDAMappingAxiom map = it.next();
				CQIE cq = (CQIEImpl) map.getTargetQuery();
				List<Atom> atoms = cq.getBody();
				Iterator<Atom> it2 = atoms.iterator();
				ArrayList<Atom> newList = new ArrayList<Atom>();
				boolean update = false;
				while(it2.hasNext()){
					Atom atom = it2.next();
					String n = apic.getEntityNameRenderer().getPredicateName(atom);
					if(n.equals(name)){
						update = true;
					}else{
						newList.add(atom);
					}
				}

				cq.updateBody(newList);
				if(update){
					if(newList.size()==0){
						mappingsToRemove.add(map.getId());
					}else{
						mappingsToUpdate.put(map.getId(), cq);
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
				CQIE cq = mappingsToUpdate.get(key);
				updateTargetQueryMapping(ds.getSourceID(), key, cq);
			}
		}
	}

	/**
	 * Removes all entities that were involved in the Renaming Event and replaces
	 * with the new ones.
	 */

	private void replaceAxiom(String old, String neu, URI newUri){

		HashMap<URI, DataSource> datasources = dscontroller.getAllSources();
		Set<URI> keys = datasources.keySet();
		Iterator<URI> kit = keys.iterator();
		while(kit.hasNext()){
			DataSource ds = datasources.get(kit.next());
			ArrayList<OBDAMappingAxiom> maps = getMappings(ds.getSourceID());
			Iterator<OBDAMappingAxiom> it = maps.iterator();
			Map<String, CQIE> mappingsToUpdate = new HashMap<String, CQIE>();
			while(it.hasNext()){
				OBDAMappingAxiom map = it.next();
				CQIE cq = (CQIEImpl) map.getTargetQuery();
				List<Atom> atoms = cq.getBody();
				Iterator<Atom> it2 = atoms.iterator();
				ArrayList<Atom> newList = new ArrayList<Atom>();
				boolean update = false;
				while(it2.hasNext()){
					Atom atom = it2.next();
					String n = apic.getEntityNameRenderer().getPredicateName(atom);
					if(n.equals(old)){
						update = true;
						PredicateImp predicate = (PredicateImp) atom.getPredicate();
						predicate.setName(newUri);
						newList.add(atom);
					}else{
						newList.add(atom);
					}
				}
				cq.updateBody(newList);
				if(update){
					mappingsToUpdate.put(map.getId(), cq);
				}
			}

			Iterator<String> it4 = mappingsToUpdate.keySet().iterator();
			while(it4.hasNext()){
				String key = it4.next();
				CQIE cq = mappingsToUpdate.get(key);
				updateTargetQueryMapping(ds.getSourceID(), key, cq);
			}
		}
	}
}
