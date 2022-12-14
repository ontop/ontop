package it.unibz.inf.ontop.spec.mapping.validation;

/*
 * #%L
 * ontop-quest-owlapi
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

import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;

import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;


/**
 * This test is adapted from SimpleMappingVirtualABoxTest.
 *
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */

// TODO: find proper location fot the test

public class MetaMappingTargetQueryValidatorTest {

	@Test
	public void testValidate() throws MappingException {
		OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
				.nativeOntopMappingFile("src/test/resources/metamapping.obda")
				.jdbcUrl("dummy")
				.jdbcDriver("dummy")
				.build();

		SQLPPMapping ppMapping = configuration.loadProvidedPPMapping();

        // run validator
		try {
         //   for (SQLPPTriplesMap mapping : ppMapping.getTripleMaps()) {
         //       if (!TargetQueryValidator.validate(mapping.getTargetAtoms(), vocabulary).isEmpty()) {
         //           throw new Exception("Found an invalid target query: " + mapping.getTargetAtoms());
         //       }
         //   }
		}
		catch (Exception e) {
			fail("The target query has problem:" + e.getMessage());
		}
	}
}
