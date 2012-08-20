package it.unibz.krdb.obda.owlapi3.directmapping;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;

import java.sql.DatabaseMetaData;

public class DirectMappingAxiom {
	protected DatabaseMetaData md;
	protected DBMetadata obda_md;
	protected DataDefinition table;
	protected String SQLString;
	protected String CQString;
	
	public DirectMappingAxiom(){		
	}
	
	public DirectMappingAxiom(DataDefinition dd, DatabaseMetaData md, DBMetadata obda_md){
		this.table = dd;
		this.SQLString = new String("SELECT * FROM "+dd.getName());
		this.md = md;
		this.obda_md = obda_md;
	}
	
	public DirectMappingAxiom(DirectMappingAxiom dmab){
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

}
