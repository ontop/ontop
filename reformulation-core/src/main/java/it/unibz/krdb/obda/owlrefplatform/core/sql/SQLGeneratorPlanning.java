package it.unibz.krdb.obda.owlrefplatform.core.sql;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.BNodePredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import it.unibz.krdb.sql.DBMetadata;

public class SQLGeneratorPlanning extends SQLGenerator {

    private static final long serialVersionUID = 4787823427006462830L;

    public SQLGeneratorPlanning(DBMetadata metadata,
	    SQLDialectAdapter sqladapter) {
	super(metadata, sqladapter);
    }

    /**
     * 
     * @param metadata
     * @param sqladapter
     * @param sqlGenerateReplace
     * @param uriid is null in case we are not in the SI mode
     */
    public SQLGeneratorPlanning(DBMetadata metadata, SQLDialectAdapter sqladapter, boolean sqlGenerateReplace, boolean distinctResultSet, SemanticIndexURIMap uriid) {
	super(metadata, sqladapter, sqlGenerateReplace, distinctResultSet, uriid);
    }


    /**
     * produces the select clause of the sql query for the given CQIE
     * 
     * @param query
     *            the query
     * @return the sql select clause
     */
    @Override
    protected String getSelectClause(List<String> signature, CQIE query,
	    QueryAliasIndex index, boolean distinct) throws OBDAException {
	/*
	 * If the head has size 0 this is a boolean query.
	 */
	List<Term> headterms = query.getHead().getTerms();
	StringBuilder sb = new StringBuilder();

	sb.append("SELECT ");
	if (distinct && !distinctResultSet) {
	    sb.append("DISTINCT ");
	}
	//Only for ASK
	if (headterms.size() == 0) {
	    sb.append("'true' as x");
	    return sb.toString();
	}

	/**
	 * Set that contains all the variable names created on the top query.
	 * It helps the dialect adapter to generate variable names according to its possible restrictions.
	 * Currently, this is needed for the Oracle adapter (max. length of 30 characters).
	 */
	Set<String> sqlVariableNames = new HashSet<>();  // REMOVE

	Iterator<Term> hit = headterms.iterator();
	int hpos = 0;
	while (hit.hasNext()) {

	    Term ht = hit.next();
	    String mainColumn = getMainColumnForSELECT(ht, signature, hpos, index, sqlVariableNames);

	    sb.append(mainColumn);
	    if (hit.hasNext()) {
		sb.append(", ");
	    }
	    hpos++;
	}
	return sb.toString();
    }

    protected String getMainColumnForSELECT(Term ht,
	    List<String> signature, int hpos, QueryAliasIndex index, Set<String> sqlVariableNames) {

	String templateColumns = null;

	if (ht instanceof URIConstant) { //
	    URIConstant uc = (URIConstant) ht;
	    templateColumns = sqladapter.getSQLLexicalFormString(uc.getURI());
	} 
	else if (ht == OBDAVocabulary.NULL) { //
	    templateColumns = "NULL";
	} 
	else if (ht instanceof Function) {
	    /*
	     * if it's a function we need to get the nested value if its a
	     * datatype function or we need to do the CONCAT if its URI(....).
	     */
	    Function ov = (Function) ht;
	    Predicate function = ov.getFunctionSymbol();

	    /*
	     * Adding the column(s) with the actual value(s)
	     */
	    if (ov.isDataTypeFunction()) { 
		/*
		 * Case where we have a typing function in the head (this is the
		 * case for all literal columns
		 */
		int size = ov.getTerms().size();
		if ((function instanceof Literal) || size > 2 ) {
		    templateColumns = getSQLStringForTemplateFunction(ov, index);
		}
		else {
		    Term term = ov.getTerms().get(0);
		    if (term instanceof ValueConstant) {
			templateColumns = getSQLLexicalForm((ValueConstant) term);
		    } else {
			templateColumns = getSQLString(term, index, false);
		    }
		}
	    }
	    else if (function instanceof URITemplatePredicate) {
		// New template based URI building functions
		templateColumns = getSQLStringForTemplateFunction(ov, index);
	    }
	    else if (function instanceof BNodePredicate) {
		// New template based BNODE building functions
		templateColumns = getSQLStringForTemplateFunction(ov, index);
	    }
	    else if (ov.isOperation()) {
		templateColumns = getSQLString(ov, index, false); 
	    }
	    else 
		throw new IllegalArgumentException("Error generating SQL query. Found an invalid function during translation: " + ov);
	} 
	else 
	    throw new RuntimeException("Cannot generate SELECT for term: " + ht);

	/*
	 * If we have a column we need to still CAST to VARCHAR
	 */
	return templateColumns;
    }

    /**
     * URI(..., t.c, t1.c1) -> t.c, t1.c1
     */
    public String getSQLStringForTemplateFunction(Function ov, QueryAliasIndex index) {

	List<String> colNames = new ArrayList<>();

	/*
	     * The first inner term determines the form of the result
	     */
	    Term t = ov.getTerms().get(0);
	    
	    if (t instanceof ValueConstant || t instanceof BNode) {
//		/*
//		* The function is actually a template. The first parameter is a
//		* string of the form http://.../.../ or empty "{}" with place holders of the form
//		 * {}. The rest are variables or constants that should be put in
//		 * place of the place holders. 
		
		for( int termIndex = 1; termIndex < ov.getTerms().size(); ++termIndex ){
		    Term colDLogName = ov.getTerms().get(termIndex);
		    colNames.add( getSQLString( colDLogName, index, false ) );
		}
	    }
	    else if (t instanceof Variable) {
		/*
		 * The function is of the form uri(x), we need to simply return the
		 * value of X
		 */
		colNames.add( getSQLString(t, index, false) );
	    } 
	    else if (t instanceof URIConstant) {
		/*
		 * The function is of the form uri("http://some.uri/"), i.e., a
		 * concrete URI, we return the string representing that URI.
		 */
		URIConstant uc = (URIConstant) t;
		colNames.add( sqladapter.getSQLLexicalFormString(uc.getURI()) ); // FIXME ( Davide> I don't know what it does )
	    }
	    else if (t instanceof Function) {
		/*
		 * The function is of the form uri(CONCAT("string",x)),we simply return the value from the database.
		 */
		colNames.add( getSQLString(t, index, false) );
	    }
	    else{
		/*
		 * Unsupported case
		 */
		throw new IllegalArgumentException("Error, cannot generate URI constructor clause for a term: " + ov);
	    }
	    StringBuilder result = new StringBuilder();
	    
	    for( String s : colNames ){
		if( result.length() != 0 ){
		    result.append(", ");
		}
		result.append(s);
	    }
	    
	    return result.toString();
	}
};