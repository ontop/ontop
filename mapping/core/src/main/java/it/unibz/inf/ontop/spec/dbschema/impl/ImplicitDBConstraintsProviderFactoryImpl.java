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

        if (!constraintFile.isPresent())
            return baseMetadataProvider;

        try (BufferedReader reader = new BufferedReader(new FileReader(constraintFile.get()))) {
            ImmutableList.Builder<DatabaseRelationDescriptor> ucBuilder = ImmutableList.builder();
            ImmutableList.Builder<Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor>> fkBuilder = ImmutableList.builder();
            QuotedIDFactory idFactory = baseMetadataProvider.getQuotedIDFactory();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] s = line.split(":");
                if (s.length == 2) { // Primary Key	/ Unique Constraint
                    ucBuilder.add(new DatabaseRelationDescriptor(idFactory, s[0], s[1].split(",")));
                }
                else if (s.length == 4) { // Foreign Key
                    fkBuilder.add(Maps.immutableEntry(
                            new DatabaseRelationDescriptor(idFactory, s[0], s[1].split(",")),
                            new DatabaseRelationDescriptor(idFactory, s[2], s[3].split(","))));
                }
            }

            ImmutableList<Map.Entry<DatabaseRelationDescriptor, DatabaseRelationDescriptor>> foreignKeys = fkBuilder.build();

            String offenders = foreignKeys.stream()
                    .filter(c -> c.getKey().attributeIds.size() != c.getValue().attributeIds.size())
                    .map(c -> c.getKey() + " does not match " + c.getValue())
                    .collect(Collectors.joining(", "));

            if (!offenders.isEmpty())
                throw new MetadataExtractionException("Different numbers of columns for user-supplied foreign keys: " + offenders);

            return new ImplicitDBConstraintsProvider(baseMetadataProvider, ucBuilder.build(), foreignKeys);
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
