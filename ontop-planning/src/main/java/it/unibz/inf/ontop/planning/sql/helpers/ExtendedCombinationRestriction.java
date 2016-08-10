package it.unibz.inf.ontop.planning.sql.helpers;

import java.util.Collections;
import java.util.List;

public class ExtendedCombinationRestriction {
    
    private List<ExtendedRestriction> restrictions;

    public ExtendedCombinationRestriction( List<ExtendedRestriction> extendedRestrictions ){
	this.restrictions = extendedRestrictions;
    }
    
    List<ExtendedRestriction> getRestrictions(){
	return Collections.unmodifiableList(this.restrictions);
    }
    
    int numFragments(){
	return this.getRestrictions().size();
    }
    
    ExtendedRestriction getFragmentOfIndex( int index ){
	return this.restrictions.get(index);
    }
}
