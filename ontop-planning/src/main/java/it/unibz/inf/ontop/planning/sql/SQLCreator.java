package it.unibz.inf.ontop.planning.sql;

import it.unibz.inf.ontop.planning.OntopPlanning;
import it.unibz.inf.ontop.planning.datatypes.MFragIndexToVarIndex;
import it.unibz.inf.ontop.planning.datatypes.Restriction;
import it.unibz.inf.ontop.planning.datatypes.Signature;
import it.unibz.inf.ontop.planning.datatypes.Template;
import it.unibz.inf.ontop.planning.sql.helpers.ExtendedCombinationRestriction;
import it.unibz.inf.ontop.planning.sql.helpers.ExtendedRestriction;
import it.unibz.inf.ontop.planning.sql.helpers.ExtendedSignature;
import it.unibz.inf.ontop.planning.sql.helpers.ExtendedTerm;
import it.unibz.inf.ontop.planning.sql.helpers.RestrictionDecorator;
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

import org.openrdf.query.algebra.Load;

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
	    
	    ExtendedCombinationRestriction extendedCombination =  extendCombinationRestriction(combination, op, mOutVariableToFragmentsVariables, aliasMap);
	    
	    // Let's make a projection
	    String proj = makeProj(extendedCombination, op);
	    
	    // makeFrom 
	    String from = makeFrom(extendedCombination, op);
	    
	    // Join condition
	    String joinCondition = makeJoinCondition(combination, op, mOutVariableToFragmentsVariables, aliasMap); // ON f_0."wlbNpdidWellbore"=f_1."wlbNpdidWellbore" 
	    union.add(joinCondition);
	}
	
	String body = uniteAll(union);

	return body;
    }

    private String makeFrom(ExtendedCombinationRestriction extendedCombination, OntopPlanning op) {

	class LocalUtils{ // Helper class
	    
	    // Produce something like qview1."blabla" AS t1v1, ..., AS tnvm
	    String renameProjections( String sql, ExtendedSignature eS ){
		
		List<String> splits = Arrays.asList( sql.split("UNION") );
		
		for( String cq : splits ){
		    String proj = cq.substring(cq.indexOf("SELECT") + 6, cq.indexOf("FROM"));
		    
		    List<String> commaSplits = Arrays.asList(cq.split(","));
		    StringBuilder commaSplitsRenamedBuilder = new StringBuilder();
		    
		    for( int termCounter = 0; termCounter < eS.getOutVariables().size(); ++termCounter ){
			ExtendedTerm t = eS.getTermOf( eS.getOutVariables().get(termCounter) );
			for( int termVarCounter = 0; termVarCounter < t.getTermVariables().size(); ++termVarCounter ){
			    String aliasName = "t"+termCounter+"v"+termVarCounter;
			    String newProjElement = commaSplits.get(termCounter + termVarCounter) + " AS " + aliasName;
			    if( commaSplitsRenamedBuilder.length() > 0 )
				commaSplitsRenamedBuilder.append(", ");
			    commaSplitsRenamedBuilder.append(newProjElement);
			}
		    }
		    String newSql = commaSplitsRenamedBuilder.toString() + cq.substring(cq.indexOf("FROM") + 4);
		    System.out.println(newSql);
		}
		return null;
	    }
	}
	
	LocalUtils utils = new LocalUtils();
	StringBuilder builder = new StringBuilder();
	int fragIndex = 0;
	
	for( ExtendedRestriction fragment : extendedCombination.getRestrictions() ){
	    
	    if( builder.length() > 0 ) builder.append(", ");
	    
	    ++fragIndex;
	    
	    ExtendedSignature eS = fragment.getExtendedSignature();
	    
	    List<String> variablesStrings = new ArrayList<>();
	    
	    for( Variable v : eS.getOutVariables() ){
		variablesStrings.add(v.toString());
	    }
	    String sql = op.getSQLForDL(fragment.getDLog(), variablesStrings);
	    
	    // Prune SELECT * FROM ( ... )
	    sql = sql.substring(sql.indexOf("(") + 1, sql.lastIndexOf(")"));
	    
	    utils.renameProjections(sql, eS);
	    
	    builder.append( sql + ")" + " f_"+fragIndex );
	}
	
	return builder.toString();
    }

    /**
     * 
     * @param extendedCombination
     * @param op
     * @return SELECT template(f_1.blabla) AS x, template(f1.cici) as y, etc. FROM caca
     */
    private String makeProj(ExtendedCombinationRestriction extendedCombination, OntopPlanning op) {
	
	String CONCAT_OP = "||";
		
	StringBuilder builder = new StringBuilder();
	
	List<Variable> doneVars = new ArrayList<>();
	
	// OutVariables
	
	int fragIndex = 0;
	for( ExtendedRestriction fragment : extendedCombination.getRestrictions() ){
	    
	    ++fragIndex;
	    
	    for( Variable v : fragment.getExtendedSignature().getOutVariables() ){
		if( !doneVars.contains(v) ){
		    
		    if( builder.length() > 0 ) builder.append(", ");
		    
		    // AS v
		    String as = " AS " + v;
		    
		    ExtendedTerm t = fragment.getExtendedSignature().getTermOf(v);
		    String result = toSQLConcat( fragIndex, v, t, CONCAT_OP );
		    
		    builder.append(result);
		    builder.append(as);
		    
		    
		    doneVars.add(v);
		}
		
	    }
	}
	return builder.toString();
    }

    private String toSQLConcat(int fragIndex, Variable v, ExtendedTerm t, String CONCAT_OP) {
	
	StringBuilder builder = new StringBuilder();
	
	List<String> splits = t.split();
	
	for( int i = 0; i < t.getTermVariables().size(); ++i ){
	    
	    if( i > 0 ) builder.append(" " + CONCAT_OP + " ");
	    
	    builder.append("'" + splits.get(i) + "'");
	    builder.append(" " + CONCAT_OP + " ");
	    
	    QualifiedAttributeID qA = t.getAliasesFor(t.getTermVariables().get(i)).iterator().next();
	    
	    String replaced = "f_" + fragIndex + qA.toString().substring(qA.toString().indexOf("."), qA.toString().length()); 
	    
	    builder.append( replaced );
	
	    // http://sws.ifi.uio.no/data/npd-v2/wellbore/ || [qview1."wlbNpdidWellbore", qview2."wlbNpdidWellbore", qview3."wlbNpdidWellbore"] || /stratum/ || [qview1."lsuNpdidLithoStrat"]/cores
	    
	    
	    if( (i == t.getTermVariables().size() -1)  && (i+1 < splits.size()) ){
		builder.append(" " + CONCAT_OP + " ");
		builder.append("'" + splits.get(i+1) + "'");
	    }
	}
	return builder.toString();
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
    private ExtendedCombinationRestriction extendCombinationRestriction(
	    CombinationRestriction combination,
	    OntopPlanning op,
	    LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables, 
	    AliasMap aliasMap) {
	

	List<ExtendedRestriction> restrictions = new ArrayList<>();
	
	for( int fragIndex = 0; fragIndex < combination.numFragments(); ++fragIndex ){
	    
	    // Transform the restriction in SQL
	    Restriction r = combination.getFragmentOfIndex(fragIndex);
	    
	    ExtendedSignature.Builder signatureBuilder = new ExtendedSignature.Builder();
	    signatureBuilder.signature(r.getSignature());
	    for( Variable v : mOutVariableToFragmentsVariables.keySet() ){
		List<MFragIndexToVarIndex> list = mOutVariableToFragmentsVariables.get(v);
		
		
		for( MFragIndexToVarIndex el : list ){
		    if( el.getFragIndex() == fragIndex ){
			// Retrieve term
			Term t = retrieveTerm( r, el.getVarIndex() );
			List<Variable> variablesInTerm = varsOf(t, op);
			
			ExtendedTerm eT = new ExtendedTerm(t, variablesInTerm, aliasMap.getMapForFragment(fragIndex) );
			
			signatureBuilder.addOutVariableAndTerm(v, eT);
						
		    }
		}
	    }
	    ExtendedRestriction eR = new ExtendedRestriction.Builder(r).signature(signatureBuilder.build()).build();
	    restrictions.add(eR);
	}
	
	return new ExtendedCombinationRestriction(restrictions);
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
	    List<String> signature = op.outVarsListForFragment(fragIndex, mOutVariableToFragmentsVariables);
	    
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



