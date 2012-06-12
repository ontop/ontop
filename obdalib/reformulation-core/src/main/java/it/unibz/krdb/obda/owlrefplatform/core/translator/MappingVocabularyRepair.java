package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * This is a hack class that helps fix and OBDA model in which the mappings
 * include predicates that have not been properly typed.
 * 
 * @author mariano
 * 
 */
public class MappingVocabularyRepair {

	private static OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	Logger log = LoggerFactory.getLogger(MappingVocabularyRepair.class);

	public void fixOBDAModel(OBDAModel model, Set<Predicate> vocabulary) {
		if (vocabulary.size() == 0) {
			// Nothing to repair!
			return;
		}
		
		log.debug("Fixing OBDA Model");
		for (OBDADataSource source : model.getSources()) {
			Collection<OBDAMappingAxiom> mappings = new LinkedList<OBDAMappingAxiom>(model.getMappings(source.getSourceID()));
			model.removeAllMappings(source.getSourceID());
			try {
				model.addMappings(source.getSourceID(), fixMappingPredicates(mappings, vocabulary));
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/***
	 * Makes sure that the mappings given are correctly typed w.r.t. the given
	 * vocabualry.
	 * 
	 * @param originalMappings
	 * @param equivalencesMap
	 * @return
	 */
	public Collection<OBDAMappingAxiom> fixMappingPredicates(Collection<OBDAMappingAxiom> originalMappings, Set<Predicate> vocabulary) {
		log.debug("Reparing/validating {} mappings", originalMappings.size());
		HashMap<URI, Predicate> urimap = new HashMap<URI, Predicate>();
		for (Predicate p : vocabulary) {
			urimap.put(p.getName(), p);
		}

		Collection<OBDAMappingAxiom> result = new LinkedList<OBDAMappingAxiom>();
		for (OBDAMappingAxiom mapping : originalMappings) {

			CQIE targetQuery = (CQIE) mapping.getTargetQuery();
			List<Atom> body = targetQuery.getBody();
			List<Atom> newbody = new LinkedList<Atom>();

			for (Atom atom : body) {
				Predicate p = atom.getPredicate();
				Atom newatom = null;
				Predicate predicate = urimap.get(p.getName());
				if (predicate == null) {
					throw new RuntimeException("ERROR: Mapping references an unknown class/property: " + p.getName());
				}
				newatom = dfac.getAtom(predicate, atom.getTerms());
				newbody.add(newatom);
			}
			CQIE newTargetQuery = dfac.getCQIE(targetQuery.getHead(), newbody);
			result.add(dfac.getRDBMSMappingAxiom(mapping.getId(),((OBDASQLQuery) mapping.getSourceQuery()).toString(), newTargetQuery));
		}
		log.debug("Repair done. Returning {} mappings", result.size());
		return result;

	}
}
