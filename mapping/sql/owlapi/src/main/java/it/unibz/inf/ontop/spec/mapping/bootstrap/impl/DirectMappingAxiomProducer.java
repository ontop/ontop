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
import java.util.function.Function;
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

	private enum ForeignKeyComponent {
		SOURCE("FK", ForeignKeyConstraint::getRelation),
		TARGET("FKR", ForeignKeyConstraint::getReferencedRelation);

		private final String suffix;
		private final Function<ForeignKeyConstraint, NamedRelationDefinition> getter;

		ForeignKeyComponent(String suffix, Function<ForeignKeyConstraint, NamedRelationDefinition> getter) {
			this.suffix = suffix;
			this.getter = getter;
		}

		String getSuffix() {
			return suffix;
		}

		NamedRelationDefinition getRelation(ForeignKeyConstraint fk) {
			return getter.apply(fk);
		}
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

		Optional<String> alias = getOptionalFKAlias(fk, ForeignKeyComponent.SOURCE);
		Optional<String> referencedAlias = getOptionalFKAlias(fk, ForeignKeyComponent.TARGET);
		String columnPrefix = getFKColumnPrefix(fk, ForeignKeyComponent.SOURCE);
		String referencedColumnPrefix = getFKColumnPrefix(fk, ForeignKeyComponent.TARGET);

		String columns = Stream.concat(
    				getIdentifyingAttributes(fk.getRelation()).stream()
							.map(a -> getQualifiedColumnName(alias, a) + " AS " + getColumnAlias(columnPrefix, a)),
					getIdentifyingAttributes(fk.getReferencedRelation()).stream()
							.map(a -> getQualifiedColumnName(referencedAlias, a) + " AS " + getColumnAlias(referencedColumnPrefix, a)))
				.collect(Collectors.joining(", "));

		String tables = fk.getRelation().getID().getSQLRendering()
				+ alias.map(a -> " AS " + a).orElse("")
				+ ", "
				+ fk.getReferencedRelation().getID().getSQLRendering()
				+ referencedAlias.map(a -> " AS " + a).orElse("");

		String conditions = fk.getComponents().stream()
				.map(c -> getQualifiedColumnName(alias, c.getAttribute())
						+ " = "
						+ getQualifiedColumnName(referencedAlias, c.getReferencedAttribute()))
				.collect(Collectors.joining(" AND "));

		return String.format("SELECT %s FROM %s WHERE %s", columns, tables, conditions);
	}


	private static ImmutableList<Attribute> getIdentifyingAttributes(NamedRelationDefinition table) {
		Optional<UniqueConstraint> pk = table.getPrimaryKey();
		return pk.map(UniqueConstraint::getAttributes)
				.orElse(table.getAttributes());
	}

	private static Optional<String> getOptionalFKAlias(ForeignKeyConstraint fk, ForeignKeyComponent component) {
		return fk.getRelation() == fk.getReferencedRelation()
				? Optional.of(getAlias(component.getRelation(fk).getID(), component.getSuffix()))
				: Optional.empty();
	}

	private static String getAlias(RelationID relationID, String suffix) {
		// TODO: find a better way of constructing IDs
		QuotedID nameId = relationID.getComponents().get(RelationID.TABLE_INDEX);
		String name = nameId.getName();
		return nameId.getSQLRendering().replace(name, name + "_" + suffix);
	}

	private static String getFKColumnPrefix(ForeignKeyConstraint fk, ForeignKeyComponent component) {
		return fk.getRelation() == fk.getReferencedRelation()
				? getTableName(component.getRelation(fk)) + "_" + component.getSuffix()
				: getTableName(component.getRelation(fk));
	}

	private static String getTableName(NamedRelationDefinition relation) {
		return relation.getID().getComponents().get(RelationID.TABLE_INDEX).getName();
	}

	private static String getColumnAlias(String prefix, Attribute attr) {
    	String rendering = attr.getID().getSQLRendering();
    	String name = attr.getID().getName(); // TODO: find a better way of constructing IDs
    	return rendering.replace(name, prefix + "_" + name);
	}

	private static String getQualifiedColumnName(Optional<String> alias, Attribute attr) {
		String tableRef = alias.orElseGet(() -> ((NamedRelationDefinition)attr.getRelation()).getID().getSQLRendering());
		return tableRef + "." + attr.getID().getSQLRendering();
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
				getFKColumnPrefix(fk, ForeignKeyComponent.SOURCE) + "_", bnodeTemplateMap);
		ImmutableTerm obj = generateTerm(fk.getReferencedRelation(),
				getFKColumnPrefix(fk, ForeignKeyComponent.TARGET) + "_", bnodeTemplateMap);

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

			ImmutableList<ImmutableFunctionalTerm> arguments = getVariablesWithCast(pk.getAttributes(), varNamePrefix);

			return termFactory.getIRIFunctionalTerm(builder.build(), arguments);
		}
		else {
			ImmutableList<ImmutableFunctionalTerm> vars = getVariablesWithCast(td.getAttributes(), varNamePrefix);

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

	private ImmutableList<ImmutableFunctionalTerm> getVariablesWithCast(ImmutableList<Attribute> attributes, String varNamePrefix) {
		return attributes.stream()
				.map(a -> termFactory.getVariable(varNamePrefix + a.getID().getName()))
				.map(termFactory::getPartiallyDefinedConversionToString)
				.collect(ImmutableCollectors.toList());
	}

}
