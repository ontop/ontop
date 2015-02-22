package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.collect.Multimap;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import org.semanticweb.ontop.owlrefplatform.core.abox.RepositoryChangedListener;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.VocabularyValidator;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.owlrefplatform.core.reformulation.QueryRewriter;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;

import java.sql.Connection;

/**
 * Automatically extracted.
 *
 * TODO: clean it.
 */
public interface IQuest extends RepositoryChangedListener {
    void setImplicitDBConstraints(ImplicitDBConstraints userConstraints);

    VocabularyValidator getVocabularyValidator();

    QueryRewriter getRewriter();

    NativeQueryGenerator cloneIfNecessaryNativeQueryGenerator();

    QuestUnfolder getQuestUnfolder();

    OBDAModel getOBDAModel();

    Multimap<Predicate,Integer> copyMultiTypedFunctionSymbolIndex();

    void dispose();

    QuestPreferences getPreferences();

    void setupRepository() throws Exception;

    void updateSemanticIndexMappings() throws DuplicateMappingException, OBDAException;

    void close();

    @Deprecated
    void releaseSQLPoolConnection(Connection co);

    @Deprecated
    Connection getSQLPoolConnection() throws OBDAException;

    // get a real (non pool) connection - used for protege plugin
    IQuestConnection getNonPoolConnection() throws OBDAException;

    OBDAConnection getConnection() throws OBDAException;

    UriTemplateMatcher getUriTemplateMatcher();

    DatalogProgram unfold(DatalogProgram query, String targetPredicate) throws OBDAException;

    void repositoryChanged();

    boolean isSemIdx();

	TBoxReasoner getReasoner();

    RDBMSSIRepositoryManager getSemanticIndexRepository();

    DBMetadata getMetaData();

    LinearInclusionDependencies getDataDependencies();

    QueryCache getQueryCache();
}
