package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.collect.Multimap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.UnfoldingMechanism;

import java.util.List;

public interface IQuestUnfolder {

    /**
     * TODO: stop this bad practice
     */
    @Deprecated
    public void setup();

    List<CQIE> getRules();

    Multimap<Predicate, Integer> processMultipleTemplatePredicates();

    void applyTMappings(boolean optimizeMap, TBoxReasoner reformulationReasoner, boolean full) throws OBDAException;

    void extendTypesWithMetadata(TBoxReasoner tBoxReasoner, EquivalenceMap equivalenceMaps) throws OBDAException;

    void addNOTNULLToMappings();

    void normalizeLanguageTagsinMappings();

    void normalizeEqualities();

    void addABoxAssertionsAsFacts(Iterable<Assertion> assertions);

    void updateSemanticIndexMappings(List<OBDAMappingAxiom> mappings, TBoxReasoner reformulationReasoner) throws OBDAException;

    UriTemplateMatcher getUriTemplateMatcher();

    DatalogProgram unfold(DatalogProgram query, String targetPredicate) throws OBDAException;

    UnfoldingMechanism getDatalogUnfolder();
}
