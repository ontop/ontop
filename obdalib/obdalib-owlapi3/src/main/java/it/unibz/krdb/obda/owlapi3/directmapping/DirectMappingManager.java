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
	private List<DirectMappingAxiomBasic> DMList;
	
	public DirectMappingManager(){
		this.DMList=new ArrayList<DirectMappingAxiomBasic>();
	}
	
	public DirectMappingManager(DatabaseMetaData md, DBMetadata obdamd){
		this.DMList=new ArrayList<DirectMappingAxiomBasic>();
		this.md = md;
		this.obdamd = obdamd;
	}
	
	public List<OBDARDBMappingAxiom> getDirectMapping(OBDADataFactoryImpl dfi) throws RecognitionException{
		List<OBDARDBMappingAxiom> templist= new ArrayList<OBDARDBMappingAxiom>();
		generateDirectMapping();
		for(int i=0;i<DMList.size();i++){
			DirectMappingAxiomBasic tempDMAxiomBasic=DMList.get(i);
			DatalogProgramParser dpp = new DatalogProgramParser();
			templist.add(dfi.getRDBMSMappingAxiom(tempDMAxiomBasic.getSQL(), dpp.parse(tempDMAxiomBasic.getCQ())));
		}
		return templist;
	}
	
	private void generateDirectMapping(){
		for(int i=0;i<this.obdamd.getTableList().size();i++){
			if(existPK(this.obdamd.getTableList().get(i))){
				DirectMappingAxiomBasic temp = new DirectMappingAxiomBasic(obdamd.getTableList().get(i),md);
				temp.generateCQ();
				DMList.add(new DirectMappingAxiomBasic(temp));
			}
		}
	}
	
	private boolean existPK(DataDefinition dd){
		boolean existPK = false;
		for(int i=0;i<dd.getAttributes().size();i++){
			if(dd.getAttribute(i).bPrimaryKey){
				existPK = true;
			}
		}
		return existPK;
	}
	

}
