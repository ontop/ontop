package it.unibz.inf.ontop.planning.sql;

import it.unibz.inf.ontop.planning.OntopPlanning;
import it.unibz.inf.ontop.planning.datatypes.MFragIndexToVarIndex;
import it.unibz.inf.ontop.planning.datatypes.Restriction;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.owlrefplatform.core.sql.SQLGenerator.QueryAliasIndex;
import it.unibz.krdb.sql.QualifiedAttributeID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;

public class SQLCreator {

    private List<List<Restriction>> combinations;

    private static SQLCreator instance = null;

    private SQLCreator(){
	this.combinations = new ArrayList<>();
    }

    // ***** Helper Classes ***** //

    /**
     * 
     * Mapping between datalog variables and attribute names.
     * 
     * @author Davide Lanti
     *
     */
    private class AliasMap{
	private List<Map<Variable, Set<QualifiedAttributeID>>> fragmentsMaps;

	private AliasMap(List<Restriction> combination, OntopPlanning op) {

	    this.fragmentsMaps = new ArrayList<>();

	    for( Restriction r : combination ){
		// Transform the restriction in SQL
		CQIE cq = r.getDLog().getRules().iterator().next();
		Map<Variable, Set<QualifiedAttributeID>> aliasMap = op.getAliasMap(cq);
		this.fragmentsMaps.add(aliasMap);
	    }
	}

	Map<Variable, Set<QualifiedAttributeID>> getMapForFragment(int fragIndex){
	    return this.fragmentsMaps.get(fragIndex);
	}

	private Set<QualifiedAttributeID> getAliasesFor(int fragIndex,
		List<Variable> variablesInTerm) {

	    Set<QualifiedAttributeID> result = new HashSet<>();

	    for( Variable v : variablesInTerm ){
		result.addAll( getMapForFragment(fragIndex).get(v) );
	    }

	    return result;
	}
    };

    /**
     * 
     * @author Davide Lanti
     * 
     * Variable -> FragId -> [col1, ..., coln]
     */
    private class JoinStructurer{
	
	private static final String FRAG_ID = "f_";
	
	// Variable -> FragId -> [col1, \ldots, coln]
	private Map<Variable, Map<Integer, Set<QualifiedAttributeID>>> state;
	//	private List<Map<Variable, Set<QualifiedAttributeID>>> aliasMap;

	private JoinStructurer() {
	    this.state = new HashMap<>();
	}

	private void add(Variable v, int fragIndex, Set<QualifiedAttributeID> attrs) {
	    if( state.containsKey(v) ){
		state.get(v).put(fragIndex, attrs);
	    }
	    else{
		Map<Integer, Set<QualifiedAttributeID>> map = new HashMap<>();
		map.put(fragIndex, attrs);
		state.put(v, map);
	    }
	}

	@Override
	public String toString(){
	    return state.toString();
	}
	
	public String joinString() {
//	    {x={0=[qview1."wlbNpdidWellbore", qview2."wlbNpdidWellbore", qview3."wlbNpdidWellbore"], 1=[qview1."wlbNpdidWellbore", qview2."wlbNpdidWellbore"]}}
	    
	    StringBuilder builder = new StringBuilder();
	    
	    for( Variable v : this.state.keySet() ){
		Map<Integer, Set<QualifiedAttributeID>> mFragIndexToAttrs =  this.state.get(v);
		for( Integer fragIndex : mFragIndexToAttrs.keySet() ){
		    QualifiedAttributeID first = mFragIndexToAttrs.get( fragIndex ).iterator().next();
		    
		    String colName = JoinStructurer.FRAG_ID + fragIndex + "." + first.getAttribute().getSQLRendering();
		    
		    if( builder.toString().endsWith("=") ){
			builder.append(colName);
			builder.append(", ");
		    }
		    else{
			builder.append(colName + "=");
		    }
		    
		    //		    qA.
		}
		builder.deleteCharAt(builder.length() -1); // Remove .
	    }
	    
	    return " ON " + builder.toString();
	}
    }

    // ***** ***** //

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
	   
	    AliasMap aliasMap = new AliasMap(combination, op);
	    
	    // SELECT projection FROM () AS F1, JOIN () AS F2, ..., ON 
	    Map<String, String> mColToTemplateToAttach = makeProjList(combination, op, mOutVariableToFragmentsVariables, aliasMap);
	    // Projection stucture: 
	    String sql = makeJoinCondition(combination, op, mOutVariableToFragmentsVariables, aliasMap);
	    union.add(sql);
	}
	
	String body = uniteAll(union);

	return body;
    }

    private Map<String, String> makeProjList(
	    List<Restriction> combination,
	    OntopPlanning op,
	    LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables, 
	    AliasMap aliasMap) {
	
//  	It can be that a combination projects out twice the same variable. Hence, we need renaming for each.
    	//	
	
	return null;
    }

    private String uniteAll(List<String> union) {
	// TODO Auto-generated method stub
	return null;
    }

    private String makeJoinCondition(
	    List<Restriction> combination,
	    OntopPlanning op,
	    LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables, AliasMap aliasMap) {

	String result = "";

	JoinStructurer structurer = new JoinStructurer(); // Davide> At the moment I am re-creating this every time
	                                  //       but this behavior could be optimized I think...

	

	for( int fragIndex = 0; fragIndex < combination.size(); ++fragIndex ){

	    // Transform the restriction in SQL
	    Restriction r = combination.get(fragIndex);
	    List<String> signature = op.makeSignatureForFragment(fragIndex, mOutVariableToFragmentsVariables);
	    String sql = op.getSQLForDL(r.getDLog(), signature);

	    // Make the projLists for Joins
	    List<String> projList = retrieveProjections(sql); // TTT TODO Davide, what was this doing?

	    // Update joins structurer	    
	    for( Variable v : mOutVariableToFragmentsVariables.keySet() ){
		List<MFragIndexToVarIndex> list = mOutVariableToFragmentsVariables.get(v);
		if( list.size() > 1 ){
		    // Join
		    for( MFragIndexToVarIndex el : list ){
			if( el.getFragIndex() == fragIndex ){
			    // Retrieve term
			    Term t = retrieveTerm( r, el.getVarIndex() );
			    List<Variable> variablesInTerm = varsOf(t, op);

			    structurer.add(v, fragIndex, aliasMap.getAliasesFor(fragIndex, variablesInTerm) );
			}
		    }
		}
	    }
	    
	}
	// Ok, now we have
	// {x={0=[qview1."wlbNpdidWellbore", qview2."wlbNpdidWellbore", qview3."wlbNpdidWellbore"], 1=[qview1."wlbNpdidWellbore", qview2."wlbNpdidWellbore"]}}
	result = structurer.joinString();
	System.out.println(result);
	return result;
    }

    private List<Variable> varsOf(Term t, OntopPlanning op) {

	List<Variable> result = new ArrayList<>();

	if( t instanceof Function ){
	    Function t1 = (Function)t;
	    for( Term t2 : t1.getTerms() ){
		if( t2 instanceof Variable )
		    if( ((Variable) t2).getName().matches(("t[0-9]*_[0-9]*f[0-9]+") ) ){
			// After the production of sql, ontop changes the names for the
			// variables in the datalog program and appends to them suffixes
			// like "f9", or in general "f[0-9]*". If the aliases map
			// was produced before the generation of the SQL, the variable
			// names will not match because the names in the map do 
			// not contain these suffixes. Therefore, here I remove 
			// the suffixes in case I find some
			Variable v = op.getFactory().getVariable(((Variable) t2).getName().substring(0, ((Variable) t2).getName().indexOf("f")) );
			result.add(v);
		    }
		    else{
			result.add( (Variable)t2 );
		    }
	    }
	}
	return result;
    }

    private Term retrieveTerm(Restriction r, int varIndex) {

	CQIE first = r.getDLog().getRules().iterator().next();
	Term result = first.getHead().getTerm(varIndex);

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
