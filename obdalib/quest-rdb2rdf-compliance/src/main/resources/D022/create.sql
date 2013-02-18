CREATE TABLE "Target" (
	"litattr1" INT,
	-- PRIMARY KEY("PK"),
	"key1attr1" CHAR(4),
	"key1attr2" CHAR(4),
	UNIQUE ("key1attr1", "key1attr2"),
	"key2attr1" CHAR(4),
	"key2attr2" CHAR(4),
	UNIQUE ("key2attr2", "key2attr1")
);

CREATE TABLE "Source" (
	"ID" INT,
	PRIMARY KEY("ID"),
	"attrA" CHAR(4),
	"attrB" CHAR(4),
	FOREIGN KEY ("attrA", "attrB") REFERENCES "Target"("key2attr2", "key2attr1")
);

INSERT INTO "Target" ("litattr1", "key1attr1", "key1attr2", "key2attr1", "key2attr2")
              VALUES (1010      , 'K1A1'     , 'K1A2'     , 'K2A1'     , 'K2A2');

INSERT INTO "Source" ("ID", "attrA", "attrB")
              VALUES (1100, 'K2A2' , 'K2A1' );
