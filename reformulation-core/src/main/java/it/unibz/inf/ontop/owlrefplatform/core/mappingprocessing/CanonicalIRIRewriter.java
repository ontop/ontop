package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.Substitution;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.SubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.SubstitutionUtilities;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Rewrite the mappings to use the canonical iri
 *
 */
public class CanonicalIRIRewriter {

    //rewritten mappings
    private List<CQIE> outputMappings;

    // original uri     -> canonical iri
    private Map<ValueConstant, String> can_iri_rename;

    // original uri     -> canonical iri
    private Map<ValueConstant, ValueConstant> can_iri_map;

    private Map<ValueConstant, CQIE> uri_mapping_map;

    private Map<ValueConstant, List<Term>> uri_column_map;

    private static final Logger log = LoggerFactory.getLogger(CanonicalIRIRewriter.class);

    private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();


    //rewrite all the URI of the mappings with canonical iri if defined
    public List<CQIE> buildCanonicalSameAsMappings(List<CQIE> mappings) {

        outputMappings = new ArrayList<>();
        can_iri_rename = new HashMap<>();
        can_iri_map = new HashMap<>();
        uri_mapping_map = new HashMap<>();
        uri_column_map = new HashMap<>();

        analyzeCanonicalIRIMappings(mappings);

        // When no Canonical IRI is used, do nothing
        if (can_iri_map.isEmpty()) {
            return mappings;
        }

        for (CQIE mapping : mappings) {

            Optional<CQIE> newMapping = Optional.empty();

            Function head = mapping.getHead();

            Predicate predicate = head.getFunctionSymbol();

            if (predicate.isCanonicalIRIProperty()) {
                // we throw away this mapping
                continue;
            }

            if (predicate.isObjectProperty()) {

                Term subjectURI = head.getTerm(0);

                Term templateSubURI = null;

                //if subjectURI is an IRI get canonicalIRI
                if (subjectURI instanceof Function) {
                    templateSubURI = ((Function) subjectURI).getTerm(0);
                }


                if (templateSubURI != null && can_iri_map.containsKey(templateSubURI)) {

                    CanonicalURIMapping canonicalsURIMapping = new CanonicalURIMapping(mapping, (Function) subjectURI).create();

                    final Function newHead = canonicalsURIMapping.getNewHeadTerm();
                    List<Function> newsURIBody = canonicalsURIMapping.getNewURIBody();

                    newMapping = Optional.of(fac.getCQIE(fac.getFunction(predicate, newHead, head.getTerm(1)), newsURIBody));

                    Function headNewMapping = newMapping.get().getHead();

                    Term objectURI = headNewMapping.getTerm(1);

                    //if objectURI is an IRI get canonicalIRI
                    if (objectURI instanceof Function) {

                        Function objectURINewMapping = (Function) objectURI;

                        Term templateObjURINewMapping = objectURINewMapping.getTerm(0);


                        if (can_iri_map.containsKey(templateObjURINewMapping)) {

                            CanonicalURIMapping canonicaloURIMapping = new CanonicalURIMapping(newMapping.get(), objectURINewMapping).create();
                            final Function newObjectHead = canonicaloURIMapping.getNewHeadTerm();
                            List<Function> newoURIBody = canonicaloURIMapping.getNewURIBody();

                            newMapping = Optional.of(fac.getCQIE(fac.getFunction(predicate, headNewMapping.getTerm(0), newObjectHead), newoURIBody));
                        }

                    }

                // if subjectURI does not have a canonicalIRI
                } else {
                    //if objectURI is an IRI  get canonicalIRI
                    Term objectURI = head.getTerm(1);

                    if (objectURI instanceof Function) {
                        Term templateObjURI = ((Function) objectURI).getTerm(0);


                        if (can_iri_map.containsKey(templateObjURI)) {
                            CanonicalURIMapping canonicaloURIMapping = new CanonicalURIMapping(mapping, (Function) objectURI).create();
                            final Function newObjectHead = canonicaloURIMapping.getNewHeadTerm();
                            List<Function> newoURIBody = canonicaloURIMapping.getNewURIBody();

                            newMapping = Optional.of(fac.getCQIE(fac.getFunction(predicate, head.getTerm(0), newObjectHead), newoURIBody));

                        }
                    }
                }


            } else if (predicate.isDataProperty()) {
                Term subjectURI = head.getTerm(0);

                //if subjectURI is an IRI get canonicalIRI
                if (subjectURI instanceof Function) {

                    Term templateURI = ((Function) subjectURI).getTerm(0);

                    if (can_iri_map.containsKey(templateURI)) {
                        CanonicalURIMapping canonicalURIMapping = new CanonicalURIMapping(mapping, (Function) subjectURI).create();
                        final Function newObjectHead = canonicalURIMapping.getNewHeadTerm();
                        List<Function> newURIBody = canonicalURIMapping.getNewURIBody();

                        newMapping = Optional.of(fac.getCQIE(fac.getFunction(predicate, newObjectHead, head.getTerm(1)), newURIBody));

                    }

                }

            } else if (predicate.isClass()) {

                Term subjectURI = head.getTerm(0);

                //if subjectURI is an IRI get canonicalIRI
                if (subjectURI instanceof Function) {

                    Term templateURI = ((Function) subjectURI).getTerm(0);

                    if (can_iri_map.containsKey(templateURI)) {

                        CanonicalURIMapping canonicalURIMapping = new CanonicalURIMapping(mapping, (Function) subjectURI).create();
                        final Function newSubjectTerm = canonicalURIMapping.getNewHeadTerm();
                        List<Function> newURIBody = canonicalURIMapping.getNewURIBody();

                        newMapping = Optional.of(fac.getCQIE(fac.getFunction(predicate, newSubjectTerm), newURIBody));

                    }
                }

            }

//            head.getTerms().stream().map(term -> canonicalTerm(term)).collect(Collectors.toList());

            outputMappings.add(newMapping.orElse(mapping.clone()));

        }

        return outputMappings;

    }


    private class CanonicalResult {
        Term headTerm;
        List<Function> bodyFunctions;
    }


    private  CanonicalResult canonicalTerm(Term term) {
        return null;
    }


    //get the canonicalIRIs from the original mappings
    private void analyzeCanonicalIRIMappings(List<CQIE> mappings) {

        for (CQIE mapping : mappings) {

            Function head = mapping.getHead();

            Predicate predicate = head.getFunctionSymbol();

            if (predicate.isCanonicalIRIProperty()) { // we check for ontop:is_canonical_iri

                //rename all the variables to avoid conflicts while merging the mappings
                Set<Variable> variables = mapping.getReferencedVariables();


                Function headURI = (Function) head.getTerm(0);
                String rename = can_iri_rename.get(headURI.getTerm(0));
                if(rename ==null){
                    rename = "_canonical"+can_iri_rename.size();
                    can_iri_rename.put((ValueConstant) headURI.getTerm(0), rename);
                }

                final String finalRename = rename;
                Map<Variable, Term> map = variables.stream()
                        .collect(Collectors.toMap(
                                var -> var,
                                var -> fac.getVariable(var.getName() + finalRename)));

                Substitution substitution = new SubstitutionImpl(map);
                CQIE canonicalMapping = SubstitutionUtilities.applySubstitution(mapping, substitution, true);

                //get template uri and columns of canonical uri and object uri
                Function canonHead = canonicalMapping.getHead();

                Function canonicalTerm = (Function) canonHead.getTerm(0);
                Function objectTerm = (Function) canonHead.getTerm(1);

                ValueConstant canonURI = (ValueConstant) canonicalTerm.getTerm(0);
                List<Term> canonURIColumns = canonicalTerm.getTerms().subList(1, canonicalTerm.getTerms().size());

                ValueConstant objectURI = (ValueConstant) objectTerm.getTerm(0);
                List<Term> objectURIColumns = objectTerm.getTerms().subList(1, objectTerm.getTerms().size());

                //store the canonical iri of the object uri
                can_iri_map.put(objectURI, canonURI);

                //store the columns
                uri_column_map.put(objectURI, objectURIColumns);
                uri_column_map.put(canonURI, canonURIColumns);
                //store the renamed mapping
                uri_mapping_map.put(objectURI, canonicalMapping);
            }
        }
    }

    private class CanonicalURIMapping {
        private CQIE mapping;
        private Term templateURI;
        private Function uriTerm;
        private List<Term> newURITerms;
        private List<Function> newURIBody;

        public CanonicalURIMapping(CQIE mapping, Function uriTerm) {
            this.mapping = mapping;
            this.uriTerm = uriTerm;
            this.templateURI = uriTerm.getTerm(0);

        }

//        public List<Term> getNewURITerms() {
//            return newURITerms;
//        }

        public Function getNewHeadTerm() {return fac.getUriTemplate(newURITerms);}

        //public List<Function>

        public List<Function> getNewURIBody() {
            return newURIBody;
        }

//        substitute the old uri with the new canonical iri
        public CanonicalURIMapping create() {

            //get the canonical version of the uri and useful columns
            ValueConstant canonicalTemplateURI = can_iri_map.get(templateURI);
            newURITerms = new ArrayList<>();
            List<Term> termsURI = new ArrayList<>();
            newURITerms.add(canonicalTemplateURI);
            List<Term> columnsCanonURI = uri_column_map.get(canonicalTemplateURI);
            newURITerms.addAll(columnsCanonURI);

            //get template uri and table column name
            List<Term> columnsURI = uri_column_map.get(templateURI);
            termsURI.add(templateURI);
            termsURI.addAll(columnsURI);
            Function target = fac.getUriTemplate(termsURI);

            //get substitution
            Substitution subs = UnifierUtilities.getMGU(uriTerm, target);
            //Substitution subs = UnifierUtilities.getMGU(target, uriTerm);
            CQIE newMapping = SubstitutionUtilities.applySubstitution(mapping, subs, true);
            newURIBody = new ArrayList<>();
            CQIE canonicalMapping = uri_mapping_map.get(templateURI);
            newURIBody.addAll(canonicalMapping.getBody());
            newMapping.getBody().stream().filter(m -> !newURIBody.contains(m)).forEach(m -> newURIBody.add(m));

            return this;
        }
    }
}
