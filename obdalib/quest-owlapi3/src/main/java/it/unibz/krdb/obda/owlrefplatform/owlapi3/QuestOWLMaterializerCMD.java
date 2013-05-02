package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OBDAModelSynchronizer;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class QuestOWLMaterializerCMD {

	public static void main(String args[]) {

		if (args.length != 2 && args.length != 3) {
			System.out.println("Usage");
			System.out.println(" QuestOWLMaterializerCMD  obdafile owlfile->yes/no [outputfile]");
			System.out.println("");
			System.out.println(" obdafile   The full path to the OBDA file");
			System.out.println(" owlfile    yes/no to use the OWL file or not");
			System.out.println(" outputfile [OPTIONAL] The full path to the output file");
			System.out.println("");
			return;
		}

		String obdafile = args[0].trim();
		String yesno = args[1].trim();
		String owlfile = null;
		if (yesno.toLowerCase().equals("yes"))
			owlfile = obdafile.substring(0, obdafile.length()-4) + "owl";
		
		String out = null;
		BufferedOutputStream output = null;
		BufferedWriter writer = null;
		if (args.length == 3) {
			out = args[2].trim();
		}

		try {
			final long startTime = System.currentTimeMillis();
			
			if (out != null) {
				output = new BufferedOutputStream(new FileOutputStream(out)); 
			} else {
				output = new BufferedOutputStream(System.out);
			}
			writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
			
			OWLOntology ontology = null;
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			if (owlfile != null) {
			// Loading the OWL ontology from the file as with normal OWLReasoners
				ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
			}
			else {
				ontology = manager.createOntology();
			}
			
			OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
			OBDAModel obdaModel = fac.getOBDAModel();
			ModelIOManager ioManager = new ModelIOManager(obdaModel);
			ioManager.load(obdafile);

			OBDAModelSynchronizer.declarePredicates(ontology, obdaModel);

			OWLAPI3Materializer materializer = null;
			if (owlfile != null) {
				Ontology onto =  new OWLAPI3Translator().translate(ontology);
				materializer = new OWLAPI3Materializer(onto, obdaModel);
			}
			else
				materializer = new OWLAPI3Materializer(obdaModel);
	
			while(materializer.hasNext()) 
				manager.addAxiom(ontology, materializer.next());
			manager.saveOntology(ontology, new OWLXMLOntologyFormat(), new WriterDocumentTarget(writer));	
			
			System.out.println("NR of TRIPLES: "+materializer.getTriplesCount());
			System.out.println("VOCABULARY SIZE (NR of QUERIES): "+materializer.getVocabularySize());
			
			materializer.disconnect();
			if (out!=null)
				output.close();
			
			final long endTime = System.currentTimeMillis();
			final long time = endTime - startTime;
			System.out.println("Elapsed time to materialize: "+time + " {ms}");
			
		} catch (Exception e) {
			System.out.println("Error materializing ontology:");
			e.printStackTrace();
		} 

	}

	
}
