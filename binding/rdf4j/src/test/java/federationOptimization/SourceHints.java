package federationOptimization;

import java.util.HashSet;
import java.util.Set;

public class SourceHints {
    public Set<EmptyFederatedJoin> emptyFJs;
    public Set<Redundancy> redundancy;
    public Set<MaterializedView> matView;

    public SourceHints(){
        emptyFJs = new HashSet<EmptyFederatedJoin>();
        redundancy = new HashSet<Redundancy>();
        matView = new HashSet<MaterializedView>();
    }
}
