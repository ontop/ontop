package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi2.OWLAPI2ABoxIterator;
import it.unibz.krdb.obda.owlapi2.OWLAPI2Translator;
import it.unibz.krdb.obda.owlapi2.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.abox.NTripleAssertionIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer.VirtualTriplePredicateIterator;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * An instance of Store that encapsulates all the functionality needed for a
 * "classic" store.
 * 
 * @author mariano
 * 
 */
public class QuestDBClassicStore extends QuestDBAbstractStore {

	// TODO all this needs to be refactored later to allow for transactions,
	// autocommit enable/disable, clients ids, etc

	private static final long serialVersionUID = 2495624993519521937L;

	private static Logger log = LoggerFactory.getLogger(QuestDBClassicStore.class);

	public QuestDBClassicStore(String name, URI tboxFile, QuestPreferences config) throws Exception {

		super(name);

		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC))
			throw new Exception("A classic repository must be created with the CLASSIC flag in the configuration.");

		OWLAPI2Translator translator = new OWLAPI2Translator();
		OWLOntology owlontology = man.loadOntology(tboxFile);
		Ontology tbox = translator.mergeTranslateOntologies(Collections.singleton(owlontology));
		questInstance = new Quest();
		questInstance.setPreferences(config);
		questInstance.loadTBox(tbox);
		questInstance.setupRepository();

		log.debug("Store {} has been created successfully", name);
	}

	public void createDB() throws OBDAException {
		checkConnection();
		QuestStatement st = conn.createStatement();
		try {
			st.createDB();
		} catch (Exception e) {
			throw new OBDAException(e);
		} finally {
			st.close();
		}
	}

	/* Move to query time ? */
	public void createIndexes() throws OBDAException {
		checkConnection();
		QuestStatement st = conn.createStatement();
		try {
			st.createIndexes();
		} catch (Exception e) {
			throw new OBDAException(e);
		} finally {
			st.close();
		}

	}

	/* Move to query time ? */
	public void dropIndexes() throws OBDAException {
		checkConnection();
		QuestStatement st = conn.createStatement();
		try {
			st.dropIndexes();
		} catch (Exception e) {
			throw new OBDAException(e);
		} finally {
			st.close();
		}
	}

	/* Move to query time ? */
	public boolean isIndexed() throws OBDAException {
		checkConnection();
		QuestStatement st = conn.createStatement();
		boolean result = st.isIndexed();
		st.close();
		return result;
	}

	/* Move to query time ? */
	public int load(URI rdffile, boolean useFile) throws OBDAException {
		checkConnection();
		String pathstr = rdffile.toString();
		int dotidx = pathstr.lastIndexOf('.');
		String ext = pathstr.substring(dotidx);
		int result = -1;
		QuestStatement st = conn.createStatement();
		try {

			if (ext.toLowerCase().equals(".owl")) {

				OWLOntology owlontology = man.loadOntologyFromPhysicalURI(rdffile);
				Set<OWLOntology> ontos = man.getImportsClosure(owlontology);

				OWLAPI2ABoxIterator aBoxIter = new OWLAPI2ABoxIterator(ontos, questInstance.getEquivalenceMap());
				result = st.insertData(aBoxIter, useFile, 50000, 5000);
			} else if (ext.toLowerCase().equals(".nt")) {
				NTripleAssertionIterator it = new NTripleAssertionIterator(rdffile, questInstance.getEquivalenceMap());
				result = st.insertData(it, useFile, 50000, 5000);
			}
			return result;

		} catch (Exception e) {
			throw new OBDAException(e);
		} finally {
			st.close();
		}

	}

	/* Move to query time ? */
	public void deleteAllTriples() {

	}

	/* Move to query time ? */
	public void addOBDAModel(URI model) {

	}

	/* Move to query time ? */
	public void addOBDAModel(URI model, String name) {

	}

	/* Move to query time ? */
	public void dropOBDAModel(URI model) {

	}

	/* Move to query time ? */
	public void dropOBDAModel(String name) {

	}

	public OBDAModel getOBDAModel(String name) {
		return null;
	}

	public List<OBDAModel> getOBDAModels() {
		return null;
	}

	/* Move to query time ? */
	public void loadOBDAModel(URI uri) throws OBDAException {
		checkConnection();
		QuestStatement st = conn.createStatement();
		VirtualTriplePredicateIterator assertionIter = null;
		try {
			OBDAModel obdaModel = OBDADataFactoryImpl.getInstance().getOBDAModel();
			DataManager io = new DataManager(obdaModel);
			io.loadOBDADataFromURI(uri, URI.create(""), obdaModel.getPrefixManager());
			VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(obdaModel, questInstance.getEquivalenceMap());
			assertionIter = (VirtualTriplePredicateIterator) materializer.getAssertionIterator();
			st.insertData(assertionIter, 50000, 5000);

		} catch (Exception e) {
			throw new OBDAException(e);
		} finally {
			st.close();

			try {
				if (assertionIter != null)
					assertionIter.disconnect();
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new OBDAException(e.getMessage());
			}
		}
	}

	/* Move to query time ? */
	public void loadOBDAModel(URI uri, boolean fast) {

	}

	/* Move to query time ? */
	public void loadOBDAModel(String name) {

	}

	/* Move to query time ? */
	public void loadOBDAModel(String name, boolean fast) {

	}

	/* Move to query time ? */
	public void refreshOBDAData() {

	}

	/* Move to query time ? */
	public void refreshOBDAData(boolean fast) {

	}

	@Override
	public void drop() throws OBDAException {
		checkConnection();
		QuestStatement st = conn.createStatement();
		try {
			st.dropRepository();
		} catch (Exception e) {
			throw new OBDAException(e);
		} finally {
			st.close();
			questInstance.dispose();
		}
	}

	public void analyze() throws OBDAException {
		checkConnection();
		QuestStatement st = conn.createStatement();
		try {
			st.analyze();
		} catch (Exception e) {
			throw new OBDAException(e);
		} finally {
			st.close();
		}

	}

}
