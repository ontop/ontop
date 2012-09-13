package it.unibz.krdb.obda.owlapi3.directmapping;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.JDBCConnectionManager;
import it.unibz.krdb.sql.TableDefinition;

public class DirectMappingEngine {
	
	private JDBCConnectionManager conMan;
	private String baseuri;
	
	public DirectMappingEngine(){
		conMan = JDBCConnectionManager.getJDBCConnectionManager();
		baseuri = new String("http://example.org#");
	}
	
	public void setBaseURI(String prefix){
		if(prefix.endsWith("#")){
			this.baseuri = prefix;
		}else this.baseuri = prefix+"#";
	}
	
	public void enrichOntology(OWLOntology ontology, OBDAModel model) throws OWLOntologyStorageException, SQLException{
		List<OBDADataSource> sourcelist = new ArrayList<OBDADataSource>();
		sourcelist = model.getSources();
		OntoExpansion oe = new OntoExpansion();
		if(model.getPrefixManager().getDefaultPrefix().endsWith("#")){
			oe.setURI(model.getPrefixManager().getDefaultPrefix());
		}else{
			oe.setURI(model.getPrefixManager().getDefaultPrefix()+"#");
		}
		
		//For each data source, enrich into the ontology
		for(int i=0;i<sourcelist.size();i++){
			oe.enrichOntology(conMan.getMetaData(sourcelist.get(i)), ontology);
		}
	}
	
	public OWLOntology getOntology(OWLOntologyManager manager, OBDAModel model) throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException{
		OWLOntology ontology = manager.createOntology();
		enrichOntology(ontology, model);
		return ontology;		
	}
	
	
	public OBDAModel extractMappings(OBDADataSource source) throws SQLException, DuplicateMappingException{
		OBDAModelImpl model = new OBDAModelImpl();
		insertMapping(source, model);
		return model;
	}
	
	
	//Duplicate Exception may happen
	public void insertMapping(OBDADataSource source, OBDAModel model) throws SQLException, DuplicateMappingException{		
		for(int i=0;i<conMan.getMetaData(source).getTableList().size();i++){
			TableDefinition td = conMan.getMetaData(source).getTableList().get(i);
			model.addMapping(source.getSourceID(), getMapping(td, source));
		}		
	}
	
	
	
	public OBDAMappingAxiom getMapping(DataDefinition table, OBDADataSource source) throws SQLException{
		DirectMappingAxiom dma = new DirectMappingAxiom(table, conMan.getMetaData(source));
		dma.setbaseuri(this.baseuri);
		OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
		
		return dfac.getRDBMSMappingAxiom(dma.getSQL(), dma.getCQ(dfac));
	}
	

	
	
	
	
	

}
