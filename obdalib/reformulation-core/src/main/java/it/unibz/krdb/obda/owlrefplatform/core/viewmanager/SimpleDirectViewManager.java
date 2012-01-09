package it.unibz.krdb.obda.owlrefplatform.core.viewmanager;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.ontology.Ontology;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
* The SimpleDirectViewManager is the module which allows us 
* to translate CQIEs into sql queries using the direct mapping approach.
* 
* @author Manfred Gerstgrasser
*
*/

public class SimpleDirectViewManager implements ViewManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6387967678286702723L;
	private Ontology ontology = null;
	private Set<URI> loadedURIs = null;
	private HashMap<String, String> prefixMap = null;
	private Map<String, String> ontoMapper = null;
	private Map<String, String> classMapper = null;
	private Map<String, String> datapropertyMapper = null;
	private Map<String, String> objectporpertyMapper = null;
	private String baseuri = null;
	private Atom orgHead = null;
	
	public SimpleDirectViewManager(){
		
	}
	
	public SimpleDirectViewManager(PrefixManager man,Ontology onto, Set<URI> uris){
		ontology = onto;
		loadedURIs = uris;
//		ontoMapper = ABoxToDBDumper.getInstance().getOntolgyMapper();
//		classMapper = ABoxToDBDumper.getInstance().getClassMapper();
//		datapropertyMapper = ABoxToDBDumper.getInstance().getDataPropertyMapper();
//		objectporpertyMapper = ABoxToDBDumper.getInstance().getObjectPropertyMapper();
		if(uris.size() == 0){
			throw new NullPointerException();
		}
		prefixMap = new HashMap<String, String>();
		Iterator<URI> it = uris.iterator();
		while(it.hasNext()){
			String u = it.next().toString();
			int i = u.lastIndexOf("/");
			String ontoname = u.substring(i+1,u.length()-4);
			prefixMap.put(u, ontoname);
		}
		baseuri = man.getURIForPrefix("xml:base");
	}
	
	/**
	 * translates the given atom name into its alias used when dumping
	 * the abox. 
	 */
	public String getTranslatedName(Atom atom) throws Exception {
		String aux = atom.getPredicate().getName().toString();
		String frag = atom.getPredicate().getName().getFragment();
		int i = aux.lastIndexOf("#");
		String uri = aux.substring(0,i);
		String ontoname = prefixMap.get(uri);
		if(ontoname == null){
			uri = baseuri;
			ontoname = prefixMap.get(uri);
		}
		String onto = ontoMapper.get(ontoname);
		if(onto == null){
			throw new Exception("ERROR: could not identify the ontology to which an entity refered in the query belongs. ");
		}
		String entity = null;
		if(atom.getArity() == 1){
			entity = classMapper.get(frag);
		}else{
			entity = datapropertyMapper.get(frag); 
			if(entity == null){
				entity = objectporpertyMapper.get(frag);
			}
		}
		if(ontoname != null && entity != null){
			return onto +"_"+entity;
		}else{
			throw new Exception(frag + " is not an entity of " + uri);
		}
	}

	/**
	 * copies the the given atom in order to keep references to the 
	 * original variable names used in the data log program.
	 * Note: we need this reference in order to provide the same column
	 * names the user specified in the initial sparql query. E.g. 
	 * if the user does "Select $a $b $c where ...." we will name the 
	 * columns in the answer also a, b, c. 
	 */
	@Override
	public void storeOrgQueryHead(Atom head) {
		orgHead = head;		
	}
}
