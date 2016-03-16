Teradata JDBC Driver 15.10.00.14

Part of the Teradata Tools and Utilities 15.10 product suite


Get the Latest Software
-----------------------

We are always adding new features to the Teradata JDBC Driver, and
fixing issues that might affect your application.

If you obtained this release of the Teradata JDBC Driver from
physical media, please check whether a newer version is available at
http://downloads.teradata.com/download/connectivity/jdbc-driver


System Requirements
-------------------

This release of the Teradata JDBC Driver requires JDK/JRE 1.4.2, 5.0, 6.0, 7.0, or 8.0.

This release of the Teradata JDBC Driver supports Teradata Database
13.10, 14.00, 14.10, 15.00, and 15.10.


Release Notes
-------------

This section highlights issues that you should be aware of when upgrading to this release
of the Teradata JDBC Driver. Please refer to the Teradata JDBC Driver Reference for more
information about the driver.

This release includes changes to address the following DRs:

DR 179996 Connection parameter LITERAL_UNDERSCORE=ON/OFF for DatabaseMetaData pattern arguments

This release includes changes to address the following DRs, originally included
in release 15.10.00.12:

DR 177757 Skip when possible non-COP hostname lookup before COP Discovery

This release includes changes to address the following DRs, originally included
in release 15.10.00.11:

DR 179413 DatabaseMetaData getImportedKeys/getExportedKeys provide PK_NAME if available

This release includes changes to address the following DRs, originally included
in release 15.00.00.33:

DR 121720 Local escape function teradata_provide
DR 179976 Dynamic RS has -1 activity count (a.k.a. 18446744073709551615) when Java SP fetches past last row

This release includes changes to address the following DRs, originally included
in release 15.00.00.32:

DR 179083 Coverity found null pointer dereferences & resource leaks in JDBC Driver

This release includes changes to address the following DRs, originally included
in release 15.00.00.30:

DR 178714 Escape functions {fn teradata_call_param_rs} and {fn teradata_auto_out_param}

This release includes changes to address the following DRs, originally included
in release 15.00.00.29:

DR 174886 Support Unicode Pass Through

This release includes changes to address the following DRs, originally included
in release 15.00.00.28:

DR 148961 Support backslash ( \ ) as LIKE ESCAPE character in pattern arguments of DatabaseMetaData methods
DR 161165 Use equals (=) rather than LIKE for DatabaseMetaData method pattern arguments not containing a pattern

This release includes changes to address the following DRs, originally included
in release 15.00.00.27:

DR 167410 Provide ResultSet SQLWarning when row count exceeds Integer.MAX_VALUE
DR 176246 Translate new JDBC 15.10 error messages into Japanese

This release includes changes to address the following DRs, originally included
in release 15.00.00.26:

DR 163202 Improve LOB performance
DR 174526 Improve ClientProgramName identification

This release includes changes to address the following DRs, originally included
in release 15.00.00.25:

DR 159947 Support Primary AMP Index
DR 173903 Support pass-thru tokens in SQL request text

This release includes changes to address the following DRs, originally included
in release 15.00.00.23:

DR 128828 provide Monitor partition connection support for Statement.cancel and Statement.setQueryTimeout
DR 170226 Enhance Connection.getClientInfo to return profile query band values

This release includes changes to address the following DRs, originally included
in release 15.00.00.22:

DR 163170 JDBC 4.0 API changes for DatabaseMetaData getNumericFunctions getStringFunctions getTimeDateFunctions
DR 170566 Use TD 15.10 string function RIGHT for escape syntax
DR 170631 Support the INITIATE CHECK command

This release includes changes to address the following DRs, originally included
in release 14.10.00.40:

DR 173521 Avoid sending unneeded Continue Request messages with Cancel parcel

This release includes changes to address the following DRs, originally included
in release 14.10.00.37:

DR 163490 Translate new JDBC 15.0 error messages into Japanese
DR 164061 Support the Connection.isValid method
DR 168727 Support SQL current database for JDBC FastExport SELECT statements
DR 170713 Improve DatabaseMetaData.getUDTs to return Class Name and Data Type for Array Types

This release includes changes to address the following DRs, originally included
in release 14.10.00.36:

DR 168132 avoid JDK call to System.err.println in case of XML transform error
DR 170225 Coverity found null pointer dereferences in JDBC Driver

This release includes changes to address the following DRs, originally included
in release 14.10.00.35:

DR 171241 Increase the JDBC FastLoad maximum transmitted message size to 1MB

This release includes changes to address the following DRs, originally included
in release 14.10.00.34:

DR 148040 Centralized administration for data encryption

This release includes changes to address the following DRs, originally included
in release 14.10.00.33:

DR 155892 Support for detection of replayed or out-of-order messages

This release includes changes to address the following DRs, originally included
in release 14.10.00.32:

DR 144067 Remove all references to the Cryptix source version of the AES algorithm
DR 155353 Complete AES-256 implementation started in 14.0
DR 163542 In wrap or unwrap throw GSSException if MsgProp is NULL
DR 165341 TeraGSS Java on Linux + Oracle/Sun Java blocks on /dev/random increasing logon time
DR 165369 TERAGSSJAVA: LDAP mechanism is not working

This release includes changes to address the following DRs, originally included
in release 14.10.00.27:

DR 162376 Support JSON data type

This release includes changes to address the following DRs, originally included
in release 14.10.00.26:

DR 110776 support INTERVAL data types for implicit data type conversions, and for use with the EXTRACT function
DR 144698 Support TIME WITH TIME ZONE and TIMESTAMP WITH TIME ZONE Struct bind values
DR 162127 Clarify exception for DBS request of same LOB token more than once

This release includes changes to address the following DRs, originally included
in release 14.10.00.25:

DR 168961 Improve Monitor connection PreparedStatement interoperability with JDK 7

This release includes changes to address the following DRs, originally included
in release 14.10.00.23:

DR 154833 Recoverable Network Support
DR 154881 Support Redrive protocol to automatically redrive SQL request after communication failure
DR 165410 setObject(col, new BigDecimal("1.5e-39"), Types.DECIMAL) generates StringIndexOutOfBoundsException

This release includes changes to address the following DRs, originally included
in release 14.10.00.21:

DR 165252 Improve JDBC FastLoad support for UTF8 session character set

This release includes changes to address the following DRs, originally included
in release 14.10.00.19:

DR 160024 Support PERIOD data type for JDBC FastExport
DR 160027 Support PERIOD data type for JDBC FastLoad and JDBC FastLoad CSV
DR 162940 JDBC PERIOD attributes have the same column type name as the PERIOD, which causes getAttributes (Map) to fail

This release includes changes to address the following DRs, originally included
in release 14.10.00.18:

DR 144415 STRICT_ENCODE connection parameter

This release includes changes to address the following DRs, originally included
in release 14.00.00.42:

DR 173198 remove obsolete JDBC FastLoad support for V2R6.1 and earlier

This release includes changes to address the following DRs, originally included
in release 14.00.00.41:

DR 167776 CLOB INOUT parameter assigned new value in procedure body is truncated to 4 bytes when input was NULL
DR 172432 Error 1178 thrown for query returning result set of Array columns mixed with other type columns

This release includes changes to address the following DRs, originally included
in release 14.00.00.40:

DR 169289 Reconnect count not limited by RECONNECT_COUNT connection parameter after JDBC DR 159065 change

This release includes changes to address the following DRs, originally included
in release 14.00.00.39:

DR 167176 Avoid NullPointerException at logon when System property "java.vm.info" is not set

This release includes changes to address the following DRs, originally included
in release 14.00.00.38:

DR 161300 Slow JDBC logons inside TdgssContext.initSecContext 14.00
DR 165159 Change TdgssConfigApi access to accommodate TeraGSSJava DR 161300

This release includes changes to address the following DRs, originally included
in release 14.00.00.37:

DR 164718 EON support for DatabaseMetaData getMax...NameLength methods

This release includes changes to address the following DRs, originally included
in release 14.00.00.35:

DR 163466 Translate new JDBC 14.10 error messages into Japanese
DR 163807 Updatable ResultSet exception "No value has been set for parameter" with unique column

This release includes changes to address the following DRs, originally included
in release 14.00.00.33:

DR 153317 Provide ClientAttributes COPSuffixHostName and Client/Server IPAddresses/Ports
DR 162211 Implement Connection.getClientInfo to return query band values
DR 163421 Improve logging of LCC activity
DR 163501 Support PreparedStatement setAsciiStream setBinaryStream setCharacterStream with long length

This release includes changes to address the following DRs, originally included
in release 14.00.00.31:

DR 160663 Support JDBC FastLoad CSV as an application server data source
DR 160795 Support new JDBC 4.0 PreparedStatement.setAsciiStream methods for JDBC FastLoad CSV
DR 161123 Bypass JDBC FastExport for unsupported ResultSet scrollability, concurrency, or holdability

This release includes changes to address the following DRs, originally included
in release 14.00.00.30:

DR 162982 Support LDAP password containing space character
DR 163164 Correct DatabaseMetaData getNumericFunctions getStringFunctions getSystemFunctions getTimeDateFunctions

This release includes changes to address the following DRs, originally included
in release 14.00.00.29:

DR 160209 Support the SHOW IN XML command

This release includes changes to address the following DRs, originally included
in release 14.00.00.28:

DR 99266 implement ResultSet holdability CLOSE_CURSORS_AT_COMMIT

This release includes changes to address the following DRs, originally included
in release 14.00.00.27:

DR 162129 Support JDK 7

This release includes changes to address the following DRs, originally included
in release 14.00.00.26:

DR 156715 Support UTF8 session character set with Console partition
DR 159277 Provide DBS RFC 159237 DBC.IndicesV[X].IndexDatabaseName as getIndexInfo INDEX_QUALIFIER

This release includes changes to address the following DRs, originally included
in release 14.00.00.25:

DR 68722 Accommodate 64-bit Activity Count

This release includes changes to address the following DRs, originally included
in release 14.00.00.24:

DR 100184 DatabaseMetaData.getIndexInfo should support arguments containing double quotes

This release includes changes to address the following DRs, originally included
in release 14.00.00.23:

DR 160380 Support new JDBC 4.0 API methods for non-SQL connections

This release includes changes to address the following DRs, originally included
in release 14.00.00.22:

DR 127422 support sending fixed-width BYTE data values to the database
DR 160029 Provide PreparedStatement setObject method with scaleOrLength argument for Raw connections

This release includes changes to address the following DRs, originally included
in release 14.00.00.18:

DR 134645 Add support for the JDBC 4.0 SQLXML data type

This release includes changes to address the following DRs, originally included
in release 14.00.00.17:

DR 153117 JDBC FastLoad/FastExport GOVERN=OFF to use TASM fail-fast no-wait for Check Workload End

This release includes changes to address the following DRs, originally included
in release 14.00.00.16:

DR 159065 Support reconnect after database restart for MPP systems

This release includes changes to address the following DRs, originally included
in release 14.00.00.15:

DR 55968 Implement java.sql.Driver.getPropertyInfo

This release includes changes to address the following DRs, originally included
in release 14.00.00.14:

DR 157883 Increased Diffie-Hellman keysize to 2048bits used excessive CPU resulting in increased logon times

This release includes changes to address the following DRs, originally included
in release 14.00.00.13:

DR 157308 URL parameters USER/PASSWORD used when DriverManager.getConnection user/password arguments omitted or null

This release includes changes to address the following DRs, originally included
in release 14.00.00.12:

DR 156903 Support the "gtwcontrol -u yes" option for Send Connect Response with Integrity Only

This release includes changes to address the following DRs, originally included
in release 14.00.00.11:

DR 156851 Connection parameter LOGMECH=NONE enables DatabaseMetaData version retrieval without logon

This release includes changes to address the following DRs, originally included
in release 14.00.00.10:

DR 109167 Statement.execute to provide update count for MERGE statement
DR 155909 Stricter syntax check to determine whether Insert statement qualifies for JDBC FastLoad
DR 156706 Translate new JDBC 14.0 error messages into Japanese

This release includes changes to address the following DRs, originally included
in release 14.00.00.09:

DR 143362 Support the SQL NUMBER data type as the JDBC NUMERIC data type

This release includes changes to address the following DRs, originally included
in release 14.00.00.08:

DR 156036 Add connection parameter FINALIZE_AUTO_CLOSE=ON/OFF (default OFF) to control finalize method auto-closing JDBC objects

This release includes changes to address the following DRs, originally included
in release 14.00.00.05:

DR 147216 Modify TDGSS Configuration Files to support more encryption types
DR 147218 Stronger Encryption for the Teradata Security Mechanisms
DR 150335 Need a better exception message when unsupported AES keysize encountered
DR 150599 Build error in TdgssConfigFile.xsd: element LdapConfig not found in the Schema
DR 152773 Do not allow legacy logons for TD2 and LDAP when AES-128 bit logons are not allowed

This release includes changes to address the following DRs, originally included
in release 14.00.00.04:

DR 138098 support the SQL ARRAY data type and the java.sql.Array data type

This release includes changes to address the following DRs, originally included
in release 13.10.00.37:

DR 172943 Support insert without return of Structured UDT values using getGeneratedKeys with column list
DR 178409 Log the Teradata JDBC Driver version number

This release includes changes to address the following DRs, originally included
in release 13.10.00.31:

DR 155128 Case-insensitive session character set names for CHARSET connection parameter
DR 155129 Throw SQLException from commit and get/setAutoCommit for closed connection

This release includes changes to address the following DRs, originally included
in release 13.10.00.30:

DR 154743 Incorrect SQLSTATE for some Exceptions returned from PreparedStatement.executeBatch
DR 155013 Accommodate PCLUSERNAMERESP parcel for Java Stored Procedure default connection

This release includes changes to address the following DRs, originally included
in release 13.10.00.29:

DR 148644 support Monitor partition 1MB response message

This release includes changes to address the following DRs, originally included
in release 13.10.00.28:

DR 135100 Teradata dialect for Hibernate

This release includes changes to address the following DRs, originally included
in release 13.10.00.27:

DR 153226 Updatable ResultSet Error 1244 Column index value 0 is outside the valid range

This release includes changes to address the following DRs, originally included
in release 13.10.00.26:

DR 137214 Translate new JDBC 13.10 error messages into Japanese

This release includes changes to address the following DRs, originally included
in release 13.10.00.25:

DR 96348 implement setFetchSize and setMaxRows to use FetchRowCount parcel

This release includes changes to address the following DRs, originally included
in release 13.10.00.24:

DR 107800 JDBC reconnect after database communication failure

This release includes changes to address the following DRs, originally included
in release 13.10.00.23:

DR 152249 Support result set returned from the Create/Alter Replication Group commands

This release includes changes to address the following DRs, originally included
in release 13.10.00.22:

DR 141717 Reduce synchronization on Connection and Statement objects

This release includes changes to address the following DRs, originally included
in release 13.10.00.21:

DR 129622 Support Mandatory Access Control
DR 146934 provide ClientInterfaceKind, ClientInterfaceVersion, and ClientAttributesEx

This release includes changes to address the following DRs, originally included
in release 13.10.00.19:

DR 109963 Monitor connection support for UTF8 and UTF16 session character sets
DR 149284 support TD_ANYTYPE

This release includes changes to address the following DRs, originally included
in release 13.10.00.18:

DR 148441 unify parameter marker implementation classes
DR 149859 Return empty result set from DatabaseMetaData getClientInfoProperties for older databases

This release includes changes to address the following DRs, originally included
in release 13.10.00.16:

DR 148993 Avoid error 3749 "Options Parcel information is invalid" with TD13.10 DisableSipSupport=TRUE

This release includes changes to address the following DRs, originally included
in release 13.10.00.15:

DR 145765 tdgssjava TD2 unwrap fails for integrity-only with Defective Token error
DR 146421 Multi-threaded concurrent logon attempts can throw GSSException: Error during MIC calculation
DR 148119 JDBC failing intermittently with "The LAN message Authentication is invalid"

This release includes changes to address the following DRs, originally included
in release 13.10.00.12:

DR 122378 support the ClientAttributes feature as an improvement for LogonSource
DR 138855 JDBC 4.0 API DatabaseMetaData getClientInfoProperties
DR 148350 ignore the HUTConfig returned from FastLoad BEGIN LOADING statement

This release includes changes to address the following DRs, originally included
in release 13.10.00.11:

DR 107402 JDBC 4.0 API Specification support

This release includes changes to address the following DRs, originally included
in release 13.10.00.09:

DR 147980 support multiple Record parcels for Console partition response messages

This release includes changes to address the following DRs, originally included
in release 13.10.00.08:

DR 147087 PreparedStatement executeQuery after getMoreResults throws SQLException with error code 1077

This release includes changes to address the following DRs, originally included
in release 13.10.00.07:

DR 115641 enable application custom type mapping for Distinct, Structured, and Internal UDT values
DR 147134 ResultSet getBinaryStream to return null for NULL column value

This release includes changes to address the following DRs, originally included
in release 13.10.00.06:

DR 146852 PreparedStatement.setObject(n,BigDecimal,DECIMAL,scale) scale argument specifies minimum scale

This release includes changes to address the following DRs, originally included
in release 13.10.00.05:

DR 146722 Improve DBS statement cache hit ratio for PreparedStatement VARCHAR bind values

This release includes changes to address the following DRs, originally included
in release 13.10.00.04:

DR 94091 Remove "CREATE PROCEDURE FROM EXTERNAL NAME" feature from driver
DR 136075 Avoid sending extra ET commands which cause 3510 errors in DBQLOGTBL
DR 145227 support session character set UTF8 for JDBC FastLoad CSV

This release includes changes to address the following DRs, originally included
in release 13.10.00.03:

DR 139067 DatabaseMetaData getSQLKeywords method to query SYSLIB.SQLRestrictedWords
DR 140609 Additional support for DISTINCT user-defined types

This release includes changes to address the following DRs, originally included
in release 13.00.00.33:

DR 166995 JDBC scalar function LOG returns incorrect results
DR 167160 JDBC Fastload error with ENCRYPTDATA=ON

This release includes changes to address the following DRs, originally included
in release 13.00.00.32:

DR 164292 Provide correct SQLException error codes to distinguish logon vs non-logon communication failure

This release includes changes to address the following DRs, originally included
in release 13.00.00.31:

DR 155666 Accommodate more rows from the Teradata Database than the Activity Count indicates

This release includes changes to address the following DRs, originally included
in release 13.00.00.30:

DR 163433 ResultSet.close and Statement.close may not always close response spools spanning multiple messages
DR 163530 Support JDBC 4.0 API Service Provider mechanism for automatic class loading

This release includes changes to address the following DRs, originally included
in release 13.00.00.29:

DR 155367 Intermittent socket communication failures when using setQueryTimeout with large result sets
DR 160371 Intermittent "Read timeout after abort was sent" on Linux

This release includes changes to address the following DRs, originally included
in release 13.00.00.26:

DR 143576 add connection parameter TYPE=FASTLOADCSV
DR 145146 add JDBC FastLoad CSV connection parameter FIELD_SEP

This release includes changes to address the following DRs, originally included
in release 13.00.00.25:

DR 143408 DatabaseMetaData methods use Data Dictionary V-views for TD 12.0 and later

This release includes changes to address the following DRs, originally included
in release 13.00.00.24:

DR 106710 enhance getProcedures and getProcedureColumns to report on external stored procedures also
DR 133599 DatabaseMetaData getTables support for table type GLOBAL TEMPORARY
DR 143844 The JDBC Driver throws a NullPointerException if executeBatch is called when the batch is empty

This release includes changes to address the following DRs, originally included
in release 13.00.00.23:

DR 137079 JDBC 4.0 API support ResultSet getAsciiStream and getCharacterStream for CLOB column
DR 141432 Restore missing code/methods for SIP/LOB/PREP support

This release includes changes to address the following DRs, originally included
in release 13.00.00.22:

DR 142328 Changes for DatabaseMetaData getColumns

This release includes changes to address the following DRs, originally included
in release 13.00.00.21:

DR 138607 Improve Java Tdgss initialization performance

This release includes changes to address the following DRs, originally included
in release 13.00.00.20:

DR 139320 add support for session charsets KANJISJIS_0S and KANJIEUC_0U in JDBC FastLoad
DR 139380 add support for additional session charsets in JDBC FastExport
DR 140148 add PERIOD type name to SQLWarning message when attempting JDBC FastLoad into table with PERIOD data type
DR 140534 NullPointerException from PreparedStatement setNull Types.STRUCT when using V2R6.1

This release includes changes to address the following DRs, originally included
in release 13.00.00.19:

DR 101075 Include TTU client version in Client Config parcel built by JDBC
DR 138870 Some invalid JDBC methods throw NullPointerException

This release includes changes to address the following DRs, originally included
in release 13.00.00.18:

DR 115639 input and output java.sql.Struct values for Structured and Internal UDT values
DR 117048 JDBC support for Period Data Types as java.sql.Struct
DR 139824 DatabaseMetaData getColumns should ignore Error 3523 The user does not have any access to dbname.tabname

This release includes changes to address the following DRs, originally included
in release 13.00.00.17:

DR 139774 Reduce finalizer locking for already-closed ResultSets

This release includes changes to address the following DRs, originally included
in release 13.00.00.16:

DR 124457 enable PreparedStatement batch execution to return individual success and error conditions for each parameter set

This release includes changes to address the following DRs, originally included
in release 13.00.00.15:

DR 123191 support TD 13.10 TASM workload management

This release includes changes to address the following DRs, originally included
in release 13.00.00.14:

DR 136315 Coverity found null pointer dereferences in JDBC Driver

This release includes changes to address the following DRs, originally included
in release 13.00.00.13:

DR 127101 support Extended Object Names in parcels

This release includes changes to address the following DRs, originally included
in release 13.00.00.12:

DR 134440 JDBC Error 858 is returned when single & double quotes are combined in the same query & a '?' is located in the quotes

This release includes changes to address the following DRs, originally included
in release 13.00.00.11:

DR 135882 OutOfMemoryError for getMoreResults with multi-statement request returning large result set
DR 136276 ResultSetMetaData getPrecision for DATE column (regression caused by DR 106221)
DR 136380 getDate on CHAR/VARCHAR column containing integer date values throws SQLException with error 1332

This release includes changes to address the following DRs, originally included
in release 13.00.00.09:

DR 104643 CallableStatement.getObject needs to return Time or Timestamp objects for TIME or TIMESTAMP data types
DR 106221 ResultSet and CallableStatement getDate with Calendar parameter to use Calendar's TimeZone to construct Date
DR 106222 ResultSet and CallableStatement getTime/getTimestamp with Calendar parameter to set or use Calendar's TimeZone
DR 134413 PreparedStatement/CallableStatement setDate with Calendar parameter to use Calendar's TimeZone to send DATE value
DR 134573 Enable ResultSet and CallableStatement getTimestamp to return complete TIME value fractional seconds
DR 135093 DatabaseMetaData getColumnPrivileges support for leading spaces in database object names

This release includes changes to address the following DRs, originally included
in release 13.00.00.08:

DR 122317 support FastExport direct export without spooling
DR 122340 provide TRUSTED_SQL connection parameter and {fn teradata_untrusted} escape function
DR 129206 SQL keyword changes and SQLState mappings for TD 13.10

This release includes changes to address the following DRs, originally included
in release 13.00.00.07:

DR 120610 Clean up Coverity defects found in 13g TDGSS JAVA LIBRARIES
DR 123280 GSSException from initSecContext needs to provide the exception cause for troubleshooting
DR 123711 change remaining sample programs to use CHARSET=UTF8 after DBS DR 118299 is available
DR 124683 TDGSSJAVA Kerberos fails when Data Encryption specified and server is MPRAS
DR 125601 TdgssLibraryConfigFile.xml and TdgssUserConfigFile.xml incorrectly specified DH G key in litle endian
DR 132356 TDGSS Java LDAP Mechanism does not handle Non "US-ASCII" characters in logdata
DR 132545 JDBC does not send correct charset in Unicode LDAP logons
DR 133045 If a password includes certain escape chars, the UPN created should have those chars escaped
DR 133291 Modify JDBC sample program for Transaction Isolation levels
DR 133304 Support PreparedStatement setNull with PreparedStatement setClob or setBlob in a batch insert request
DR 133717 Change log level for socket read timeout exceptions from ERROR to INFO

This release includes changes to address the following DRs, originally included
in release 13.00.00.06:

DR 132370 DatabaseMetaData getColumns support for Period and Geospatial data types

This release includes changes to address the following DRs, originally included
in release 13.00.00.05:

DR 132251 improve Thread reference usage for pooled Statement objects

This release includes changes to address the following DRs, originally included
in release 13.00.00.04:

DR 98096 In ANSI mode w/auto commit on, an unnecessary commit is sent after a prepare
DR 99487 enable use of PreparedStatement batch update when LOB_SUPPORT=off

This release includes changes to address the following DRs, originally included
in release 13.00.00.03:

DR 131214 Provide incremental fetch for Console connection result sets

This release includes changes to address the following DRs, originally included
in release 13.00.00.02:

DR 122326 provide a Raw connection type
DR 129026 use INDIC mode for JDBC FastExport
DR 130385 JDBC Driver support for STARTUP string specified by CREATE/MODIFY USER
DR 130413 Connection.getTransactionIsolation may be incorrect after SET SESSION CHARACTERISTICS command

This release includes changes to address the following DRs, originally included
in release 13.00.00.01:

DR 92605 ResultSet getter methods should throw exception for invalid RS cursor position
DR 95122 remove remaining code for obsolete feature: DR63489 connection parameter
DR 95817 remove undocumented connection pool implementation
DR 96825 remove obsolete CASE_SENSITIVE connection parameter and DataSource property
DR 97757 remove undocumented and obsolete connection parameters
DR 99263 add ConnectionPoolDataSource getters and setters defined by JDBC 3.0 spec 
DR 101602 Exception thrown when a stored procedure called w/ literal IN value & ? OUT vals
DR 102420 return true from DatabaseMetaData.supportsStatementPooling
DR 102470 IndexOutOfBoundsException when a connection string parameter option is missing
DR 103542 Prep stmt batch: setObject/Types.TINYINT and setByte or setObject for same parameter throws exception w/error 857
DR 106528 ResultSet methods isBeforeFirst and isLast return incorrect values for empty result set
DR 108938 Change DatabaseMetaData.getColumns to use the current USEXVIEWS setting
DR 109268 exception with V2R6.0 and earlier when trying to CALL an SP with an OUT parameter with leading spaces in its name
DR 110190 PreparedStatement.execute throws exception for Execute Macro with '?' in macro name
DR 110895 JDBC returns IndexOutOfBoundsException when getObject(index) and other get methods are called with an invalid index
DR 113199 ArrayIndexOutOfBoundException from a reused CallableStatement.setNull with OUT parameter
DR 113547 DBS Error 3130 "Response limit exceeded" while executing Statement.executeBatch() method
DR 113577 scrollable ResultSet not returned as requested from Statement.executeQuery after Statement.executeBatch
DR 113847 SQLException w/error 3760 when attempting to call SP with a space in its name on V2R6.0 and earlier 
DR 114195 {fn TIMESTAMPADD(SQL_TSI_MONTH,count,ts)} returns error when input day-of-month exceeds target month's number of days
DR 115508 NullPointerException from executeXXX methods when invalid charset specified for Java Stored Procedure default connection
DR 116004 JDBC Driver not following Java naming standards or interfaces causing rework
DR 116442 support DNS hostname aliases with Kerberos authentication
DR 120323 BatchUpdateException does not return correct update count while using SELECT statement in Statement.executeBatch
DR 121210 JDBC Driver support for NoPI tables
DR 124418 Throw SQLException chain for DBS error 7980 from CALL to SQLJ stored procedure
DR 125135 provide JDBC sample program to demonstrate use of Geospatial data
DR 126018 additional support for select-list parameter markers
DR 129058 DatabaseMetaData.getColumns support for object names with leading spaces in Data Dictionary
DR 129638 support KANJISJIS_0S, KANJIEUC_0U, LATIN1252_0A with MONITOR partition

This release includes changes to address the following DRs, originally included
in release 12.00.00.111:

DR 92048 Add COP connection parameter and DataSource property
DR 113453 CONNECT_FAILURE_TTL connection parameter
DR 126776 connectivity changes
DR 131226 provide connection parameters for TCP socket options
DR 131929 Enable TCP connection parameter to control TCP send and receive buffer sizes

This release includes changes to address the following DRs, originally included
in release 12.00.00.110:

DR 132603 Login timeout may wait too long

This release includes changes to address the following DRs, originally included
in release 12.00.00.109:

DR 132157 support PreparedStatement batch update with no parameter markers

This release includes changes to address the following DRs, originally included
in release 12.00.00.106:

DR 129949 Conditional connection is not terminated by JDBC driver if NEW_PASSWORD is invalid

This release includes changes to address the following DRs, originally included
in release 12.00.00.105:

DR 105976 getColumns & getBestRowIdentifier return incorrect values for COLUMN_SIZE for char types on Japanese-enabled DBS
DR 125463 remove unneeded trailing semicolons in SQL request text in sample programs
DR 127065 DatabaseMetaData.getIndexInfo fails to return Unique Primary Index when using UTF8 session character set

This release includes changes to address the following DRs, originally included
in release 12.00.00.104:

DR 69205 send DATE, TIME, and TIMESTAMP type codes to DBS for better implicit data type conversions
DR 97560 provide Teradata-specific escape syntax to set JDBC driver log level
DR 101194 driver should downgrade scrollable result set to forward-only if LOB_SUPPORT=off
DR 102357 PreparedStatement.addBatch exception: setTime/Timestamp & setNull TIME/TIMESTAMP
DR 107027 implement login timeout functionality - use value set by DriverManager.setLoginTimeout or DataSource.setLoginTimeout
DR 108348 support returning dynamic result sets from a Java stored procedure
DR 110511 Modify the Teradata JDBC driver to send StatementInfo parcels from the client in a request message
DR 111264 support FastExport
DR 112298 JDBC FastLoad data validation
DR 112564 Change ClearCase directory structure, and change all package statements to remove "ncr"
DR 112565 Change all copyright comments that contain 'NCR'
DR 112566 Change error message prefixes that contain '[NCR]'
DR 112569 Put the tdgssjava classes into the terajdbc4.jar file
DR 112572 Change the SQL connection Logon & Logoff to use the 'Generic' classes
DR 113344 add a reason for not invoking JDBC FastLoad to the PreparedStatement SQLWarning chain
DR 113678 provide SQLException chain and SQLWarning chain for create/replace XSP
DR 114956 support user impersonation with QueryBand
DR 114981 provide error code and SQLState at the beginning of all SQLException messages
DR 115170 avoid sending Continue/Cancel with RPO=S if Resp/BigResp was sent and EndRequest was received
DR 115627 Support Java User-Defined Functions (UDFs)
DR 115664 support Novell eDirectory for use with the LDAP mechanism
DR 115855 PreparedStatement and CallableStatement setObject should send fractional seconds of Time value to database
DR 116276 support TDGSS mechanism attribute GenerateCredentialFromLogon - move username@@password into mechdata
DR 116279 Translate new JDBC 13.0 error messages into Japanese
DR 116489 SQL keyword changes and SQLState mappings for TD 13.0
DR 116761 Support the consumption of dynamic result sets in an SQL stored procedure
DR 118803 KATAKANAEBCDIC session character set is not supported - omit from JDBC Driver User Guide
DR 120309 support data encryption and user authentication for high-level JDBC FastLoad and FastExport
DR 120378 support a literal IP address as a Teradata Database hostname
DR 120705 Test case prepareNull.java failed with the following exception message against TD 13d build (WS 03)
DR 121130 getParameterMetaData() fails with multi-statement macro and multi-statement requests
DR 121311 if DBS error occurs when inserting LOBs using PreparedStatement batch, driver violates protocol and DBS ends connection
DR 121952 avoid SQLException from Statement.close if connection is already closed
DR 122425 JDBC Driver sent Abort request message for completed request while subsequent ET request was in progress
DR 122427 NullPointerException thrown if LOB length is 0 and a read(data) is performed with data array having nonzero length
DR 123376 Modify JDBC DatabaseMetaData.getColumnPrivileges to handle new access rights added for DBS 13.0
DR 123428 WebSphere Application Server 6.1 DataSource Test Connection failed
DR 123694 Test cases PersistDataSource.java and PersistPoolDataSource.java unable to lookup datasource
DR 124800 NullPointerException when connecting to V2R5.0

This release includes changes to address the following DRs, originally included
in release 12.00.00.01:

DR 118048 IndexOutOfBoundsException from ResultSet positioning methods for large scrollable result set with V2R6.2 and earlier
DR 118571 WebSphere DataSource custom property CHARSET not working for PreparedStatement.setString non-ASCII characters
DR 119329 TeraEncrypt: Error tdgss-stack-trace-begin>>> java.lang.ArrayIndexOutOfBoundsException (shipped with tdgssjava 12.0.1.2)

This release includes changes to address the following DRs, originally included
in release 12.00.00.00:

DR 51544 Updateable result sets. 
DR 58075 Blob and Clob update methods added by the JDBC 3.0 specification. 
DR 92927 handle database password expiration. 
DR 92937 add connection parameter to choose X views or non-X views for metadata. 
DR 94241 provide getMoreResults (KEEP_CURRENT_RESULT) for multistmt req cursor positioning. 
DR 99338 certify WebLogic 9.1 on Windows. 
DR 99339 certify WebLogic 9.1 on Solaris/SPARC. 
DR 99341 certify WebLogic 9.1 on Linux. 
DR 99343 certify ColdFusion MX 7 on Windows. 
DR 101800 support "jdbc:default:connection" URL for use in a Java Stored Procedure. 
DR 102453 JDBC support for Stored Procedure Dynamic Result Sets. 
DR 102730 support full ANSI MERGE statement. 
DR 102732 support the SET QUERY_BAND statement. 
DR 102852 add TD 12.0 reserved words to DatabaseMetaData.getSQLKeywords. 
DR 103778 certify with JBoss 3.2.3 on Windows.
DR 103780 certify with WebSphere 6.1 on Windows. 
DR 103781 certify with WebSphere 6.1 on AIX. 
DR 103782 certify with WebSphere 6.1 on Solaris/SPARC. 
DR 104096 update application server documentation for TTU 12.0 release. 
DR 104748 certify Windows XP Professional x64 on EM64T with JDK 5.0 32-bit. 
DR 104749 certify Windows XP Professional x64 on EM64T with JDK 5.0 64-bit. 
DR 104750 certify 32-bit Windows Server 2003 on EM64T with JDK 5.0 32-bit. 
DR 107100 ResultSet and CallableStatement getString for BYTE/VARBYTE value to use session charset instead of JVM default charset.
DR 107197 JDBC-related corrections to the Introduction to Teradata Warehouse.
DR 108118 UNIX-Kerberos target name is case-sensitive, so Teradata JDBC Driver must change Teradata@m/c to TERADATA@m/c. 
DR 108385 Change Teradata JDBC Driver version to 12.0.0.1. 
DR 108390 accommodate DBS version change from V2R7.0 to 12.0. 
DR 109658 support CHARSET= connection parameter for jdbc:default:connection for Java Stored Procedures. 
DR 109689 Corrections for UNIX classpath listed in Chapter 2 section "Running a Sample Application".
DR 109728 Certify TTU 12.0 JDBC Driver with JBoss 4.0 and JDK 5.0. 
DR 110204 Statement.getMoreResults (KEEP_CURRENT_RESULT) to support only TYPE_SCROLL_INSENSITIVE.
DR 110539 Statement.execute fails for a CALL to a stored procedure.
DR 112657 SQLState mappings for External Stored Procedure error codes.
DR 112978 accommodate 12m DBS DR 110445 change to DBC.Columns.ColumnName value 'RETURN' is now 'RETURN0'.

This release includes changes to address the following DRs, originally included
in release 03.04.00.06:

DR 131684 add JDBC FastLoad support for EJB transactions with multiple PreparedStatements

This release includes changes to address the following DRs, originally included
in release 03.04.00.05:

DR 131418 support DBS_PORT connection parameter with TYPE=FASTLOAD and FASTEXPORT

This release includes changes to address the following DRs, originally included
in release 03.04.00.03:

DR 96980 GtwConfigParcel could fail when new gateway features added
DR 97103 improve correlation of TDSession objects with log messages
DR 98176 avoid sending cancel request if RESP/BIGRESP is used and ENDREQUEST received
DR 100090 modify driver to avoid DBS crash when using LOB_SUPPORT=off and large result set
DR 106708 add LOG=TIMING measurements
DR 114154 CallableStatement batch support to execute CALL sequentially to work within DBS restriction of single CALL at a time
DR 114470 TTU 8.1 Teradata JDBC Driver 3.3 to support TD 12.0 (backport DR 108390)
DR 115269 improve JDBC FastLoad exception handling of PreparedStatement.executeBatch and Connection.rollback
DR 115914 Customer gets errors on jdbc connection attempts originating from TdgssConfigApi (shipped with tdgssjava 6.2.2.19)
DR 118851 Java TDGSS_6.1.1.93_GCA fails against a 12.00.00.04 DBS for Kerberos (shipped with tdgssjava 6.2.2.22)
DR 119000 ResultSetMetaData.isAutoIncrement should return true for identity column
DR 119320 Add ActivityType 123 for REPLACE UDF
DR 120597 force connection failure for invalid response message header
DR 120929 DatabaseMetaData.getColumns returns incorrect information when using UTF8 session character set

This release includes changes to address the following DRs, originally included
in release 03.04.00.02:

DR 107900 support ambiguous data type for ResultSet columns with SIP-enabled prepare of select-list parameter markers
DR 110436 provide consistency for ACCOUNT connection parameter
DR 112806 PreparedStatement: treat Types.FLOAT & Types.REAL as Types.DOUBLE in setObject and preserve float precision in setFloat
DR 113069 executeQuery may return error 1182 if URL param CLIENT_CHARSET is set and the DBS is running V2R6.1 or earlier

This release includes changes to address the following DRs, originally included
in release 03.04.00.01:

DR 100351 support the MONITOR partition
DR 100352 support the DBCCONS partition
DR 103835 return actual update count for MERGE insert, MERGE update, and MERGE mixed SQL statements
DR 104893 provide high-level JDBC FastLoad connection for automatic management of multiple low-level connections
DR 106917 CallableStatement.getObject fails for INOUT Clob parameter when input value was sent to 6.2 DBS as VARCHAR
DR 108500 ResultSetMetaData.getSchemaName to return database name with Teradata Database 6.2
DR 108926 enable JDBC Load and Export sessions to be identifiable by TASM
DR 109424 Make Elicit File protocol work with the JDBC driver and 64 bit Windows DBS
DR 109760 Change sample program T21400JD to demonstrate call to corrected UDF Judf01
DR 110059 Return correct values for getColumnDisplaySize for temporal data types
DR 110112 provide pre-V2R6.2 high-level JDBC FastLoad connection for automatic management of multiple low-level connections
DR 111254 JDBC FastLoad support for BIGINT and large DECIMAL, JDBC FastLoad support for EJB transactions

This release includes changes to address the following DRs, originally included
in release 03.04.00.00:

DR 58028 JDBC 3.0 ParameterMetaData methods.
DR 94704 RFC: Support the retrieval of auto-generated keys from an insert statement.
DR 95968 Add support for BIGINT and DECIMAL(38).
DR 96457 certify JDBC driver with Teradata Database on Windows x64.
DR 96467 update application server documentation for TTU 8.2 / JDBC 3.4 release.
DR 97514 ResultSetMetaData methods to use actual database values rather than COMPAT_xxx.
DR 97554 provide User Defined Type (UDT) information from DatabaseMetaData methods.
DR 99270 certify Solaris 10 64-bit on AMD64 with JDK 5.0 32-bit & 64-bit.
DR 99337 certify Red Hat AS 4.0 64-bit on AMD64 with JDK 5.0 32-bit & 64-bit.
DR 104782 setMaxFieldSize(small value) causes subsequent exceptions from getDate for ANSI date, getTime, and getTimestamp

This release includes changes to address the following DRs, originally included
in release 03.03.00.04:

DR 87473 getColumnDisplaySize returns number of bytes for Character columns
DR 104043 DatabaseMetaData.getTableTypes should return ResultSet ordered by TABLE_TYPE
DR 108939 User Guide must say setAsciiStream/setCharacterStream are not supported for binding data to BYTE or VARBYTE destinations
DR 109213 Encryption problem message may not display an argument
DR 109231 TYPE_SCROLL_INSENSITIVE used with execute method causes subsequent getMoreResults to hang or throw exception
DR 109294 Statement.executeBatch must clear the batch
DR 109615 DatabaseMetaData.getColumns throws exception

This release includes changes to address the following DRs, originally included
in release 03.03.00.03:

DR 101277 enable UDFs, XSPs, and UDMs to be created from resources on client using JDBC
DR 106841 improve SQLException from executeXXX when InputStream bound with setXxxStream is closed before executeXXX
DR 107345 TTU 8.1: Getting NullPointerException from tdgssjava (shipped with tdgssjava 6.2.0.4)
DR 107536 CallableStatement executeUpdate: error 6906 (iterated request disallowed) occurs after error 1184 (invalid parameter)
DR 107987 workaround for Java 2 security Sun Bug ID 6205384 - SocketPermission ignored for unknown host 
DR 108260 restrict printed characters to 7-bit ASCII in debug log message dumps

This release includes changes to address the following DRs, originally included
in release 03.03.00.02:

DR 101381 scalar functions (TIMESTAMPADD, TIMESTAMPDIFF) need SQL_TSI_HOUR
DR 103411 Add support for inserting NULL, Unicode characters, and remaining data types to be supported by JDBC FastLoad
DR 103740 with CHARSET=UTF16, PreparedStatement.executeBatch should accept TIMESTAMP values w/varying number of fractional digits
DR 103772 send millisecond portion of java.sql.Time values to DBS with omitted TNANO connection parameter, & with TNANO=1 or more
DR 104020 getTime & getObject methods: return TIME values w/fractional seconds as java.sql.Time values w/milliseconds precision
DR 104370 throw SQLException when DBS says 'n' characters are coming but really only sends a fraction of 'n'
DR 104825 insert trailing zeros before TIME ZONE for TIME or TIMESTAMP values with varied precisions and no TNANO or TSNANO
DR 105073 Modify setBinary/Ascii/CharacterStream methods to determine when to send Deferred LOB/VARCHAR/VARBYTE values
DR 105265 Ensure driver is registered only once with DriverManager
DR 105633 non-prepared Statement.executeBatch is limited to fewer statements with V2R6.x than with V2R5.x
DR 105834 PreparedStatement.setAsciiStream drops trailing spaces
DR 106091 stream/reader from Clob.getAsciiStream/getCharacterStream should remain valid for Connection lifetime 
DR 106115 Clob.getSubString truncates data when a multi-byte character set is used
DR 106116 cannot execute more than once a CallableStatement with OUT parameters on V2R6.0 and earlier
DR 106118 Incorrect data is returned if the ResultSet is closed while reading a Blob
DR 106136 A finalize statement needs to be implemented for LobStatement 
DR 106243 ResultSet.getAsciiStream returns invalid data on mainframe z/OS USS

This release includes changes to address the following DRs, originally included
in release 03.03.00.01:

DR 58030 RFC: JDBC3.0: DatabaseMetaData methods (New in JDBC 3.0)
DR 58032 RFC: JDBC3.0: DatabaseMetaData methods (MODIFIED from JDBC2.0)
DR 96462 certify Teradata JDBC Driver with SAP Web Application Server 6.40
DR 97255 document Teradata JDBC Driver configuration with SAP Universal Data Connector
DR 98047 DatabaseMetaData.getProcedures returns ResultSet that differs from API javadoc
DR 98048 DatabaseMetaData.getProcedureColumns returns ResultSet differs from API javadoc
DR 98050 DatabaseMetaData.getColumns fails when a database name contains a quote
DR 98051 PreparedStatement.setCharacterStream and setAsciiStream drop trailing spaces
DR 98053 DatabaseMetaData.getColumns returns RS w/RSMD.getColumnTypeName null for all columns
DR 98055 DatabaseMetaData.getPrimaryKeys returns ResultSet that differs from API javadoc
DR 98855 setBigDecimal throw DataTruncation for >18 integral digits; & round frac. digits
DR 99578 implement JDBC 1.0 DatabaseMetaData methods
DR 99720 low-level FastLoad connection with PreparedStatement batch update
DR 99760 AppServer-HowTo HTML doc needs modification for JRun datasource definition
DR 100404 use 24-hour values rather than 12-hour for JDBC driver log message timestamps
DR 100902 automatic close of garbage-collected Statement and ResultSet objects
DR 101115 PreparedStatement.setLong to throw DataTruncation when long value has 19 digits
DR 101767 ResultSet.absolute fails for negative row numbers
DR 102405 thread deadlock for concurrent calls to Statement.executeQuery & ResultSet.close
DR 103173 securerandom.source and/or java.security.egd dont work in 1.5.0_05 (tdgssjava 6.1.0.18)

This release includes changes to address the following DRs, originally included
in release 03.03.00.00:

DR 50036 RFC: JDBC1.0: Escape syntax support
DR 83850 RFC: JDBC2.0: PreparedStatement.getMetaData()
DR 89445 ResultSetMetaData.getColumnClassName should return class name, not null
DR 89449 Need to add COMPAT_xxx URL parameters for three ResultSetMetaData methods 
DR 91121 disallow specification of a username and password when LDAP or Kerberos is used
DR 91636 Implement Denial of services feature as documented in TRP 541-0004949 
DR 91796 update application server documentation for TTU 8.1 / JDBC 3.3 release
DR 92136 Enable transaction isolation level TRANSACTION_READ_UNCOMMITTED
DR 92143 DatabaseMetaData: obtain DBS limits from Config Response parcel
DR 92146 obtain DBS version/release from new feature item in Config Response parcel
DR 92212 testing: support V2R6.1 feature: external security clause for sprocs/UDFs
DR 92216 J2SE 5.0 (JDK 1.5) certification
DR 92219 Solaris 10/SPARC 32-bit and 64-bit certification
DR 92221 AIX 5.3 32-bit and 64-bit certification
DR 92222 SuSE Linux 32-bit certification
DR 92223 WebSphere 6.0 Certification
DR 92228 WebLogic 8.1 with both Sun JVM and JRockit JVM Certification
DR 92230 ColdFusion MX 6.1 Certification
DR 92231 JBoss 4.0 Certification
DR 92449 Implement UTF16 support for tdgss as documented in TRP 541-0005061
DR 92450 eliminate HELP PROCEDURE before calls to sprocs with OUT params
DR 92648 translate new JDBC 3.3 error messages into Japanese
DR 92693 TTU 8.1 / JDBC 3.3 User Guide: no support for V2R6.1 User Defined Types (UDTs)
DR 92736 Include info for the sample files in samples.jar for MVS in the JDBC Users Guide
DR 92919 DatabaseMetaData APIs should support patterns containing single quotes
DR 93293 Single quote in comment throws invalid error via JDBC
DR 93890 getHoldability APIs should return HOLD_CURSORS_OVER_COMMIT, not throw exception
DR 94311 testing: support V2R6.1 feature: activity count overflow warning
DR 94816 JDBC User Guide section "Improving Performance": list use of PreparedStatement
DR 95061 TTU 8.1 JDBC User Guide addition: CALL statements not using Escape Syntax
DR 95334 corrections for JDBC User Guide Appendix D - Data Type Conversions
DR 95741 JDBC User Guide changes to Chapter 1 section "Support for Internationalization"
DR 95961 database error in TERA mode with autocommit off incorrectly turns autocommit on
DR 95969 incomplete update count array in TERA mode w/autocommit off for failed batch req
DR 96149 Provide warning message from SuccessParcel to Statement.getWarnings method
DR 96253 Package TdgssUserConfigFile.xml in a jar file
DR 96653 Revamp JDBC Connection Pool Orange Book as HTML docs in appserver-howto.jar
DR 96824 JDBC User Guide change: discontinued CASE_SENSITIVE connection parameter
DR 96914 TTU 8.1 JDBC User Guide addition: new section: "Planned Future Changes"
DR 97038 TTU 8.1 JDBC User Guide addition: new section: LogonSource Format
DR 97290 Large batch requests using LOB params, may fail to process all requests in batch
DR 97428 HPUX 11.23/Itanium-2 32-bit and 64-bit certification
DR 97550 corrections for COMPAT_GETSCHEMA and COMPAT_GETTABLE information in JDBC UG
DR 97585 TTU 8.1 JDBC User Guide: new contents for section: Response Limit Exceeded Error
DR 97723 fix sample programs T21301JD, T21302JD to use ConnectionPoolDataSource correctly
DR 97747 TTU8.1 JDBC Users Guide: need to add an explanation for DBS error 3926
DR 97816 DatabaseMetaData.getMaxStatements should return 16, not 1048500
DR 98089 TTU 8.1 JDBC User Guide changes: miscellaneous "Supported Methods" changes
DR 98110 TTU 8.1 JDBC UG: refer readers to appserver documentation in download package

This release includes changes to address the following DRs, originally included
in release 03.02.00.03:

DR 56133 Implement JDBC1.0 APIs DatabaseMetaData.getExportedKeys and getImportedKeys
DR 90532 PreparedStatement.setBigDecimal throws java.lang.ArithmeticException exception
DR 92609 Wrong value in the database when PreparedStatement.setBigDecimal is being used
DR 97022 PreparedStatement.executeBatch throws ClassCastException: java.util.ArrayList

This release includes changes to address the following DRs, originally included
in release 03.02.00.02:

DR 84637 RFC: Statement.execute() should not retrieve all the data before returning
DR 87267 JDBC error: ('0A'X) is not a valid Teradata SQL token
DR 89201 UT: NullPointerException for unbound prep stmt parameter (should be SQLException)
DR 90136 JDBC throws parameter error when ? occurs in 2nd quoted string
DR 91353 setNull does not work correctly when the sql type is a Boolean
DR 91951 CLIENT_CHARSET connection parameter
DR 92125 add LOG=TIMING connection parameter
DR 92294 JDBCException when creating UDF: Function 'Judf01' already exists
DR 92697 getTables() to get the table type, the resultset contains "T" instead of Table
DR 92918 map replication error code 6699 to SQLState 40001 (transaction rolled back)
DR 93054 Logon fails with 8019 from jdbc when ldap mechanism is used
DR 93156 Inserting null decimal datatypes not working when URL LOB_SUPPORT=OFF
DR 93157 Using PreparedStatement.setObject(int, Object) to set null value throws NPE
DR 93549 Type 4 driver Get-column-by-name from 2nd RS throws "column not found" exception
DR 94407 do not include class files in samples.jar
DR 94605 Statement.addBatch fails when SQL contains trailing space
DR 94923 row fetching too slow with >1000 rows in result set using 1MB response messages
DR 94970 PreparedStatement.getResultSet should return null for non-RS-returning statement
DR 95078 interoperability issue when Connection.close called while query is in progress
DR 95302 provide SQLState for V2R6.0.x retryable error codes 3231 and 3319
DR 95828 CLIENT_CHARSET DataSource property
DR 95943 DatabaseMetaData.getSQLKeywords for V2R5.0, V2R5.1, and V2R6.0

This release includes changes to address the following DRs, originally included
in release 03.02.00.01:

DR 85852 TeraLocalPrepared/CallableStatement.getResultSetType and other incorrect methods

This release includes changes to address the following DRs, originally included
in release 03.02.00.00:

DR 57921 RFC: JDBC Certification on Windows 2003 Server (32-bit/64-bit)
DR 63499 RFC: Make changes to files for JDBC to use JDK 1.4
DR 68162 RFC: JDBC2.0: PreparedStatement batch updates
DR 68625 RFC: Add more sample files to JDBC package
DR 68837 RFC: V2R6: 1 MB/APH Responses
DR 68844 RFC: V2R6: Security Improvements and Extensions
DR 69061 RFC: JDBC2.0: Scrollable ResultSets (bi-directional cursor positioning)
DR 84400 RFC: Certify Sun Microsystems RowSet implementation with JDBC driver
DR 84635 RFC: remove jdbc4.properties file - use connection attributes
DR 84672 RFC: Remove platform packaging for JDBC Type 4 driver
DR 84853 RFC: Test JDBC Driver with WebSphere 5.1
DR 84854 RFC: Test JDBC driver with WebLogic 8.1 
DR 85123 RFC: JDBC Certification on 64-bit Linux
DR 85393 RFC: Remove Type 3 driver from product
DR 85397 RFC: Update values for DatabaseMetaData functions for V2R6
DR 85434 createStatement: Downgrade RS type and concurrency, and generate SQLWarning
DR 85536 RFC: Enable Type 4 driver to be built on Solaris
DR 85980 RFC: remove sample applets
DR 85981 DatabaseMetaData.getIndexInfo() is not implemented correctly
DR 86049 JDBC was not handling nulls in where clauses correctly in releases 3.1 & earlier
DR 86456 RFC: support LOBs as stored procedure output parameters
DR 86471 TRANSACTION_READ_UNCOMMITTED(1) is not supported 
DR 87018 ResultSet.getConcurrency should return CONCUR_READ_ONLY
DR 87512 various ResultSetMetaData methods throw exceptions
DR 88400 RCI: Null Pointer dereference in ParcelFactory.java
DR 88403 RCI: Null Pointer Dereference in Statement.java
DR 88405 RCI: Resource leak of FileInputStream
DR 88409 RCI: Resource Leak ResultSets not being closed
DR 88581 Null Pointer exception in TDGSS interface code
DR 88763 Deprecate TeraStatement.getSpl and setSpl methods for TTU 8.0
DR 89173 STV: Statement.getXXX and PrepStmt.setXXX conversions must match User Guide


Troubleshooting Topics
----------------------


TERAJDBC4 ERROR ... The com.ncr.teradata.TeraDriver class name is deprecated
----------------------------------------------------------------------------
New Teradata JDBC Driver class names are available.

   - For JDBC URL connections: com.teradata.jdbc.TeraDriver
   - For WebSphere Data Sources: com.teradata.jdbc.TeraConnectionPoolDataSource

The old class names will continue to work; however, a warning message
will be printed as a reminder to switch over to the new class names.

   TERAJDBC4 ERROR ... The com.ncr.teradata.TeraDriver class name is deprecated.
   Please use the com.teradata.jdbc.TeraDriver class name instead.

   TERAJDBC4 ERROR ... The com.ncr.teradata.TeraConnectionPoolDataSource class name is deprecated.
   Please use the com.teradata.jdbc.TeraConnectionPoolDataSource class name instead.


Solution:

Please change your applications and data source definitions at your
earliest convenience, because the old class names will only be supported
for a limited number of future releases.


Unable to connect to database when using HPUX 11.23 JDK 5.0
-----------------------------------------------------------

When attempting to connect to the Teradata Database when HPUX 11.23 JDK 5.0,
you may receive an exception similar to: TeraEncrypt: Error
tdgss-stack-trace-begin>>> GSSException: Failure unspecified at GSS-API
level (Mechanism level: Failure during key generation by algorithm layer.)

This is a known problem with HPUX 11.23 JDK 5.0. HP's web site states
the following:

 SecureRandom engine implementation (11i HP Integrity and HP9000 PA-RISC) 

  Beginning with 5.0, Sun Microsystems' SecureRandom engine implementation 
  supports a new algorithm, NativePRNG, in addition to SHA1PRNG. NativePRNG 
  will only be available if /dev/random and /dev/urandom are installed in 
  your system. Because HP-UX does not support seeding entropy generating 
  devices such as /dev/random, applications that rely on this will not be 
  able to use NativePRNG. An attempt to seed the device will cause an 
  exception. This defect is expected to be fixed in a future release.


Solution:

No solution is available for this issue at the present time.


Installation
------------

This release of the Teradata JDBC Driver is distributed as platform-independent 
jar files. For downloading convenience, the platform-independent jar files are 
bundled together and provided in both zip format and tar format.

TeraJDBC__indep_indep.15.10.00.14.zip and TeraJDBC__indep_indep.15.10.00.14.tar
both contain the same set of platform-independent files:

  readme.txt               - this file
  terajdbc4.jar            - the Teradata JDBC Driver
  tdgssconfig.jar          - the Teradata security configuration

Download either the zip file or the tar file, and unzip (or untar) the downloaded
file into a directory of your choice, and then set your classpath to refer to the
necessary jar files.

Your classpath must include:

  terajdbc4.jar
  tdgssconfig.jar

Your classpath must NOT include any jar files from any previous releases of
the Teradata JDBC Driver. It is recommended, but not required, that any
previous release of the Teradata JDBC Driver be uninstalled prior to
downloading and using this release of the Teradata JDBC Driver.


Documentation
-------------

Documentation for how to use the Teradata JDBC Driver is available at
http://developer.teradata.com/connectivity/reference/jdbc-driver
