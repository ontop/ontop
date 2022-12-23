package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.dbschema.impl.json.JsonView;

public interface OntopViewFKSaturator {

    void saturateForeignKeys(ImmutableList<Lens> viewDefinitions,
                             ImmutableMultimap<RelationID, RelationID> childrenMultimap,
                             ImmutableMap<RelationID, JsonView> jsonViewMap);
}
