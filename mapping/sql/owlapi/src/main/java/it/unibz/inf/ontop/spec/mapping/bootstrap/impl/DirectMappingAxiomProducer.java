package it.unibz.inf.ontop.spec.mapping.bootstrap.impl;

/*
 * #%L
 * ontop-obdalib-owlapi
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DirectMappingAxiomProducer {

	private final String baseIRI;

	private final TermFactory termFactory;
	private final org.apache.commons.rdf.api.RDF rdfFactory;
	private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
	private final TargetAtomFactory targetAtomFactory;
	private final TypeFactory typeFactory;

	public DirectMappingAxiomProducer(String baseIRI, TermFactory termFactory, TargetAtomFactory targetAtomFactory,
									  org.apache.commons.rdf.api.RDF rdfFactory,
									  DBFunctionSymbolFactory dbFunctionSymbolFactory, TypeFactory typeFactory) {

		this.baseIRI = Objects.requireNonNull(baseIRI, "Base IRI must not be null!");
		
		this.termFactory = termFactory;
		this.targetAtomFactory = targetAtomFactory;
		this.rdfFactory = rdfFactory;
		this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
		this.typeFactory = typeFactory;
	}


	public String getSQL(NamedRelationDefinition table) {
		return String.format("SELECT * FROM %s", table.getID().getSQLRendering());
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
    public String getRefSQL(ForeignKeyConstraint fk) {

    	String columns = Stream.concat(
    				getIdentifyingAttributes(fk.getRelation()).stream(),
					getIdentifyingAttributes(fk.getReferencedRelation()).stream())
				.map(a -> getQualifiedColumnName(a) + " AS " + getColumnAlias(a))
				.collect(Collectors.joining(", "));

		String tables = fk.getRelation().getID().getSQLRendering() +
				", " + fk.getReferencedRelation().getID().getSQLRendering();

		String conditions = fk.getComponents().stream()
				.map(c -> getQualifiedColumnName(c.getAttribute()) + " = " + getQualifiedColumnName(c.getReferencedAttribute()))
				.collect(Collectors.joining(" AND "));

		return String.format("SELECT %s FROM %s WHERE %s", columns, tables, conditions);
	}

	private static ImmutableList<Attribute> getIdentifyingAttributes(NamedRelationDefinition table) {
		Optional<UniqueConstraint> pk = table.getPrimaryKey();
		return pk.map(UniqueConstraint::getAttributes)
				.orElse(table.getAttributes());
	}

	private static String getTableName(NamedRelationDefinition relation) {
		return relation.getID().getComponents().get(RelationID.TABLE_INDEX).getName();
	}
	private static String getColumnAlias(Attribute attr) {
    	String rendering = attr.getID().getSQLRendering();
    	String name = attr.getID().getName(); // TODO: find a better way of constructing IDs
    	return rendering.replace(name,
				getTableName((NamedRelationDefinition)attr.getRelation()) + "_" + name);
	}
	
	private static String getQualifiedColumnName(Attribute attr) {
		 return new QualifiedAttributeID(((NamedRelationDefinition)attr.getRelation()).getID(), attr.getID()).getSQLRendering();
	}


    /**
     * Definition row graph: an RDF graph consisting of the following triples:
     * <p/>
     *   - the row type triple.
     *   - a literal triple for each column in a table where the column value is non-NULL.
     *
     */
    public ImmutableList<TargetAtom> getCQ(NamedRelationDefinition table, Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap) {

		ImmutableList.Builder<TargetAtom> atoms = ImmutableList.builder();

		//Class Atom
		ImmutableTerm sub = generateTerm(table, "", bnodeTemplateMap);
		atoms.add(targetAtomFactory.getTripleTargetAtom(sub,
				termFactory.getConstantIRI(RDF.TYPE),
				termFactory.getConstantIRI(rdfFactory.createIRI(getTableIRIString(table)))));

		//DataType Atoms
		for (Attribute att : table.getAttributes()) {
			// TODO: check having a default datatype is ok
			IRI typeIRI = att.getTermType().getNaturalRDFDatatype()
					.map(RDFDatatype::getIRI)
					.orElse(XSD.STRING);

			Variable objV = termFactory.getVariable(att.getID().getName());
			ImmutableTerm obj = termFactory.getRDFLiteralFunctionalTerm(objV, typeIRI);

			atoms.add(targetAtomFactory.getTripleTargetAtom(sub,
					termFactory.getConstantIRI(getLiteralPropertyIRI(att)),
					obj));
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
											  Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap) {

		ImmutableTerm sub = generateTerm(fk.getRelation(),
				getTableName(fk.getRelation()) + "_", bnodeTemplateMap);
		ImmutableTerm obj = generateTerm(fk.getReferencedRelation(),
				getTableName(fk.getReferencedRelation()) + "_", bnodeTemplateMap);

		TargetAtom atom = targetAtomFactory.getTripleTargetAtom(sub,
				termFactory.getConstantIRI(getReferencePropertyIRI(fk)),
				obj);
		return ImmutableList.of(atom);
	}


    /**
     *
     * table IRI:
     *      the IRI consisting of the percent-encoded form of the table name.
     *
     * @return table IRI
     */
	private String getTableIRIString(NamedRelationDefinition table) {
		return baseIRI + R2RMLIRISafeEncoder.encode(getTableName(table));
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
    private IRI getLiteralPropertyIRI(Attribute attr) {
        return rdfFactory.createIRI(getTableIRIString((NamedRelationDefinition)attr.getRelation())
                + "#" + R2RMLIRISafeEncoder.encode(attr.getID().getName()));
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
    private IRI getReferencePropertyIRI(ForeignKeyConstraint fk) {
        return rdfFactory.createIRI(getTableIRIString(fk.getRelation())
                + "#ref-" + fk.getComponents().stream()
				.map(c -> R2RMLIRISafeEncoder.encode(c.getAttribute().getID().getName()))
				.collect(Collectors.joining(";")));
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
     * @param td
     * @param bnodeTemplateMap
	 * @return
     */
    private ImmutableTerm generateTerm(NamedRelationDefinition td, String varNamePrefix,
                                       Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap) {
		
		Optional<UniqueConstraint> pko = td.getPrimaryKey();
		if (pko.isPresent()) {
			UniqueConstraint pk = pko.get();

			Template.Builder builder = Template.builder();

			// TODO: IMPROVE
			builder.addSeparator(getTableIRIString(td) + "/" +
							R2RMLIRISafeEncoder.encode(pk.getAttributes().get(0).getID().getName()) + "=");
			builder.addColumn();

			for (int i = 1; i < pk.getAttributes().size(); i++) {
				builder.addSeparator(
						";" + R2RMLIRISafeEncoder.encode(pk.getAttributes().get(i).getID().getName()) + "=");
				builder.addColumn();
			}

			ImmutableList<Variable> arguments = pk.getAttributes().stream()
					.map(a -> termFactory.getVariable(varNamePrefix + a.getID().getName()))
					.collect(ImmutableCollectors.toList());

			return termFactory.getIRIFunctionalTerm(builder.build(), arguments);
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


}
