package it.unibz.inf.ontop.io;

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
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDADataSourceFactory;
import it.unibz.inf.ontop.model.impl.OBDADataSourceFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Optional;

/**
 * Refer to the Ontop Native Mapping Language for SQL, remove DataSource information from the file
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
    private OBDADataSource dataSource;
    private Reader outputReader;


    /**
     * Create an SQL Mapping Parser for generating an OBDA model.
     */

    public OldSyntaxMappingConverter(Reader fileReader, String fileName) throws InvalidMappingExceptionWithIndicator, MappingIOException, DuplicateMappingException {

        outputReader = new StringReader(load(fileReader, fileName).toString());

    }

    public Optional<OBDADataSource> getOBDADataSource() throws InvalidMappingException, DuplicateMappingException, MappingIOException {

        return Optional.of(dataSource);

    }

    public Reader getOutputReader() {
        return outputReader;
    }


	private  Writer load(Reader reader, String fileName )
            throws MappingIOException, InvalidMappingExceptionWithIndicator, DuplicateMappingException {
		
		String line;
        Writer fileWriter = new StringWriter();

        try (LineNumberReader lineNumberReader = new LineNumberReader(reader)) {

            while ((line = lineNumberReader.readLine()) != null) {
                try {
                    if (line.contains(SOURCE_DECLARATION_TAG)) {
                        LOG.warn("Old Syntax OBDA file the datasource declaration will be removed from the mapping file.");

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

	//read datasource information and store it as a property
    private void readSourceDeclaration(LineNumberReader reader) throws IOException {
        String line;
        String jdbcUrl = "", userName= "", password= "", driverClass = "";
        String sourceUri = "";
        while (!(line = reader.readLine()).isEmpty()) {
            int lineNumber = reader.getLineNumber();
            String[] tokens = line.split("[\t| ]+", 2);

            final String parameter = tokens[0].trim();
            final String inputParameter = tokens[1].trim();
            if (parameter.equals(Label.sourceUri.name())) {
                sourceUri = inputParameter;
            } else if (parameter.equals(Label.connectionUrl.name())) {
                 jdbcUrl = inputParameter;
            } else if (parameter.equals(Label.username.name())) {
                 userName = inputParameter;
            } else if (parameter.equals(Label.password.name())) {
                 password = inputParameter;
            } else if (parameter.equals(Label.driverClass.name())) {
                 driverClass =  inputParameter;
            } else {
                String msg = String.format("Unknown parameter name \"%s\" at line: %d.", parameter, lineNumber);
                throw new IOException(msg);
            }
        }
        dataSource = DS_FACTORY.getJDBCDataSource(sourceUri, jdbcUrl,userName,password,driverClass);
    }

}
