package it.unibz.krdb.obda.owlrefplatform.core.questdb;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.abox.NTripleAssertionIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer.VirtualTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2ABoxIterator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
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

	public QuestDBClassicStore(String name, URI tboxFile, ReformulationPlatformPreferences config) throws Exception {

		super(name);

		if (!config.getProperty(ReformulationPlatformPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC))
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

	/* Move to query time ? */
	public void createIndexes() throws Exception {
		questInstance.createIndexes();
	}

	/* Move to query time ? */
	public void dropIndexes() throws Exception {
		questInstance.dropIndexes();
	}

	/* Move to query time ? */
	public boolean isIndexed() {
		return questInstance.isIndexed();
	}

	/* Move to query time ? */
	public int load(URI rdffile, boolean useFile) throws SQLException, OWLOntologyCreationException, IOException {
		String pathstr = rdffile.toString();
		int dotidx = pathstr.lastIndexOf('.');
		String ext = pathstr.substring(dotidx);
		int result = -1;
		if (ext.toLowerCase().equals(".owl")) {

			OWLOntology owlontology = man.loadOntologyFromPhysicalURI(rdffile);
			Set<OWLOntology> ontos = man.getImportsClosure(owlontology);

			OWLAPI2ABoxIterator aBoxIter = new OWLAPI2ABoxIterator(ontos, questInstance.getEquivalenceMap());
			result = questInstance.insertData(aBoxIter, useFile);
		} else if (ext.toLowerCase().equals(".nt")) {
			NTripleAssertionIterator it = new NTripleAssertionIterator(rdffile, questInstance.getEquivalenceMap());
			result = questInstance.insertData(it, useFile);
		}
		return result;

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
	public void loadOBDAModel(URI uri) throws Exception {
		OBDAModel obdaModel = OBDADataFactoryImpl.getInstance().getOBDAModel();
		DataManager io = new DataManager(obdaModel);
		io.loadOBDADataFromURI(uri, URI.create(""), obdaModel.getPrefixManager());
		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(obdaModel, questInstance.getEquivalenceMap());
		VirtualTriplePredicateIterator assertionIter = (VirtualTriplePredicateIterator) materializer.getAssertionIterator();
		questInstance.insertData(assertionIter);
		assertionIter.disconnect();
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
	public void drop() throws Exception {
		questInstance.dropRepository();
		questInstance.dispose();
	}

}
