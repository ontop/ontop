package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import org.semanticweb.ontop.owlrefplatform.core.abox.RepositoryChangedListener;
import org.semanticweb.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.VocabularyValidator;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import org.semanticweb.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import org.semanticweb.ontop.pivotalrepr.MetadataForQueryOptimization;
import org.semanticweb.ontop.model.DataSourceMetadata;

/**
 * Automatically extracted.
 *
 * TODO: clean it.
 */
public interface IQuest extends RepositoryChangedListener {

    VocabularyValidator getVocabularyValidator();

    NativeQueryGenerator cloneIfNecessaryNativeQueryGenerator();

    QuestUnfolder getQuestUnfolder();

    OBDAModel getOBDAModel();

    Multimap<Predicate,Integer> copyMultiTypedFunctionSymbolIndex();

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

    Optional<RDBMSSIRepositoryManager> getOptionalSemanticIndexRepository();

    DataSourceMetadata getMetaData();

    QueryCache getQueryCache();

    MetadataForQueryOptimization getMetadataForQueryOptimization();

    DatalogProgram getRewriting(DatalogProgram initialProgram) throws OBDAException;

    ExpressionEvaluator getExpressionEvaluator();

    SparqlAlgebraToDatalogTranslator getSparqlAlgebraToDatalogTranslator();
}
