package it.unibz.inf.ontop.owlrefplatform.core;

import java.util.List;
import java.util.Optional;

import it.unibz.inf.ontop.model.*;

import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.injection.QuestCorePreferences;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

/**
 * Automatically extracted.
 *
 * TODO: clean it.
 */
public interface IQuest {

    OBDAModel getOBDAModel();

    void dispose();

    QuestCorePreferences getPreferences();

    void setupRepository() throws Exception;

    void close();

    // get a real (non pool) connection - used for protege plugin
    IQuestConnection getNonPoolConnection() throws OBDAException;

    IQuestConnection getConnection() throws OBDAException;

	//TBoxReasoner getReasoner();

    SemanticIndexURIMap getUriMap();

    QuestQueryProcessor getEngine();

    Optional<RDBMSSIRepositoryManager> getOptionalSemanticIndexRepository();

    DBMetadata getMetaData();

    ImmutableOntologyVocabulary getVocabulary();

    // TEST ONLY
    List<CQIE> getUnfolderRules();
}
