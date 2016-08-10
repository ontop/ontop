package it.unibz.inf.ontop.planning.sql;

import java.util.Collections;
import java.util.List;

import it.unibz.inf.ontop.planning.datatypes.Restriction;

public class CombinationRestriction{
    
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