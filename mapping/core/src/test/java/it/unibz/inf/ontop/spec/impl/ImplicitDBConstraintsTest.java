package it.unibz.inf.ontop.spec.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.spec.dbschema.impl.ImplicitDBConstraintsProviderFactoryImpl;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.*;

public class ImplicitDBConstraintsTest {

	private static final String DIR = "src/test/resources/userconstraints/";
	private static final MetadataProvider md;

	private static final ImplicitDBConstraintsProviderFactory CONSTRAINT_EXTRACTOR = Guice.createInjector()
			.getInstance(ImplicitDBConstraintsProviderFactoryImpl.class);

	private static final NamedRelationDefinition TABLENAME, TABLE2;

	static {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
		DBTermType stringDBType = builder.getDBTypeFactory().getDBStringType();

		TABLENAME = builder.createDatabaseRelation("TABLENAME",
			"KEYNAME", stringDBType, false);

		TABLE2 = builder.createDatabaseRelation( "TABLE2",
			"KEY1", stringDBType, false,
			"KEY2", stringDBType, false);

		md = builder.build();
	}
	
	@Test
	public void testEmptyUserConstraints() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "empty_constraints.lst")), md);

		List<RelationID> refs = uc.getRelationIDs();
		assertEquals(2, refs.size());
	}

	@Test
	public void testPKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "pkeys.lst")), md);
		List<RelationID> refs = uc.getRelationIDs();
		assertEquals(2, refs.size());
	}

	@Test
	public void testAddPrimaryKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "pkeys.lst")), md);
		uc.insertIntegrityConstraints(TABLENAME, uc);
		Attribute attr = TABLENAME.getAttribute(1);
		assertEquals(ImmutableList.of(attr), TABLENAME.getUniqueConstraints().get(0).getAttributes());
	}


	@Test
	public void testTables() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "fkeys.lst")), md);
		List<RelationID> refs = uc.getRelationIDs();
		assertEquals(2, refs.size());
		assertTrue(refs.contains(md.getQuotedIDFactory().createRelationID( "TABLE2")));
	}

	@Test
	public void testAddForeignKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "fkeys.lst")), md);
		uc.insertIntegrityConstraints(TABLENAME, uc);
		ForeignKeyConstraint fk = TABLENAME.getForeignKeys().get(0);
		assertNotNull(fk);
		Attribute ref = fk.getComponents().get(0).getReferencedAttribute();
		assertEquals(md.getQuotedIDFactory().createRelationID("TABLE2"), ((NamedRelationDefinition)ref.getRelation()).getID());
		assertEquals(md.getQuotedIDFactory().createAttributeID("KEY1"), ref.getID());
	}

	@Test
	public void testAddKeys() throws MetadataExtractionException {
		MetadataProvider uc = CONSTRAINT_EXTRACTOR.extract(
				Optional.of(new File(DIR + "keys.lst")), md);
		uc.insertIntegrityConstraints(TABLENAME, uc);
		ForeignKeyConstraint fk = TABLENAME.getForeignKeys().get(0);
		assertNotNull(fk);
		Attribute ref = fk.getComponents().get(0).getReferencedAttribute();
		assertEquals(md.getQuotedIDFactory().createRelationID("TABLE2"), ((NamedRelationDefinition)ref.getRelation()).getID());
		assertEquals(md.getQuotedIDFactory().createAttributeID("KEY1"), ref.getID());
		assertEquals(ImmutableList.of(TABLENAME.getAttribute(1)),
				TABLENAME.getUniqueConstraints().get(0).getAttributes());
	}
}
