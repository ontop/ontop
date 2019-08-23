package it.unibz.inf.ontop.spec.mapping.converter;

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

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingExceptionWithIndicator;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.protege.core.OBDADataSourceFactory;
import it.unibz.inf.ontop.protege.core.impl.OBDADataSourceFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Optional;
import java.util.Properties;

/**
 * Refer to the Ontop Native Mapping Language for SQL, get DataSource information from the file
 * and return a Reader with the mapping information without the SourceDeclaration
 *
 */
public class OldSyntaxMappingConverter {


    protected enum Label {
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, target, source
    }

    protected static final String SOURCE_DECLARATION_TAG = "[SourceDeclaration]";

    private static final OBDADataSourceFactory DS_FACTORY = OBDADataSourceFactoryImpl.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(OldSyntaxMappingConverter.class);
    private Properties dataSourceProperties;
    private Reader outputReader;


    /**
     *  Read the old Syntax obda mapping file and extract the data source information
     */

    public OldSyntaxMappingConverter(Reader fileReader, String fileName) throws InvalidMappingExceptionWithIndicator, MappingIOException, DuplicateMappingException {

        outputReader = new StringReader(extractSourceDeclaration(fileReader, fileName).toString());

    }

    //return the extracted obda data source

    public Optional<Properties> getOBDADataSourceProperties() throws InvalidMappingException, DuplicateMappingException, MappingIOException {

        return Optional.ofNullable(dataSourceProperties);

    }

    //return the content of the  mapping file without the source declaration part
    public Reader getOutputReader() {
        return outputReader;
    }


	private  Writer extractSourceDeclaration(Reader reader, String fileName )
            throws MappingIOException, InvalidMappingExceptionWithIndicator, DuplicateMappingException {
		
		String line;
        Writer fileWriter = new StringWriter();

        try (LineNumberReader lineNumberReader = new LineNumberReader(reader)) {

            while ((line = lineNumberReader.readLine()) != null) {
                try {
                    if (line.contains(SOURCE_DECLARATION_TAG)) {
                        LOG.warn("Old Syntax in the OBDA file, the datasource declaration will be removed from the mapping file.");

                        readSourceDeclaration(lineNumberReader);

                    } else {
                        fileWriter.write(line + System.lineSeparator());

                        continue;
                    }
                } catch (Exception e) {
                    throw new IOException(String.format("ERROR reading %s at line: %s", fileName,
                            lineNumberReader.getLineNumber()
                                    + " \nMESSAGE: " + e.getMessage()), e);
                }
            }

            fileWriter.close();

        } catch (IOException e) {
            throw new MappingIOException(e);
        }



        return fileWriter;

	}

	//read and store datasource information
    private void readSourceDeclaration(LineNumberReader reader) throws IOException {
        String line;
        dataSourceProperties  = new Properties();

        while (!(line = reader.readLine()).isEmpty()) {
            int lineNumber = reader.getLineNumber();
            String[] tokens = line.split("[\t| ]+", 2);

            final String parameter = tokens[0].trim();
            final String inputParameter = tokens[1].trim();

            if (parameter.equals(Label.sourceUri.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_NAME, inputParameter);
            } else if (parameter.equals(Label.connectionUrl.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_URL, inputParameter);
            } else if (parameter.equals(Label.username.name())) {
                dataSourceProperties.put(OntopSQLCredentialSettings.JDBC_USER, inputParameter);
            } else if (parameter.equals(Label.password.name())) {
                dataSourceProperties.put(OntopSQLCredentialSettings.JDBC_PASSWORD, inputParameter);
            } else if (parameter.equals(Label.driverClass.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_DRIVER, inputParameter);

            } else {
                String msg = String.format("Unknown parameter name \"%s\" at line: %d.", parameter, lineNumber);
                throw new IOException(msg);
            }
        }

    }

}
