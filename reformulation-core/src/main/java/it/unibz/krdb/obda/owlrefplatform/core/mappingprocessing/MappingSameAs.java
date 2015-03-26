package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.*;

public class MappingSameAs {

    private List<CQIE> rules;

    private Map<ValueConstant, ValueConstant> sameAsMap;

    private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

    private Set<Predicate> dataPropertiesMapped;

    private Set<Predicate> objectPropertiesMapped;
    /**
     * Constructs a mapping containing the URI of owl:sameAs
     *
     */
    public MappingSameAs(List<CQIE> rules) throws OBDAException{

        this.rules =  rules;

        dataPropertiesMapped = new HashSet<Predicate>();
        objectPropertiesMapped = new HashSet<Predicate>();

        getSameAs();

        getPropertiesWithSameAs();
    }

    /**
     * method that gets the uris from the mappings of same as and store it in a map
     */
    private void getSameAs() throws OBDAException {

    	 sameAsMap= new HashMap<>();

        for (CQIE rule : rules) {

            Function atom = rule.getHead();

            Predicate predicate = atom.getFunctionSymbol();
            if (predicate.getArity() == 2 && predicate.getName().equals("http://www.w3.org/2002/07/owl#sameAs")) { // we check for owl same as


                Term term1 = atom.getTerm(0);
                Term term2 = atom.getTerm(1);

                if (term1 instanceof Function && term2 instanceof  Function ){

                    Function uri1 = (Function) term1;
                    ValueConstant prefix1 = (ValueConstant) uri1.getTerm(0);

                    Function uri2 = (Function) term2;
                    ValueConstant prefix2 = (ValueConstant) uri2.getTerm(0);
                    sameAsMap.put(prefix1, prefix2);


                }
                else
                    throw new OBDAException("owl:samesAs is not built properly");




            }

        }


    }

    /*
    get data and object predicate that needs the same as
     */
    private void getPropertiesWithSameAs() throws OBDAException {



        for (CQIE rule : rules) {

            Function atom = rule.getHead();

            Predicate functionSymbol = atom.getFunctionSymbol();



            if (atom.getArity() == 2) {


                Function term1 = (Function) atom.getTerm(0);
                Function term2 = (Function) atom.getTerm(1);
                boolean t1uri = (term1.getFunctionSymbol() instanceof URITemplatePredicate);
                boolean t2uri = (term2.getFunctionSymbol() instanceof URITemplatePredicate);


                if (t1uri && t2uri ) {

                    //TODO use constants
                    if(!functionSymbol.getName().equals("http://www.w3.org/2002/07/owl#sameAs")){

                        Term prefix1 = term1.getTerm(0);
                        Term prefix2 =  term2.getTerm(0);

                        if (sameAsMap.containsKey(prefix1) ||  (sameAsMap.containsKey(prefix2))) {

                            objectPropertiesMapped.add(functionSymbol);

                        }



                    }


                }
                else
                {

                    Term prefix1 = term1.getTerm(0);


                    if (sameAsMap.containsKey(prefix1)) {
//                        Function dataProperty = fac.getFunction(functionSymbol,prefix1, term2);
                        dataPropertiesMapped.add(functionSymbol);
                    }


                }

            }
//            else{
//
//                Function term1 = (Function) atom.getTerm(0);
//                if (term1.getFunctionSymbol() instanceof URITemplatePredicate){
//
//
//                    ValueConstant prefix1 = (ValueConstant) term1.getTerm(0);
//
//
//                    if (sameAsMap.containsKey(prefix1)) {
//                        dataPropertiesMapped.add(functionSymbol);
//                    }
//                }
//
//            }
//                else
//                    throw new OBDAException("error finding owl:sameAs related to " + atom);

        }

    }



    public Set<Predicate> getDataPropertiesWithSameAs(){

        return dataPropertiesMapped;
    }

    public Set<Predicate> getObjectPropertiesWithSameAs(){

        return objectPropertiesMapped;
    }


}

