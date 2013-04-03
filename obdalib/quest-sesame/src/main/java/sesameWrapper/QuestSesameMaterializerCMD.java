package sesameWrapper;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBVirtualStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;

class QuestSesameMaterializerCMD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// check argument correctness
		if (args.length != 3 && args.length != 4) {
			System.out.println("Usage:");
			System.out.println(" QuestSesameMaterializerCMD owlfile obdafile format [outputfile]");
			System.out.println("");
			System.out.println(" owlfile    The full path to the OWL file");
			System.out.println(" obdafile   The full path to the OBDA file");
			System.out.println(" format     The desired output format: N3/Turtle/RDFXML");
			System.out.println(" outputfile [OPTIONAL] The full path to output file");
			System.out.println("");
			return;
		}

		// get parameter values
		String owlfile = args[0].trim();
		String obdafile = args[1].trim();
		String format = args[2].trim();
		String out = null;
		if (args.length == 4)
			out = args[3].trim();
		Writer writer = null;
		
		
		try {
			
			if (out != null) {
				writer = new BufferedWriter(new FileWriter(new File(out))); 
			} else {
				writer = new BufferedWriter(new OutputStreamWriter(System.out));
			}
			
			URI owlURI =  new File(owlfile).toURI();
			URI obdaURI =  new File(obdafile).toURI();
			QuestDBVirtualStore store = new QuestDBVirtualStore("test", owlURI, obdaURI);
			OBDAModel model = store.getObdaModel(obdaURI);
			
			SesameMaterializer materializer = new SesameMaterializer(model);
			RDFHandler handler = null;
			
			if (format.equals("N3"))
			{
				handler = new N3Writer(writer);
			}
			else if (format.equals("Turtle"))
			{
				handler = new TurtleWriter(writer);
			}
			else if (format.equals("RDFXML"))
			{
				handler = new RDFXMLWriter(writer);
			}

			handler.startRDF();
			while(materializer.hasNext())
				handler.handleStatement(materializer.next());
			handler.endRDF();
			
			if (out!=null)
				writer.close();
			
		} catch (Exception e) {
			System.out.println("Error materializing ontology:");
			e.printStackTrace();
		} 

	}

}
