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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint.Component;
import org.apache.commons.rdf.api.IRI;

import java.util.*;


public class DirectMappingAxiomProducer {

	private final String baseIRI;

	private final TermFactory termFactory;
	private final org.apache.commons.rdf.api.RDF rdfFactory;
	private final TargetAtomFactory targetAtomFactory;

	public DirectMappingAxiomProducer(String baseIRI, TermFactory termFactory, TargetAtomFactory targetAtomFactory,
									  org.apache.commons.rdf.api.RDF rdfFactory) {
		this.termFactory = termFactory;
        this.baseIRI = Objects.requireNonNull(baseIRI, "Base IRI must not be null!");
		this.targetAtomFactory = targetAtomFactory;
		this.rdfFactory = rdfFactory;
	}


	public String getSQL(DatabaseRelationDefinition table) {
		return String.format("SELECT * FROM %s", table.getID().getSQLRendering());
	}

	public Map<String, ImmutableList<TargetAtom>> getRefAxioms(DatabaseRelationDefinition table) {
		Map<String, ImmutableList<TargetAtom>> refAxioms = new HashMap<>();
		for (ForeignKeyConstraint fk : table.getForeignKeys())
			refAxioms.put(getRefSQL(fk), getRefCQ(fk));
		
		return refAxioms;
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
    private String getRefSQL(ForeignKeyConstraint fk) {

		Set<Object> columns = new LinkedHashSet<>(); // Set avoids duplicated and LinkedHashSet keeps the insertion order
		for (Attribute attr : getIdentifyingAttributes(fk.getRelation()))
			columns.add(getColumnNameWithAlias(attr));

		List<String> conditions = new ArrayList<>(fk.getComponents().size());
		for (ForeignKeyConstraint.Component comp : fk.getComponents()) {
			columns.add(getColumnNameWithAlias(comp.getReference()));	
			conditions.add(getColumnName(comp.getAttribute()) + " = " + getColumnName(comp.getReference()));
		}
		
		for (Attribute attr : getIdentifyingAttributes(fk.getReferencedRelation())) 
			columns.add(getColumnNameWithAlias(attr));

		final String tables = fk.getRelation().getID().getSQLRendering() + 
							", " + fk.getReferencedRelation().getID().getSQLRendering();
		
		return String.format("SELECT %s FROM %s WHERE %s", 
				Joiner.on(", ").join(columns), tables, Joiner.on(" AND ").join(conditions));
	}

	private static List<Attribute> getIdentifyingAttributes(DatabaseRelationDefinition table) {
		UniqueConstraint pk = table.getPrimaryKey();
		if (pk != null)
			return pk.getAttributes();
		else
			return table.getAttributes();
	}
	
	private static String getColumnNameWithAlias(Attribute attr) {
		 return getColumnName(attr) + 
				 " AS " + attr.getRelation().getID().getTableName() + "_" + attr.getID().getName();
	}
	
	private static String getColumnName(Attribute attr) {
		 return attr.getQualifiedID().getSQLRendering();
	}


    /**
     * Definition row graph: an RDF graph consisting of the following triples:
     * <p/>
     *   - the row type triple.
     *   - a literal triple for each column in a table where the column value is non-NULL.
     *
     */
    public ImmutableList<TargetAtom> getCQ(DatabaseRelationDefinition table) {

		ImmutableList.Builder<TargetAtom> atoms = ImmutableList.builder();

		//Class Atom
		ImmutableTerm sub = generateSubject(table, false);
		atoms.add(getAtom(getTableIRI(table.getID()), sub));

		//DataType Atoms
		for (Attribute att : table.getAttributes()) {
			// TODO: revisit this
			RDFDatatype type = (RDFDatatype) att.getTermType();
			Variable objV = termFactory.getVariable(att.getID().getName());
			ImmutableTerm obj = termFactory.getImmutableTypedTerm(objV, type);
			
			atoms.add(getAtom(getLiteralPropertyIRI(att), sub, obj));
		}

		return atoms.build();
	}

    /**
     * Definition row graph: an RDF graph consisting of the following triples:
     *
     * - a reference triple for each <column name list> in a table's foreign keys where none of the column values is NULL.
     *
     */
	private ImmutableList<TargetAtom> getRefCQ(ForeignKeyConstraint fk) {
        ImmutableTerm sub = generateSubject(fk.getRelation(), true);
		ImmutableTerm obj = generateSubject(fk.getReferencedRelation(), true);

		TargetAtom atom = getAtom(getReferencePropertyIRI(fk), sub, obj);
		return ImmutableList.of(atom);
	}


    /**
     *
     * table IRI:
     *      the relative IRI consisting of the percent-encoded form of the table name.
     *
     * @return table IRI
     */
	private IRI getTableIRI(RelationID tableId) {
		return rdfFactory.createIRI(baseIRI + R2RMLIRISafeEncoder.encode(tableId.getTableName()));
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
        return rdfFactory.createIRI(baseIRI + R2RMLIRISafeEncoder.encode(attr.getRelation().getID().getTableName())
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
        List<String> attributes = new ArrayList<>(fk.getComponents().size());
 		for (Component component : fk.getComponents())
            attributes.add(R2RMLIRISafeEncoder.encode(component.getAttribute().getID().getName()));
        
        return rdfFactory.createIRI(baseIRI + R2RMLIRISafeEncoder.encode(fk.getRelation().getID().getTableName())
                + "#ref-" + Joiner.on(";").join(attributes));
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
     * @return
     */
    private ImmutableTerm generateSubject(DatabaseRelationDefinition td, boolean ref) {
		
		String varNamePrefix = "";
		if (ref)
			varNamePrefix = td.getID().getTableName() + "_";

		UniqueConstraint pk = td.getPrimaryKey();
		if (pk != null) {
			List<ImmutableTerm> terms = new ArrayList<>(pk.getAttributes().size() + 1);
			
			List<String> attributes = new ArrayList<>(pk.getAttributes().size());
			for (Attribute att : pk.getAttributes()) 
				attributes.add(R2RMLIRISafeEncoder.encode(att.getID().getName()) + "={}");
			
			String template = baseIRI + R2RMLIRISafeEncoder.encode(td.getID().getTableName()) + "/" + Joiner.on(";").join(attributes);
			terms.add(termFactory.getConstantLiteral(template));
			
			for (Attribute att : pk.getAttributes())
				terms.add(termFactory.getVariable(varNamePrefix + att.getID().getName()));

			return termFactory.getImmutableUriTemplate(ImmutableList.copyOf(terms));
		}
		else {
			List<ImmutableTerm> vars = new ArrayList<>(td.getAttributes().size());
			for (Attribute att : td.getAttributes())
				vars.add(termFactory.getVariable(varNamePrefix + att.getID().getName()));

			return termFactory.getImmutableBNodeTemplate(ImmutableList.copyOf(vars));
		}
	}

	private TargetAtom getAtom(IRI iri, ImmutableTerm s, ImmutableTerm o) {
		return targetAtomFactory.getTripleTargetAtom(s,
				convertIRIIntoGroundFunctionalTerm(iri),
				o);
	}

	private TargetAtom getAtom(IRI iri, ImmutableTerm s) {
    	return targetAtomFactory.getTripleTargetAtom(s,
				convertIRIIntoGroundFunctionalTerm(RDF.TYPE),
				convertIRIIntoGroundFunctionalTerm(iri));
	}

	private GroundFunctionalTerm convertIRIIntoGroundFunctionalTerm(IRI iri) {
    	return (GroundFunctionalTerm) termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(iri.getIRIString()));
	}


}
