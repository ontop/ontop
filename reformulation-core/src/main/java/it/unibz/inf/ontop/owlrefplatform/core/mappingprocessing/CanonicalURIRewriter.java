package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestUnfolder;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.Substitution;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.SubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.SubstitutionUtilities;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.UnifierUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Rewrite the sameAs mappings to use the canonical uri
 */
public class CanonicalURIRewriter {

    //rewritten mappings
    private List<CQIE> outputMappings;

    private Map<ValueConstant, ValueConstant> can_uri_map;

    private Map<ValueConstant, CQIE> uri_mapping_map;

    private Map<ValueConstant, List<Term>> uri_column_map;

    private static final Logger log = LoggerFactory.getLogger(QuestUnfolder.class);

    private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();


    //rewrite all the URI of the mappings with canonical iri if defined
    public List<CQIE> buildCanonicalSameAsMappings(List<CQIE> mappings) {

        outputMappings = new ArrayList<>();
        can_uri_map = new HashMap<>();
        uri_mapping_map = new HashMap<>();
        uri_column_map = new HashMap<>();


        analyzeCanonicalIRIMappings(mappings);

        for (CQIE mapping : mappings) {

            Function head = mapping.getHead();

            Predicate predicate = head.getFunctionSymbol();

            if(predicate.isCanonicalIRIProperty()){
                continue;
            }

            if (predicate.isObjectProperty()) {

                Function subjectURI = (Function) head.getTerm(0);

                Function objectURI = (Function) head.getTerm(1);

                Term templatesURI = subjectURI.getTerm(0);
                Term templateoURI = objectURI.getTerm(0);

                if (can_uri_map.containsKey(templatesURI)) {

                    CanonicalURIMapping canonicalsURIMapping = new CanonicalURIMapping(mapping, head, templatesURI).create();
                    List<Term> newsURITerms = canonicalsURIMapping.getNewURITerms();
                    List<Function> newsURIBody = canonicalsURIMapping.getNewURIBody();

                    CQIE newMapping = fac.getCQIE(fac.getFunction(predicate,  fac.getUriTemplate(newsURITerms), head.getTerm(1)), newsURIBody);

                    Function headNewMapping = newMapping.getHead();
                    Function objectURINewMapping = (Function) headNewMapping.getTerm(1);

                    Term templateoURINewMapping = objectURINewMapping.getTerm(0);

                    if (can_uri_map.containsKey(templateoURINewMapping)) {

                        CanonicalURIMapping canonicaloURIMapping = new CanonicalURIMapping(newMapping, headNewMapping, templateoURINewMapping).create();
                        List<Term> newoURITerms = canonicaloURIMapping.getNewURITerms();
                        List<Function> newoURIBody = canonicaloURIMapping.getNewURIBody();

                        newMapping = fac.getCQIE(fac.getFunction(predicate, head.getTerm(0), fac.getUriTemplate(newoURITerms)), newoURIBody);
                    }

                    outputMappings.add(newMapping);

                } else if (can_uri_map.containsKey(templateoURI)) {

                    CanonicalURIMapping canonicaloURIMapping = new CanonicalURIMapping(mapping, head, templateoURI).create();
                    List<Term> newoURITerms = canonicaloURIMapping.getNewURITerms();
                    List<Function> newoURIBody = canonicaloURIMapping.getNewURIBody();

                    CQIE newMapping = fac.getCQIE(fac.getFunction(predicate, head.getTerm(0), fac.getUriTemplate(newoURITerms)), newoURIBody);
                    outputMappings.add(newMapping);

                }


            } else if (predicate.isDataProperty()) {

                Function subjectURI = (Function) head.getTerm(0);

                Term templateURI = subjectURI.getTerm(0);
                if (can_uri_map.containsKey(templateURI)) {
                    CanonicalURIMapping canonicalURIMapping = new CanonicalURIMapping(mapping, head, templateURI).create();
                    List<Term> newURITerms = canonicalURIMapping.getNewURITerms();
                    List<Function> newURIBody = canonicalURIMapping.getNewURIBody();

                    CQIE newMapping = fac.getCQIE(fac.getFunction(predicate,  fac.getUriTemplate(newURITerms), head.getTerm(1)), newURIBody);
                    outputMappings.add(newMapping);
                }

            } else if (predicate.isClass()) {

                Function subjectURI = (Function) head.getTerm(0);

                Term templateURI = subjectURI.getTerm(0);

                if (can_uri_map.containsKey(templateURI)) {
                    List<Term> newURITerms = new ArrayList<>();
                    ValueConstant canonicalTemplateURI = can_uri_map.get(templateURI);
                    newURITerms.add(canonicalTemplateURI);


                    List<Term> termsURI = new ArrayList<>();

                    List<Term> columnsURI = uri_column_map.get(templateURI);
                    termsURI.add(templateURI);
                    termsURI.addAll(columnsURI);


                    Function target = fac.getUriTemplate(termsURI);
                    Substitution subs = UnifierUtilities.getMGU(target,(Function)head.getTerm(0));

                    CQIE newMapping = SubstitutionUtilities.applySubstitution(uri_mapping_map.get(templateURI), subs, true);

                    List<Function> newURIBody = new ArrayList<>();
                    newURIBody.addAll(newMapping.getBody());

                    newMapping = fac.getCQIE(fac.getFunction(predicate, fac.getUriTemplate(newURITerms)), newURIBody);
                    outputMappings.add(newMapping);
                }


            } else {


                outputMappings.add(mapping);
            }
        }


        return outputMappings;

    }

//    private CQIE rewriteMappingWithSubjectInCanonicalURI(CQIE mapping, Function head, Predicate predicate, Term templateURI) {
//        ValueConstant canonicalTemplateURI = can_uri_map.get(templateURI);
//        List<Term> newURITerms = new ArrayList<>();
//        List<Term> termsURI = new ArrayList<>();
//        newURITerms.add(canonicalTemplateURI);
//        List<Term> columnsURI = uri_column_map.get(templateURI);
//        termsURI.add(templateURI);
//        termsURI.addAll(columnsURI);
//        List<Term> columnsCanonURI = uri_column_map.get(canonicalTemplateURI);
//        newURITerms.addAll(columnsCanonURI);
//
//        Function target = fac.getUriTemplate(termsURI);
//        Substitution subs = UnifierUtilities.getMGU(target,(Function)head.getTerm(0));
//
//        CQIE newMapping = SubstitutionUtilities.applySubstitution(uri_mapping_map.get(templateURI), subs, true);
//
//        List<Function> newURIBody = new ArrayList<>();
//        newURIBody.addAll(newMapping.getBody());
//        newURIBody.addAll(mapping.getBody());
//
//        return fac.getCQIE(fac.getFunction(predicate, fac.getUriTemplate(newURITerms), head.getTerm(1)), newURIBody);
//    }


    //get the canonicalIRIs
    private void analyzeCanonicalIRIMappings(List<CQIE> mappings) {

        for (CQIE mapping : mappings) {

            Function head = mapping.getHead();

            Predicate predicate = head.getFunctionSymbol();

            if (predicate.isCanonicalIRIProperty()) { // we check for ontop:is_canonical_iri


                //rename all the variables to avoid conflicts while merging the mappings
                Set<Variable> variables = mapping.getReferencedVariables();

                Map<Variable, Term> map = new HashMap<>();
                variables.forEach(variable -> map.put(variable, fac.getVariable(variable.getName() + "_canonical")));

                SubstitutionImpl substitution = new SubstitutionImpl(map);

                CQIE canonicalMapping = SubstitutionUtilities.applySubstitution(mapping, substitution, true);

                Function canonHead = canonicalMapping.getHead();

                Function canonicalTerm = (Function) canonHead.getTerm(0);
                Function objectTerm = (Function) canonHead.getTerm(1);

                //get template uri and columns
                ValueConstant canonURI = (ValueConstant) canonicalTerm.getTerm(0);
                List<Term> canonURIColumns = canonicalTerm.getTerms().subList(1, canonicalTerm.getTerms().size());

                ValueConstant objectURI = (ValueConstant) objectTerm.getTerm(0);
                List<Term> objectURIColumns = objectTerm.getTerms().subList(1, objectTerm.getTerms().size());

                can_uri_map.put(objectURI, canonURI);

                //get column used in the template URI for the canonical uri and the object and keep the renamed mapping

                uri_column_map.put(objectURI, objectURIColumns);
                uri_column_map.put(canonURI, canonURIColumns);
                uri_mapping_map.put(objectURI, canonicalMapping);


            }
        }
    }

    private class CanonicalURIMapping {
        private CQIE mapping;
        private Function head;
        private Term templateURI;
        private List<Term> newURITerms;
        private List<Function> newURIBody;

        public CanonicalURIMapping(CQIE mapping, Function head, Term templateURI) {
            this.mapping = mapping;
            this.head = head;
            this.templateURI = templateURI;
        }

        public List<Term> getNewURITerms() {
            return newURITerms;
        }

        public List<Function> getNewURIBody() {
            return newURIBody;
        }

        public CanonicalURIMapping create() {
            ValueConstant canonicalTemplateURI = can_uri_map.get(templateURI);
            newURITerms = new ArrayList<>();
            List<Term> termsURI = new ArrayList<>();
            newURITerms.add(canonicalTemplateURI);
            List<Term> columnsURI = uri_column_map.get(templateURI);
            termsURI.add(templateURI);
            termsURI.addAll(columnsURI);
            List<Term> columnsCanonURI = uri_column_map.get(canonicalTemplateURI);
            newURITerms.addAll(columnsCanonURI);

            Function target = fac.getUriTemplate(termsURI);
            Substitution subs = UnifierUtilities.getMGU(target,(Function)head.getTerm(0));

            CQIE newMapping = SubstitutionUtilities.applySubstitution(uri_mapping_map.get(templateURI), subs, true);
            newURIBody = new ArrayList<>();
            newURIBody.addAll(newMapping.getBody());
            mapping.getBody().stream().filter(m -> !newURIBody.contains(m)).forEach(m -> newURIBody.add(m));

            return this;
        }
    }
}
