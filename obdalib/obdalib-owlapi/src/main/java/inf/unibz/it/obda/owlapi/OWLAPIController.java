package inf.unibz.it.obda.owlapi;

import inf.unibz.it.obda.api.controller.APIController;

import org.semanticweb.owl.model.OWLOntology;

public class OWLAPIController extends APIController {

	OWLOntology					currentOntology	= null;
	// URI currentOntologyPhysicalURI = null;
//	OWLOntologyManager			mmger			= null;

//	private final OWLOntology	root;

//	private SimplePrefixManager	prefixmanager	= null;

//	private final Logger		log				= LoggerFactory.getLogger(this.getClass());

//	private final OWLAPICoupler	apicoupler;
	private boolean				loadingData;

	public OWLAPIController() {
		super();

//		prefixmanager = new SimplePrefixManager();
//		mmger = owlman;
//		
//		this.root = root;
		
//		setMapcontroller(new SynchronizedMappingController(dscontroller, this));
//		setCoupler(new OWLAPICoupler(owlman, getPrefixManager()));
//		setCurrentOntologyURI(root.getURI());
//		owlman.addOntologyChangeListener((OWLOntologyChangeListener) getMappingController());
	}

//	@Override
//	public void setCurrentOntologyURI(URI uri) {
//		this.currentOntology = mmger.getOntology(uri);
//		super.setCurrentOntologyURI(uri);
//
//	}

	// @Override
	// public File getCurrentOntologyFile() {
	// currentOntologyPhysicalURI = mmger.getPhysicalURIForOntology(root);
	//
	// if (currentOntologyPhysicalURI == null)
	// return null;
	//
	// File owlfile = new File(currentOntologyPhysicalURI);
	// return owlfile;
	//
	// // File obdafile = new File(ioManager.getOBDAFile(owlfile.toURI()));
	// // return obdafile;
	// }

	// private void triggerOntologyChanged() {
	// if (!this.loadingData) {
	//
	// OWLOntology ontology = root;
	//
	// if (ontology != null) {
	// OWLClass newClass =
	// mmger.getOWLDataFactory().getOWLClass(URI.create(ontology.getURI() +
	// "#RandomMarianoTest6677841155"));
	// OWLAxiom axiom =
	// mmger.getOWLDataFactory().getOWLDeclarationAxiom(newClass);
	//
	// AddAxiom addChange = new AddAxiom(ontology, axiom);
	// try {
	// mmger.applyChange(addChange);
	//
	// RemoveAxiom removeChange = new RemoveAxiom(ontology, axiom);
	// mmger.applyChange(removeChange);
	// } catch (OWLOntologyChangeException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

//	public boolean loadData(URI obdafileURI) {
//		loadingData = true;
//
//		try {
//			// URI obdafile = getIOManager().getOBDAFile(owlFile);
//			getIOManager().loadOBDADataFromURI(obdafileURI, getCurrentOntologyURI());
//		} catch (Exception e) {
//			loadingData = false;
//			log.error(e.getMessage(), e);
//		}
//
//		return loadingData;
//	}
//
//	public void saveData(URI obdaFile) throws FileNotFoundException, ParserConfigurationException, IOException {
//		getIOManager().saveOBDAData(obdaFile);
//	}

//	@Override
//	public Set<URI> getOntologyURIs() {
//		HashSet<URI> uris = new HashSet<URI>();
//		Set<OWLOntology> ontologies = mmger.getOntologies();
//		for (OWLOntology owlOntology : ontologies) {
//			uris.add(owlOntology.getURI());
//		}
//		return uris;
//	}

}
