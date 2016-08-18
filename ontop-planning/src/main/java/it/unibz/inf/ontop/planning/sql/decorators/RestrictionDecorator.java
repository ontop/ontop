package it.unibz.inf.ontop.planning.sql.decorators;

import it.unibz.inf.ontop.planning.datatypes.Restriction;
import it.unibz.inf.ontop.planning.datatypes.Signature;
import it.unibz.krdb.obda.model.DatalogProgram;

public class RestrictionDecorator {
    private final Restriction component;

    protected RestrictionDecorator( Restriction component ) {

	this.component = component;
    }

    public DatalogProgram getDLog(){
	return this.component.getDLog();
    }
    
    public Signature getSignature(){
	return this.component.getSignature();
    }

    @Override
    public String toString(){
	return this.component.toString() + "\n";
    }
};
