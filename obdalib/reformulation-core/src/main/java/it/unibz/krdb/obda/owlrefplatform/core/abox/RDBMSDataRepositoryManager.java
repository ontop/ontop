package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.exception.PunningException;

import java.io.IOException;
import java.io.OutputStream;
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
public interface RDBMSDataRepositoryManager {

	public static String	TYPE_DIRECT		= "directdbabox";

	public static String	TYPE_SI			= "semanticindexdbabox";

	public static String	TYPE_UNIVERSAL	= "universaldbabox";

	public void setConfig(Properties confi);

	public void setDatabase(OBDADataSource db) throws SQLException, ClassNotFoundException;

	public void setTBox(Ontology ontology);

	public void setVocabulary(Set<Predicate> vocabulary) throws PunningException;

	public String getType();

	public void getTablesDDL(OutputStream out) throws IOException;

	public void getIndexDDL(OutputStream out) throws IOException;

	public void getSQLInserts(Iterator<Assertion> data, OutputStream out) throws IOException;

	public void getCSVInserts(Iterator<Assertion> data, OutputStream out) throws IOException;

	public void createDBSchema(boolean dropExisting) throws SQLException;

	public void createIndexes() throws SQLException;

	/***
	 * Drops completely existing table for this repository, including the
	 * metadata table.
	 * 
	 * @throws SQLException
	 */
	public void dropDBSchema() throws SQLException;

	public void insertData(Iterator<Assertion> data) throws SQLException;

	public Ontology getABoxDependencies();

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
	public void loadMetadata() throws SQLException;

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
	public boolean checkMetadata() throws SQLException;

	public Collection<OBDAMappingAxiom> getMappings();

	public void collectStatistics() throws SQLException;

	void getDropDDL(OutputStream out) throws IOException;

	void getMetadataSQLInserts(OutputStream outstream) throws IOException;

	void insertMetadata() throws SQLException;

}
