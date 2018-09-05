package it.unibz.inf.ontop.answering.benchmark;

import java.util.List;

import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogProgram;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

/**
 * 
 * @author Davide Lanti
 *
 * This object stores information useful
 * for benchmarking purposes. A new instance of this object
 * should be created for each executed query.
 */
public class OntopBenchmark {
    
    private final long rewritingTime; // Time spent in the rewrite procedure
    private final long unfoldingTime;  // Time spent to transform the output of the rewrite procedure into an SQL
    private final DatalogProgram programAfterUnfolding;
    private final DatalogProgram programAfterRewriting;
    
    public static class Builder{
	// Required parameters
	private final long rewritingTime;
	private final long unfoldingTime;
	
	// Optional parameters
	private DatalogProgram programAfterRewriting = null;
	private DatalogProgram programAfterUnfolding = null;
	
	
	// Mandatory Constructor
	public Builder( long unfoldingTime, long rewritingTime ){
	    this.unfoldingTime = unfoldingTime;
	    this.rewritingTime = rewritingTime;
	}
	
	public Builder programAfterRewriting( DatalogProgram programAfterRewriting ){
	    this.programAfterRewriting = programAfterRewriting;
	    return this;
	}
	
	public Builder programAfterUnfolding( DatalogProgram programAfterUnfolding ){
	    this.programAfterUnfolding = programAfterUnfolding;
	    return this;
	}
	
	public OntopBenchmark build(){
	    OntopBenchmark instance = new OntopBenchmark(this);
	    return instance;
	}
    }
    
    private OntopBenchmark( Builder builder ){
	this.rewritingTime = builder.rewritingTime;
	this.unfoldingTime = builder.unfoldingTime;
	 
	this.programAfterRewriting = builder.programAfterRewriting;
	this.programAfterUnfolding = builder.programAfterUnfolding;
    }
 
    /**
     * 
     * @return Time spent in the rewrite procedure
     */
    public long getRewritingTime(){
	return this.rewritingTime;
    }

    /**
     * 
     * @return Time spent to transform the output of the rewrite procedure into an SQL
     */
    public long getUnfoldingTime(){

	return this.unfoldingTime;
    }
    public int getUCQSizeAfterRewriting() {
	int result = 0;
	if( sizesCollected() )
	    result = this.programAfterRewriting.getRules().size();

	return result;
    }

    public int getMinQuerySizeAfterRewriting() {
	int toReturn = Integer.MAX_VALUE;
	if( sizesCollected() ){
	    List<CQIE> rules = programAfterRewriting.getRules();
	    for (CQIE rule : rules) {
		int querySize = getBodySize(rule.getBody());
		if (querySize < toReturn) {
		    toReturn = querySize;
		}
	    }
	}
	return toReturn;
    }

    public int getMaxQuerySizeAfterRewriting() {
	int toReturn = Integer.MIN_VALUE;
	if( sizesCollected() ){
	    List<CQIE> rules = programAfterRewriting.getRules();
	    for (CQIE rule : rules) {
		int querySize = getBodySize(rule.getBody());
		if (querySize > toReturn) {
		    toReturn = querySize;
		}
	    }
	}
	return toReturn;
    }

    public int getUCQSizeAfterUnfolding() {
	int result = 0;
	if( sizesCollected() )
	    result = programAfterUnfolding.getRules().size();

	return result;
    }

    public int getMinQuerySizeAfterUnfolding() {
	int toReturn = Integer.MAX_VALUE;
	if( sizesCollected() ){
	    List<CQIE> rules = programAfterUnfolding.getRules();
	    for (CQIE rule : rules) {
		int querySize = getBodySize(rule.getBody());
		if (querySize < toReturn) {
		    toReturn = querySize;
		}
	    }
	}
	return (toReturn == Integer.MAX_VALUE) ? 0 : toReturn;
    }

    public int getMaxQuerySizeAfterUnfolding() {
	int toReturn = Integer.MIN_VALUE;
	if( sizesCollected() ){
	    List<CQIE> rules = programAfterUnfolding.getRules();
	    for (CQIE rule : rules) {
		int querySize = getBodySize(rule.getBody());
		if (querySize > toReturn) {
		    toReturn = querySize;
		}
	    }
	}
	return (toReturn == Integer.MIN_VALUE) ? 0 : toReturn;
    }    	
    
    private boolean sizesCollected(){
	return this.programAfterRewriting != null && this.programAfterUnfolding != null 
		&& this.programAfterRewriting.getRules() != null && this.programAfterUnfolding.getRules() != null;
    }
    
    private int getBodySize(List<? extends Function> atoms) {
	int counter = 0;
	for (Function atom : atoms) {
	    Predicate predicate = atom.getFunctionSymbol();
	    if (predicate instanceof AtomPredicate) {
		counter++;
	    }
	}
	return counter;
    }
};
