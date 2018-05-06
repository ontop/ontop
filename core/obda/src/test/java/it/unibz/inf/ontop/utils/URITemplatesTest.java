package it.unibz.inf.ontop.utils;

/*
 * #%L
 * ontop-obdalib-core
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

import java.util.Arrays;

import it.unibz.inf.ontop.injection.OntopModelConfiguration;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URITemplatesTest {

    private static final TermFactory TERM_FACTORY = OntopModelConfiguration.defaultBuilder().build().getTermFactory();
	
	@SuppressWarnings("unchecked")
    @Test
	public void testFormat(){
		assertEquals("http://example.org/A/1", URITemplates.format("http://example.org/{}/{}", "A", 1));
		
		assertEquals("http://example.org/A", URITemplates.format("http://example.org/{}", "A"));
		
		assertEquals("http://example.org/A/1", URITemplates.format("http://example.org/{}/{}", Arrays.asList("A", 1)));
		
		assertEquals("http://example.org/A", URITemplates.format("http://example.org/{}", Arrays.asList("A")));

        assertEquals("http://example.org/A", URITemplates.format("{}", Arrays.asList("http://example.org/A")));
	}

    @Test
    public void testGetUriTemplateString1(){
        ImmutableFunctionalTerm f1 = TERM_FACTORY.getImmutableUriTemplate(TERM_FACTORY.getConstantLiteral("http://example.org/{}/{}"), //
                TERM_FACTORY.getVariable("X"), TERM_FACTORY.getVariable("Y"));
        assertEquals("http://example.org/{X}/{Y}", URITemplates.getUriTemplateString(f1));
    }

    @Test
    public void testGetUriTemplateString2(){
        ImmutableFunctionalTerm f1 = TERM_FACTORY.getImmutableUriTemplate(TERM_FACTORY.getConstantLiteral("{}"), //
                TERM_FACTORY.getVariable("X"));
        assertEquals("{X}", URITemplates.getUriTemplateString(f1));
    }

    @Test
    public void testGetUriTemplateString3(){

        ImmutableFunctionalTerm f1 = TERM_FACTORY.getImmutableUriTemplate(TERM_FACTORY.getConstantLiteral("{}/"), //
                TERM_FACTORY.getVariable("X"));
        assertEquals("{X}/", URITemplates.getUriTemplateString(f1));
    }

    @Test
    public void testGetUriTemplateString4(){

        ImmutableFunctionalTerm f1 = TERM_FACTORY.getImmutableUriTemplate(TERM_FACTORY.getConstantLiteral("http://example.org/{}/{}/"), //
                TERM_FACTORY.getVariable("X"), TERM_FACTORY.getVariable("Y"));
        assertEquals("http://example.org/{X}/{Y}/", URITemplates.getUriTemplateString(f1));
    }

    @Test
    public void testGetUriTemplateString5(){

        ImmutableFunctionalTerm f1 = TERM_FACTORY.getImmutableUriTemplate(TERM_FACTORY.getConstantLiteral("http://example.org/{}/{}/{}"), //
                TERM_FACTORY.getVariable("X"), TERM_FACTORY.getVariable("Y"), TERM_FACTORY.getVariable("X"));
        assertEquals("http://example.org/{X}/{Y}/{X}", URITemplates.getUriTemplateString(f1));
    }
}
