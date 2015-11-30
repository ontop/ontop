package it.unibz.krdb.obda.owlrefplatform.core.abox;

import java.util.HashMap;

public class SemanticIndexURIMap {
	
	// Semantic Index URI reference structures
	private final HashMap<String, Integer> uriIds = new HashMap<> (100000);
	private final HashMap <Integer, String> uriMap2 = new HashMap<> (100000);
	
	
	/**
	 * set(uri, id) is used only by RDBMSSIRepository
	 * 
	 */
	void set(String uri, int id) {
		uriIds.put(uri, id);
		uriMap2.put(id, uri);
	}

	/***
	 * We look for the ID in the list of IDs, if its not there, we return -2, which we know will never appear
	 * on the DB. This is correct because if a constant appears in a query, and that constant was never inserted
	 * in the DB, the query must be empty (that atom), by putting -2 as id, we will enforce that.
	 * @param uri
	 * @return
	 */
	
	public int getId(String uri) {
		Integer index =  uriIds.get(uri);
		if (index != null)
			return index;
		return -2;
	}

	public String getURI(Integer id) {
		return uriMap2.get(id);
	}
}
