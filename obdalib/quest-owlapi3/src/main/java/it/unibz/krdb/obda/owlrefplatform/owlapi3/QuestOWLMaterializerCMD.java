package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class QuestOWLMaterializerCMD {

	public static void main(String args[]) {

		if (args.length != 3 && args.length != 4) {
			System.out.println("Usage");
			System.out.println(" QuestOWLMaterializerCMD owlfile obdafile format [outputfile]");
			System.out.println("");
			System.out.println(" owlfile    The full path to the OWL file");
			System.out.println(" obdafile   The full path to the OBDA file");
			System.out.println(" format      The desired output format: N3/Turtle/RDFXML");
			System.out.println(" outputfile [OPTIONAL] The full path to the output file");
			System.out.println("");
			return;
		}

		String owlfile = args[0].trim();
		String obdafile = args[1].trim();
		String format = args[2].trim();
		String out = null;
		OutputStream writer = null;
		if (args.length == 4) {
			out = args[3].trim();
		}

		try {
			if (out != null) {
				writer = new FileOutputStream(new File(out)); 
			} else {
				writer = System.out;
			}
			
			/*
			 * Loading the OWL ontology from the file as with normal OWLReasoners
			 */
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

			/*
			 * Loading the OBDA model (database declaration and mappings) from the
			 * .obda file (this code will change in the future)
			 */
			OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
			OBDAModel obdaModel = fac.getOBDAModel();
			ModelIOManager ioManager = new ModelIOManager(obdaModel);
			ioManager.load(obdafile);

			OWLAPI3Materializer materializer = new OWLAPI3Materializer(obdaModel);
		
			
			if (format.equals("N3"))
			{
				OWLAPI3ToFileMaterializer.materializeN3(writer, obdaModel);
			}
			else if (format.equals("Turtle"))
			{
				manager.removeAxioms(ontology, ontology.getAxioms());
				while(materializer.hasNext()) 
					manager.addAxiom(ontology, materializer.next());
				manager.saveOntology(ontology, new TurtleOntologyFormat(), writer);
			}
			else if (format.equals("RDFXML"))
			{
				manager.removeAxioms(ontology, ontology.getAxioms());
				while(materializer.hasNext()) 
					manager.addAxiom(ontology, materializer.next());
				manager.saveOntology(ontology, new RDFXMLOntologyFormat(), writer);	
			}
			
			if (out!=null)
				writer.close();
			
			
		} catch (Exception e) {
			System.out.println("Error materializing ontology:");
			e.printStackTrace();
		} 

	}

	
}
