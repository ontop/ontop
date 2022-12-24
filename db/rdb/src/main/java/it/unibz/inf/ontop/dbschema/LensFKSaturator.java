package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.dbschema.impl.json.JsonLens;

public interface LensFKSaturator {

    void saturateForeignKeys(ImmutableList<Lens> lenses,
                             ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                             ImmutableMap<RelationID, JsonLens> jsonLensMap);
}
