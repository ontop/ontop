package org.semanticweb.ontop.owlrefplatform.core;

/*
 * #%L
 * ontop-quest-db
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.owlapi3.OWLAPI3ABoxIterator;
import org.semanticweb.ontop.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import org.semanticweb.ontop.owlrefplatform.core.abox.NTripleAssertionIterator;
import org.semanticweb.ontop.owlrefplatform.core.abox.QuestMaterializer;
import org.semanticweb.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of QuestDBStatement.
 *
 * TODO: rename it QuestDBStatementImpl.
 */
// DISABLED TEMPORARILY FOR MERGING PURPOSES (NOT BREAKING CLIENTS WITH this ugly name IQquestOWLStatement)
//public class QuestDBStatement implements IQuestDBStatement {
public class QuestDBStatement implements OBDAStatement {

	private final IQuestStatement st;
    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private Logger log = LoggerFactory.getLogger(QuestDBStatement.class);

	protected transient OWLOntologyManager man = OWLManager.createOWLOntologyManager();

	public QuestDBStatement(IQuestStatement st, NativeQueryLanguageComponentFactory nativeQLFactory) {
        this.nativeQLFactory = nativeQLFactory;
		this.st = st;
	}

	public int add(Assertion data) throws OBDAException {
		return st.insertData(Collections.singleton(data).iterator(), false, -1, -1);
	}

	public int add(Iterator<Assertion> data) throws OBDAException {
		return st.insertData(data, false, -1, -1);
	}

	/***
	 * As before, but using recreateIndexes = false.
	 */
	public int add(Iterator<Assertion> data, int commit, int batch) throws OBDAException {
		return st.insertData(data, false, commit, batch);
	}

	public int add(URI rdffile) throws OBDAException {
		return load(rdffile, false, -1, -1);
	}

	public int add(URI rdffile, int commit, int batch) throws OBDAException {
		return load(rdffile, false, commit, batch);
	}

	public int addWithTempFile(URI rdffile) throws OBDAException {
		return load(rdffile, true, -1, -1);
	}

	public int addFromOBDA(URI obdaFile) throws OBDAException {
		return loadOBDAModel(obdaFile, false, -1, -1);
	}

	public int addFromOBDA(URI obdaFile, int commitrate, int batchinserts) throws OBDAException {
		return loadOBDAModel(obdaFile, false, commitrate, batchinserts);
	}

	public int addFromOBDAWithTempFile(URI obdaFile) throws OBDAException {
		return loadOBDAModel(obdaFile, true, -1, -1);
	}

	/* Move to query time ? */
	private int load(URI rdffile, boolean useFile, int commit, int batch) throws OBDAException {
		String pathstr = rdffile.toString();
		int dotidx = pathstr.lastIndexOf('.');
		String ext = pathstr.substring(dotidx);

        IQuest questInstance = st.getQuestInstance();

		int result = -1;
		try {
			if (ext.toLowerCase().equals(".owl")) {
				OWLOntology owlontology = man.loadOntologyFromOntologyDocument(IRI.create(rdffile));
				Set<OWLOntology> ontos = man.getImportsClosure(owlontology);

				EquivalentTriplePredicateIterator aBoxNormalIter =
						new EquivalentTriplePredicateIterator(new OWLAPI3ABoxIterator(ontos),
								questInstance.getReasoner());

				result = st.insertData(aBoxNormalIter, useFile, commit, batch);
			}
			else if (ext.toLowerCase().equals(".nt")) {
				NTripleAssertionIterator it = new NTripleAssertionIterator(rdffile);
				EquivalentTriplePredicateIterator aBoxNormalIter =
						new EquivalentTriplePredicateIterator(it, questInstance.getReasoner());

				result = st.insertData(aBoxNormalIter, useFile, commit, batch);
			}
			return result;
		} catch (Exception e) {
			throw new OBDAException(e);
		} finally {
			st.close();
		}
	}

	/* Move to query time ? */
	private int loadOBDAModel(URI uri, boolean useFile, int commit, int batch)
			throws OBDAException {
		Iterator<Assertion> assertionIter = null;
		QuestMaterializer materializer = null;
		try {
            MappingParser parser = nativeQLFactory.create(new File(uri));
            OBDAModel obdaModel = parser.getOBDAModel();

			materializer = new QuestMaterializer(obdaModel);
			assertionIter =  materializer.getAssertionIterator();
			int result = st.insertData(assertionIter, useFile, commit, batch);
			return result;

		} catch (Exception e) {
			throw new OBDAException(e);
		} finally {
			st.close();
			try {
				if (assertionIter != null)
					materializer.disconnect();
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new OBDAException(e.getMessage());
			}
		}
	}

	@Override
	public void cancel() throws OBDAException {
		st.cancel();
	}

	@Override
	public void close() throws OBDAException {
		st.close();
	}

	@Override
	public ResultSet execute(String query) throws OBDAException {
		return st.execute(query);
	}

	@Override
	public int executeUpdate(String query) throws OBDAException {
		return st.executeUpdate(query);
	}

	@Override
	public OBDAConnection getConnection() throws OBDAException {
		return st.getConnection();
	}

	@Override
	public int getFetchSize() throws OBDAException {
		return st.getFetchSize();
	}

	@Override
	public int getMaxRows() throws OBDAException {
		return st.getMaxRows();
	}

	@Override
	public void getMoreResults() throws OBDAException {
		st.getMoreResults();
	}

	@Override
	public TupleResultSet getResultSet() throws OBDAException {
		return st.getResultSet();
	}

	@Override
	public int getQueryTimeout() throws OBDAException {
		return st.getQueryTimeout();
	}

	@Override
	public void setFetchSize(int rows) throws OBDAException {
		st.setFetchSize(rows);
	}

	@Override
	public void setMaxRows(int max) throws OBDAException {
		st.setMaxRows(max);
	}

	@Override
	public boolean isClosed() throws OBDAException {
		return st.isClosed();
	}

	@Override
	public void setQueryTimeout(int seconds) throws OBDAException {
		st.setQueryTimeout(seconds);
	}


	/**
	 * Ontop is not SQL-specific anymore.
	 *
	 * Use getTargetQuery instead.
	 */
	@Deprecated
	public String getSQL(String query) throws OBDAException {
		return st.unfoldAndGenerateTargetQuery(query).getNativeQueryString();
	}

	public TargetQuery getTargetQuery(String query) throws OBDAException {
		return st.unfoldAndGenerateTargetQuery(query);
	}

	@Override
	public String getSPARQLRewriting(String query) throws OBDAException {
		return st.getSPARQLRewriting(query);
	}

	@Override
	public int getTupleCount(String query) throws OBDAException {
		return st.getTupleCount(query);
	}

	public String getRewriting(String query) throws OBDAException {
		
		QueryParser qp = QueryParserUtil.createParser(QueryLanguage.SPARQL);
		ParsedQuery pq = null; // base URI is null
		try {
			pq = qp.parseQuery(query, null);
		} catch (MalformedQueryException e) {
			throw new OBDAException(e);
		}

		SparqlAlgebraToDatalogTranslator tr = new SparqlAlgebraToDatalogTranslator(this.st.getQuestInstance().getUriTemplateMatcher());
		
		ImmutableList<String> signatureContainer = tr.getSignature(pq);
		return st.getRewriting(pq, signatureContainer);
	}
}
