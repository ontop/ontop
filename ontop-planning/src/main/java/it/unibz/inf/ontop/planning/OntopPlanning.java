package it.unibz.inf.ontop.planning;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedListMultimap;

import it.unibz.inf.ontop.planning.datatypes.MFragIndexToVarIndex;
import it.unibz.inf.ontop.planning.datatypes.Restriction;
import it.unibz.inf.ontop.planning.datatypes.Signature;
import it.unibz.inf.ontop.planning.datatypes.Template;
import it.unibz.inf.ontop.planning.fragments.MapOutVariableToFragVariables;
import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.sql.SQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConfiguration;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.sql.QualifiedAttributeID;

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OntopPlanning {

    QuestOWLStatement st;
    OBDADataFactory fac;
    
    public OntopPlanning(String owlfile, String obdafile) throws OWLException, IOException, InvalidMappingException, InvalidPredicateDeclarationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
        
        fac = OBDADataFactoryImpl.getInstance();
        OBDAModel obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdafile);

        QuestPreferences pref = new QuestPreferences();

        pref.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);
        pref.setCurrentValueOf(QuestPreferences.SQL_GENERATE_TEMPLATES, QuestConstants.FALSE);
        
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
    
    public OBDADataFactory getFactory(){
	return this.fac;
    }
        
    /**
     * 
     * @param fragments
     * @return 
     * @throws MalformedQueryException
     */
    public LinkedListMultimap<Variable, MFragIndexToVarIndex> getmOutVariableToFragmentsVariables(List<String> fragments) throws MalformedQueryException{
	QueryParser parser = QueryParserUtil.createParser(QueryLanguage.SPARQL);
	
	LinkedListMultimap<Variable, MFragIndexToVarIndex> variableOccurrences = LinkedListMultimap.create();
	
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
        	variableOccurrences.put(fac.getVariable(name), new MFragIndexToVarIndex(fragCounter, argIndex++));
            }
            ++fragCounter;
	}
	return variableOccurrences;	
    }
       
    /**
     * 
     * @param A DatalogProgram D
     * @return (D|_s1, ..., D|_sn), where s1, ..., sn in SignaturesOf(D)
     */
    public List<Restriction> splitDLogWRTTemplates(DatalogProgram prog){
	
	List<Restriction> result = new ArrayList<>();
	
	List<Signature> encounteredSignatures = new ArrayList<>();
	for( CQIE cq : prog.getRules() ){
	    
	    Signature ts = calculateTemplatesSignature(cq);
	    
	    if( !encounteredSignatures.contains(ts) ){ // New Signature
		encounteredSignatures.add(ts);
		DatalogProgram restrictionProgram = calculateRestriction(prog, ts);
		result.add( new Restriction(ts, restrictionProgram) );
	    }
	}
	return result;
    }

    private Signature calculateTemplatesSignature(CQIE cq) {
	Function head = cq.getHead();
	
	Signature.Builder builder = new Signature.Builder();
	
	for( Term t : head.getTerms() ){
	    
	    if (t instanceof ValueConstant || t instanceof BNode) {
		System.out.println(t + " ValueConstant || BNode");
		assert false : "Unsupported";
	    }
	    else if( t instanceof Variable ){
		System.out.println(t + " Variable");
		assert false : "Unsupported";
	    }
	    else if( t instanceof URIConstant ){
		System.out.println(t + " URIConstant");
		assert false : "Unsupported";
	    }
	    else if( t instanceof Function ){
		// Data and URIs
//		System.out.println(t + " Function");
//		System.out.println(takeTemplateString(t));

		builder.template( new Template(takeTemplateString( t )) );
	    }
	}
	Signature result = builder.build();
	
	return result;
    }

	// Restrict a datalog program to a certain template
	private DatalogProgram calculateRestriction(DatalogProgram prog,
		Signature templ) {

	    DatalogProgram copy = prog.clone();

	    List<CQIE> toRemove = new LinkedList<>();
	    for( Iterator<CQIE> it = copy.getRules().iterator(); it.hasNext();  ){
		CQIE cq = it.next();
		Signature ts = calculateTemplatesSignature(cq);
		if( !ts.equals(templ) ){
		   // Prune the rule
		    toRemove.add(cq);
		}
	    }
	    
	    copy.removeRules(toRemove);
	    
	    return copy;
	}
	
    private String takeTemplateString(Term t) {
	assert (t instanceof Function) : "Assertion Failed: t is NOT an object or a data value\n";
	
	String result = null;
	
	String termString = t.toString();
	if( termString.startsWith("URI") ){
	    result = termString.substring( 0, termString.indexOf(",") ) + ")";
	}
	else{
	    result = termString.substring( 0, termString.indexOf("(") );
	}
	
	return result;
    }

    public String getSQLForDLogUnfoldings(List<DatalogProgram> unfoldedFragments){
	return null;
    }
    
    public String getSQLForDL(DatalogProgram d, List<String> signature){
	
	String sql = st.getSQLFromDLog(d, signature);
	return sql;
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

    public List<String> makeSignatureForFragment(
	    int fragIndex,
	    LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables) {
	
	List<String> result = new ArrayList<>();
	
	for( Variable v : mOutVariableToFragmentsVariables.keySet() ){
	    for( MFragIndexToVarIndex mFragIndexToVarIndex : mOutVariableToFragmentsVariables.get(v) ){
		if( fragIndex == mFragIndexToVarIndex.getFragIndex() ){
		    result.add(v.getName());
		}
	    }
	}
	
	return result;
    }

    public Map<Variable, Set<QualifiedAttributeID>> getAliasMap(CQIE cq) {
	return this.st.getAliasMap(cq);
    }
}



