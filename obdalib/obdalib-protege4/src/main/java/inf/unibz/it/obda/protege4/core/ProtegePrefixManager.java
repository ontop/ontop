/*
 * Copyright 2010 OBDA-API. All rights reserved.
 * Use is subject to license terms.
 */
package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.io.PrefixManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.protege.editor.owl.ui.prefix.PrefixMapper;
import org.protege.editor.owl.ui.prefix.PrefixMapperManager;

/**
 * Wraps the Protege prefix mapping for the OBDA plugin.
 * 
 * @author Josef Hardi <josef.hardi@gmail.com>
 */
public class ProtegePrefixManager implements PrefixManager {
  private PrefixMapperManager protegePrefixManager = null;

  /**
   * The constructor. It uses an instance of the Protege's prefix manager
   */
  public ProtegePrefixManager() {
    protegePrefixManager = PrefixMapperManager.getInstance();
  }

  /**
   * Adds the given prefix together with the corresponding ontology URI 
   * to the manager
   *
   * @param uri the ontolgy URI
   * @param prefix the prefix
   */
  public void addUri(String uri, String prefix) {
    final PrefixMapper mapper = protegePrefixManager.getMapper();   
    mapper.addPrefixMapping(prefix, uri.toString());
  }

  /**
   * Returns the corresponding ontology URI for the given prefix
   *
   * @param prefix the prefix
   * @return the corresponding ontology URI or null if the prefix is not registered
   */
  public String getURIForPrefix(String prefix) {
    final Map<String, String> prefixes = getPrefixMap();
    return prefixes.get(prefix);
  }

  /**
   * Returns the corresponding prefix for the given ontology URI
   *
   * @param prefix the prefix
   * @return the corresponding prefix or null if the ontology URI is not registered
   */
  public String getPrefixForURI(String uri) {
    final Map<String, String> prefixes = getPrefixMap();

    Iterator<Map.Entry<String, String>> iter = prefixes.entrySet().iterator();
    while(iter.hasNext()) {
      Map.Entry<String, String> entry = iter.next();
      if (entry.getValue().equals(uri)) {
        return entry.getKey();
      }
    }
    return null;
  }

  /**
   * Returns a map with all registered prefixes and the corresponding 
   * ontology URIs.
   * 
   * @return a hash map
   */
  public HashMap<String, String> getPrefixMap() {
    return new HashMap<String, String>(protegePrefixManager.getPrefixes());
  }
}
