package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestUnfolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Rewrite the sameAs mappings to use the canonical uri
 */
public class SameAsRewriting {

    //rewritten mappings
    private static List<CQIE> canonicalMappings = new LinkedList<>();

    private static Map<ValueConstant, ValueConstant> can_uri_map;

    private static Multimap<ValueConstant, Function> tableColumnsMultimap;

    private static final Logger log = LoggerFactory.getLogger(QuestUnfolder.class);

    private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();


    public static List<CQIE> getCanonicalSameAsMappings(List<CQIE> mappings) {

        canonicalMappings = new LinkedList<>();
        can_uri_map = new HashMap<>();
        tableColumnsMultimap = HashMultimap.create();

        for (CQIE mapping : mappings){

            Function head = mapping.getHead();

            Predicate predicate = head.getFunctionSymbol();

            if (predicate.isSameAsProperty()) { // we check for owl:sameas

                Function canonicalTerm = (Function) head.getTerm(0);
                Function objectTerm = (Function) head.getTerm(1);

                //get template uri and columns
                ValueConstant canonURI = (ValueConstant) canonicalTerm.getTerm(0);
                List<Term> canonURIColumns = canonicalTerm.getTerms().subList(1, canonicalTerm.getTerms().size());

                ValueConstant objectURI = (ValueConstant) objectTerm.getTerm(0);
                List<Term> objectURIColumns = objectTerm.getTerms().subList(1, objectTerm.getTerms().size());

                can_uri_map.put(objectURI, canonURI);

                List<Function> body = mapping.getBody();

                //get the table names
                for (Function bodyFunc : body){

                    if(bodyFunc.getTerms().containsAll(canonURIColumns)){

                        if(!tableColumnsMultimap.containsKey(canonURI)) {

                            tableColumnsMultimap.put(canonURI, fac.getFunction(bodyFunc.getFunctionSymbol(), canonURIColumns));
                        }

                        tableColumnsMultimap.put(objectURI, fac.getFunction(bodyFunc.getFunctionSymbol(), objectURIColumns));
                    }
                }

            }
        }


        return canonicalMappings;

    }
}
