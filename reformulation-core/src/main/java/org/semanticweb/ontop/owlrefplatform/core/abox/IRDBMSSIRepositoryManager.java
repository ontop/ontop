package org.semanticweb.ontop.owlrefplatform.core.abox;

import java.util.Map;

/**
 * Extracted from the implementation.
 * TODO: clean it
 */
public interface IRDBMSSIRepositoryManager extends RDBMSDataRepositoryManager {
    void addRepositoryChangedListener(RepositoryChangedListener list);

    SemanticIndexURIMap getUriMap();
}
