package it.unibz.inf.ontop.planning;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedListMultimap;

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConfiguration;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OntopPlanning {

    QuestOWLStatement st;


    public OntopPlanning(String owlfile, String obdafile) throws OWLException, IOException, InvalidMappingException, InvalidPredicateDeclarationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

        OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
        OBDAModel obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdafile);

        QuestPreferences pref = new QuestPreferences();

        // pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
        // pref.setCurrentValueOf(QuestPreferences.REWRITE, QuestConstants.TRUE);

        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(pref).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);

        QuestOWLConnection qconn = reasoner.getConnection();
        st = qconn.createStatement();

    }

    public List<DatalogProgram> getDLogUnfoldingsForFragments(List<String> fragments) throws OWLException, MalformedQueryException {
	
	List<DatalogProgram> result = new ArrayList<>();
	
        for (String f : fragments) {   
            DatalogProgram prog = st.getDLogUnfolding(f);
            result.add(prog);
        }
	return result;
    }
    
    /**
     * 
     * @param fragments
     * @return 
     * @throws MalformedQueryException
     */
    public LinkedListMultimap<String, MFragIndexToVarIndex> getFragmentsJoinVariables(List<String> fragments) throws MalformedQueryException{
	QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
	
	LinkedListMultimap<String, MFragIndexToVarIndex> variableOccurrences = LinkedListMultimap.create();
	
	int fragCounter = 0;
	for( String fragment : fragments ){
	    ParsedQuery pq = parser.parseQuery(fragment, null);    
	    
	    Set<String> bindingNames = pq.getTupleExpr().getBindingNames();

	    // Davide> Very ugly, but this is the internal order
	    //       of projection variables in ontop
	    List<String> orderedBindingNames = new ArrayList<>();
	    for( String name : bindingNames ){
		orderedBindingNames.add(name);
	    }

	    int argIndex = 0;
            for( String name : orderedBindingNames ){
        	variableOccurrences.put(name, new MFragIndexToVarIndex(fragCounter, argIndex++));
            }
            ++fragCounter;
	}
	System.out.println(variableOccurrences);
	return variableOccurrences;	
    }
    
    /**
     * Side effect on parameter <i>programs<\i>
     * @param programs
     * @param joinOn varName -> [(fragInxed, varIndex), ...]
     */
    public void pruneDLogPrograms(List<DatalogProgram> programs, LinkedListMultimap<String, MFragIndexToVarIndex> joinOn){
	
	class LocalUtils{
	    List<Pair<DatalogProgram, MFragIndexToVarIndex>> getProgramsList(List<MFragIndexToVarIndex> joins){
		List<Pair<DatalogProgram, MFragIndexToVarIndex>> result = new ArrayList<>();
		for( MFragIndexToVarIndex map : joins ){
		    result.add( new Pair<DatalogProgram, MFragIndexToVarIndex>(programs.get(map.fragIndex), map) );
		}
		return result;
	    }
	    
	    // URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}",t9_7) -> http://sws.ifi.uio.no/data/npd-v2/wellbore/{}
	    String cleanTerm(Term t){
		String s = t.toString();
		String result =s.substring(s.indexOf("(")+2, s.lastIndexOf("\""));
		return result;
	    }

	    public boolean inAll(String s, List<Pair<DatalogProgram, MFragIndexToVarIndex>> rest, List<MFragIndexToVarIndex> joins) {
		
		for( Pair<DatalogProgram, MFragIndexToVarIndex> pair : rest ){
		    DatalogProgram prog = pair.first;
		    int varIndex = pair.second.varIndex;
		    boolean found = false;
		    for( CQIE cq : prog.getRules() ){
			Function head = cq.getHead();
			Term t = head.getTerm(varIndex);
			String termString = cleanTerm(t);
			if( termString.equals(s) ){
			    found = true;
			    break;
			}
		    }
		    if( !found ) return false;
		}
		return true;
	    }

	    public void prunePrograms(List<Pair<DatalogProgram, MFragIndexToVarIndex>> pairs, List<String> prunableTermsFromPrograms) {
		for( Pair<DatalogProgram, MFragIndexToVarIndex> pair : pairs ){
		    DatalogProgram prog = pair.first;
		    int varIndex = pair.second.getVarIndex();
		    
		    List<CQIE> removableRules = new ArrayList<>();
		    for( String s : prunableTermsFromPrograms ){
			for( CQIE cq : prog.getRules() ){
			    Function head = cq.getHead();
			    Term t = head.getTerm(varIndex);
			    String termString = cleanTerm(t);
			    if( termString.equals(s) ){
				removableRules.add(cq);
			    }
			}
		    }
		    prog.removeRules(removableRules);
		}
	    }
	};
	
	LocalUtils utils = new LocalUtils();
	
	for( String varName : joinOn.keySet() ){
	    List<MFragIndexToVarIndex> joins = joinOn.get(varName);
	    if( joins.size() > 1 ){ 
		List<Pair<DatalogProgram, MFragIndexToVarIndex>> progs = utils.getProgramsList( joins );
		
		DatalogProgram firstDLogProg = progs.get(0).first;
		MFragIndexToVarIndex firstMFragIndexToVarIndex = progs.get(0).second;
		List<Pair<DatalogProgram, MFragIndexToVarIndex>> rest = new ArrayList<>();
		for( int i = 1; i < progs.size(); ++i ){
		    rest.add(progs.get(i));
		}
		
		List<String> encounteredTerms = new ArrayList<String>();
		
		List<String> prunableTermsFromPrograms = new ArrayList<String>();
		for( CQIE cq : firstDLogProg.getRules() ){
		    int varIndex = firstMFragIndexToVarIndex.varIndex;
		    Function head = cq.getHead();
		    Term t = head.getTerm(varIndex);
		    String s = utils.cleanTerm(t);
		    if( encounteredTerms.contains(s) ) continue;
		    encounteredTerms.add(s);
		    if( !utils.inAll(s, rest, joins) ){
			prunableTermsFromPrograms.add(s);
		    }
		}
		
		// Now it is the time to prune
		utils.prunePrograms(progs, prunableTermsFromPrograms);
	    }
	}
    }
        
    public String getSQLForDLogUnfoldings(List<DatalogProgram> unfoldedFragments){
	return null;
    }
    
    public String getSQLForFragments(List<String> fragments) throws OWLException, MalformedQueryException {

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
        QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);

        int fragCounter = 0;

        LinkedListMultimap<String, Integer> variableOccurrences = LinkedListMultimap.create();

        for (String f : fragments) {
            fragCounter++;
            if (fragCounter != 1) {
                sqlBuilder.append(",");
            }

            String s = st.getUnfolding(f);

            final String FRAGMENT_PREFIX = "f_";
            sqlBuilder.append("(").append(s).append(") " + FRAGMENT_PREFIX).append(fragCounter);

            ParsedQuery pq = parser.parseQuery(f, null);
            Set<String> bindingNames = pq.getTupleExpr().getBindingNames();

            int finalI = fragCounter;
            bindingNames.forEach(name -> variableOccurrences.put(name, finalI));
        }


        List<String> joinConditions = new ArrayList<>();

        for (String v : variableOccurrences.keySet()) {
            List<Integer> occurrences = variableOccurrences.get(v);
            if (occurrences.size() > 1) {
                for (int i = 0; i < occurrences.size(); i++) {
                    for (int j = i + 1; j < occurrences.size(); j++){
                        joinConditions.add(String.format("f_%d.%s = f_%d.%s", occurrences.get(i), v, occurrences.get(j),v));
                    }
                }
            }
        }

        if(joinConditions.size() > 0){
            sqlBuilder.append("\n WHERE ");
            String condition = Joiner.on(" AND ").join(joinConditions);
            sqlBuilder.append(condition);
        }

        return sqlBuilder.toString();
    }

    public static void main(String[] args) throws InvalidMappingException, OWLException, InvalidPredicateDeclarationException, IOException {


//        st.close();
//
//        reasoner.dispose();

    }


}

class Pair<S,T>{
    final S first;
    final T second;
    
    Pair(S first, T second){
	this.first = first;
	this.second = second;
    }
    
    @Override
    public String toString(){
	return "(" + this.first + ", " + this.second + ")";   
    }
    
}

class MFragIndexToVarIndex{
    
    final int fragIndex;
    final int varIndex;
    
    MFragIndexToVarIndex(Integer fragIndex, Integer varIndex) {
	this.fragIndex = fragIndex;
	this.varIndex = varIndex;
    }
    
    int getFragIndex(){
	return this.fragIndex;
    }
    
    int getVarIndex(){
	return this.varIndex;
    }
    
    @Override
    public String toString() {
	return "fragIndex := " + this.fragIndex + ", varIndex := " + this.varIndex + ")";
    }
};