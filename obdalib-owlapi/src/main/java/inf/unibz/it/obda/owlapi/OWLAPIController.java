package inf.unibz.it.obda.owlapi;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.io.DataManager;
import inf.unibz.it.obda.constraints.controller.RDBMSCheckConstraintController;
import inf.unibz.it.obda.constraints.controller.RDBMSForeignKeyConstraintController;
import inf.unibz.it.obda.constraints.controller.RDBMSPrimaryKeyConstraintController;
import inf.unibz.it.obda.constraints.controller.RDBMSUniquenessConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSCheckConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSPrimaryKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.dependencies.controller.RDBMSDisjointnessDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dl.codec.constraints.xml.RDBMSCheckConstraintXMLCodec;
import inf.unibz.it.obda.dl.codec.constraints.xml.RDBMSForeignKeyConstraintXMLCodec;
import inf.unibz.it.obda.dl.codec.constraints.xml.RDBMSPrimaryKeyConstraintXMLCodec;
import inf.unibz.it.obda.dl.codec.constraints.xml.RDBMSUniquenessConstraintXMLCodec;
import inf.unibz.it.obda.dl.codec.dependencies.xml.RDBMSDisjointnessDependencyXMLCodec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

//import org.apache.log4j.Logger;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.UnknownOWLOntologyException;

public class OWLAPIController extends APIController {

	OWLOntology			currentOntology				= null;
	URI					currentOntologyPhysicalURI	= null;
	OWLOntologyManager	mmger						= null;
	private OWLOntology	root;

	public OWLAPIController(OWLOntologyManager owlman, OWLOntology root) {
		super();
//		mapcontroller = new SynchronizedMappingController(dscontroller, this);
//		ioManager = new DataManager(dscontroller, mapcontroller, queryController);
		try {
			mmger = owlman;
			this.root = root;
			currentOntologyPhysicalURI = mmger.getPhysicalURIForOntology(root);
			setCurrentOntologyURI(root.getURI());	
			
			
			RDBMSForeignKeyConstraintController fkc = new RDBMSForeignKeyConstraintController();
			addAssertionController(RDBMSForeignKeyConstraint.class, fkc, new RDBMSForeignKeyConstraintXMLCodec());
			dscontroller.addDatasourceControllerListener(fkc);
			RDBMSPrimaryKeyConstraintController pkc = new RDBMSPrimaryKeyConstraintController();
			addAssertionController(RDBMSPrimaryKeyConstraint.class, pkc, new RDBMSPrimaryKeyConstraintXMLCodec());
			dscontroller.addDatasourceControllerListener(pkc);
			RDBMSCheckConstraintController ccc = new RDBMSCheckConstraintController();
			addAssertionController(RDBMSCheckConstraint.class,ccc, new RDBMSCheckConstraintXMLCodec());
			dscontroller.addDatasourceControllerListener(ccc);
			RDBMSDisjointnessDependencyController ddc = new RDBMSDisjointnessDependencyController();
			addAssertionController(RDBMSDisjointnessDependency.class, ddc,new RDBMSDisjointnessDependencyXMLCodec());
			dscontroller.addDatasourceControllerListener(ddc);
			RDBMSUniquenessConstraintController uqc = new RDBMSUniquenessConstraintController();
			addAssertionController(RDBMSUniquenessConstraint.class, uqc, new RDBMSUniquenessConstraintXMLCodec());
			dscontroller.addDatasourceControllerListener(uqc);
			
			apicoupler = new OWLAPICoupler(this, owlman, root);
			setCoupler(apicoupler);
		} catch (Exception e) {
			e.printStackTrace();
		}

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

	private OWLAPICoupler	apicoupler;
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




	public void loadData(URI owlFile) {
		loadingData = true;
		try {
			URI obdafile = getIOManager().getOBDAFile(owlFile);
			getIOManager().loadOBDADataFromURI(obdafile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			loadingData = false;
		}
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

}
