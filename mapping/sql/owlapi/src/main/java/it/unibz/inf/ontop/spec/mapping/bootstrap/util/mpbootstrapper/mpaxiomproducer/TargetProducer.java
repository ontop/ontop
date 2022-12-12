package it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.mpaxiomproducer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.Pair;
import org.apache.commons.rdf.api.IRI;

import java.util.List;
import java.util.Map;

public class TargetProducer {

    private final TermsProducer termsProducer;

    public TargetProducer(TermsProducer termsProducer){
        this.termsProducer = termsProducer;
    }

    /**
     * Definition row graph: an RDF graph consisting of the following triples:
     * <p/>
     *   - the row type triple.
     *   - a literal triple for each column in a table where the column value is non-NULL.
     *
     */
    public ImmutableList<TargetAtom> getCQ(NamedRelationDefinition table,
                                           Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
                                           BootConf bootConf) {

        ImmutableList.Builder<TargetAtom> atoms = ImmutableList.builder();

        //Class Atom
        ImmutableTerm sub = termsProducer.generateTerm(table, "", bnodeTemplateMap, bootConf);

        atoms.add(getAtom(termsProducer.getRdfFactory().createIRI(termsProducer.getTableIRIString(table, bootConf.getDictionary())), sub));

        //DataType Atoms
        for (Attribute att : table.getAttributes()) {
            String tableName = table.getID().getComponents().get(RelationID.TABLE_INDEX).getName();
            String attName = att.getID().getName();
            if( bootConf.getDictionary().isEmpty() || bootConf.getDictionary().containsAttribute(tableName, attName) ) {

                // TODO: check having a default datatype is ok
                IRI typeIRI = att.getTermType().getNaturalRDFDatatype()
                        .map(RDFDatatype::getIRI)
                        .orElse(XSD.STRING);

                Variable objV = termsProducer.getTermFactory().getVariable(attName);
                ImmutableTerm obj = termsProducer.getTermFactory().getRDFLiteralFunctionalTerm(objV, typeIRI);

                atoms.add(getAtom(termsProducer.getLiteralPropertyIRI(att, bootConf.getDictionary()), sub, obj));

            }
        }
        return atoms.build();
    }

    /**
     * Definition row graph: an RDF graph consisting of the following triples:
     *
     * - a reference triple for each <column name list> in a table's foreign keys where none of the column values is NULL.
     *
     */
    public ImmutableList<TargetAtom> getRefCQ(ForeignKeyConstraint fk,
                                              Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
                                              BootConf bootConf) {

        ImmutableTerm sub = termsProducer.generateTerm(fk.getRelation(),
                fk.getRelation().getID().getComponents().get(RelationID.TABLE_INDEX).getName() + "_", bnodeTemplateMap, bootConf);
        ImmutableTerm obj = termsProducer.generateTerm(fk.getReferencedRelation(),
                fk.getReferencedRelation().getID().getComponents().get(RelationID.TABLE_INDEX).getName() + "_", bnodeTemplateMap, bootConf);

        TargetAtom atom = getAtom(termsProducer.getReferencePropertyIRI(fk, bootConf), sub, obj);
        return ImmutableList.of(atom);
    }

    /**
     * Definition row graph: an RDF graph consisting of the following triples:
     *
     * - a reference triple for each <column name list> in a table's foreign keys where none of the column values is NULL.
     *
     */
    public ImmutableList<TargetAtom> getRefCQ(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> joinPair,
                                              Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
                                              BootConf bootConf, ImmutableList<NamedRelationDefinition> tableDefs){

        NamedRelationDefinition table1 = SourceProducer.retrieveDatabaseTableDefinition(tableDefs, retrieveLeftRelationID(joinPair));
        NamedRelationDefinition table2 = SourceProducer.retrieveDatabaseTableDefinition(tableDefs, retrieveRightRelationID(joinPair));

        ImmutableTerm sub = termsProducer.generateTerm(table1,
                table1.getID().getComponents().get(RelationID.TABLE_INDEX).getName() + "_", bnodeTemplateMap, bootConf);
        ImmutableTerm obj = termsProducer.generateTerm(table2,
                table2.getID().getComponents().get(RelationID.TABLE_INDEX).getName() + "_", bnodeTemplateMap, bootConf);

        TargetAtom atom = getAtom(termsProducer.getReferencePropertyIRI(joinPair, table1), sub, obj);
        return ImmutableList.of(atom);
    }

    TargetAtom getAtom(IRI iri, ImmutableTerm s, ImmutableTerm o) {
        return termsProducer.getTargetAtomFactory().getTripleTargetAtom(s,
                termsProducer.getTermFactory().getConstantIRI(iri),
                o);
    }

    TargetAtom getAtom(IRI iri, ImmutableTerm s) {
        return termsProducer.getTargetAtomFactory().getTripleTargetAtom(s,
                termsProducer.getTermFactory().getConstantIRI(RDF.TYPE),
                termsProducer.getTermFactory().getConstantIRI(iri));
    }

    private static RelationID retrieveLeftRelationID(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> joinPair){
        return joinPair.first().get(0).getRelation();
    }

    private static RelationID retrieveRightRelationID(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> joinPair){
        return joinPair.second().get(0).getRelation();
    }

    public ImmutableList<TargetAtom> getCQClustering(NamedRelationDefinition table, Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap, String clusterValue, BootConf bootConf) {

        ImmutableList.Builder<TargetAtom> atoms = ImmutableList.builder();

        //Class Atom
        ImmutableTerm sub = termsProducer.generateTerm(table, "", bnodeTemplateMap, bootConf);

        atoms.add(getAtom(termsProducer.getRdfFactory().createIRI(
                termsProducer.getTableIRIStringCluster(table, bootConf.getDictionary(), clusterValue)), sub));

        return atoms.build();
    }
}
