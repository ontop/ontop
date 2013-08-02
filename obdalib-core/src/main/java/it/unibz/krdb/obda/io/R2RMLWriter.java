/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.BNodePredicateImpl;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class R2RMLWriter {
	
	BufferedWriter out;
	List<OBDAMappingAxiom> mappings;
	URI sourceUri;
	PrefixManager prefixmng;
	
	public R2RMLWriter(File file, OBDAModel obdamodel, URI sourceURI)
	{
		try {
			this.out = new BufferedWriter(new FileWriter(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sourceUri = sourceURI;
		this.mappings = obdamodel.getMappings(sourceUri);
		this.prefixmng = obdamodel.getPrefixManager(); 
	}
	
	public R2RMLWriter(OBDAModel obdamodel, URI sourceURI)
	{
		this.sourceUri = sourceURI;	
		this.mappings = obdamodel.getMappings(sourceUri);
		this.prefixmng = obdamodel.getPrefixManager(); 
	}

	
	public void write(File file)
	{
		try {
			this.out = new BufferedWriter(new FileWriter(file));
			
			out.write("@prefix rr: <http://www.w3.org/ns/r2rml#> .\n");
			
		Map<String, String> prefixes = prefixmng.getPrefixMap();
		out.write("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n");
		for(String key : prefixes.keySet())
		{
			out.write("@prefix "+key+" <"+prefixes.get(key)+"> .\n");
		}
			out.write("@base <http://example.com/base/> .\n\n");
			
		for (OBDAMappingAxiom mapping : mappings)
		{
			if (mapping.getId().contains("join"))
				getJoinMapping(mapping);
			else{
				out.write("<"+mapping.getId().replaceAll(" ", "_")+">\n\t a rr:TriplesMap;\n");
			
			//write sql table
			out.write("\trr:logicalTable "+getSQL(mapping.getSourceQuery().toString()));
			
			
			OBDAQuery targetQuery = mapping.getTargetQuery();
			
			//write subjectMap
			out.write("\trr:subjectMap ["+getSubjectMap(targetQuery)+" ]");
			
			List<String> predobjs = getPredObjMap(targetQuery);
			if (predobjs.size() > 0)
				out.write(";\n");
			else
				out.write(".\n\n");
			
			for (int i=0;i<predobjs.size()-1;i++)
				out.write("\trr:predicateObjectMap [\n"+ predobjs.get(i) + "\n\t];\n");
			if(predobjs.size() > 0)
				out.write("\trr:predicateObjectMap [\n"+ predobjs.get(predobjs.size()-1) + "\n\t].\n\n");
			
			}
		}
			
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			
		}
	}
	
	private String getSQL(String sql) {
		if (sql.contains("*"))
		{
			String table = getTableName(sql);
			return ("[ rr:tableName \"\\"+table.substring(0, table.length()-1)+"\\\"\" ];\n");
		}
		else
			return ("[ rr:sqlQuery \"\"\"\n\t\t"+sql+"\n\t\"\"\" ];\n");
	}

	private void getJoinMapping(OBDAMappingAxiom mapping) throws IOException {
		
		String sql = mapping.getSourceQuery().toString();
		String childSql = getChildSQL(sql);
		String parentSql = getParentSQL(sql);
		String refMap = findParentRef(parentSql);
		
		
		
		OBDAQuery targetQuery = mapping.getTargetQuery();
		
		
		out.write("<"+mapping.getId()+">\n\t a rr:TriplesMap;\n");
		
		//write sql table
		out.write("\trr:logicalTable "+getSQL(childSql));
	
		
		//write subjectMap
		out.write("\trr:subjectMap ["+getSubjectMap(targetQuery)+" ];\n");
		
		//write predobjmap
		out.write("\trr:predicateObjectMap [\n"+ getJoinPredicate(targetQuery) + getJoinObject(sql,refMap) + "\t].\n\n");
		
		
	}

	private String getJoinObject(String targetQuery, String parent) {
		String childCond = getChildCond(targetQuery);
		String parentCond = getParentCond(targetQuery);
		
		return "\t\t rr:objectMap [\n\t\t\ta rr:RefObjectMap;\n\t\t\trr:parentTriplesMap <"+
				parent+">;\n\t\t\trr:joinCondition [\n\t\t\t\trr:child \"\\\""+childCond+"\\\"\";\n\t\t\t\trr:parent \"\\\""
				+parentCond+"\\\"\";\n\t\t];\n\t];\n";
		
	}

	private String getJoinPredicate(OBDAQuery targetQuery) {
		//there's only one term in the body
		Term term = ((CQIE)targetQuery).getBody().get(0);
		if (term instanceof FunctionalTermImpl)
		{
			Function atom = (FunctionalTermImpl) term;
			//not class atoms
			if (atom.getTerms().size() > 1)
			{
				return ("\t\t rr:predicate \t<"+atom.getFunctionSymbol().toString()+"> ;\n");
			}
		}
		return null;
	}

	private String findParentRef(String parentSql) {
		for(OBDAMappingAxiom mapping : mappings)
		{
			if (mapping.getSourceQuery().toString().equals(parentSql))
				return mapping.getId();
		}
		return null;
	}

	private String getParentCond(String sql) {
		int begin = sql.indexOf("PARENT.");
		int end = sql.length();
		
		if (sql.contains("AND"))
			//i'm screwed
		end = 0;
		
		return sql.substring(begin+7, end);
	}

	private String getChildCond(String sql) {
		int begin = sql.indexOf("CHILD.");
		int end = sql.indexOf(" = ");
		return sql.substring(begin+6, end);
	}

	private String getChildSQL(String sql) {
		String child = "";
	int index = sql.indexOf("(");
		child = sql.substring(index+1,sql.indexOf(" as CHILD")-1);
		
		return (child);
	}
	
	private String getParentSQL(String sql) {
		String parent = "";
		int index = sql.indexOf("CHILD, ");
		parent = sql.substring(index+8,sql.indexOf(" as PARENT")-1);
		
		return (parent);
	}

	private String getSubjectMap(OBDAQuery targetQuery) {
		
		String subject = "";
		List<String> classes = new ArrayList<String>();
		CQIE cqie = (CQIE)targetQuery;
		List<Function>  body = cqie.getBody();
		Iterator<Function> it = body.iterator();
		while(it.hasNext())
		{
			Term term = it.next();
			if (term instanceof Function)
			{	
				Function atom = (Function) term;
				int arity = atom.getTerms().size();
				
				if (arity == 1) {
					// class
					if (atom.getPredicate().isClass())
						classes.add(atom.getFunctionSymbol().toString());
				}
			}

		}
		
			//get first term = subject
			Term term = body.get(0).getTerm(0);
			if (term instanceof FunctionalTermImpl)
			{
				Function atom = (FunctionalTermImpl) term;
			int arity = atom.getTerms().size();
			
			
			if (arity == 1) {
			
				// constant - arity 1
				if (!atom.getPredicate().isClass())
					subject += " rr:constant " + removeJoinKeyword(atom.getTerm(0))+ ";\n";
				
			} else if (arity == 2 || atom.getPredicate().equals(OBDAVocabulary.RDFS_LITERAL_LANG)) {
				
				// column - arity 2 - base prefix + {} + 1 var
				 if (atom.getTerm(0).toString().equals("\"http://example.com/base/{}\""))
					 subject += " rr:column \"\\\""+ removeJoinKeyword(atom.getTerm(1))+"\\\"\"";
				 else //template - arity 2
					 subject += " rr:template " + getTemplate(atom);
				
			} else if (arity > 2) { 
				// template - any arity
				 subject += " rr:template " + getTemplate(atom) +"\n";
			}

			if (atom.getFunctionSymbol() instanceof BNodePredicateImpl) {
				// bnode				
				subject += ";  rr:termType rr:BlankNode ";
			}
			if (atom.getPredicate().equals(OBDAVocabulary.RDFS_LITERAL_LANG))
				subject += "; rr:termType rr:Literal ";
			

			for(String cl : classes)
				subject += ";\n\t\t\t\t\t rr:class <"+cl+">\n";
		}
		
		
		return subject;
	}
	private String removeJoinKeyword(Term atom)
	{
		String str = atom.toString();
		if (str.startsWith("CHILD_"))
			return str.substring(6);
		else if (str.startsWith("PARENT_"))
			return str.substring(7);
		return str;
	}
	
	private String getTemplate(Function atom)
	{
		String temp = atom.getTerm(0).toString();
		String newtemp = "";
		//copy uri part
		int oldidx=0;
		
		int i=1;
		while(temp.contains("{}"))
		{
			int idx = temp.indexOf("{}");
			newtemp+= temp.substring(oldidx, idx);
			
			oldidx = idx+2;
			newtemp += "{\\\""+removeJoinKeyword(atom.getTerm(i)) + "\\\"}";
			i++;
			temp = temp.replaceFirst("[{]", "[");
			
		}
			
		newtemp += temp.substring(oldidx, temp.length());
		return newtemp;
	}

	private String getTableName(String sql)
	{
		int index = sql.indexOf("FROM");
		return sql.substring(index+5, sql.length());
	}
	
	
	private List<String> getPredObjMap(OBDAQuery targetQuery)
	{
		List<String> predobj = new ArrayList<String>();
		CQIE cqie = (CQIE)targetQuery;
		List<Function>  body = cqie.getBody();
		Iterator<Function> it = body.iterator();
		while(it.hasNext())
		{
			Term term = it.next();
			if (term instanceof Function)
			{
				Function atom = (Function) term;
				//not class atoms
				if (atom.getTerms().size() > 1)
				{
					predobj.add("\t\t rr:predicate \t<"+atom.getFunctionSymbol().toString()+"> ;\n"+getObject(atom.getTerm(1)));
				}
			}
			
		}
		
		return predobj;
	}
	
	private String getObject(Term obj)
	{
		String object = "";
		if(obj instanceof FunctionalTermImpl)
		{	
			
			FunctionalTermImpl fobj = ((FunctionalTermImpl) obj);
			int size = fobj.getTerms().size();
			if (size == 1)
			{	//object 
				if (fobj.isDataTypeFunction()) {
					Predicate p = fobj.getFunctionSymbol();
					if (p instanceof DataTypePredicate || p instanceof URITemplatePredicate)
						object = "\t\t rr:objectMap \t[ rr:column \"\\\""+fobj.getTerm(0).toString()+"\\\"\"; rr:datatype <"+ p.toString() + "> ]";
					else
						object = "\t\t rr:objectMap \t[ rr:column \"\\\""+fobj.getTerm(0).toString()+"\\\"\"; rr:datatype "+ p.toString() + " ]";
				} else
					object = "\t\t rr:object \t "+((Function)obj).getTerm(0).toString();
				return object;
			}
			else
			{
				String lang ="", templ ="";
				
				if (fobj.getTerm(0).toString().startsWith("\"http"))
					templ = " rr:template "+getTemplate((Function)obj);
				else
					templ = " rr:column \"\\\""+fobj.getTerm(0).toString()+"\\\"\"";
				
				
				if (fobj.getFunctionSymbol().equals(OBDAVocabulary.RDFS_LITERAL_LANG))
				{
					if(fobj.getTerm(size-1) instanceof ValueConstant){
						if (!((ValueConstant)(fobj.getTerm(size-1))).toString().equals(OBDAVocabulary.NULL.toString()))
						 lang = ";  rr:language "+ fobj.getTerm(size-1).toString();
					}
					object = "\t\t rr:objectMap \t[ "+templ+ lang+" ]";
				}
				else 
				{
					object = "\t\t rr:objectMap \t[ "+templ+" ]";
				}
			}
		}
		else if (obj instanceof Variable)
			object = "\t\t rr:objectMap [ rr:column \"\\\""+obj.toString()+"\\\"\" ]";
		else if (obj instanceof URIConstant)
			System.out.println("URIConst: "+obj.toString());
		else if (obj instanceof ValueConstant)
			System.out.println("ValueConst: "+obj.toString());
		else if (((Function)obj).isDataFunction())
		{
			Iterator<Variable> varcol = ((Function)obj).getVariables().iterator();
			object = "\t\t rr:objectMap \t[ rr:column \"\\\""+varcol.next().toString()+"\\\"\";  rr:datatype xsd:"+
					((Function)obj).getFunctionSymbol().toString().split("#")[1]+"]";
		}
		else
			System.out.println("Found: "+obj.toString());
		
		return object;
	}
	
	public static void main(String args[])
	{
		String file = "C:/Project/Test Cases/mapping2.ttl";
		//"C:/Project/Timi/Workspace/obdalib-parent/quest-rdb2rdf-compliance/src/main/resources/D004/r2rmlb.ttl";
		R2RMLReader reader = new R2RMLReader(file);
		R2RMLWriter writer = new R2RMLWriter(reader.readModel(URI.create("blah")),URI.create("blah"));
		File out = new File("C:/Project/Test Cases/mapping1.ttl");
				//"C:/Project/Timi/Workspace/obdalib-parent/quest-rdb2rdf-compliance/src/main/resources/D004/WRr2rmlb.ttl");
		writer.write(out);
		
	}
}
