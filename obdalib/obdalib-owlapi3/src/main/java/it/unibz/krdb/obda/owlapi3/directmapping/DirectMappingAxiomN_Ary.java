package it.unibz.krdb.obda.owlapi3.directmapping;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.ForeignKey;

import java.sql.DatabaseMetaData;

public class DirectMappingAxiomN_Ary extends DirectMappingAxiom{
	
	public DirectMappingAxiomN_Ary(DataDefinition dd, DatabaseMetaData md, DBMetadata obda_md){
		this.table = dd;
		this.SQLString = new String("SELECT * FROM "+dd.getName());
		this.md = md;
		this.obda_md = obda_md;
	}

	private String generatePKURI(DataDefinition dd){
		String pk_uri=new String("<\"&:;" + dd.getName() + "-{$" + getDataDefinitionPK(dd) + "}\">");
		return new String(pk_uri);
	}
	
	private String generateURI(DataDefinition dd){
		String uri = new String("<\"&:;" + dd.getName() + "/");
		for(int i=0;i<dd.getAttributes().size();i++){
			if(dd.getAttribute(i+1).bForeignKey){
				uri += "{$" +dd.getAttributeName(i+1)+"}/";
			}
		}
		uri += "}\">";
		return uri;
	}
	
	private String getDataDefinitionPK(DataDefinition dd){
		String pk = new String();
		for(int i=0;i<dd.getAttributes().size();i++){
			if(dd.getAttribute(i+1).bPrimaryKey){
				pk = dd.getAttributeName(i+1);
			}
		}
		return new String(pk);
	}
	
	public void generateCQ(){
		CQString=generateURI(this.table);
		CQString+=" a :"+this.table.getName()+" ; ";
		
		for(int i=0;i<table.getAttributes().size();i++){
			CQString+=":"+table.getName()+"-"+table.getAttributeName(i+1)+" $"+table.getAttributeName(i+1)+" ; ";
		}
		for(int i=0;i<table.getAttributes().size();i++){
			if(this.table.getAttribute(i+1).bForeignKey){
				String FKName = new String(this.table.getAttributeName(i+1));
				ForeignKey FK = new ForeignKey(this.md, this.table.getName(), FKName);
				String fk_uri = generateURI(this.obda_md.getDefinition(FK.getCoPKTable()));
				CQString += " :"+table.getName()+"-ref-"+table.getAttributeName(i+1);
				CQString+=" "+fk_uri+" ; ";
			}
		}
		CQString = CQString.substring(0, CQString.length()-2);
		CQString += ". ";
	}
	
	

}
