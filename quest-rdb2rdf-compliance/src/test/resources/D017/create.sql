CREATE TABLE "植物" (
  "名" VARCHAR(10),
  "使用部" VARCHAR(10),
  "条件" VARCHAR(10),
  PRIMARY KEY ("名", "使用部")
);
INSERT INTO "植物" ("名", "使用部", "条件") VALUES ('しそ', '葉', '新鮮な');

CREATE TABLE "成分" (
  "皿"  VARCHAR(10),
  "植物名" VARCHAR(10),
  "使用部" VARCHAR(10),
  FOREIGN KEY ("植物名", "使用部") REFERENCES "植物"("名", "使用部")
);
INSERT INTO "成分" ("皿", "植物名", "使用部") VALUES ('しそのとまと', 'しそ', '葉');