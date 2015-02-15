package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.collect.Multimap;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import org.semanticweb.ontop.owlrefplatform.core.abox.IRDBMSSIRepositoryManager;
import org.semanticweb.ontop.owlrefplatform.core.abox.RepositoryChangedListener;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.VocabularyValidator;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.owlrefplatform.core.reformulation.QueryRewriter;
import org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

    IQuestUnfolder getQuestUnfolder();

    OBDAModel getOBDAModel();

    Multimap<Predicate,Integer> copyMultiTypedFunctionSymbolIndex();

    void dispose();

    Properties getPreferences();

    void disconnect() throws SQLException;

    void setupRepository() throws Exception;

    void updateSemanticIndexMappings() throws DuplicateMappingException, OBDAException;

    void close();

    void releaseSQLPoolConnection(Connection co);

    Connection getSQLPoolConnection() throws OBDAException;

    // get a real (non pool) connection - used for protege plugin
    OBDAConnection getNonPoolConnection() throws OBDAException;

    OBDAConnection getConnection() throws OBDAException;

    UriTemplateMatcher getUriTemplateMatcher();

    DatalogProgram unfold(DatalogProgram query, String targetPredicate) throws OBDAException;

    void repositoryChanged();

    boolean isSemIdx();

	TBoxReasoner getReasoner();

    IRDBMSSIRepositoryManager getSemanticIndexRepository();

    DBMetadata getMetaData();

    LinearInclusionDependencies getDataDependencies();
}
