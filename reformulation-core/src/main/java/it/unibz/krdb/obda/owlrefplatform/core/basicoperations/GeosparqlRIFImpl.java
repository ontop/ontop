package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.PredicateImpl;
import it.unibz.krdb.obda.model.impl.URITemplatePredicateImpl;

import java.util.List;

import org.h2.engine.FunctionAlias;

import net.sf.jsqlparser.statement.select.FunctionItem;

public class GeosparqlRIFImpl {
	
	public static void applyRIFrules(CQIE result, DatalogProgram pr) {

		List<Function> body = result.getBody();
		

		// collecting all equalities as substitutions 
		
		 OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
		  

		for (int i = 0; i < body.size(); i++) {
			Function atom = body.get(i);
			Function fuc;
			Predicate p = atom.getFunctionSymbol();
			List<Term> terms = atom.getTerms();

			
			if(terms.size() < 2)
				continue; 
			
			Predicate aswktPredicate =  dfac.getObjectPropertyPredicate(OBDAVocabulary.asWKTPredicate );
			Variable geomVar1 = dfac.getVariable(atom.getTerm(0).toString()+"serial");
			Variable geomVar2 = dfac.getVariable(atom.getTerm(1).toString()+"serial");
			Variable hasGeomVar1 = dfac.getVariable(atom.getTerm(0).toString()+"geom");
			Variable hasGeomVar2 = dfac.getVariable(atom.getTerm(1).toString()+"geom");
			Predicate defaultGeomPred =  dfac.getObjectPropertyPredicate(OBDAVocabulary.defaultGeomPredicate );
	
			if (p.getName().equals(OBDAVocabulary.geoContains)){
				fuc =  dfac.getFunctionSpatialContains(geomVar1, geomVar2);
			}else if (p.getName().equals(OBDAVocabulary.geoOverlaps)){
				fuc = dfac.getFunctionOverlaps(geomVar1, geomVar2);
			}else if (p.getName().equals(OBDAVocabulary.geoEquals)){
				fuc = dfac.getFunctionSpatialEquals(geomVar1, geomVar2);
			}else if (p.getName().equals(OBDAVocabulary.geoIntersects)){
				fuc = dfac.getFunctionSpatialIntersects(geomVar1, geomVar2);
			}else if (p.getName().equals(OBDAVocabulary.geoTouches)){
				fuc = dfac.getFunctionSpatialTouches(geomVar1, geomVar2);
			}else if (p.getName().equals(OBDAVocabulary.geoWithin)){
				fuc = dfac.getFunctionContains(geomVar1, geomVar2);
			}else if (p.getName().equals(OBDAVocabulary.geoCrosses)){
				fuc = dfac.getFunctionSpatialCrosses(geomVar1, geomVar2);
			}else {
				continue ;
			}
			
	
     /*       if (atom.getFunctionSymbol() == OBDAVocabulary.EQ) {
                if (!mgu.composeTerms(atom.getTerm(0), atom.getTerm(1)))
                    continue;*/

            body.set(i, fuc);
            CQIE origResult = result.clone();
            CQIE newRule = origResult.clone();           
            body.add(dfac.getFunction(aswktPredicate, terms.get(0), geomVar1));
            body.add(dfac.getFunction(aswktPredicate, terms.get(1), geomVar2)); //updated rule
            List<Function> newbody = newRule.getBody();
            newbody.add( dfac.getFunction(defaultGeomPred, terms.get(0), hasGeomVar1)); //adding feature-feature rule
            newbody.add(dfac.getFunction(defaultGeomPred, terms.get(1), hasGeomVar2));
            newbody.add(dfac.getFunction(aswktPredicate, hasGeomVar1, geomVar1));
            newbody.add(dfac.getFunction(aswktPredicate, hasGeomVar2, geomVar2));       
            pr.appendRule(newRule);
            newRule = origResult.clone();  //adding feature-geometry rule
            newbody = newRule.getBody();
            newbody.add( dfac.getFunction(defaultGeomPred, terms.get(0), hasGeomVar1));
            newbody.add(dfac.getFunction(aswktPredicate, hasGeomVar1, geomVar1));
            newbody.add(dfac.getFunction(aswktPredicate, terms.get(1), geomVar2));
            pr.appendRule(newRule);
            newRule=null;
            newRule = origResult.clone(); //adding geometry-feature rule
            newbody = newRule.getBody();
            newbody.add( dfac.getFunction(defaultGeomPred, terms.get(1), hasGeomVar2));
            newbody.add(dfac.getFunction(aswktPredicate, hasGeomVar2, geomVar2));
            newbody.add(dfac.getFunction(aswktPredicate, terms.get(0), geomVar1));
            pr.appendRule(newRule);
            }
            //search for nested equalities in AND function
/*            else if (atom.getFunctionSymbol() == OBDAVocabulary.AND) {
                nestedEQSubstitutions(atom, mgu);

                //we remove the function if empty because all its terms were equalities
                if (atom.getTerms().isEmpty()) {
                    body.remove(i);
                    i--;
                }
                else {
                    //if there is only a term left we remove the conjunction
                    if (atom.getTerms().size() == 1) {
                        body.set(i, (Function) atom.getTerm(0));
                    }
                }
            }
        }

		SubstitutionUtilities.applySubstitution(result, mgu, false);
*/	}

}
