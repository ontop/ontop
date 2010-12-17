package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.protege.editor.owl.ui.prefix.PrefixMapperManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

/***
 *
 * Interacts with OWLAPI objects to inspect the original ontology. Note that
 * this coupler only works fine if the active ontology is setup properly so that
 * the EntityFinder finds the classes.
 *
 * TODO Needs to be refactored to handle URI's properly. In general, the whole
 * OBDA API needs to be refactored like this.
 *
 *
 * @author Mariano Rodriguez Muro
 *
 */
public class OWLAPICoupler implements APICoupler {

	// OWLModelManager owlman = null;
//	EntityFinder			finder	= null;
	private final APIController	apic;

	// private OWLModelManager mmgr;

	private OWLOntologyManager mmgr = null;

	private final OWLOntology merged = null;

	private OWLOntology	mergedOntology;

	private HashSet<String>	dataProperties;

	private HashSet<String>	classesURIs;

	private HashSet<String>	objectProperties;

	private PrefixMapperManager prefixManager = null;

	private HashMap<URI, OntologyEntitiyInformation> infoMap = null;

	public OWLAPICoupler(APIController apic, OWLOntologyManager mmgr, OWLOntology root) {
		// this.mmgr = manager;
		this.apic = apic;
		this.mmgr = mmgr;
		this.prefixManager = PrefixMapperManager.getInstance();
		this.infoMap = new HashMap<URI, OntologyEntitiyInformation>();
		Map<String, String> prefixes = prefixManager.getPrefixes();

		Set<String> keys = prefixes.keySet();
		for (String key : keys) {
			System.out.println(key + ": " + prefixes.get(key));
		}
//		synchWithOntology(root);
	}

	public void addNewOntologyInfo(OWLOntology root){
		OntologyEntitiyInformation info = new OntologyEntitiyInformation(root);
		infoMap.put(root.getURI(), info);
	}

	public void updateOntologies(){
		Set<URI> set = infoMap.keySet();
		Iterator<URI> it = set.iterator();
		while(it.hasNext()){
			infoMap.get(it.next()).refresh();
		}
	}

	public void updateOntology(URI uri){
		OntologyEntitiyInformation info = infoMap.get(uri);
		if(info != null){
			info.refresh();
		}
	}

	public boolean isDatatypeProperty(URI ontouri, URI propertyURI) {
		if(ontouri.toString().equals("")){
			ontouri = apic.getCurrentOntologyURI();
		}
		OntologyEntitiyInformation info = infoMap.get(ontouri);
		if(info == null){
			return false;
		}else{
			return info.isDatatypeProperty(propertyURI);
		}
	}

	public boolean isNamedConcept(URI ontouri,URI propertyURI) {
		if(ontouri.toString().equals("")){
			ontouri = apic.getCurrentOntologyURI();
		}
		OntologyEntitiyInformation info = infoMap.get(ontouri);
		if(info == null){
			return false;
		}else{
			return info.isNamedConcept(propertyURI);
		}
	}

	public boolean isObjectProperty(URI ontouri,URI propertyURI) {
		if(ontouri.toString().equals("")){
			ontouri = apic.getCurrentOntologyURI();
		}
		OntologyEntitiyInformation info = infoMap.get(ontouri);
		if(info == null){
			return false;
		}else{
			return info.isObjectProperty(propertyURI);
		}
	}

	public OWLOntologyManager getOWLOntologyManager(){
		return mmgr;
	}

	public String getPrefixForUri(URI uri){

		if(prefixManager == null){
			prefixManager = PrefixMapperManager.getInstance();
		}
		String sh =prefixManager.getMapper().getShortForm(uri);
		if(sh!= null){
			if(sh.contains(":")){
				String aux[] = sh.split(":");
				if(isCurrentOntology(aux[0])){
					return "";
				}
				return aux[0];
			}else{
				return "";
			}
		}else{
			return uri.toString();
		}
	}

	@Override
	public String getUriForPrefix(String prefix) {

		if(prefix.equals("")){
			return apic.getCurrentOntologyURI().toString();
		}else{
			String s = prefixManager.getMapper().getValue(prefix);
			if(s != null){
				return s;
			}else{
				try {//testing whether the prefix is a not registered uri. If so we
					// go on with the whole uri, otherwise the prefix is wrong.
					URI uri = new URI(prefix);
					return prefix;
				} catch (URISyntaxException e) {
					return null;
				}
			}
		}
	}

	private boolean isCurrentOntology(String pref){
		URI currentOnto = apic.getCurrentOntologyURI();
		String tmp = this.getUriForPrefix(pref);
		if(tmp.endsWith("#")|| tmp.endsWith("/")){
			tmp = tmp.substring(0,tmp.length()-1);
		}
		URI uri = URI.create(tmp);

		return uri.equals(currentOnto);
	}

	@Override
	public void removeOntology(URI ontouri) {

		infoMap.remove(ontouri);
	}

}
