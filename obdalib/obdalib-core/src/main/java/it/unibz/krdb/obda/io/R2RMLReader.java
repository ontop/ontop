package it.unibz.krdb.obda.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.openrdf.model.Graph;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.CQIEImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

public class R2RMLReader {
	
	private R2RMLManager manager;
	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private OBDAModel obdaModel = fac.getOBDAModel();
	
	private Graph graph ;
	
	
	public R2RMLReader(String file)
	{
	
		manager = new R2RMLManager(file);
		graph = manager.getGraph();
	}
	
	
	
	public OBDAModel readMapping(URI location){
		try {
			//add to the model the mappings retrieved from the manager
			obdaModel.addMappings(location, manager.getMappings(graph));
		} catch (DuplicateMappingException e) {
			e.printStackTrace();
		}
		return obdaModel;
	}
	
	public OBDAModel readMapping(URI location, String JdbcUri, String user, String pwd, String driver){

		ArrayList<OBDAMappingAxiom> mappings = manager.getMappings(graph);
		try {
			//add to the model the mappings retrieved from the manager
			obdaModel.addMappings(location, mappings);
		} catch (DuplicateMappingException e) {
			e.printStackTrace();
		}
		return obdaModel;	
	}
	

	public static void main(String args[])
	{
		String file = "C:/Project/Test Cases/mapping.ttl";
		R2RMLReader reader = new R2RMLReader(file);
		try {
			reader.readMapping(new URI("blah"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}

}
