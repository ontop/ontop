package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.spec.dbschema.impl.ImplicitDBConstraintsProvider.DatabaseRelationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Moved from ImplicitDBContraintsReader (by Dag Hovland)
 */

@Singleton
public class ImplicitDBConstraintsProviderFactoryImpl implements ImplicitDBConstraintsProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImplicitDBConstraintsProviderFactoryImpl.class);

    @Inject
    private ImplicitDBConstraintsProviderFactoryImpl() {
    }

    @Override
    public MetadataProvider extract(Optional<File> constraintFile, MetadataProvider baseMetadataProvider) throws MetadataExtractionException {

        if (constraintFile.isEmpty())
            return baseMetadataProvider;

        try (BufferedReader reader = new BufferedReader(new FileReader(constraintFile.get()))) {

            ImmutableList.Builder<DatabaseRelationDescriptor> nnBuilder = ImmutableList.builder(); // not null
            ImmutableList.Builder<DatabaseRelationDescriptor> ucBuilder = ImmutableList.builder(); // unique
            ImmutableList.Builder<DatabaseRelationDescriptor> pkBuilder = ImmutableList.builder(); // primary key
            ImmutableList.Builder<Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor>> fkBuilder = ImmutableList.builder(); // foreign key

            QuotedIDFactory idFactory = baseMetadataProvider.getQuotedIDFactory();

            int lineNum = 0;
            String line;
            while ((line = reader.readLine()) != null) {

                // Skip empty lines and comment lines starting with '#' (after trimming)
                ++lineNum;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Tokenize, trimming arguments (space that are part of table/attribute names are handled by quoting)
                String[] s = line.split(":");
                for (int i = 0; i < s.length; ++i) {
                    s[i] = s[i].trim();
                }

                // Determine the type of constraint, editing 's' so that it contains only the constraint arguments
                String type = null;
                if (s.length >= 2 && s[0].isEmpty()) { // extended generic syntax ":type:...args..."
                    type = s[1].toLowerCase();
                    s = Arrays.copyOfRange(s, 2, s.length);
                } else if (s.length == 2) { // unique constraint format "rel:attr_list"
                    type = "uc";
                } else if (s.length == 4) { // foreigk key format "rel1:attr_list1:rel2:attr_list2"
                    type = "fk";
                }

                // Record recognized constraints, skipping unrecognized ones
                if ("nn".equals(type)) {
                    nnBuilder.add(new DatabaseRelationDescriptor(idFactory, s[0], s[1].split(",")));
                } else if ("uc".equals(type)) {
                    ucBuilder.add(new DatabaseRelationDescriptor(idFactory, s[0], s[1].split(",")));
                } else if ("pk".equals(type)) {
                    pkBuilder.add(new DatabaseRelationDescriptor(idFactory, s[0], s[1].split(",")));
                } else if ("fk".equals(type)) {
                    fkBuilder.add(Maps.immutableEntry(
                            new DatabaseRelationDescriptor(idFactory, s[0], s[1].split(",")),
                            new DatabaseRelationDescriptor(idFactory, s[2], s[3].split(","))));
                } else {
                    LOGGER.warn("Unrecognized constraint at line {}: '{}'", lineNum, line);
                }
            }

            ImmutableList<Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor>> foreignKeys = fkBuilder.build();

            String offenders = foreignKeys.stream()
                    .filter(c -> c.getKey().attributeIds.size() != c.getValue().attributeIds.size())
                    .map(c -> c.getKey() + " does not match " + c.getValue())
                    .collect(Collectors.joining(", "));

            if (!offenders.isEmpty())
                throw new MetadataExtractionException("Different numbers of columns for user-supplied foreign keys: " + offenders);

            return new ImplicitDBConstraintsProvider(baseMetadataProvider, nnBuilder.build(), ucBuilder.build(), pkBuilder.build(), foreignKeys);
        }
        catch (FileNotFoundException e) {
            LOGGER.warn("Could not find file {} in directory {}\nCurrent dir using System:{}",
                    constraintFile, System.getenv().get("PWD"), System.getProperty("user.dir"));
            throw new MetadataExtractionException("Constraint file " + constraintFile + " does not exist");
        }
        catch (IOException e) {
            LOGGER.warn("Problem reading keys from the constraint file {}\n{}", constraintFile, e.getMessage());
            throw new MetadataExtractionException(e);
        }
    }
}
