package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.vocabulary.Ontop;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionImpl;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Rewrite the mappings to use the canonical iri
 *
 * WARNING: MUTABLE!!!!
 *
 */
public class CanonicalIRIRewriter {

    //used to recognize if we are working on the subject or object of the mapping target
    private enum Position {

        SUBJECT (0),
        OBJECT (2);

        private final int code;

        Position(int code) {
            this.code = code;
        }

        public int getPosition() {
            return code;
        }

    };


    //rewritten mappings
    private List<CQIE> outputMappings;

    // canonical iri -> suffix for variable renaming
    private Map<RDFLiteralConstant, String> canIriVariablesSuffix;

    private Map<RDFLiteralConstant, CQIE> uriMappingMap;

    private static final Logger log = LoggerFactory.getLogger(CanonicalIRIRewriter.class);

    private final SubstitutionUtilities substitutionUtilities;
    private final TermFactory termFactory;
    private final UnifierUtilities unifierUtilities;
    private final ImmutabilityTools immutabilityTools;

    public CanonicalIRIRewriter(SubstitutionUtilities substitutionUtilities, TermFactory termFactory,
                                UnifierUtilities unifierUtilities, ImmutabilityTools immutabilityTools) {
        this.substitutionUtilities = substitutionUtilities;
        this.termFactory = termFactory;
        this.unifierUtilities = unifierUtilities;
        this.immutabilityTools = immutabilityTools;
    }


    //rewrite all the URI of the mappings with canonical iri if defined
    public List<CQIE> buildCanonicalIRIMappings(List<CQIE> mappings) {

        outputMappings = new ArrayList<>();
        canIriVariablesSuffix = new HashMap<>();;
        uriMappingMap = new HashMap<>();

        //search for obda:isCanonicalIriOf in the mappings
        analyzeCanonicalIRIMappings(mappings);

        // When no Canonical IRI is used, do nothing
        if (uriMappingMap.isEmpty()) {
            return mappings;
        }

        for (CQIE mapping : mappings) {

            Optional<CQIE> newMapping = Optional.empty();

            Function head = mapping.getHead();

            if (isCanonicalProperty(head)) {
                // we throw away this mapping
                continue;
            }

            Term subjectURI = head.getTerm(0);

            Term templateSubURI;
            //if subjectURI is an IRI get canonicalIRI
            if (subjectURI instanceof Function) {

                templateSubURI = ((Function) subjectURI).getTerm(0);

                if (uriMappingMap.containsKey(templateSubURI)) {

                    newMapping = Optional.of(new CanonicalIRIMapping(mapping, (Function) subjectURI, Position.SUBJECT).create());
                }
            }

            if (head.getArity() == 3) {

                CQIE mapping2 = newMapping.orElse(mapping);

                Function headNewMapping = mapping2.getHead();
                Term objectURI = headNewMapping.getTerm(2);

                //if objectURI is an IRI get canonicalIRI
                if (objectURI instanceof Function) {

                    Function objectURINewMapping = (Function) objectURI;
                    Term templateObjURINewMapping = objectURINewMapping.getTerm(0);


                    if (uriMappingMap.containsKey(templateObjURINewMapping)) {

                        newMapping = Optional.of(new CanonicalIRIMapping(mapping2, (Function) objectURI, Position.OBJECT).create());

                    }

                }
            }


            outputMappings.add(newMapping.orElse(mapping.clone()));

        }

        return outputMappings;

    }

    private boolean isCanonicalProperty(Function headAtom) {
        Predicate predicate = headAtom.getFunctionSymbol();

        if (predicate instanceof RDFAtomPredicate) {
            VariableGenerator variableGenerator = new VariableGenerator(headAtom.getVariables(), termFactory);
            ImmutableList<VariableOrGroundTerm> arguments = headAtom.getTerms().stream()
                    .map(immutabilityTools::convertIntoImmutableTerm)
                    .map(t -> t instanceof NonGroundFunctionalTerm ? variableGenerator.generateNewVariable() : t)
                    .map(t -> (VariableOrGroundTerm) t)
                    .collect(ImmutableCollectors.toList());

            return ((RDFAtomPredicate) predicate)
                    .getPropertyIRI(arguments)
                    .filter(Ontop.CANONICAL_IRI::equals)
                    .isPresent();
        }
        else
            return false;
    }


    //get the canonicalIRIs from the original mappings
    private void analyzeCanonicalIRIMappings(List<CQIE> rules) {

        for (CQIE rule : rules) {

            Function head = rule.getHead();

            if (isCanonicalProperty(head)) { // we check for ontop:is_canonical_iri

                //rename all the variables to avoid conflicts while merging the mappings
                Set<Variable> variables = rule.getReferencedVariables();

                Function headURI = (Function) head.getTerm(0);
                RDFLiteralConstant canonicalIRIName = (RDFLiteralConstant) headURI.getTerm(0);

                Function objectTerm = (Function) head.getTerm(2);
                RDFLiteralConstant objectURIName = (RDFLiteralConstant) objectTerm.getTerm(0);

                //get or assign a suffix for each canonicalIRI
                String rename = canIriVariablesSuffix.get(canonicalIRIName);
                if(rename ==null){
                    rename = "_canonical"+ canIriVariablesSuffix.size();
                    canIriVariablesSuffix.put(canonicalIRIName, rename);
                }

                final String finalRename = rename;
                Map<Variable, Term> map = variables.stream()
                        .collect(Collectors.toMap(
                                var -> var,
                                var -> termFactory.getVariable(var.getName() + finalRename)));

                //apply substitution for variables renaming
                Substitution substitution = new SubstitutionImpl(map, termFactory);
                CQIE canonicalMapping = substitutionUtilities.applySubstitution(rule, substitution, true);

                //store the renamed mapping
                uriMappingMap.put(objectURIName, canonicalMapping);
            }
        }
    }

    private class CanonicalIRIMapping {
        private CQIE mapping;
        private Term templateURI;
        private Function uriTerm;
        private Position termPosition;

        public CanonicalIRIMapping(CQIE mapping, Function uriTerm, Position termPosition) {
            this.mapping = mapping;
            this.uriTerm = uriTerm;
            this.templateURI = uriTerm.getTerm(0);
            this.termPosition = termPosition;

        }

//        substitute the old uri with the new canonical iri
        public CQIE create() {

            //get the canonical version of the uri and useful columns
            CQIE canonicalMapping = uriMappingMap.get(templateURI);
            Function canonHead = canonicalMapping.getHead();
            final Function templateCanURI = (Function) canonHead.getTerm(0);

            //get templateuri
            Function target = (Function) canonHead.getTerm(2);

            //get substitution
            Substitution subs = unifierUtilities.getMGU(uriTerm, target);

            CQIE newMapping = substitutionUtilities.applySubstitution(mapping, subs, true);
            Function currentHead = newMapping.getHead();
            currentHead.setTerm(termPosition.getPosition(), templateCanURI);

            List<Function> newURIBody = new ArrayList<>();
            newURIBody.addAll(canonicalMapping.getBody());
            //get body values from the new mapping that have not been added already from the body of the canonical mapping
            newURIBody.addAll(newMapping.getBody().stream()
                                .filter(m -> !newURIBody.contains(m))
                                .collect(Collectors.toList()));
            newMapping.updateBody(newURIBody);

            return newMapping;
        }
    }
}
