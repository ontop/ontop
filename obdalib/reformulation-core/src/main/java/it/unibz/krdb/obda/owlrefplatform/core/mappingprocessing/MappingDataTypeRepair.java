package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MappingDataTypeRepair {
    
    private DBMetadata metadata;
    
    private Map<String, List<Object[]>> termOccurenceIndex;
    
    private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

    public MappingDataTypeRepair(DBMetadata metadata) {
        this.metadata = metadata;
    }
    
    public void insertDataTyping(DatalogProgram mappingDatalog) throws OBDAException {
        List<CQIE> mappingRules = mappingDatalog.getRules();
        for (CQIE rule : mappingRules) {
            prepareIndex(rule);
            Atom atom = rule.getHead();
            Predicate predicate = atom.getPredicate();
            if (predicate.getArity() == 2 && predicate.getType(1) == COL_TYPE.LITERAL) {
            	// if it's a data property
                Variable variable = (Variable) atom.getTerm(1);
                Predicate functor = getDataTypeFunctor(variable);
                Term newTerm = ofac.getFunctionalTerm(functor, variable);
                atom.setTerm(1, newTerm);
            } else {
            	// skip
            }
        }
    }

    private Predicate getDataTypeFunctor(Variable variable) throws OBDAException {
        List<Object[]> list = termOccurenceIndex.get(variable.getName());
        if (list == null) {
            throw new OBDAException("Unknown term in head");
        }
        Object[] o = list.get(0);
        Atom atom = (Atom) o[0];
        Integer pos = (Integer) o[1];
        
        String tableName = atom.getPredicate().toString();
        DataDefinition tableMetadata = metadata.getDefinition(tableName);
        
        Attribute attribute = tableMetadata.getAttribute(pos);
        
        switch (attribute.type) {
            case Types.VARCHAR: return OBDAVocabulary.XSD_STRING;
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.SMALLINT: return OBDAVocabulary.XSD_INTEGER;
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.REAL: return OBDAVocabulary.XSD_DOUBLE;
            case Types.DATE: // H2 driver uses this type for timestamp type
            case Types.TIMESTAMP: return OBDAVocabulary.XSD_DATETIME;
            case Types.BOOLEAN: 
            case Types.BINARY:
            case Types.BIT: return OBDAVocabulary.XSD_BOOLEAN;
            default: return OBDAVocabulary.RDFS_LITERAL;
        }
    }
    
    private void prepareIndex(CQIE rule) {
        termOccurenceIndex = new HashMap<String, List<Object[]>>();
        List<Atom> body = rule.getBody();
        Iterator<Atom> it = body.iterator();
        while (it.hasNext()) {
            Atom a = (Atom) it.next();
            List<Term> terms = a.getTerms();
            Iterator<Term> tit = terms.iterator();
            int i = 1;
            while (tit.hasNext()) {
                Term t = tit.next();
                if (t instanceof AnonymousVariable) {
                    i++;
                } else if (t instanceof VariableImpl) {
                    Object[] o = new Object[2];
                    o[0] = a;  // atom
                    o[1] = i;  // index
                    i++;
                    List<Object[]> aux = termOccurenceIndex.get(((VariableImpl) t).getName());
                    if (aux == null) {
                        aux = new LinkedList<Object[]>();
                    }
                    aux.add(o);
                    termOccurenceIndex.put(((VariableImpl) t).getName(), aux);
                } else if (t instanceof FunctionalTermImpl) {
                    // NO-OP
                } else if (t instanceof ValueConstant) {
                    // NO-OP
                } else if (t instanceof URIConstant) {
                    // NO-OP
                }
            }
        }
    }
}
