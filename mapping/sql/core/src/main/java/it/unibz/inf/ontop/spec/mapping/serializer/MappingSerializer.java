package it.unibz.inf.ontop.spec.mapping.serializer;

import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;

import java.io.File;
import java.io.IOException;

public interface MappingSerializer {

    void write(File file, SQLPPMapping ppMapping) throws IOException;

}
