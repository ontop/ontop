package it.unibz.inf.ontop.answering.reformulation.input.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import org.eclipse.rdf4j.query.algebra.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RDF4JConstructTemplate implements ConstructTemplate {
	@Nonnull
    private final UnaryTupleOperator projection;
    @Nullable
	private final Extension extension;

	RDF4JConstructTemplate(@Nonnull UnaryTupleOperator projection, @Nullable Extension extension) {
		this.projection = projection;
		this.extension = extension;
	}

	@Override
	public ImmutableList<ProjectionElemList> getProjectionElemList() {
		if (projection instanceof Projection) {
			return ImmutableList.of(((Projection) projection).getProjectionElemList());
		}
		if (projection instanceof MultiProjection) {
			return ImmutableList.copyOf(((MultiProjection) projection).getProjections());
		}
		return ImmutableList.of();
	}

	@Override
	public Extension getExtension() {
		return extension;
	}
}
