package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;

import java.util.*;

public abstract class AbstractDBMetadata implements DBMetadata {

    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;

    protected AbstractDBMetadata(TermFactory termFactory, DatalogFactory datalogFactory) {
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
    }

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
                    terms1.add(termFactory.getVariable("t" + i));

                // Roman: important correction because table2 may not be in the same case
                // (e.g., it may be all upper-case)
                int len2 = def2.getAttributes().size();
                List<Term> terms2 = new ArrayList<>(len2);
                for (int i = 1; i <= len2; i++)
                    terms2.add(termFactory.getVariable("p" + i));

                // do the swapping
                for (Map.Entry<Integer,Integer> swap : positionMatch.entrySet())
                    terms1.set(swap.getKey(), terms2.get(swap.getValue()));

                Function head = termFactory.getFunction(def2.getAtomPredicate(), terms2);
                Function body = termFactory.getFunction(def.getAtomPredicate(), terms1);

                CQIE rule = datalogFactory.getCQIE(head, body);
                multimapBuilder.put(def.getAtomPredicate(), rule);
                if (printouts)
                    System.out.println("   FK_" + ++count + " " +  head + " :- " + body);
            }
        }
        if (printouts)
            System.out.println("===END OF FOREIGN KEY RULES");
        return multimapBuilder.build();
    }

    protected TermFactory getTermFactory() {
        return termFactory;
    }

    protected DatalogFactory getDatalogFactory() {
        return datalogFactory;
    }
}
