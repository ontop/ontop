package it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.mpaxiomproducer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.DirectMappingAxiomProducer;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.Pair;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import java.util.List;
import java.util.Map;

/**
 * @author Davide Lanti
 * Mapping Pattern Axiom Producer
 */
public class MPMappingAxiomProducer extends DirectMappingAxiomProducer {
	private TargetProducer targetProducer;
	private SourceProducer sourceProducer;
	private TermsProducer termsProducer;

    public MPMappingAxiomProducer(String baseIRI, TermFactory termFactory, TargetAtomFactory targetAtomFactory, RDF rdfFactory, DBFunctionSymbolFactory dbFunctionSymbolFactory, TypeFactory typeFactory) {
        super(baseIRI, termFactory, targetAtomFactory, rdfFactory, dbFunctionSymbolFactory, typeFactory);
        this.sourceProducer = new SourceProducer();
		this.termsProducer = new TermsProducer(baseIRI, termFactory, targetAtomFactory, rdfFactory, dbFunctionSymbolFactory, typeFactory);
		this.targetProducer = new TargetProducer(this.termsProducer);
    }

	public String getSQL(NamedRelationDefinition table, BootConf.NullValue nullValue){
    	return sourceProducer.getSQL(table, nullValue);
	}

	/***
	 * Definition reference triple: an RDF triple with:
	 * <p/>
	 * subject: the row node for the row.
	 * predicate: the reference property IRI for the columns.
	 * object: the row node for the referenced row.
	 *
	 * @param fk
	 * @return
	 */
	public String getRefSQL(ForeignKeyConstraint fk, BootConf conf) {
		return sourceProducer.getRefSQL(fk, conf);
	}

	/**
	 * Definition row graph: an RDF graph consisting of the following triples:
	 * <p/>
	 *   - the row type triple.
	 *   - a literal triple for each column in a table where the column value is non-NULL.
	 *
	 */
//	@Override TODO> Completely remove?
//	public ImmutableList<TargetAtom> getCQ(NamedRelationDefinition table, Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap) {
//		return targetProducer.getCQ(table, bnodeTemplateMap);
//	}

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
    	return targetProducer.getCQ(table, bnodeTemplateMap, bootConf);
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
		return targetProducer.getRefCQ(fk, bnodeTemplateMap, bootConf);
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
		return targetProducer.getRefCQ(joinPair, bnodeTemplateMap, bootConf, tableDefs);
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
		 return termsProducer.getReferencePropertyIRI(fk, bootConf);
	 }

	// (T.C1,T.C2) = (T1.C3,T1.C4) -> T#join-C1;C2
	public IRI getReferencePropertyIRI(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> joinPair, NamedRelationDefinition table) {
	 	return termsProducer.getReferencePropertyIRI(joinPair, table);
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
	public IRI getReferencePropertyIRI(ForeignKeyConstraint fk) {
		return termsProducer.getReferencePropertyIRI(fk);
	}

	/**
	 *
	 * table IRI:
     *      the IRI consisting of the percent-encoded form of the table name.
	 *
	 * @return table IRI
	 */
	public String getTableIRIString(NamedRelationDefinition table, it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.dictionary.Dictionary dictionary) {
		return termsProducer.getTableIRIString(table, dictionary);
	}

	public String getRefSQL(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> pair, ImmutableList<NamedRelationDefinition> tableDefs, BootConf conf) {
		return sourceProducer.getRefSQL(pair, tableDefs, conf);
	}

	// A \subclassOf B
	public static List<NamedRelationDefinition> retrieveParentTables(NamedRelationDefinition table) {
		return SourceProducer.retrieveParentTables(table);
	}

	 /**
	  Retrieve the ancestor of the subclassOf hierarchy. If there is a cycle, and no definite ancestor, then
	  the ancestor is to be decided upon lexicographic order.
	  In case of multiple-inheritance, I assign the same template to all connected tables.
	  The algorithm follows a bfs strategy
	  **/
	public static NamedRelationDefinition retrieveAncestorTable(NamedRelationDefinition source) {
		return SourceProducer.retrieveAncestorTable(source);
	}

	public static NamedRelationDefinition retrieveDatabaseTableDefinition(ImmutableList<NamedRelationDefinition> tables, RelationID relationID) {
		return SourceProducer.retrieveDatabaseTableDefinition(tables, relationID);
	}

	public ImmutableList<TargetAtom> getClusteringCQ(NamedRelationDefinition table, Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap, String clusterValue, BootConf bootConf) {
		return this.targetProducer.getCQClustering(table, bnodeTemplateMap, clusterValue, bootConf);
	}

	public String getClusteringSQL(String clusteringMapEntryKey, String clusteringAttribute, NamedRelationDefinition table, BootConf.NullValue nullValue) {
		return this.sourceProducer.getClusteringSQL(clusteringMapEntryKey, clusteringAttribute, table, nullValue);
	}
}
