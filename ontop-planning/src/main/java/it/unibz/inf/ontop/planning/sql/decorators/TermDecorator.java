package it.unibz.inf.ontop.planning.sql.decorators;

import it.unibz.krdb.obda.model.Term;

public class TermDecorator implements Term {
    
    /**
     * 
     */
    private static final long serialVersionUID = 5976108717983161763L;
    protected Term component;
    
    TermDecorator( Term component ){
	this.component = component;
    }
    
    @Override
    public Term clone(){
	return component.clone();
    }
    
    
}
