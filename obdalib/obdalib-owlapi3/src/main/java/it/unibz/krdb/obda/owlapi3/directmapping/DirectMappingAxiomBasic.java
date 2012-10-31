package it.unibz.krdb.obda.owlapi3.directmapping;

import java.sql.DatabaseMetaData;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.ForeignKey;

public class DirectMappingAxiomBasic {
	
	private DatabaseMetaData md;
	private DataDefinition table;
	private String SQLString;
	private String CQString;
	
	public DirectMappingAxiomBasic(){		
	}
	
	public DirectMappingAxiomBasic(DataDefinition dd, DatabaseMetaData md){
		this.table = dd;
		this.SQLString = new String("SELECT * FROM "+dd.getName());
		this.md = md;
	}
	
	public DirectMappingAxiomBasic(DirectMappingAxiomBasic dmab){
		this.SQLString = new String(dmab.getSQL());
		this.CQString = new String(dmab.getCQ());
	}
	
	
	public String getSQL(){
		return this.SQLString;
	}
	
	public String getCQ(){
		return this.CQString;
	}
	
	
	public void setDD(DataDefinition dd){
		this.table = dd;
	}
	
	public void setDBMD(DatabaseMetaData md){
		this.md = md;
	}
	
	private String generateURI(DataDefinition dd){
		String pk_uri=new String("<\"&:;" + dd.getName() + "-{$" + getDataDefinitionPK(dd) + "}\">");
		return new String(pk_uri);
	}
	
	private String getDataDefinitionPK(DataDefinition dd){
		String pk = new String();
		for(int i=0;i<dd.getAttributes().size();i++){
			if(dd.getAttribute(i).bPrimaryKey){
				pk = dd.getAttributeName(i);
			}
		}
		return new String(pk);
	}
	
	public void generateCQ(){
		CQString+=generateURI(this.table);
		CQString+=" a :"+this.table.getName()+" ; ";
		for(int i=0;i<table.getAttributes().size();i++){
			CQString+=":"+table.getName()+"#"+table.getAttributeName(i)+" $"+table.getAttributeName(i);
			if((i+1)!=table.getAttributes().size()){
				CQString+=" ; ";
			}else{
				CQString+=" . ";
			}
		}
		for(int i=0;i<table.getAttributes().size();i++){
			if(this.table.getAttribute(i).bForeignKey){
				String FKName = new String(this.table.getAttributeName(i));
				ForeignKey FK = new ForeignKey(this.md, this.table.getName(), FKName);
				DBMetadata obda_md = new DBMetadata(md);
				String fk_uri = generateURI(obda_md.getDefinition(FK.getCoPKTable()));
				CQString+=generateURI(this.table)+" :"+table.getName()+"#ref-"+table.getAttributeName(i);
				CQString+=" "+fk_uri+" . ";
			}
		}
	}
	

}
