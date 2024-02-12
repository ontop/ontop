package it.unibz.inf.ontop.spec.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.spec.dbschema.impl.ImplicitDBConstraintsProviderFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.MappingTestingTools.createMetadataProviderBuilder;
import static org.junit.jupiter.api.Assertions.*;

public class ImplicitDBConstraintsTest {

	private static final String DIR = "src/test/resources/userconstraints/";

	private static final ImplicitDBConstraintsProviderFactory CONSTRAINT_EXTRACTOR = Guice.createInjector()
			.getInstance(ImplicitDBConstraintsProviderFactoryImpl.class);

	private MetadataProvider md;

	@BeforeEach
	void setup() {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
		DBTermType stringDBType = builder.getDBTypeFactory().getDBStringType();

		builder.createDatabaseRelation("TABLENAME",
				"KEYNAME", stringDBType, false,
				"ATTR1", stringDBType, false, // not null from metadata, not touched by constraint file
				"ATTR2", stringDBType, true, // nullable in metadata, not null in constraint file
				"ATTR3", stringDBType, true); // nullable in metadata, not touched by constraint file

		builder.createDatabaseRelation("TABLE2",
				"KEY1", stringDBType, false,
				"KEY2", stringDBType, false);

		md = builder.build();
	}
	
	@Test
	public void testEmptyUserConstraints() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(Optional.of(new File(DIR + "empty_constraints.lst")), md);
		List<RelationID> refs = uc.getRelationIDs();
		NamedRelationDefinition rel1 = uc.getRelation(refs.get(0));
		NamedRelationDefinition rel2 = uc.getRelation(refs.get(1));
		assertAll(
				() -> assertEquals(ImmutableList.of(), rel1.getUniqueConstraints()),
				() -> assertEquals(ImmutableList.of(), rel2.getUniqueConstraints()),
				() -> assertEquals(ImmutableList.of(), rel1.getForeignKeys()),
				() -> assertEquals(ImmutableList.of(), rel2.getForeignKeys()));
	}

	@Test
	public void testPKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(Optional.of(new File(DIR + "pkeys.lst")), md);
		List<RelationID> refs = uc.getRelationIDs();
		NamedRelationDefinition rel1 = uc.getRelation(refs.get(0));
		NamedRelationDefinition rel2 = uc.getRelation(refs.get(1));
		assertAll(
				() -> assertEquals(ImmutableList.of(), rel1.getUniqueConstraints()),
				() -> assertEquals(ImmutableList.of(), rel2.getUniqueConstraints()),
				() -> assertEquals(ImmutableList.of(), rel1.getForeignKeys()),
				() -> assertEquals(ImmutableList.of(), rel2.getForeignKeys()));
	}

	@Test
	public void testAddPrimaryKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(Optional.of(new File(DIR + "pkeys.lst")), md);
		NamedRelationDefinition table = uc.getRelation(uc.getQuotedIDFactory().createRelationID("TABLENAME"));
		uc.insertIntegrityConstraints(table, uc);
		Attribute attr = table.getAttribute(1);
		assertEquals(ImmutableList.of(attr), table.getUniqueConstraints().get(0).getAttributes());
	}


	@Test
	public void testTables() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(Optional.of(new File(DIR + "fkeys.lst")), md);
		List<RelationID> refs = uc.getRelationIDs();
		NamedRelationDefinition rel1 = uc.getRelation(refs.get(0));
		NamedRelationDefinition rel2 = uc.getRelation(refs.get(1));
		assertAll(
				() -> assertEquals(ImmutableList.of(), rel1.getUniqueConstraints()),
				() -> assertEquals(ImmutableList.of(), rel2.getUniqueConstraints()),
				() -> assertEquals(ImmutableList.of(), rel1.getForeignKeys()),
				() -> assertEquals(ImmutableList.of(), rel2.getForeignKeys()));
	}

	@Test
	public void testAddForeignKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(Optional.of(new File(DIR + "fkeys.lst")), md);
		NamedRelationDefinition table = uc.getRelation(uc.getQuotedIDFactory().createRelationID("TABLENAME"));
		uc.insertIntegrityConstraints(table, uc);
		ForeignKeyConstraint fk = table.getForeignKeys().get(0);
		Attribute ref = fk.getComponents().get(0).getReferencedAttribute();
		assertAll(
				() -> assertEquals(md.getQuotedIDFactory().createRelationID("TABLE2"), ((NamedRelationDefinition)ref.getRelation()).getID()),
				() -> assertEquals(md.getQuotedIDFactory().createAttributeID("KEY1"), ref.getID()));
	}

	@Test
	public void testAddKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(Optional.of(new File(DIR + "keys.lst")), md);
		NamedRelationDefinition table = uc.getRelation(uc.getQuotedIDFactory().createRelationID("TABLENAME"));
		uc.insertIntegrityConstraints(table, uc);
		ForeignKeyConstraint fk = table.getForeignKeys().get(0);
		Attribute ref = fk.getComponents().get(0).getReferencedAttribute();
		assertAll(
				() -> assertEquals(md.getQuotedIDFactory().createRelationID("TABLE2"), ((NamedRelationDefinition)ref.getRelation()).getID()),
				() -> assertEquals(md.getQuotedIDFactory().createAttributeID("KEY1"), ref.getID()),
				() -> assertEquals(ImmutableList.of(table.getAttribute(1)), table.getUniqueConstraints().get(0).getAttributes()));
	}

	@Test
	public void testAddConstraintsExtendedSyntax() throws MetadataExtractionException {
		QuotedIDFactory idf = md.getQuotedIDFactory();
		MetadataProvider mp = CONSTRAINT_EXTRACTOR.extract(Optional.of(new File(DIR + "all_constraints_extended_syntax.lst")), md);
		NamedRelationDefinition table = mp.getRelation(mp.getQuotedIDFactory().createRelationID("TABLENAME"));
		NamedRelationDefinition table2 = mp.getRelation(mp.getQuotedIDFactory().createRelationID("TABLE2"));
		mp.insertIntegrityConstraints(table, mp);
		mp.insertIntegrityConstraints(table2, mp);

		// Check for TABLENAME attribute nullability: ATTR1 not null (metadata), ATTR2 not null (constraint file), ATTR3 nullable (metadata)
		assertAll(
				() -> assertFalse(table.getAttribute(idf.createAttributeID("ATTR1")).isNullable()),
				() -> assertFalse(table.getAttribute(idf.createAttributeID("ATTR2")).isNullable()),
				() -> assertTrue(table.getAttribute(idf.createAttributeID("ATTR3")).isNullable()));

		// Check for unique constraint TABLE2(KEY1,KEY2)
		List<UniqueConstraint> ucs = table2.getUniqueConstraints();
		assertAll(
				() -> assertEquals(1, ucs.size()),
				() -> assertFalse(ucs.get(0).isPrimaryKey()),
				() -> assertEquals(2, ucs.get(0).getAttributes().size()),
				() -> assertEquals(idf.createAttributeID("KEY1"), ucs.get(0).getAttributes().get(0).getID()),
				() -> assertEquals(idf.createAttributeID("KEY2"), ucs.get(0).getAttributes().get(1).getID()));

		// Check for primary key TABLENAME(KEYNAME)
		UniqueConstraint pk = table.getPrimaryKey().orElse(null);
		assertNotNull(pk);
		assertAll(
				() -> assertTrue(pk.isPrimaryKey()),
				() -> assertEquals(1, pk.getAttributes().size()),
				() -> assertEquals(idf.createAttributeID("KEYNAME"), pk.getAttributes().get(0).getID()));

		// Check for (exactly one) foreign key TABLENAME(KEYNAME) REFERENCES TABLE2(KEY1)
		List<ForeignKeyConstraint> fks = table.getForeignKeys();
		assertAll(
				() -> assertEquals(1, fks.size()),
				() -> assertEquals(1, fks.get(0).getComponents().size()),
				() -> assertEquals(idf.createRelationID("TABLE2"), fks.get(0).getReferencedRelation().getID()),
				() -> assertEquals(idf.createAttributeID("KEY1"), fks.get(0).getComponents().get(0).getReferencedAttribute().getID()),
				() -> assertEquals(idf.createAttributeID("KEYNAME"), fks.get(0).getComponents().get(0).getAttribute().getID()));
	}

}
