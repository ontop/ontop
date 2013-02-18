CREATE TABLE "Target" (
	"PK" INT,
	PRIMARY KEY("PK"),
	"key1attr1" CHAR(5),
	"key1attr2" CHAR(5),
	UNIQUE ("key1attr1", "key1attr2"),
	"key2attr1" CHAR(5),
	"key2attr2" CHAR(5),
	UNIQUE ("key2attr2", "key2attr1")
);

CREATE TABLE "Source" (
	"ID" INT,
	PRIMARY KEY("ID"),
	"attrA" CHAR(5),
	"attrB" CHAR(5),
	FOREIGN KEY ("attrA", "attrB") REFERENCES "Target"("key2attr2", "key2attr1")
);

INSERT INTO "Target" ("PK", "key1attr1", "key1attr2", "key2attr1", "key2attr2")
              VALUES (1010, 'K1A11'    , 'K1A21'    , 'K2A11'    , 'K2A21'    );
INSERT INTO "Target" ("PK", "key1attr1", "key1attr2", "key2attr1", "key2attr2")
              VALUES (1011, 'K1A12'    , 'K1A22'    , NULL       , 'K2A22'    );

INSERT INTO "Source" ("ID", "attrA", "attrB")
              VALUES (1100, 'K2A21', 'K2A11');
INSERT INTO "Source" ("ID", "attrA", "attrB")
              VALUES (1101, 'K2A22', NULL   );

