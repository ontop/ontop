package org.semanticweb.ontop.owlrefplatform.core.abox;

/*
 * #%L
 * ontop-reformulation-core
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

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.impl.PunningException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/***
 * A Data Repository Manager is an utility setup the data back end of the
 * reasoner. The duties of a manager are:
 * 
 * - Constructing a repository schema from a vocabulary - Constructing the
 * mappings for the schema - Inserting ABox assertions into the repository -
 * Expanding the data in the repository with respect to a TBox - Describing the
 * level of completeness of the repository by means of ABox dependencies.
 * 
 * The top priority in the manager is performance. The manager should be able to
 * handle very large TBox and ABoxes without compromising responsiveness of the
 * application. If possible, it should make use of little memory and deal with
 * data in a "streaming" way.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public interface RDBMSDataRepositoryManager extends Serializable {

	public static String TYPE_DIRECT = "directdbabox";

	public static String TYPE_SI = "semanticindexdbabox";

	public static String TYPE_UNIVERSAL = "universaldbabox";

	public void setConfig(Properties confi);

	// public void disconnect();
	//
	// public Connection getConnection();
	//
	// public void setDatabase(Connection conn);

	public void setTBox(TBoxReasoner reasonerDag);

	public void setVocabulary(Set<Predicate> vocabulary) throws PunningException;

	public String getType();

	public void getTablesDDL(OutputStream out) throws IOException;

	public void getIndexDDL(OutputStream out) throws IOException;

	public void getSQLInserts(Iterator<Assertion> data, OutputStream out) throws IOException;

	// public void getCSVInserts(Iterator<Assertion> data, OutputStream out)
	// throws IOException;

	public void createDBSchema(Connection conn, boolean dropExisting) throws SQLException;

	public boolean isDBSchemaDefined(Connection conn) throws SQLException;

	/***
	 * Forces the repository to setup indexes for the data tables. Exactly which
	 * indexes depends on the repository and the configuraiton of the
	 * repository.
	 * 
	 * @throws SQLException
	 */
	public void createIndexes(Connection conn) throws SQLException;

	/***
	 * Drops any existing indexes in this repository. It will force "isIndexed"
	 * to return false
	 * 
	 * @throws SQLException
	 */
	public void dropIndexes(Connection conn) throws SQLException;

	/***
	 * Drops completely existing table for this repository, including the
	 * metadata table.
	 * 
	 * @throws SQLException
	 */
	public void dropDBSchema(Connection conn) throws SQLException;

	/***
	 * Inserts all the triples in the iterator to the repository using SQL
	 * INSERT commands. Commits will be done using prepared batch statements.
	 * The size of the batch package, and a commit rate can be specified using
	 * the commit and batch parameters respectively. parameter. parameter.
	 * 
	 * @param conn
	 * @param data
	 * @param commit
	 *            A rate >= 1. If commit = n, every n inserts there will be a
	 *            call to conn.commit() If commit = < 1, no calls to
	 *            conn.commit() are done.
	 * @param batch
	 *            The number of inserts before a call to statement.executeBatch
	 *            done. If batch < 1 then the full content of the iterator is
	 *            considered a single batch.
	 * @return
	 * @throws SQLException
	 */
	public int insertData(Connection conn, Iterator<Assertion> data, int commit, int batch) throws SQLException;

	/***
	 * Returns true if indexes are active in this repository.
	 * 
	 * @return
	 */
	public boolean isIndexed(Connection conn);

	//public Ontology getABoxDependencies();

	/***
	 * Attempts to load the metadata from the database. This will override the
	 * existing metadata.
	 * 
	 * The metadata of a repository contains the vocabulary that was used to
	 * create the repository and more information, depending on the kind of
	 * repository. Possible data that might be retrieved are index and ranges
	 * used to store and map the vocabulary, etc.
	 * 
	 * @throws Exception
	 */
	public void loadMetadata(Connection conn) throws SQLException;

	/***
	 * Checks if the metadata stored in the DB matches the vocabulary given to
	 * the manager.
	 * 
	 * If there is no metadata in the DB, then the method always returns true.
	 * The method returns false If there is metadata and this metadata indicates
	 * a conflict of vocabularies (e.g., a class or property is not registred in
	 * the metadata or it is registered but it has a different type).
	 * 
	 * @return
	 * @throws SQLException
	 */
	public boolean checkMetadata(Connection conn) throws SQLException;

	public ImmutableList<OBDAMappingAxiom> getMappings() throws OBDAException;

	public void collectStatistics(Connection conn) throws SQLException;

	void getDropDDL(OutputStream out) throws IOException;

	void getMetadataSQLInserts(OutputStream outstream) throws IOException;

	void insertMetadata(Connection conn) throws SQLException;

	public long loadWithFile(Connection conn, Iterator<Assertion> data) throws SQLException, IOException;

}
