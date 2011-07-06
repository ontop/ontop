package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.Query;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectMappingGenerator {

	private final OBDADataFactory	predicateFactory	= OBDADataFactoryImpl.getInstance();
	private final OBDADataFactory	termFactory			= OBDADataFactoryImpl.getInstance();
	private int						mappingcounter		= 1;

	private final Logger			log					= LoggerFactory.getLogger(ABoxToDBDumper.class);

	public Set<OBDAMappingAxiom> getMappings(DataSource datasource) throws Exception {
		throw new Exception("Not yet implemented");
	}

	public Set<OBDAMappingAxiom> getMappings(Set<OWLOntology> ontologies, Map<URIIdentyfier, String> tableMap) {

		// Iterator<OWLOntology> ontologyIterator = ontologies.iterator();
		Set<OBDAMappingAxiom> mappings = new HashSet<OBDAMappingAxiom>();
		mappingcounter = 0;

		for (URIIdentyfier uriid : tableMap.keySet()) {
			mappingcounter = mappingcounter + 1;

			if (uriid.getType() == URIType.CONCEPT) {
				/* Creating the mapping */
				URI name = uriid.getUri();
				// URIIdentyfier id = new URIIdentyfier(name, URIType.CONCEPT);
				String tablename = tableMap.get(uriid);
				Term qt = termFactory.getVariable("x");
				List<Term> terms = new Vector<Term>();
				terms.add(qt);
				Predicate predicate = predicateFactory.getPredicate(name, terms.size());
				PredicateAtom bodyAtom = predicateFactory.getAtom(predicate, terms);
				// List<Atom> body = new Vector<Atom>();
				// body.add(bodyAtom); // the body
				predicate = predicateFactory.getPredicate(URI.create("q"), terms.size());
				PredicateAtom head = predicateFactory.getAtom(predicate, terms); // the
																					// head
				Query cq = predicateFactory.getCQIE(head, bodyAtom);
				String sql = "SELECT term0 as x FROM " + tablename;
				OBDAMappingAxiom ax = predicateFactory.getRDBMSMappingAxiom("id" + mappingcounter, predicateFactory.getSQLQuery(sql), cq);
				mappings.add(ax);

				log.debug("Mapping {} created: {}", ax.getId(), ax.toString());

			} else if (uriid.getType() == URIType.OBJECTPROPERTY) {

				String tablename = tableMap.get(uriid);
				Term qt1 = termFactory.getVariable("x");
				Term qt2 = termFactory.getVariable("y");
				List<Term> terms = new Vector<Term>();
				terms.add(qt1);
				terms.add(qt2);
				Predicate predicate = predicateFactory.getPredicate(uriid.getUri(), terms.size());
				PredicateAtom bodyAtom = predicateFactory.getAtom(predicate, terms);
				// List<Atom> body = new Vector<Atom>();
				// body.add(bodyAtom); // the body
				predicate = predicateFactory.getPredicate(URI.create("q"), terms.size());
				PredicateAtom head = predicateFactory.getAtom(predicate, terms); // the
																					// head
				Query cq = predicateFactory.getCQIE(head, bodyAtom);
				String sql = "SELECT term0 as x, term1 as y FROM " + tablename;
				OBDAMappingAxiom ax = predicateFactory.getRDBMSMappingAxiom("id" + mappingcounter, predicateFactory.getSQLQuery(sql), cq);

				mappings.add(ax);

				log.debug("Mapping {} created: {}", ax.getId(), ax.toString());

			} else if (uriid.getType() == URIType.DATAPROPERTY) {

				Term qt1 = termFactory.getVariable("x");
				String tablename = tableMap.get(uriid);
				Term qt2 = termFactory.getVariable("y");
				List<Term> terms = new Vector<Term>();
				terms.add(qt1);
				terms.add(qt2);
				Predicate predicate = predicateFactory.getPredicate(uriid.getUri(), terms.size());
				PredicateAtom bodyAtom = predicateFactory.getAtom(predicate, terms);
				// List<Atom> body = new Vector<Atom>();
				// body.add(bodyAtom); // the body
				predicate = predicateFactory.getPredicate(URI.create("q"), terms.size());
				PredicateAtom head = predicateFactory.getAtom(predicate, terms); // the
																					// head
				Query cq = predicateFactory.getCQIE(head, bodyAtom);
				String sql = "SELECT term0 as x, term1 as y FROM " + tablename;
				OBDAMappingAxiom ax = predicateFactory.getRDBMSMappingAxiom("id" + mappingcounter, predicateFactory.getSQLQuery(sql), cq);
				mappings.add(ax);

				log.debug("Mapping {} created: {}", ax.getId(), ax.toString());
			}

		}
		return mappings;
	}
}
