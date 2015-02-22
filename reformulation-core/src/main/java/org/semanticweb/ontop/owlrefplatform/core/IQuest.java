package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.collect.Multimap;
import org.openrdf.query.parser.ParsedQuery;
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
import java.util.List;
import java.util.Map;

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

    Map<String, Boolean> getIsDescribeCache();

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
    OBDAConnection getNonPoolConnection() throws OBDAException;

    OBDAConnection getConnection() throws OBDAException;

    UriTemplateMatcher getUriTemplateMatcher();

    DatalogProgram unfold(DatalogProgram query, String targetPredicate) throws OBDAException;

    void repositoryChanged();

    boolean isSemIdx();

	TBoxReasoner getReasoner();

    RDBMSSIRepositoryManager getSemanticIndexRepository();

    DBMetadata getMetaData();

    LinearInclusionDependencies getDataDependencies();
    
    Map<String,String> getSQLCache();

    Map<String,List<String>> getSignatureCache();

    Map<String,ParsedQuery> getSesameQueryCache();

    Map<String,Boolean> getIsBooleanCache();

    Map<String,Boolean> getIsConstructCache();
}
