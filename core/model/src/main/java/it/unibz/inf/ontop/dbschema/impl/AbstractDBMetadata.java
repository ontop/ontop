package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.term.TermFactory;
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
    public ImmutableList<CQIE> generateFKRules() {
        final boolean printouts = false;

        if (printouts)
            System.out.println("===FOREIGN KEY RULES");
        int count = 0;

        ImmutableList.Builder<CQIE> builder = ImmutableList.builder();

        Collection<DatabaseRelationDefinition> tableDefs = getDatabaseRelations();
        for (DatabaseRelationDefinition def : tableDefs) {
            for (ForeignKeyConstraint fks : def.getForeignKeys()) {

                DatabaseRelationDefinition def2 = fks.getReferencedRelation();

                // create variables for the current table
                int len1 = def.getAttributes().size();
                List<Term> terms1 = new ArrayList<>(len1);
                for (int i = 1; i <= len1; i++)
                    terms1.add(termFactory.getVariable("t" + i));

                // create variables for the referenced table
                int len2 = def2.getAttributes().size();
                List<Term> terms2 = new ArrayList<>(len2);
                for (int i = 1; i <= len2; i++)
                    terms2.add(termFactory.getVariable("p" + i));

                for (ForeignKeyConstraint.Component comp : fks.getComponents()) {
                    // indexes start at 1
                    int pos1 = comp.getAttribute().getIndex() - 1; // current column (1)
                    int pos2 = comp.getReference().getIndex() - 1; // referenced column (2)

                    terms1.set(pos1, terms2.get(pos2));
                }

                Function head = termFactory.getFunction(def2.getAtomPredicate(), terms2);
                Function body = termFactory.getFunction(def.getAtomPredicate(), terms1);

                builder.add(datalogFactory.getCQIE(head, body));
                if (printouts)
                    System.out.println("   FK_" + ++count + " " +  head + " :- " + body);
            }
        }
        if (printouts)
            System.out.println("===END OF FOREIGN KEY RULES");
        return builder.build();
    }

    protected TermFactory getTermFactory() {
        return termFactory;
    }

    protected DatalogFactory getDatalogFactory() {
        return datalogFactory;
    }
}
