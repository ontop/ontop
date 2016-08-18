package it.unibz.inf.ontop.planning.sql;

import it.unibz.inf.ontop.planning.OntopPlanning;
import it.unibz.inf.ontop.planning.datatypes.MFragIndexToVarIndex;
import it.unibz.inf.ontop.planning.datatypes.Pair;
import it.unibz.inf.ontop.planning.datatypes.Restriction;
import it.unibz.inf.ontop.planning.datatypes.Template;
import it.unibz.inf.ontop.planning.sql.decorators.ExtendedCombinationRestriction;
import it.unibz.inf.ontop.planning.sql.decorators.ExtendedRestriction;
import it.unibz.inf.ontop.planning.sql.decorators.ExtendedSignature;
import it.unibz.inf.ontop.planning.sql.decorators.ExtendedTerm;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.sql.QualifiedAttributeID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;

public class SQLCreatorImpl implements SQLCreator {

    private List<CombinationRestriction> combinations;
    
    // Build instance
    private SQLCreatorImpl(SQLCreatorImpl.BuilderImpl b){
	this.combinations = b.combinations;
    }

    public static class BuilderImpl implements SQLCreator.Builder{

	private List<CombinationRestriction> combinations;
	
	public BuilderImpl(){
	    this.combinations = new ArrayList<>();
	}
	
	@Override
	public Builder addValidCombination(List<Restriction> combination) {
	    this.combinations.add( new CombinationRestriction(combination) );
	    return this;
	}
	
	@Override
	public SQLCreator build() {
	    return new SQLCreatorImpl(this);
	}
	
    }
    
    // Public Interface
    
    @Override
    public String makeSQL(OntopPlanning op, LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables) {

	// A generalized union of all combinations!
	List<String> union = new ArrayList<>();

	for( CombinationRestriction combination : this.combinations ){
	    
	    StringBuilder builder = new StringBuilder();
	    
	    builder.append("SELECT ");

	    AliasMap aliasMap = new AliasMap(combination, op);

	    ExtendedCombinationRestriction extendedCombination =  extendCombinationRestriction(combination, op, mOutVariableToFragmentsVariables, aliasMap);

	    // Let's make a projection
	    String proj = makeProj(extendedCombination, op);
	    
	    builder.append(proj);
	    
	    builder.append("\nFROM\n");
	    
	    // makeFrom 
	    String from = makeFrom(extendedCombination, op);

	    builder.append(from);
	    
	    // Join condition
	    String joinCondition = makeJoinCondition(extendedCombination, op, mOutVariableToFragmentsVariables, aliasMap); // ON f_0."wlbNpdidWellbore"=f_1."wlbNpdidWellbore" 

	    builder.append(joinCondition);
	    
	    union.add(builder.toString());
	}
	String body = uniteAll(union);

	return body;
    }

    // Helper
    private String makeFrom(ExtendedCombinationRestriction extendedCombination, OntopPlanning op) {

	class LocalUtils{ // Helper class

	    // Produce something like qview1."blabla" AS t1v1, ..., AS tnvm
	    String renameProjections( String sql, ExtendedSignature eS ){

		List<String> splits = Arrays.asList( sql.split("UNION") );
		StringBuilder renamedSplitsBuilder = new StringBuilder();

		for( String cq : splits ){
		    String proj = cq.substring(cq.indexOf("SELECT"), cq.indexOf("FROM"));

		    List<String> commaSplits = Arrays.asList(proj.split(","));
		    StringBuilder commaSplitsRenamedBuilder = new StringBuilder();

		    int splitCounter = 0;
		    for( int termCounter = 0; termCounter < eS.getOutVariables().size(); ++termCounter ){
			ExtendedTerm t = eS.getTermOf( eS.getOutVariables().get(termCounter) );
			for( int termVarCounter = 0; termVarCounter < t.getTermVariables().size(); ++termVarCounter ){
			    Variable termVar = t.getTermVariables().get(termVarCounter);
			    String projName = t.getProjNameForTermVariable(termVar);
			    String newProjElement = commaSplits.get(splitCounter++) + " AS " + projName; 
			    if( commaSplitsRenamedBuilder.length() > 0 )
				commaSplitsRenamedBuilder.append(", ");
			    commaSplitsRenamedBuilder.append(newProjElement);
			}
		    }
		    String newSql = commaSplitsRenamedBuilder.toString() + cq.substring(cq.indexOf(" FROM"));

		    if( renamedSplitsBuilder.length() > 0 ){
			renamedSplitsBuilder.append( "\nUNION\n" ); 
		    }
		    renamedSplitsBuilder.append( newSql );
		}
		return renamedSplitsBuilder.toString();
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

	    String renamedSql = utils.renameProjections(sql, eS);

	    builder.append( "(" + renamedSql + ")" + " f_"+fragIndex );
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
	    
	    Variable termVar = t.getTermVariables().get(i);

	    if( i > 0 ) builder.append(" " + CONCAT_OP + " ");

	    builder.append("'" + splits.get(i) + "'");
	    builder.append(" " + CONCAT_OP + " ");

	    //	    QualifiedAttributeID qA = t.getAliasesFor(t.getTermVariables().get(i)).iterator().next(); // Davide> Commented out after renaming term variables as v0t0 ...
	    String qA = t.getProjNameForTermVariable(termVar);

	    //	    String replaced = "f_" + fragIndex + qA.toString().substring(qA.toString().indexOf("."), qA.toString().length()); // Davide> Commented out after renaming term variables as v0t0 ... 
	    String replaced = "f_" + fragIndex + "." + qA;

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

	    int absoluteVarID = 0;
	    for( Variable v : mOutVariableToFragmentsVariables.keySet() ){
		List<MFragIndexToVarIndex> list = mOutVariableToFragmentsVariables.get(v);


		for( MFragIndexToVarIndex el : list ){
		    if( el.getFragIndex() == fragIndex ){
			// Retrieve term
			Term t = retrieveTerm( r, el.getVarIndex() );
			List<Variable> variablesInTerm = varsOf(t, op);

			ExtendedTerm eT = new ExtendedTerm(t, variablesInTerm, aliasMap.getMapForFragment(fragIndex), absoluteVarID);

			signatureBuilder.addOutVariableAndTerm(v, eT);

		    }
		}
		++absoluteVarID;
	    }
	    ExtendedRestriction eR = new ExtendedRestriction.Builder(r).signature(signatureBuilder.build()).build();
	    restrictions.add(eR);
	}

	return new ExtendedCombinationRestriction(restrictions);
    }

    private String uniteAll(List<String> union) {
	
	StringBuilder builder = new StringBuilder();
	
	for( String jucq : union ){
	    if( builder.length() > 0 ){
		builder.append("\nUNION\n");
	    }
	    builder.append(jucq);
	}
	return builder.toString();
    }

    private String makeJoinCondition(
	    ExtendedCombinationRestriction extendedCombination,
	    OntopPlanning op,
	    LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables, AliasMap aliasMap) {

	String result = "";

	JoinStructurer structurer = new JoinStructurer(); // Davide> At the moment I am re-creating this every time
	//       but this behavior could be optimized I think...

	for( int fragIndex = 0; fragIndex < extendedCombination.numFragments(); ++fragIndex ){

	    // Transform the restriction in SQL
	    ExtendedRestriction r = extendedCombination.getFragmentOfIndex(fragIndex);

	    // Update joins structurer	    
	    for( Variable v : mOutVariableToFragmentsVariables.keySet() ){

		if( !r.getExtendedSignature().getOutVariables().contains(v) ) continue;

		ExtendedTerm eT = r.getExtendedSignature().getTermOf(v);
		List<MFragIndexToVarIndex> list = mOutVariableToFragmentsVariables.get(v);
		if( list.size() > 1 ){
		    // Join
		    for( MFragIndexToVarIndex el : list ){
			if( el.getFragIndex() == fragIndex ){
			    // Retrieve term
			    //			    Term t = retrieveTerm( r, el.getVarIndex() );

			    structurer.add( eT, fragIndex );
			}
		    }
		}
	    }
	}
	// Ok, now we have
	// {x={0=[qview1."wlbNpdidWellbore", qview2."wlbNpdidWellbore", qview3."wlbNpdidWellbore"], 1=[qview1."wlbNpdidWellbore", qview2."wlbNpdidWellbore"]}}
	result = structurer.joinString();
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

    @Override
    public String toString() {
	return combinations.toString();
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
    };

    private class JoinStructurer{

	private static final String FRAG_ID = "f_";

	// Template -> [(ExtendedTerm, f_1), (ExtendedTerm, f_2), ...] 
	// Assumption: Each term identifies a specific join variable

	private Map<Template, List<Pair<ExtendedTerm, Integer>>> state;

	private JoinStructurer() {
	    this.state = new HashMap<>();
	}

	private void add(ExtendedTerm term, int fragIndex) {

	    Template t = new Template(term);

	    if( !state.containsKey(t) ){

		List<Pair<ExtendedTerm, Integer>> toAdd = new ArrayList<>(); 
		toAdd.add(new Pair<>(term, fragIndex));
		state.put(t, toAdd );
	    }
	    else{
		state.get(t).add(new Pair<>(term, fragIndex));
	    }
	}

	@Override
	public String toString(){
	    return state.toString();
	}

	public String joinString() {

	    StringBuilder builder = new StringBuilder();

	    for( Template t : this.state.keySet() ){

		for( int varIndex = 0; varIndex < t.getArity(); ++varIndex ){
		    List<Pair<ExtendedTerm, Integer>> list = this.state.get(t);
		    
		    for( int cnt = 0; cnt < list.size(); ++cnt ){
			
			Pair<ExtendedTerm, Integer> fragInfo = list.get(cnt);
			ExtendedTerm term = fragInfo.first;
			int fragIndex = fragInfo.second;
			
			Variable termVar = term.getTermVariables().get(varIndex);
			
			String colName = JoinStructurer.FRAG_ID + (fragIndex+1) + "." + term.getProjNameForTermVariable(termVar);

			if( builder.toString().endsWith("=") ){
			    builder.append(colName);
			    builder.append(", ");
			}
			else{
			    builder.append(colName + "=");
			}
		    }
		    //		    qA.
		    builder.deleteCharAt(builder.length() -2); // Remove , 
		}
	    }
	    return " ON " + builder.toString();
	}
    };
};