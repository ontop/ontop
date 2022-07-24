package it.unibz.inf.ontop.protege.core;

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

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
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

    private static final Logger LOG = LoggerFactory.getLogger(OldSyntaxMappingConverter.class);

    private static final ImmutableMap<String, String> PARAMETER_MAP = ImmutableMap.of(
            "connectionUrl", OntopSQLCoreSettings.JDBC_URL,
            "username", OntopSQLCredentialSettings.JDBC_USER,
            "password", OntopSQLCredentialSettings.JDBC_PASSWORD,
            "driverClass", OntopSQLCredentialSettings.JDBC_DRIVER);

    protected static final String SOURCE_DECLARATION_TAG = "[SourceDeclaration]";

    private Properties dataSourceProperties;
    private final String restOfFile;


    /**
     *  Read the old-style obda mapping file and extract the data source information
     */

    public OldSyntaxMappingConverter(Reader fileReader, String fileName) throws MappingIOException {
        restOfFile = extractSourceDeclaration(fileReader, fileName);
    }

    //return the extracted obda data source

    public Optional<Properties> getDataSourceProperties()  {
        return Optional.ofNullable(dataSourceProperties);
    }

    public String getRestOfFile() {
        return restOfFile;
    }


	private String extractSourceDeclaration(Reader reader, String fileName) throws MappingIOException {
        // no need in auto-closing - it's closed by the caller
        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = lineNumberReader.readLine()) != null) {
                if (line.contains(SOURCE_DECLARATION_TAG)) {
                    LOG.warn("Old Syntax in the OBDA file, the datasource declaration will be removed from the mapping file.");
                    dataSourceProperties = readSourceDeclaration(lineNumberReader);
                }
                else {
                    sb.append(line).append(System.lineSeparator());
                }
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new MappingIOException(
                    new IOException(String.format("ERROR reading %s at line: %s", fileName,
                            lineNumberReader.getLineNumber()
                                    + " \nMESSAGE: " + e.getMessage()), e));
        }
	}

    private static Properties readSourceDeclaration(LineNumberReader reader) throws IOException {
        Properties dataSourceProperties  = new Properties();

        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            String[] tokens = line.split("[\t| ]+", 2);

            String parameterName = tokens[0].trim();
            String inputParameter = tokens[1].trim();

            if (!parameterName.equals("jdbc.name")) {

                String ontopParameterName = Optional.ofNullable(PARAMETER_MAP.get(parameterName))
                        .orElseThrow(() -> new IOException(String.format(
                                "Unknown parameter name \"%s\" at line: %d.", parameterName, reader.getLineNumber())));

                dataSourceProperties.put(ontopParameterName, inputParameter);
            }
        }
        return dataSourceProperties;
    }
}
