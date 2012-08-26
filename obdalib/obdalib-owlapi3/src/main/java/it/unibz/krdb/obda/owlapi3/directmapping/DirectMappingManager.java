package it.unibz.krdb.obda.owlapi3.directmapping;

import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.RecognitionException;


public class DirectMappingManager {
	
	private DatabaseMetaData md;
	private DBMetadata obdamd;
	private List<DirectMappingAxiom> DMList;
	private String baseuri;
	
	public DirectMappingManager(){
		this.DMList=new ArrayList<DirectMappingAxiom>();
		this.baseuri = new String("http://www.semanticweb.org/owlapi/ontologies/ontology#");
	}
	
	public DirectMappingManager(DatabaseMetaData md, DBMetadata obdamd){
		this.DMList=new ArrayList<DirectMappingAxiom>();
		this.md = md;
		this.obdamd = obdamd;
		this.baseuri = new String("http://www.semanticweb.org/owlapi/ontologies/ontology#");
	}
	
	public void setBaseURI(String uri){
		this.baseuri = uri;
	}
	
	
	public List<DirectMappingAxiom> getDirectMapping() throws RecognitionException{
		return DMList;
	}
	
	public void generateDirectMapping(){
		for(int i=0;i<this.obdamd.getTableList().size();i++){
			if(existPK(this.obdamd.getTableList().get(i))){
				DirectMappingAxiomBasic temp = new DirectMappingAxiomBasic(obdamd.getTableList().get(i),md, obdamd);
				temp.setBaseURI(this.baseuri);
				temp.generateCQ();
				this.DMList.add(new DirectMappingAxiom(temp));
			}else if(numFK(this.obdamd.getTableList().get(i))>1){
				DirectMappingAxiomN_Ary temp = new DirectMappingAxiomN_Ary(obdamd.getTableList().get(i),md, obdamd);
				temp.setBaseURI(this.baseuri);
				temp.generateCQ();
				this.DMList.add(new DirectMappingAxiom(temp));
			}
		}
	}
	
	private boolean existPK(DataDefinition dd){
		boolean existPK = false;
		for(int i=0;i<dd.getAttributes().size();i++){
			if(dd.getAttribute(i+1).bPrimaryKey){
				existPK = true;
			}
		}
		return existPK;
	}
	
	private int numFK(DataDefinition dd){
		int numFK = 0;
		for(int i=0;i<dd.getAttributes().size();i++){
			if(dd.getAttribute(i+1).bForeignKey){
				numFK ++;
			}
		}
		return numFK;
	}

}
