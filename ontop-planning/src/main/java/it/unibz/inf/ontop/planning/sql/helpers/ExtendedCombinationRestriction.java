package it.unibz.inf.ontop.planning.sql.helpers;

import java.util.Collections;
import java.util.List;

public class ExtendedCombinationRestriction {
    
    private List<ExtendedRestriction> restrictions;

    public ExtendedCombinationRestriction( List<ExtendedRestriction> extendedRestrictions ){
	this.restrictions = extendedRestrictions;
    }
    
    public List<ExtendedRestriction> getRestrictions(){
	return Collections.unmodifiableList(this.restrictions);
    }
    
    public int numFragments(){
	return this.getRestrictions().size();
    }
    
    public ExtendedRestriction getFragmentOfIndex( int index ){
	return this.restrictions.get(index);
    }
    
    @Override
    public String toString(){
	return this.restrictions.toString();
    }
}
