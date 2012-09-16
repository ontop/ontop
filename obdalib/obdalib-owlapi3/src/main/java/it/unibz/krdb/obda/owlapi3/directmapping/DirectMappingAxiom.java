package it.unibz.krdb.obda.owlapi3.directmapping;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.utils.TypeMapper;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.Reference;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.api.Attribute;

public class DirectMappingAxiom {
	protected DBMetadata obda_md;
	protected DataDefinition table;
	protected String SQLString;
	protected String baseuri;
	
	public DirectMappingAxiom(){		
	}
	
	public DirectMappingAxiom(DataDefinition dd, DBMetadata obda_md){
		this.table = dd;
		this.SQLString = new String();
		this.obda_md = obda_md;
		this.baseuri = new String("http://example.org#");
	}
	
	public DirectMappingAxiom(DirectMappingAxiom dmab){
		this.table = dmab.table;
		this.SQLString = new String(dmab.getSQL());
		this.baseuri = new String(dmab.getbaseuri());
	}
		
	public String getSQL(){
		String SQLStringTemple=new String("SELECT %s FROM %s");
		String Columns =new String();
		for(int i=0;i<table.countAttribute();i++){
			Columns+=table.getAttributeName(i+1);
		}
		SQLString=String.format(SQLStringTemple, Columns, this.table.getName());
		return new String(SQLString);
	}
	
	public CQIE getCQ(OBDADataFactory df){
		Term sub = generateSubject(df, (TableDefinition)table);
		List<Atom> atoms = new ArrayList<Atom>();
		
		//Class Atom
		atoms.add(df.getAtom(df.getClassPredicate(generateClassURI(table.getName())), sub));
		
		
		//DataType Atoms
		TypeMapper typeMapper = TypeMapper.getInstance();
		for(int i=0;i<table.countAttribute();i++){
			Attribute att = table.getAttribute(i+1);
			Predicate type = typeMapper.getPredicate(att.getType());
			Function obj = df.getFunctionalTerm(type, df.getVariable(att.getName()));
			
			atoms.add(df.getAtom(df.getDataPropertyPredicate(generateDPURI(table.getName(), att.getName())), sub, obj));
		}
		
		
		//Object Atoms
		for(int i=0;i<table.countAttribute();i++){
			if(table.getAttribute(i+1).isForeignKey()){
				Attribute att = table.getAttribute(i+1);
				Reference ref = att.getReference();
				String pkTableReference = ref.getTableReference();
				TableDefinition tdRef = (TableDefinition)obda_md.getDefinition(pkTableReference);
				Term obj = generateSubject(df, tdRef);
				
				atoms.add(df.getAtom(df.getObjectPropertyPredicate(generateOPURI(table.getName(), att.getName())), sub, obj));
			}
		}
		
		//To construct the head, there is no static field about this predicate
		List<Term> headTerms = new ArrayList<Term>();
		for(int i=0;i<table.countAttribute();i++){
			headTerms.add(df.getVariable(table.getAttributeName(i+1)));
		}
		Predicate headPredicate = df.getPredicate("http://obda.inf.unibz.it/quest/vocabulary#q", headTerms.size());
		Atom head = df.getAtom(headPredicate, headTerms);
		
		
		return df.getCQIE(head, atoms);
	}
	
	//Generate an URI for class predicate from a string(name of table)
	private URI generateClassURI(String table){
		String temple = new String(baseuri+"%s");
		return URI.create(String.format(temple, percentEncode(table)));
	}
	
	/*
	 * Generate an URI for datatype property from a string(name of column)
	 * The style should be baseuri#tablename#columnname as required in Direct Mapping Definition
	 * But Class URI does not accept two "#" in URI construction, since # "means" ending in URI
	 * "-" is used to constructed the URI at the position of the second "#"
	 */
	private URI generateDPURI(String table, String column){
		String temple = new String(baseuri+"%s"+"-"+"%s");	
		return URI.create(String.format(temple, percentEncode(table), percentEncode(column)));
	}
	
	//Generate an URI for object property from a string(name of column)
	private URI generateOPURI(String table, String column){
		String temple = new String(baseuri+"%s"+"-ref-"+"%s");
		return URI.create(String.format(temple, percentEncode(table), percentEncode(column)));
	}
	
	/*
	 * Generate the subject term of the table
	 * 
	 * 
	 * TODO replace URI predicate to BNode predicate for tables without PKs
	 * 		in the following method after 'else'
	 */
	
	private Term generateSubject(OBDADataFactory df, TableDefinition td){
		if(td.getPrimaryKeys().size()>0){
			Predicate uritemple = df.getUriTemplatePredicate(td.getPrimaryKeys().size()+1);
			List<Term> terms = new ArrayList<Term>();
			terms.add(df.getValueConstant(subjectTemple(td,td.getPrimaryKeys().size())));
			for(int i=0;i<td.getPrimaryKeys().size();i++){
				terms.add(df.getVariable(td.getPrimaryKeys().get(i).getName()));
			}
			return df.getFunctionalTerm(uritemple, terms);
			
		}
		else{
			StringBuffer columns = new StringBuffer();
			for(int i=0;i<td.countAttribute();i++){
				columns.append(td.getAttributeName(i+1));
			}
			
			/*
			 * TODO replace this predicate with BNode predicate
			 * 
			 */
			Predicate URIStandingForBNode = df.getUriTemplatePredicate(1);			
			return df.getFunctionalTerm(URIStandingForBNode, df.getVariable(columns.toString()));
		}
	}
	
	
	private String subjectTemple(TableDefinition td, int numPK){
		/*
		 * It is hard to generate a uniform temple since the number of PK differs
		 * For example, the subject uri temple with one pk should be like:
		 * 	baseuri+tablename/PKcolumnname-{}
		 * For table with more than one pk columns, there will be a "." between column names 
		 */

		String temp = new String(baseuri);
		temp+=percentEncode(td.getName());
		temp+="/";
		for(int i=0;i<numPK;i++){
			temp+=percentEncode(td.getPrimaryKeys().get(i).getName())+"-{}.";
		}
		
		//remove the last "." which is not neccesary
		temp=temp.substring(0, temp.length()-1);
		temp="\""+temp+"\"";
		return temp;
	}
	
	public String getbaseuri(){
		return baseuri;
	}
	
	public void setbaseuri(String uri){
		baseuri=new String(uri);
	}
	
	
	/*
	 * percent encoding for a String
	 */	
	private String percentEncode(String pe){
		pe = pe.replace("#", "%23");
		pe = pe.replace(".", "%2E");
		pe = pe.replace("-", "%2D");
		pe = pe.replace("/", "%2F");
		
		pe = pe.replace(" ", "%20");
		pe = pe.replace("!", "%21");
		pe = pe.replace("$", "%24");
		pe = pe.replace("&", "%26");
		pe = pe.replace("'", "%27");
		pe = pe.replace("(", "%28");
		pe = pe.replace(")", "%29");
		pe = pe.replace("*", "%2A");
		pe = pe.replace("+", "%2B");
		pe = pe.replace(",", "%2C");
		pe = pe.replace(":", "%3A");
		pe = pe.replace(";", "%3B");
		pe = pe.replace("=", "%3D");
		pe = pe.replace("?", "%3F");
		pe = pe.replace("@", "%40");
		pe = pe.replace("[", "%5B");
		pe = pe.replace("]", "%5D");
		return new String(pe);
	}
	
	
	

}
