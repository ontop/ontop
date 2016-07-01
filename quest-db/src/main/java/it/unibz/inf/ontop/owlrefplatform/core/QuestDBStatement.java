package it.unibz.inf.ontop.owlrefplatform.core;

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
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlapi.OWLAPIABoxIterator;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.mapping.MappingParser;

import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.owlrefplatform.core.abox.NTripleAssertionIterator;
import it.unibz.inf.ontop.owlrefplatform.core.abox.QuestMaterializer;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.ParsedQuery;
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
	private final Logger log = LoggerFactory.getLogger(QuestDBStatement.class);

	private transient OWLOntologyManager man = OWLManager.createOWLOntologyManager();

	public QuestDBStatement(IQuestStatement st, NativeQueryLanguageComponentFactory nativeQLFactory) {
        this.nativeQLFactory = nativeQLFactory;
		this.st = st;
	}

	public int add(Iterator<Assertion> data) throws OBDAException {
		return st.insertData(data, -1, -1);
	}

	public int add(Iterator<Assertion> data, int commit, int batch) throws OBDAException {
		return st.insertData(data, commit, batch);
	}

	public int add(URI rdffile) throws OBDAException {
		return load(rdffile, false, -1, -1);
	}

	public int addWithTempFile(URI rdffile) throws OBDAException {
		return load(rdffile, true, -1, -1);
	}

	public int addFromOBDA(URI obdaFile) throws OBDAException {
		return loadOBDAModel(obdaFile, false, -1, -1);
	}

	/* Move to query time ? */
	private int load(URI rdffile, boolean useFile, int commit, int batch) throws OBDAException {
		String pathstr = rdffile.toString();
		int dotidx = pathstr.lastIndexOf('.');
		String ext = pathstr.substring(dotidx);
		int result = -1;
		try {
			if (ext.toLowerCase().equals(".owl")) {
				OWLOntology owlontology = man.loadOntologyFromOntologyDocument(IRI.create(rdffile));
				Set<OWLOntology> ontos = man.getImportsClosure(owlontology);
				
				OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(ontos, st.getQuestInstance().getVocabulary());
				result = st.insertData(aBoxIter, /*useFile,*/ commit, batch);
			} 
			else if (ext.toLowerCase().equals(".nt")) {				
				NTripleAssertionIterator it = new NTripleAssertionIterator(rdffile);
				result = st.insertData(it, /*useFile,*/ commit, batch);
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

			materializer = new QuestMaterializer(obdaModel, false);
			assertionIter =  materializer.getAssertionIterator();
			int result = st.insertData(assertionIter, /*useFile,*/ commit, batch);
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
	 * Use getExecutableQuery instead.
	 */
	@Deprecated
	public String getSQL(String sparqlQuery) throws OBDAException {
		ExecutableQuery executableQuery = getExecutableQuery(sparqlQuery);
		return ((SQLExecutableQuery) executableQuery).getSQL();
	}

	public ExecutableQuery getExecutableQuery(String sparqlQuery) throws OBDAException {
		try {
			QuestQueryProcessor queryProcessor = st.getQuestInstance().getEngine();
			ParsedQuery pq = queryProcessor.getParsedQuery(sparqlQuery);
			// TODO: extract the construction template when existing
			return queryProcessor.translateIntoNativeQuery(
					pq, Optional.empty());
		} catch (MalformedQueryException e) {
			throw new OBDAException(e);
		}
	}
}
