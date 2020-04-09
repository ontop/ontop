package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Optional;

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
            ImmutableList.Builder<String[]> ucBuilder = ImmutableList.builder();
            ImmutableList.Builder<String[]> fkBuilder = ImmutableList.builder();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) { // Primary Key	/ Unique Constraint
                    ucBuilder.add(parts);
                }
                else if (parts.length == 4) { // Foreign Key
                    fkBuilder.add(parts);
                }
            }
            return new ImplicitDBConstraintsProvider(baseMetadataProvider, ucBuilder.build(), fkBuilder.build());
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
