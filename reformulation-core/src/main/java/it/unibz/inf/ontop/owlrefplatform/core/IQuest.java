package it.unibz.inf.ontop.owlrefplatform.core;

import java.util.Optional;
import com.google.common.collect.Multimap;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.model.*;

import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RepositoryChangedListener;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;

/**
 * Automatically extracted.
 *
 * TODO: clean it.
 */
public interface IQuest extends RepositoryChangedListener {

    OBDAModel getOBDAModel();

    void dispose();

    QuestPreferences getPreferences();

    void setupRepository() throws Exception;

    void updateSemanticIndexMappings() throws DuplicateMappingException, OBDAException;

    void close();

    // get a real (non pool) connection - used for protege plugin
    IQuestConnection getNonPoolConnection() throws OBDAException;

    IQuestConnection getConnection() throws OBDAException;

    void repositoryChanged();

	TBoxReasoner getReasoner();

    SemanticIndexURIMap getUriMap();

    QuestQueryProcessor getEngine();

    Optional<RDBMSSIRepositoryManager> getOptionalSemanticIndexRepository();

    DataSourceMetadata getMetaData();

    DatalogProgram getRewriting(DatalogProgram initialProgram) throws OBDAException;

    ExpressionEvaluator getExpressionEvaluator();

    ImmutableOntologyVocabulary getVocabulary();
}
