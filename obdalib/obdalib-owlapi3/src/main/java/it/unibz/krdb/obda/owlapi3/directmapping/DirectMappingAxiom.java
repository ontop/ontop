package it.unibz.krdb.obda.owlapi3.directmapping;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.parser.TurtleSyntaxParser;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;

import java.sql.DatabaseMetaData;

public class DirectMappingAxiom {
	protected DatabaseMetaData md;
	protected DBMetadata obda_md;
	protected DataDefinition table;
	protected String SQLString;
	protected String CQString;
	protected String baseuri;
	
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
		this.CQString = new String(dmab.getCQString());
		this.baseuri = new String(dmab.getbaseuri());
	}
	
	
	
	public String getSQL(){
		return this.SQLString;
	}
	
	public String getbaseuri(){
		return baseuri;
	}
	
	public CQIE getCQ() throws Exception{
		PrefixManager pm = new SimplePrefixManager();
		pm.addPrefix(":", this.baseuri);
		TurtleSyntaxParser tsp = new TurtleSyntaxParser(pm);
		return tsp.parse(CQString);
	}
	
	public String getCQString(){
		return CQString;
	}
	
	
	public void setDD(DataDefinition dd){
		this.table = dd;
	}
	
	public void setDBMD(DatabaseMetaData md){
		this.md = md;
	}
	
	public void setBaseURI(String uri){
		this.baseuri = new String(uri);		
	}

}
