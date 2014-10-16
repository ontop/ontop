package org.semanticweb.ontop.owlrefplatform.core.abox;

import java.util.Map;

/**
 * Extracted from the implementation.
 * TODO: clean it
 */
public interface RDBMSSIRepositoryManager extends RDBMSDataRepositoryManager {
    void addRepositoryChangedListener(RepositoryChangedListener list);

    Map<String,Integer> getUriIds();

    Map<Integer, String> getUriMap();
}
