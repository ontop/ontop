package inf.unibz.it.obda.api.io;

import java.net.URI;
import java.util.HashMap;

/**
 * The prefix manager is administrating the prefixes for ontolgyie. It allows to register and
 * unregister prefixes for ontolgies and to query them.
 *
 * @author Manfred Gerstgrasser
 *
 */

public class PrefixManager {

	/**
	 * A simple map containing for each ontolgoy URI the correpsonding prefix
	 */
	private HashMap<URI, String> uriToPrefixMap = null;
	/**
	 * A simple map containing for each prefix the correpsonding onotlogy URI
	 */
	private HashMap<String,URI> prefixToURIMap = null;

	/**
	 * The constructor. It creates a new instance of the prefix manager
	 */
	public PrefixManager (){
		uriToPrefixMap = new HashMap<URI, String>();
		prefixToURIMap = new HashMap<String, URI>();
	}

	/**
	 * Adds the given prefix together with the corresponding ontolgoy URI to the manager
	 *
	 * @param uri the ontolgy URI
	 * @param prefix the prefix
	 */
	public void addUri(URI uri, String prefix){
		uriToPrefixMap.put(uri, prefix);
		prefixToURIMap.put(prefix, uri);
	}

	/**
	 * Returns the corresponding ontology URI for the given prefix
	 *
	 * @param prefix the prefix
	 * @return the corresponding ontology URI or null if the prefix is not registered
	 */
	public URI getURIForPrefix(String prefix){
		return prefixToURIMap.get(prefix);
	}

	/**
	 * Returns the corresponding prefix for the given ontology URI
	 *
	 * @param prefix the prefix
	 * @return the corresponding prefix or null if the ontology URI is not registered
	 */
	public String getPrefixForURI(URI uri){
		return uriToPrefixMap.get(uri);
	}

	/**
	 * Returns a map with all registered prefixes and the corresponding ontology URI's
	 * @return a hash map
	 */
	public HashMap<String,URI> getPrefixMap(){
		return prefixToURIMap;
	}
}
