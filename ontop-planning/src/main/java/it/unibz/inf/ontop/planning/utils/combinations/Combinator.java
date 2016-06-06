package it.unibz.inf.ontop.planning.utils.combinations;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Randomly pick one element from each of n lists. This class
 * calculates all possible ways of doing this.
 * 
 * @author Davide Lanti
 *
 * @param <T> The type of elements in the lists
 */
public class Combinator<T> {
    
    private List<List<T>> fragments;
    private CombinationVisitor<T> visitor;
    
    public Combinator(List<List<T>> fragments, CombinationVisitor<T> visitor){
	this.fragments = fragments;
	this.visitor = visitor;
    }
    
    void combine(int fragIndex, int index, List<T> encounters){
	if( fragIndex == fragments.size() - 1 ){
	    visitor.visit(encounters);
	    encounters.remove(encounters.size()-1);
	}
	else{
	    for( int i = 0; i < fragments.get(fragIndex + 1).size(); ++i ){
		List<T> nextFrag = fragments.get(fragIndex + 1);
		encounters.add( nextFrag.get(i) );
		combine( fragIndex +1, i, encounters ) ;
	    }
	    encounters.remove(encounters.size()-1);
	}
    }
    
    public void combine(){
	List<T> f1 = fragments.get(0);
	
	for( int i = 0; i < f1.size(); ++ i){
	    List<T> encounters = new ArrayList<T>();
	    encounters.add( f1.get(i) );
	    combine( 0, i, encounters );
	}
    }
};