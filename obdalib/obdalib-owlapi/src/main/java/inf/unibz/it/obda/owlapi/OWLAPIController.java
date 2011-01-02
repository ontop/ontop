package inf.unibz.it.obda.owlapi;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.io.PrefixManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyChangeListener;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class OWLAPIController extends APIController {

	OWLOntology			currentOntology				= null;
	URI					currentOntologyPhysicalURI	= null;
	OWLOntologyManager	mmger						= null;
	private final OWLOntology	root;
	private PrefixManager prefixmanager = null;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public OWLAPIController(OWLOntologyManager owlman, OWLOntology root) {
		super();
//		mapcontroller = new SynchronizedMappingController(dscontroller, this);
		mapcontroller = new MappingController(dscontroller, this);
		prefixmanager = new PrefixManager();
		ioManager = new OWLAPIDataManager(this, prefixmanager);
		mmger = owlman;
		currentOntologyPhysicalURI = mmger.getPhysicalURIForOntology(root);
		this.root = root;
		mapcontroller = new SynchronizedMappingController(dscontroller, this);
		ioManager = new OWLAPIDataManager(this, new PrefixManager());
		apicoupler = new OWLAPICoupler(this, owlman, root,(OWLAPIDataManager) ioManager);
		setCurrentOntologyURI(root.getURI());
		setCoupler(apicoupler);
		try {
			setCurrentOntologyURI(root.getURI());
//			RDBMSForeignKeyConstraintController fkc = new RDBMSForeignKeyConstraintController();
//			addAssertionController(RDBMSForeignKeyConstraint.class, fkc, new RDBMSForeignKeyConstraintXMLCodec());
//			dscontroller.addDatasourceControllerListener(fkc);
//			RDBMSPrimaryKeyConstraintController pkc = new RDBMSPrimaryKeyConstraintController();
//			addAssertionController(RDBMSPrimaryKeyConstraint.class, pkc, new RDBMSPrimaryKeyConstraintXMLCodec());
//			dscontroller.addDatasourceControllerListener(pkc);
//			RDBMSCheckConstraintController ccc = new RDBMSCheckConstraintController();
//			addAssertionController(RDBMSCheckConstraint.class,ccc, new RDBMSCheckConstraintXMLCodec());
//			dscontroller.addDatasourceControllerListener(ccc);
//			RDBMSDisjointnessDependencyController ddc = new RDBMSDisjointnessDependencyController();
//			addAssertionController(RDBMSDisjointnessDependency.class, ddc,new RDBMSDisjointnessDependencyXMLCodec());
//			dscontroller.addDatasourceControllerListener(ddc);
//			RDBMSUniquenessConstraintController uqc = new RDBMSUniquenessConstraintController();
//			addAssertionController(RDBMSUniquenessConstraint.class, uqc, new RDBMSUniquenessConstraintXMLCodec());
//			dscontroller.addDatasourceControllerListener(uqc);

		} catch (Exception e) {
			e.printStackTrace();
		}
		owlman.addOntologyChangeListener((OWLOntologyChangeListener) mapcontroller);
	}

	@Override
	public void setCurrentOntologyURI(URI uri) {
		this.currentOntology = mmger.getOntology(uri);
		super.setCurrentOntologyURI(uri);

	}

	@Override
	public File getCurrentOntologyFile() {
		currentOntologyPhysicalURI = mmger.getPhysicalURIForOntology(root);

		if (currentOntologyPhysicalURI == null)
			return null;

		File owlfile = new File(currentOntologyPhysicalURI);
		return owlfile;

//		File obdafile = new File(ioManager.getOBDAFile(owlfile.toURI()));
//		return obdafile;
	}

	private final OWLAPICoupler	apicoupler;
	private boolean			loadingData;

	private void triggerOntologyChanged() {
		if (!this.loadingData) {

			OWLOntology ontology = root;

			if (ontology != null) {
				OWLClass newClass = mmger.getOWLDataFactory().getOWLClass(URI.create(ontology.getURI() + "#RandomMarianoTest6677841155"));
				OWLAxiom axiom = mmger.getOWLDataFactory().getOWLDeclarationAxiom(newClass);

				AddAxiom addChange = new AddAxiom(ontology, axiom);
				try {
					mmger.applyChange(addChange);

					RemoveAxiom removeChange = new RemoveAxiom(ontology, axiom);
					mmger.applyChange(removeChange);
				} catch (OWLOntologyChangeException e) {
					e.printStackTrace();
				}
			}
		}
	}




	public boolean loadData(URI owlFile) {
		loadingData = true;

		File obdaFile = new File(getIOManager().getOBDAFile(owlFile));
		if (obdaFile == null) {
			log.error("OBDA file not found.");
			return false;
		}
		if (!obdaFile.exists()) {
			return false;
		}
		if (!obdaFile.canRead()) {
			log.error("Cannot read the OBDA file:" + obdaFile.toString());
		}
		Document doc = null;
		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(obdaFile);
			doc.getDocumentElement().normalize();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		Element root = doc.getDocumentElement(); // OBDA
		if (root.getNodeName() != "OBDA") {
			log.error("The OBDA info file should start with <OBDA> tag!");
			return false;
		}
		NamedNodeMap att = root.getAttributes();
		if(att.getLength()>1){
			for(int i=0;i<att.getLength();i++){
				Node n = att.item(i);
				String name = n.getNodeName();
				String value = n.getNodeValue();
				if(name.equals("xml:base")){
					prefixmanager.addUri(URI.create(value), name);
				}else if (name.equals("version")){
					prefixmanager.addUri(URI.create(value), name);
				}else if (name.equals("xmlns")){
					prefixmanager.addUri(URI.create(value), name);
				}else if (value.endsWith(".owl#")){
					String[] aux = name.split(":");
					prefixmanager.addUri(URI.create(value), aux[1]);
				}else if(name.equals("xmlns:owl2xml")){
					String[] aux = name.split(":");
					prefixmanager.addUri(URI.create(value), aux[1]);
				}
			}
		}else{
			String ontoUrl = getCurrentOntologyURI().toString();
			int i = ontoUrl.lastIndexOf("/");
			String ontoName = ontoUrl.substring(i+1,ontoUrl.length()-4); //-4 because we want to remove the .owl suffix
			prefixmanager.addUri(URI.create(ontoUrl),"xml:base");
			prefixmanager.addUri(URI.create(ontoUrl),"xmlns");
			prefixmanager.addUri(URI.create(ontoUrl+"#"),ontoName);
		}
		try {
			URI obdafile = getIOManager().getOBDAFile(owlFile);
			getIOManager().loadOBDADataFromURI(obdafile);
		} catch (Exception e) {
			e.printStackTrace();
			loadingData = false;
		}
		return loadingData;
	}

	public void saveData(URI owlFile) throws FileNotFoundException, ParserConfigurationException, IOException {
//		Logger.getLogger(OWLAPIController.class).info("Saving OBDA data for " + owlFile.toString());
		URI obdafile = getIOManager().getOBDAFile(owlFile);
//		try {
			getIOManager().saveOBDAData(obdafile);
//		} catch (Exception e) {
//			e.printStackTrace();
//			triggerOntologyChanged();
//			Logger.getLogger(OWLAPIController.class).error(e);
//		}

	}

	@Override
	public Set<URI> getOntologyURIs() {
		HashSet<URI> uris = new HashSet<URI>();
		Set<OWLOntology> ontologies = mmger.getOntologies();
		for (OWLOntology owlOntology : ontologies) {
			uris.add(owlOntology.getURI());
		}
		return uris;
	}



	@Override
	public URI getPhysicalURIOfOntology(URI onto) {
		// TODO Auto-generated method stub
		return null;
	}

}
