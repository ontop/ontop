package it.unibz.inf.ontop.planning;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.owlrefplatform.core.sql.SQLGenerator.QueryAliasIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.LinkedListMultimap;

public class SQLCreator {

    private List<List<Restriction>> combinations;
    
    private static SQLCreator instance = null;
    
    private SQLCreator(){
	this.combinations = new ArrayList<>();
    }
    
    public static SQLCreator getInstance() {
	if( instance == null )	instance = new SQLCreator();
	return instance;
    }

    public void addValidCombination(List<Restriction> combination) {
	this.combinations.add(new ArrayList<>(combination));
    }
    
    public List<List<Restriction>> getCombinations(){
	return Collections.unmodifiableList(this.combinations);
    }
 
    public String makeBody(OntopPlanning op, LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables) {
	
	// A generalized union of all combinations!
	List<String> union = new ArrayList<>();
	
	
	for( List<Restriction> combination : this.getCombinations() ){
	    String sql = makeJoin(combination, op, mOutVariableToFragmentsVariables);
	    union.add(sql);
	}
	
	String body = uniteAll(union);
	
	return body;
    }
    
    private String uniteAll(List<String> union) {
	// TODO Auto-generated method stub
	return null;
    }

    private String makeJoin(
	    List<Restriction> combination,
	    OntopPlanning op,
	    LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables) {
	
	String result = "";
	
	for( int fragIndex = 0; fragIndex < combination.size(); ++fragIndex ){
	    
	    // Transform the restriction in SQL
	    Restriction r = combination.get(fragIndex);
	    List<String> signature = op.makeSignatureForFragment(fragIndex, mOutVariableToFragmentsVariables);
	    String sql = op.getSQLForDL(r.getDLog(), signature);
	    
	    // Make the projLists for Joins
	    List<String> projList = retrieveProjections(sql);
	    CQIE cq = r.getDLog().getRules().iterator().next();
//	    QueryAliasIndex index = TTT
	    for( Variable v : mOutVariableToFragmentsVariables.keySet() ){
		List<MFragIndexToVarIndex> list = mOutVariableToFragmentsVariables.get(v);
		if( list.size() > 1 ){
		    // Join
		    for( MFragIndexToVarIndex el : list ){
			if( el.getFragIndex() == fragIndex ){
			    
			}
		    }
		}
	    }
	}
	return result;
    }

    
    private List<String> retrieveProjections(String sql) {
	
	class LocalUtils{
	    private String header = "SELECT *\nFROM (\nSELECT ";
	    public List<String> projList(String first) {
				
		System.out.println(header.length());
		System.out.println(first.lastIndexOf("FROM"));
		String raw = first.substring( header.length(), first.lastIndexOf("FROM") );
		System.out.println(raw);
		String clean = raw.replaceAll("qview.\\.", "").trim();
		
		List<String> result = Arrays.asList( clean.split(",") );
		
		return result;
	    }
	    
	}
	
	LocalUtils utils = new LocalUtils();
	List<String> splits = Arrays.asList(sql.split("UNION"));
	
	// In a union on different column names, the result takes the 
	// name of the first element of the union (at least, in PSQL)
	String first = splits.get(0);
	
	List<String> colNames = utils.projList(first);
	return colNames;
    }
    
  @Override
    public String toString() {
	return combinations.toString();
    }    
};
