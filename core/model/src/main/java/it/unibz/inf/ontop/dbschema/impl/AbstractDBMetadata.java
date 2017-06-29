package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;

import java.util.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;

public abstract class AbstractDBMetadata implements DBMetadata {

    @Override
    public ImmutableMultimap<AtomPredicate, CQIE> generateFKRules() {
        final boolean printouts = false;

        if (printouts)
            System.out.println("===FOREIGN KEY RULES");
        int count = 0;

        ImmutableMultimap.Builder<AtomPredicate, CQIE> multimapBuilder = ImmutableMultimap.builder();
        Map<Predicate, AtomPredicate> knownPredicateMap = new HashMap<>();

        Collection<DatabaseRelationDefinition> tableDefs = getDatabaseRelations();
        for (DatabaseRelationDefinition def : tableDefs) {
            for (ForeignKeyConstraint fks : def.getForeignKeys()) {

                DatabaseRelationDefinition def2 = fks.getReferencedRelation();

                Map<Integer, Integer> positionMatch = new HashMap<>();
                for (ForeignKeyConstraint.Component comp : fks.getComponents()) {
                    // Get current table and column (1)
                    Attribute att1 = comp.getAttribute();

                    // Get referenced table and column (2)
                    Attribute att2 = comp.getReference();

                    // Get positions of referenced attribute
                    int pos1 = att1.getIndex();
                    int pos2 = att2.getIndex();
                    positionMatch.put(pos1 - 1, pos2 - 1); // indexes start at 1
                }
                // Construct CQIE
                int len1 = def.getAttributes().size();
                List<Term> terms1 = new ArrayList<>(len1);
                for (int i = 1; i <= len1; i++)
                    terms1.add(DATA_FACTORY.getVariable("t" + i));

                // Roman: important correction because table2 may not be in the same case
                // (e.g., it may be all upper-case)
                int len2 = def2.getAttributes().size();
                List<Term> terms2 = new ArrayList<>(len2);
                for (int i = 1; i <= len2; i++)
                    terms2.add(DATA_FACTORY.getVariable("p" + i));

                // do the swapping
                for (Map.Entry<Integer,Integer> swap : positionMatch.entrySet())
                    terms1.set(swap.getKey(), terms2.get(swap.getValue()));

                Function head = Relation2Predicate.getAtom(def2, terms2);
                Function body = Relation2Predicate.getAtom(def, terms1);

                CQIE rule = DATA_FACTORY.getCQIE(head, body);
                multimapBuilder.put(convertToAtomPredicate(body.getFunctionSymbol(), knownPredicateMap), rule);
                if (printouts)
                    System.out.println("   FK_" + ++count + " " +  head + " :- " + body);
            }
        }
        if (printouts)
            System.out.println("===END OF FOREIGN KEY RULES");
        return multimapBuilder.build();
    }

    protected abstract AtomPredicate convertToAtomPredicate(Predicate functionSymbol,
                                                            Map<Predicate, AtomPredicate> knownPredicateMap);
}
