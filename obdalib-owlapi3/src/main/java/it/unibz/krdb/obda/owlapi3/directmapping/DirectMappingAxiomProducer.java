package it.unibz.krdb.obda.owlapi3.directmapping;

/*
 * #%L
 * ontop-obdalib-owlapi3
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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.parser.EncodeForURI;
import it.unibz.krdb.obda.utils.JdbcTypeMapper;
import it.unibz.krdb.sql.Attribute;
import it.unibz.krdb.sql.ForeignKeyConstraint;
import it.unibz.krdb.sql.RelationID;
import it.unibz.krdb.sql.DatabaseRelationDefinition;
import it.unibz.krdb.sql.UniqueConstraint;
import it.unibz.krdb.sql.ForeignKeyConstraint.Component;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class DirectMappingAxiomProducer {

	private final String baseIRI;

	private final OBDADataFactory df;

	public DirectMappingAxiomProducer(String baseIRI, OBDADataFactory dfac) {
		this.df = dfac;
        this.baseIRI = Objects.requireNonNull(baseIRI, "Base IRI must not be null!");
	}


	public String getSQL(DatabaseRelationDefinition table) {
		return String.format("SELECT * FROM %s", table.getID().getSQLRendering());
	}

	public Map<String, List<Function>> getRefAxioms(DatabaseRelationDefinition table) {
		Map<String, List<Function>> refAxioms = new HashMap<>();
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
    public List<Function> getCQ(DatabaseRelationDefinition table) {

		List<Function> atoms = new ArrayList<>(table.getAttributes().size() + 1);

		//Class Atom
		Term sub = generateSubject(table, false);
		atoms.add(df.getFunction(df.getClassPredicate(getTableIRI(table.getID())), sub));

		//DataType Atoms
		JdbcTypeMapper typeMapper = df.getJdbcTypeMapper();
		for (Attribute att : table.getAttributes()) {
			Predicate.COL_TYPE type = typeMapper.getPredicate(att.getType());
			Variable objV = df.getVariable(att.getID().getName());
			Term obj;
			if (type == COL_TYPE.LITERAL) 
				obj = objV;
			else 
				obj = df.getTypedTerm(objV, type);
			
			atoms.add(df.getFunction(df.getDataPropertyPredicate(getLiteralPropertyIRI(att)), sub, obj));
		}

		return atoms;
	}

    /**
     * Definition row graph: an RDF graph consisting of the following triples:
     *
     * - a reference triple for each <column name list> in a table's foreign keys where none of the column values is NULL.
     *
     */
	private List<Function> getRefCQ(ForeignKeyConstraint fk) {
        Term sub = generateSubject(fk.getRelation(), true);
		Term obj = generateSubject(fk.getReferencedRelation(), true);

		Function atom = df.getFunction(df.getObjectPropertyPredicate(getReferencePropertyIRI(fk)), sub, obj);
		return Collections.singletonList(atom);
	}


    /**
     *
     * table IRI:
     *      the relative IRI consisting of the percent-encoded form of the table name.
     *
     * @return table IRI
     */
	private String getTableIRI(RelationID tableId) {
		return baseIRI + percentEncode(tableId.getTableName());
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
    private String getLiteralPropertyIRI(Attribute attr) {
        return baseIRI + percentEncode(attr.getRelation().getID().getTableName())
                + "#" + percentEncode(attr.getID().getName());
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
    private String getReferencePropertyIRI(ForeignKeyConstraint fk) {
        List<String> attributes = new ArrayList<>(fk.getComponents().size());
 		for (Component component : fk.getComponents())
            attributes.add(percentEncode(component.getAttribute().getID().getName()));
        
        return baseIRI + percentEncode(fk.getRelation().getID().getTableName())
                + "#ref-" + Joiner.on(";").join(attributes);
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

    private Term generateSubject(DatabaseRelationDefinition td, boolean ref) {
		
		String varNamePrefix = "";
		if (ref)
			varNamePrefix = td.getID().getTableName() + "_";

		UniqueConstraint pk = td.getPrimaryKey();
		if (pk != null) {
			List<Term> terms = new ArrayList<>(pk.getAttributes().size() + 1);
			
			List<String> attributes = new ArrayList<>(pk.getAttributes().size());
			for (Attribute att : pk.getAttributes()) 
				attributes.add(percentEncode(att.getID().getName()) + "={}");
			
			String template = baseIRI + percentEncode(td.getID().getTableName()) + "/" + Joiner.on(";").join(attributes);
			terms.add(df.getConstantLiteral(template));
			
			for (Attribute att : pk.getAttributes())
				terms.add(df.getVariable(varNamePrefix + att.getID().getName()));

			return df.getUriTemplate(terms);
		}
		else {
			List<Term> vars = new ArrayList<>(td.getAttributes().size());
			for (Attribute att : td.getAttributes())
				vars.add(df.getVariable(varNamePrefix + att.getID().getName()));

			return df.getBNodeTemplate(vars);
		}
	}
    

	/*
	 * percent encoding for a String
	 */
	private static String percentEncode(String pe) {

		pe = pe.replace("'", "%27");
		pe = pe.replace("-", "%2D");
		pe = pe.replace(".", "%2E");
		for (Entry<String, String> e : EncodeForURI.TABLE.entrySet())
			if (!e.getKey().equals("%22"))
				pe = pe.replace(e.getValue(), e.getKey());

		return pe;
	}

}
