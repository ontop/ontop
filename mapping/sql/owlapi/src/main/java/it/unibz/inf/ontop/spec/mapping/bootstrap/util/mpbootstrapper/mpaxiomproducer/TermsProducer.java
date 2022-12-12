package it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.mpaxiomproducer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.Pair;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.dictionary.Dictionary;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TermsProducer {

    private final String baseIRI;
    private final TermFactory termFactory;
    private final RDF rdfFactory;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final TypeFactory typeFactory;

    public TermsProducer(String baseIRI, TermFactory termFactory, TargetAtomFactory targetAtomFactory,
                                      RDF rdfFactory,
                                      DBFunctionSymbolFactory dbFunctionSymbolFactory, TypeFactory typeFactory) {

        this.baseIRI = Objects.requireNonNull(baseIRI, "Base IRI must not be null!");
        this.termFactory = termFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.rdfFactory = rdfFactory;
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
        this.typeFactory = typeFactory;
    }

    public TermFactory getTermFactory(){
        return this.termFactory;
    }

    public TargetAtomFactory getTargetAtomFactory() {
        return targetAtomFactory;
    }

    public RDF getRdfFactory() {
        return rdfFactory;
    }

    /**
     * - If the table has a primary key, the row node is a relative IRI obtained by concatenating:
     *   - the percent-encoded form of the table name,
     *   - the SOLIDUS character '/',
     *   - for each column in the primary key, in order:
     *     - the percent-encoded form of the column name,
     *     - a EQUALS SIGN character '=',
     *     - the percent-encoded lexical form of the canonical RDF literal representation of the column value as defined in R2RML section 10.2 Natural Mapping of SQL Values [R2RML],
     *     - if it is not the last column in the primary key, a SEMICOLON character ';'
     * - If the table has no primary key, the row node is a fresh blank node that is unique to this row.
     *
     */
    ImmutableTerm generateTerm(NamedRelationDefinition td, String varNamePrefix,
                               Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap) {

        Optional<UniqueConstraint> pko = td.getPrimaryKey();
        if (pko.isPresent()) {
            ImmutableList<Template.Component> template = generateTemplate(pko.get(), td, new Dictionary()); // use empty dict
            ImmutableList<Variable> arguments = generateArguments(pko.get(), varNamePrefix);

            return termFactory.getIRIFunctionalTerm(template, arguments);
        }
        else {
            ImmutableList<ImmutableTerm> vars = td.getAttributes().stream()
                    .map(a -> termFactory.getVariable(varNamePrefix + a.getID().getName()))
                    .collect(ImmutableCollectors.toList());

            /*
             * Re-use the blank node template if already existing
             */
            BnodeStringTemplateFunctionSymbol functionSymbol = bnodeTemplateMap
                    .computeIfAbsent(td,
                            d -> dbFunctionSymbolFactory.getFreshBnodeStringTemplateFunctionSymbol(vars.size()));

            ImmutableFunctionalTerm lexicalTerm = termFactory.getImmutableFunctionalTerm(functionSymbol, vars);
            return termFactory.getRDFFunctionalTerm(lexicalTerm,
                    termFactory.getRDFTermTypeConstant(typeFactory.getBlankNodeType()));
        }
    }

    /**
     * - If the table has a primary key, the row node is a relative IRI obtained by concatenating:
     *   - the percent-encoded form of the table name,
     *   - the SOLIDUS character '/',
     *   - for each column in the primary key, in order:
     *     - the percent-encoded form of the column name,
     *     - a EQUALS SIGN character '=',
     *     - the percent-encoded lexical form of the canonical RDF literal representation of the column value as defined in R2RML section 10.2 Natural Mapping of SQL Values [R2RML],
     *     - if it is not the last column in the primary key, a SEMICOLON character ';'
     * - If the table has no primary key, the row node is a fresh blank node that is unique to this row.
     *
     */
    ImmutableTerm generateTerm(NamedRelationDefinition td, String varNamePrefix,
                               Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap, BootConf bootConf) {

        Optional<UniqueConstraint> pko = td.getPrimaryKey();
        if (pko.isPresent() ) {
            if( bootConf.isEnableSH() ){
                NamedRelationDefinition ancestorTable = SourceProducer.retrieveAncestorTable(td);
                if( bootConf.getDictionary().isEmpty() || bootConf.getDictionary().containsTable(ancestorTable.getID().getComponents().get(RelationID.TABLE_INDEX).getName()) ){
                    ImmutableList<Template.Component> template = (ancestorTable == td) ? generateTemplate(pko.get(), td, bootConf.getDictionary()) : generateTemplate(ancestorTable.getPrimaryKey().get(), ancestorTable, bootConf.getDictionary());
                    ImmutableList<Variable> arguments = (ancestorTable == td) ? generateArguments(pko.get(), varNamePrefix) : generateArguments(varNamePrefix, td);
                    return termFactory.getIRIFunctionalTerm(template, arguments);
                }
            }

            ImmutableList<Template.Component> template = generateTemplate(pko.get(), td, bootConf.getDictionary());
            ImmutableList<Variable> arguments = generateArguments(pko.get(), varNamePrefix);

            return termFactory.getIRIFunctionalTerm(template, arguments);
        }
        else {
            // If no pk, attributes are coalesced (and a _coal suffix is added
            // to distinguish them from datatype attributes, which cannot be coalesced)
            // if the attributes are nullable
            ImmutableList<ImmutableTerm> vars = td.getAttributes().stream()
                    .map(a -> a.isNullable()
                            ? termFactory.getVariable(varNamePrefix + a.getID().getName() + "_coal")
                            : termFactory.getVariable(varNamePrefix + a.getID().getName())
                    )
                    .collect(ImmutableCollectors.toList());

            /*
             * Re-use the blank node template if already existing
             */
            BnodeStringTemplateFunctionSymbol functionSymbol = bnodeTemplateMap
                    .computeIfAbsent(td,
                            d -> dbFunctionSymbolFactory.getFreshBnodeStringTemplateFunctionSymbol(vars.size()));

            ImmutableFunctionalTerm lexicalTerm = termFactory.getImmutableFunctionalTerm(functionSymbol, vars);
            return termFactory.getRDFFunctionalTerm(lexicalTerm,
                    termFactory.getRDFTermTypeConstant(typeFactory.getBlankNodeType()));
        }
    }

    /**
     *
     * table IRI:
     *      the IRI consisting of the percent-encoded form of the table name.
     *
     * @return table IRI
     */
    String getTableIRIString(NamedRelationDefinition table) {
        return baseIRI + R2RMLIRISafeEncoder.encode(table.getID().getComponents().get(RelationID.TABLE_INDEX).getName());
    }

    /**
     *
     * table IRI:
     *      the IRI consisting of the percent-encoded form of the table name.
     *
     * @return table IRI
     */
    public String getTableIRIString(NamedRelationDefinition table, Dictionary dictionary) {
        if(dictionary.isEmpty()){
            return getTableIRIString(table);
        }
        String alias = dictionary.getTableAlias(table.getID().getComponents().get(RelationID.TABLE_INDEX).getName());
        return baseIRI + R2RMLIRISafeEncoder.encode(alias);
    }

    /**
     * Clustering Mapping Pattern (CE2C)
     * table IRI:
     *      the IRI consisting of the percent-encoded form of the table name.
     *
     * @return table IRI
     */
    public String getTableIRIStringCluster(NamedRelationDefinition table, Dictionary dictionary, String clusterValue) {
        if(dictionary.isEmpty()){
            return getTableIRIString(table) + "/" + clusterValue;
        }
        String alias = dictionary.getTableAlias(table.getID().getComponents().get(RelationID.TABLE_INDEX).getName());
        return baseIRI + R2RMLIRISafeEncoder.encode(alias) + "/" + clusterValue ;
    }

    /**
     * Generate an URI for datatype property from a string(name of column) The
     * style should be "baseIRI/tablename#columnname" as required in Direct
     * Mapping Definition
     *
     * A column in a table forms a literal property IRI:
     *
     * Definition literal property IRI: the concatenation of:
     *   - the percent-encoded form of the table name,
     *   - the hash character '#',
     *   - the percent-encoded form of the column name.
     */
    IRI getLiteralPropertyIRI(Attribute attr) {
        return rdfFactory.createIRI(getTableIRIString((NamedRelationDefinition)attr.getRelation())
                + "#" + R2RMLIRISafeEncoder.encode(attr.getID().getName()));
    }

    /**
     * Generate an URI for datatype property from a string(name of column) The
     * style should be "baseIRI/alias(tablename)#alias(columnname)" as required in
     * Mapping Patterns definition. "alias(X)" denotes the definition given to X
     * in @dictionary.
     *
     * A column in a table forms a literal property IRI:
     *
     * Definition literal property IRI: the concatenation of:
     *   - the percent-encoded form of the alias(table name),
     *   - the hash character '#',
     *   - the percent-encoded form of the alias(column name).
     */
    IRI getLiteralPropertyIRI(Attribute attr, Dictionary dictionary) {
        if( dictionary.isEmpty() )
            return getLiteralPropertyIRI(attr);

        String tableName = ((NamedRelationDefinition)attr.getRelation()).getID().getComponents().get(RelationID.TABLE_INDEX).getName();
        String attName = attr.getID().getName();
        return rdfFactory.createIRI(getTableIRIString((NamedRelationDefinition)attr.getRelation(), dictionary)
                + "#" + R2RMLIRISafeEncoder.encode(dictionary.getAttributeAlias(tableName, attName)));
    }


    /**
     * entityName = tName or tName + / + clusterName
     * e.g. baseIRI/tName/clusterName/pk1={};pk2={}
     */
    private ImmutableList<Template.Component> generateTemplate(UniqueConstraint keyAtts, NamedRelationDefinition td, Dictionary dictionary){

        Template.Builder builder = Template.builder();
        // TODO: IMPROVE
        builder.addSeparator(getTableIRIString(td, dictionary) + "/" +
                R2RMLIRISafeEncoder.encode(getAttributeName(keyAtts.getAttributes().get(0), td, dictionary)) + "=");
        builder.addColumn();

        for (int i = 1; i < keyAtts.getAttributes().size(); i++) {
                builder.addSeparator(
                        ";" + R2RMLIRISafeEncoder.encode(getAttributeName(keyAtts.getAttributes().get(i), td, dictionary)) + "=");
                builder.addColumn();
        }
        return builder.build();
    }

    private static String getAttributeName(Attribute att, NamedRelationDefinition td, Dictionary dictionary){
        if(dictionary.containsAttribute(td.getID().getComponents().get(RelationID.TABLE_INDEX).getName(), att.getID().getName()))
            return dictionary.getAttributeAlias(td.getID().getComponents().get(RelationID.TABLE_INDEX).getName(), att.getID().getName());
        return att.getID().getName();
    }


    /**
     * Pattern SH only: If parent table present, then generate template according to the "from" of the fkey (case pkey -> pkey)
     */
    private ImmutableList<Variable> generateArguments(String varNamePrefix, NamedRelationDefinition td) {
        for( ForeignKeyConstraint fkey : td.getForeignKeys() ){
            if( SourceProducer.checkWhetherSubclassFkey(fkey) ){
                List<Attribute> fkeyReferringAtts = fkey.getComponents().stream().map(component -> component.getAttribute()).collect(Collectors.toList());
                return fkeyReferringAtts.stream()
                        .map(a -> termFactory.getVariable(varNamePrefix + a.getID().getName()))
                        .collect(ImmutableCollectors.toList());
            }
        }
        throw new RuntimeException("Searching for a non-existing subclass-fkey");
    }

    /**
     * Helper function to generate the arguments of a URI template, according to the provided "pk"
     * @param pk
     * @param varNamePrefix
     * @return
     */
    private ImmutableList<Variable> generateArguments(UniqueConstraint pk, String varNamePrefix) {
        return pk.getAttributes().stream()
                .map(a -> termFactory.getVariable(varNamePrefix + a.getID().getName()))
                .collect(ImmutableCollectors.toList());
    }

    /**
     * Generate a URI from a join pair:
     *     (T.C1,T.C2) = (T1.C3,T1.C4) -> T#join-C1;C2
     */
    public IRI getReferencePropertyIRI(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> joinPair, NamedRelationDefinition table) {
        return rdfFactory.createIRI(getTableIRIString(table)
                + "#join-" + joinPair.first().stream()
                .map(attsID -> R2RMLIRISafeEncoder.encode(attsID.getAttribute().getName()))
                .collect(Collectors.joining(";")));
    }

    /*
     * Generate an URI for object property from a string (name of column)
     *
     * A foreign key in a table forms a reference property IRI:
     *
     * Definition reference property IRI: the concatenation of:
     *   - the percent-encoded form of the alias(table name),
     *   - the string '#ref-',
     *   - for each column in the foreign key, in order:
     *     - the percent-encoded form of the alias(column name),
     *     - if it is not the last column in the foreign key, a SEMICOLON character ';'
     */
    public IRI getReferencePropertyIRI(ForeignKeyConstraint fk, BootConf bootConf) {
        if( bootConf.getDictionary().isEmpty() )
            return getReferencePropertyIRI(fk);

        IRI result = fkeyAlias(fk,bootConf);
        if( result == null ){
            String tableName = fk.getRelation().getID().getComponents().get(RelationID.TABLE_INDEX).getName();
            result = rdfFactory.createIRI(getTableIRIString(fk.getRelation(), bootConf.getDictionary())
                    + "#ref-" + fk.getComponents().stream()
                    .map(c -> R2RMLIRISafeEncoder.encode(
                            bootConf.getDictionary().getAttributeAlias(tableName,c.getAttribute().getID().getName())))
                    .collect(Collectors.joining(";")));
        }
        return result;
    }

    /*
     * Generate an URI for object property from a string (name of column)
     *
     * A foreign key in a table forms a reference property IRI:
     *
     * Definition reference property IRI: the concatenation of:
     *   - the percent-encoded form of the table name,
     *   - the string '#ref-',
     *   - for each column in the foreign key, in order:
     *     - the percent-encoded form of the column name,
     *     - if it is not the last column in the foreign key, a SEMICOLON character ';'
     */
    public IRI getReferencePropertyIRI(ForeignKeyConstraint fk) {
        return rdfFactory.createIRI(getTableIRIString(fk.getRelation())
                + "#ref-" + fk.getComponents().stream()
                .map(c -> R2RMLIRISafeEncoder.encode(c.getAttribute().getID().getName()))
                .collect(Collectors.joining(";")));
    }

    /**
     * If, in bootConf, a "joinAlias" has been defined for foreign key "fk", then the aliased object property will have name:
     *    baseURI + tableAlias#joinAlias
     */
    private IRI fkeyAlias(ForeignKeyConstraint fk, BootConf bootConf) {
        for (Dictionary.DictEntry entry : bootConf.getDictionary().getDictEntries()) {
            for (Dictionary.DictEntry.Reference ref : entry.getReferences()) {
                String fromTable = entry.getTableName();
                String toTable = ref.getToTable();
                if (fromTable.equals(fk.getRelation().getID().getComponents().get(RelationID.TABLE_INDEX).getName())) {
                    if (toTable.equals(fk.getReferencedRelation().getID().getComponents().get(RelationID.TABLE_INDEX).getName())) {
                        List<String> compFromAtts = fk.getComponents().stream()
                                .map(c -> c.getAttribute().getID().getName().toLowerCase()).collect(Collectors.toList());
                        List<String> compToAtts = fk.getComponents().stream()
                                .map(c -> c.getReferencedAttribute().getID().getName().toLowerCase()).collect(Collectors.toList());
                        if( ref.getFromAtts().equals(compFromAtts) && ref.getToAtts().equals(compToAtts) ){
                            return rdfFactory.createIRI(baseIRI + R2RMLIRISafeEncoder.encode(bootConf.getDictionary().getTableAlias(fromTable)) + "#" + R2RMLIRISafeEncoder.encode(ref.getJoinAlias()));
                        }
                    }
                }
            }
        }
        return null;
    }
}
