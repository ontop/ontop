package it.unibz.inf.ontop.planning.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unibz.inf.ontop.planning.datatypes.Restriction;

/**
 * Let q1 JOIN ... JOIN qn be a JUCQ. This class keeps
 * track of a list unf(q1), ..., unf(qn), where unf(qi) is 
 * the Restriction result of unfolding the fragment qi.
 * 
 * CONTRACT: The signatures for the unfoldings unf(qi) should
 * be joinable. // TODO Rephrase better
 * 
 * @author Davide Lanti
 *
 */
public class CombinationRestriction{
    
    private final List<Restriction> restrictions;
    
    CombinationRestriction( List<Restriction> restrictions){
	this.restrictions = new ArrayList<>(restrictions);
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
    
    @Override
    public String toString(){
	return this.restrictions.toString();
    }
}