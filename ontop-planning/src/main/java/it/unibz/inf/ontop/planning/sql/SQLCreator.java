package it.unibz.inf.ontop.planning.sql;

import it.unibz.inf.ontop.planning.OntopPlanning;
import it.unibz.inf.ontop.planning.datatypes.MFragIndexToVarIndex;
import it.unibz.inf.ontop.planning.datatypes.Restriction;
import it.unibz.inf.ontop.planning.datatypes.Signature;
import it.unibz.inf.ontop.planning.datatypes.Template;
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

    private List<CombinationRestriction> combinations;

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

	private AliasMap(CombinationRestriction combination, OntopPlanning op) {

	    this.fragmentsMaps = new ArrayList<>();

	    for( Restriction r : combination.getRestrictions() ){
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
		builder.deleteCharAt(builder.length() -2); // Remove , 
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
	this.combinations.add( new CombinationRestriction(combination) );
    }

    public List<CombinationRestriction> getCombinations(){
	return Collections.unmodifiableList(this.combinations);
    }

    public String makeSQL(OntopPlanning op, LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables) {

	// A generalized union of all combinations!
	List<String> union = new ArrayList<>();

	for( CombinationRestriction combination : this.getCombinations() ){
	   
	    AliasMap aliasMap = new AliasMap(combination, op);
	    
	    // SELECT projection FROM () AS F1, JOIN () AS F2, ..., ON 
	    Map<List<String>, String> mColsToTemplateToAttach = makeCombinationInfos(combination, op, mOutVariableToFragmentsVariables, aliasMap);
	    
	    
	    // Projection stucture: 
	    String joinCondition = makeJoinCondition(combination, op, mOutVariableToFragmentsVariables, aliasMap); // ON f_0."wlbNpdidWellbore"=f_1."wlbNpdidWellbore" 
	    union.add(joinCondition);
	}
	
	String body = uniteAll(union);

	return body;
    }

    
    // SELECT f1.colName1, f1.colName2, ..
    // This work for a combination
    /**
     * 
     * @param combination
     * @param op
     * @param mOutVariableToFragmentsVariables
     * @param aliasMap
     * @return 
     */
    private Map<List<String>, String> makeCombinationInfos(
	    CombinationRestriction combination,
	    OntopPlanning op,
	    LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables, 
	    AliasMap aliasMap) {
	
	
	for( int fragIndex = 0; fragIndex < combination.numFragments(); ++fragIndex ){
	    
	    // Transform the restriction in SQL
	    Restriction r = combination.getFragmentOfIndex(fragIndex);
	    List<String> signature = op.makeSignatureForFragment(fragIndex, mOutVariableToFragmentsVariables);
	    
	    
	    String sql = op.getSQLForDL(r.getDLog(), signature);
	    
	    // Make the projLists for Joins
	    List<String> projList = retrieveProjections(sql); // Retrieve the projection of sql
	    
	    // Update joins structurer	    
	    for( Variable v : mOutVariableToFragmentsVariables.keySet() ){
		List<MFragIndexToVarIndex> list = mOutVariableToFragmentsVariables.get(v);
		for( MFragIndexToVarIndex el : list ){
		    if( el.getFragIndex() == fragIndex ){
			// Retrieve term
			Term t = retrieveTerm( r, el.getVarIndex() );
			List<Variable> variablesInTerm = varsOf(t, op);

			// The same term variable (e.g., t09) can be filled by several columns
			Set<QualifiedAttributeID> aliases = aliasMap.getAliasesFor(fragIndex, variablesInTerm);
		    }
		}
	    }

	}
	
//	retrieveProjections(sql)
	
//  	It can be that a combination projects out twice the same variable. Hence, we need renaming for each. (why?)
    	//	
	
	return null;
    }

    private String uniteAll(List<String> union) {
	// TODO Auto-generated method stub
	return null;
    }

    private String makeJoinCondition(
	    CombinationRestriction combination,
	    OntopPlanning op,
	    LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables, AliasMap aliasMap) {

	String result = "";

	JoinStructurer structurer = new JoinStructurer(); // Davide> At the moment I am re-creating this every time
	                                  //       but this behavior could be optimized I think...

	

	for( int fragIndex = 0; fragIndex < combination.numFragments(); ++fragIndex ){

	    // Transform the restriction in SQL
	    Restriction r = combination.getFragmentOfIndex(fragIndex);
	    List<String> signature = op.makeSignatureForFragment(fragIndex, mOutVariableToFragmentsVariables);
	    
	    
	    String sql = op.getSQLForDL(r.getDLog(), signature);

	    // Make the projLists for Joins
	    List<String> projList = retrieveProjections(sql); // Retrieve the projection of sql

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
	    
	    /**
	     
	     * 
	     * @param first
	     * @return
	     * 
	     * * SELECT *
                FROM (
                  SELECT qview1."wlbNpdidWellbore", qview1."lsuNpdidLithoStrat", qview1."wlbNpdidWellbore", qview3."wlbDrillingOperator", qview1."lsuCoreLenght"
                  FROM 
                  <br>
                  => 
                  <br>
                  ["wlbNpdidWellbore", "lsuNpdidLithoStrat", "wlbNpdidWellbore", "wlbDrillingOperator", "lsuCoreLenght"]                  
	     * 
	     */
	    public List<String> projList(String first) {

		String raw = first.substring( header.length(), first.lastIndexOf("FROM") );
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


class CombinationRestriction{
    private final List<Restriction> restrictions;
    
    CombinationRestriction( List<Restriction> restrictions){
	this.restrictions = restrictions;
    }
    
    List<Restriction> getRestrictions(){
	return Collections.unmodifiableList(this.restrictions);
    }
    
    int numFragments(){
	return this.getRestrictions().size();
    }
    
    Restriction getFragmentOfIndex( int index ){
	return this.restrictions.get(index);
    }
}

//Decorator pattern
class SignatureDecorator{

    private final Signature component;
    
    
    SignatureDecorator( Signature component ){
	this.component = component;
    }
    
    // Decorator wrapping
    public Template getTemplateOfIndex( int index ){
	
	return component.getTemplateOfIndex(index);
    }
    
    @Override 
    public boolean equals(Object other) {
	if( other instanceof SignatureDecorator )
	    return component.equals( ((SignatureDecorator) other).component );
	else return false;
    }
    
    @Override
    public int hashCode(){
	return component.hashCode();
    }
    
    @Override
    public String toString(){
	return component.toString();
    }
};

class ExtendedSignature extends SignatureDecorator{
    
    private final List<Variable> outVariables;
    private final Map<Variable, Term> mOutVarToTerm;
    private final Map<Variable, List<QualifiedAttributeID>> mTermVariableToQualifiedAttributes;

    ExtendedSignature( Signature component, List<Variable> outVariables, Map<Variable, Term> mOutVarToTerm, 
	    Map<Variable, List<QualifiedAttributeID>> mTermVariableToQualifiedAttributes ){
	
	super(component);
	this.outVariables = outVariables;
	this.mOutVarToTerm = mOutVarToTerm;
	this.mTermVariableToQualifiedAttributes = mTermVariableToQualifiedAttributes;
    }
    
    List<Variable> getOutVariables(){
	return Collections.unmodifiableList(outVariables);
    }
    
    Term getTermOf( Variable outVariable ){
	return this.mOutVarToTerm.get(outVariable);
    }
    
    List<QualifiedAttributeID> getQualifiedAttributesFor( Variable termVariable ){
	return this.mTermVariableToQualifiedAttributes.get(termVariable);
    }
    // TODO Continua questa robaccia (magari usa altri files)
};

class RestrictionInfo{
    
    private final List<Variable> outVariables;
    private final Map<Variable, Term> mOutVarToTerm;
    private final Map<Variable, List<QualifiedAttributeID>> mTermVariableToQualifiedAttributes;
    private final Restriction restriction;
    
    RestrictionInfo( List<Variable> outVariables, Map<Variable, Term> mOutVarToTerm, 
	    Map<Variable, List<QualifiedAttributeID>> mTermVariableToQualifiedAttributes, Restriction combination ){
	
	this.outVariables = outVariables;
	this.mOutVarToTerm = mOutVarToTerm;
	this.mTermVariableToQualifiedAttributes = mTermVariableToQualifiedAttributes;
	this.restriction = combination;
    }
    

    List<Variable> getOutVariables(){
	return Collections.unmodifiableList(outVariables);
    }
    
    Term getTermOf( Variable outVariable ){
	return this.mOutVarToTerm.get(outVariable);
    }
    
    List<QualifiedAttributeID> getQualifiedAttributesFor( Variable termVariable ){
	return this.mTermVariableToQualifiedAttributes.get(termVariable);
    }
    
    Restriction getCombination(){
	return this.restriction;
    }
};