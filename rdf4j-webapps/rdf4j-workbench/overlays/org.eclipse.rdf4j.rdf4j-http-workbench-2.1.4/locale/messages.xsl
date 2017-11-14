<?xml version="1.0"?>
<stylesheet version="1.0"
	xmlns="http://www.w3.org/1999/XSL/Transform">

	<!-- Titles -->
	<variable name="workbench.title">RDF4J Workbench</variable>
	<variable name="information.title">System Information</variable>
	<variable name="detail-information.title">
		Detail System Information
	</variable>
	<variable name="repository-list.title">
		List of Repositories
	</variable>
	<variable name="summary.title">Summary</variable>
	<variable name="explore.title">Explore</variable>
	<variable name="namespaces.title">
		Namespaces In Repository
	</variable>
	<variable name="contexts.title">Contexts In Repository</variable>
	<variable name="types.title">Types In Repository</variable>
	<variable name="query.title">Query Repository</variable>
	<variable name="saved-queries.title">Saved Queries</variable>
	<variable name="query-result.title">Query Result</variable>
	<variable name="export.title">Export Repository</variable>
	<variable name="update.title">Execute SPARQL Update on Repository</variable>
	<variable name="add.title">Add RDF</variable>
	<variable name="remove.title">Remove Statements</variable>
	<variable name="clear.title">Clear Repository</variable>
	<variable name="change-server.title">Connect to RDF4J Server</variable>
	<variable name="selections.title">Current Selections</variable>
	<variable name="repository-create.title">New Repository</variable>
	<variable name="repository-delete.title">
		Delete Repository
	</variable>
	<variable name="repository-location.title">
		Repository Location
	</variable>
	<variable name="repository-size.title">Repository Size</variable>
	<variable name="bad-request.title">Bad Request</variable>
	<variable name="super-classes.title">Super Classes</variable>
	<variable name="sub-classes.title">Sub Classes</variable>
	<variable name="individulas.title">Individuals</variable>
	<variable name="properties.title">Properties</variable>
	<variable name="property-domain.title">Domain</variable>
	<variable name="property-range.title">Range</variable>
	<variable name="super-properties.title">Super Properties</variable>
	<variable name="sub-properties.title">Sub Properties</variable>

	<!-- Labels -->
	<variable name="workbench.label">RDF4J Workbench</variable>
	<variable name="repository-list.label">Repositories</variable>
	<variable name="system.label">System</variable>
	<variable name="information.label">Information</variable>
	<variable name="detail-information.label">
		Detail Information
	</variable>
	<variable name="summary.label">Summary</variable>
	<variable name="explore.label">Explore</variable>
	<variable name="namespaces.label">Namespaces</variable>
	<variable name="contexts.label">Contexts</variable>
	<variable name="types.label">Types</variable>
	<variable name="query.label">Query</variable>
	<variable name="rule-query.label">Rule Query</variable>
	<variable name="matcher-query.label">Matcher Query (Optional)</variable>
	<variable name="saved-queries.label">Saved Queries</variable>
	<variable name="export.label">Export</variable>
	<variable name="modify.label">Modify</variable>
	<variable name="update.label">Update</variable>
	<variable name="sparqlupdate.label">SPARQL Update</variable>
	<variable name="add.label">Add</variable>
	<variable name="remove.label">Remove</variable>
	<variable name="delete.label">Delete</variable>
	<variable name="create.label">Create</variable>
	<variable name="next.label">Next</variable>
	<variable name="previous.label">Previous</variable>
	<variable name="cancel.label">Cancel</variable>
	<variable name="show.label">Show</variable>
	<variable name="clear.label">Clear</variable>
	<variable name="execute.label">Execute</variable>
	<variable name="change-server.label">RDF4J Server URL</variable>
	<variable name="change.label">Change</variable>
	<variable name="server.label">RDF4J Server</variable>
	<variable name="server-user.label">User (optional)</variable>
	<variable name="server-password.label">Password (optional)</variable>
	<variable name="repository.label">Repository</variable>
	<variable name="download.label">Download</variable>
	<variable name="download-format.label">Download format</variable>
	<variable name="change-server.desc">for example: http://localhost:8080/rdf4j-server</variable>
	<variable name="repository-create.label">New repository</variable>
	<variable name="repository-delete.label">
		Delete repository
	</variable>
	<variable name="show-datatypes.label">Show data types &amp; language tags</variable>

	<!-- General labels -->
	<variable name="copyright.label">
		Copyright © 2015 Eclipse RDF4J Contributors
	</variable>
	<variable name="true.label">Yes</variable>
	<variable name="false.label">No</variable>
	<variable name="none.label">None</variable>
	<variable name="all.label">All</variable>
	<variable name="readable.label">Readable</variable>
	<variable name="writeable.label">Writeable</variable>

	<!-- Fields -->
	<variable name="base-uri.label">Base URI</variable>
	<variable name="clear-warning.desc">
		WARNING: Clearing the repository will remove all statements.
		This operation cannot be undone.
	</variable>
	<variable name="remove-warning.desc">
		WARNING: Specifying only a context will remove all statements belonging to that context.
		This operation cannot be undone.
	</variable>
	<variable name="SYSTEM-warning.desc">
		WARNING: Modifying the SYSTEM repository directly is not
		advised.
	</variable>
	<variable name="clear-context.label">Clear Context(s)</variable>
	<variable name="context.label">Context</variable>
	<variable name="data-format.label">Data format</variable>
	<variable name="include-inferred.label">
		Include inferred statements
	</variable>
	<variable name="save-private.label">Save privately (do not share)</variable>
	<variable name="save.label">Save query</variable>
	<variable name="object.label">Object</variable>
	<variable name="predicate.label">Predicate</variable>
	<variable name="query-options.label">Action Options</variable>
	<variable name="query-actions.label">Actions</variable>
	<variable name="query-language.label">Query Language</variable>
	<variable name="query-string.label">Query</variable>
	<variable name="update-string.label">Update</variable>
	<variable name="subject.label">Subject</variable>
	<variable name="upload-file.desc">
		Select the file containing the RDF data you wish to upload
	</variable>
	<variable name="upload-file.label">RDF Data File</variable>
	<variable name="upload-text.desc">
		Enter the RDF data you wish to upload
	</variable>
	<variable name="upload-text.label">RDF Content</variable>
	<variable name="upload-url.desc">
		Location of the RDF data you wish to upload
	</variable>
	<variable name="upload-url.label">RDF Data URL</variable>
	<variable name="value-encoding.desc">
		Please specify subject, predicate, object and/or context of the
		statements that should be removed. Empty fields match with any
		subject, predicate, object or context. URIs, bNodes and literals should
		be entered using the N-Triples encoding. Example values in
		N-Triples encoding are:
	</variable>
	<variable name="result-limit.label">Results per page</variable>
	<variable name="result-offset.label">Results offset</variable>
	<variable name="limit10.label">10</variable>
	<variable name="limit50.label">50</variable>
	<variable name="limit100.label">100</variable>
	<variable name="limit200.label">200</variable>
	<variable name="result-limited.desc">
		The results shown maybe truncated.
	</variable>
	<variable name="prefix.label">Prefix</variable>
	<variable name="namespace.label">Namespace</variable>
	<variable name="repository-type.label">Type</variable>
	<variable name="repository-id.label">ID</variable>
	<variable name="repository-title.label">Title</variable>
	<variable name="repository-persist.label">Persist</variable>
	<variable name="repository-sync-delay.label">Sync delay</variable>
	<variable name="repository-indexes.label">Triple indexes</variable>
	<variable name="repository-evaluation-mode.label">Evaluation mode</variable>
	<variable name="remote-repository-server.label">
		RDF4J Server locations
	</variable>
	<variable name="remote-repository-id.label">
		Remote repository ID
	</variable>
	<variable name="sparql-repository-query-endpoint.label">SPARQL query endpoint URL</variable>
	<variable name="sparql-repository-update-endpoint.label">SPARQL update endpoint URL (optional)</variable>
	<variable name="federation-members.label">Federation members</variable>
	<variable name="distinct.label">Distinct</variable>
	<variable name="read-only.label">Read-only</variable>
	<variable name="federation-type.label">Member type</variable>
	<variable name="jdbc-driver.label">JDBC Driver</variable>
	<variable name="jdbc-host.label">Host</variable>
	<variable name="jdbc-port.label">Port</variable>
	<variable name="jdbc-database.label">Database</variable>
	<variable name="jdbc-properties.label">
		Connection properties
	</variable>
	<variable name="jdbc-user.label">User Name</variable>
	<variable name="jdbc-password.label">Password</variable>
	<variable name="max-triple-tables.label">
		Maximum number of triple tables
	</variable>
	<variable name="resource.label">Resource</variable>


	<!-- Information Fields -->
	<variable name="application-information.title">
		Application Information
	</variable>
	<variable name="application-name.label">Application Name</variable>
	<variable name="application-version.label">Version</variable>
	<variable name="data-directory.label">Data Directory</variable>
	<variable name="java-runtime.label">Java Runtime</variable>
	<variable name="maximum-memory.label">Maximum</variable>
	<variable name="memory.title">Memory</variable>
	<variable name="memory-used.label">Used</variable>
	<variable name="operating-system.label">Operating System</variable>
	<variable name="process-user.label">Process User</variable>
	<variable name="runtime-information.title">
		Runtime Information
	</variable>
	<variable name="system-properties.title">
		System and Environment Properties
	</variable>
	<variable name="repository-location.label">Location</variable>
	<variable name="repository-size.label">Number of Statements</variable>
	<variable name="repository-contexts-size.label">Number of Labeled Contexts</variable>
	<variable name="number-of-namespaces.label">Namespaces</variable>
	<variable name="number-of-contexts.label">Contexts</variable>

</stylesheet>
