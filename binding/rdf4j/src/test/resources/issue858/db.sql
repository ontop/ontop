CREATE TABLE "ingredientevents" (
    "subject_id" INTEGER,
    "hadm_id" INTEGER,
    "stay_id" INTEGER,
    "caregiver_id" INTEGER,
    "starttime" TIMESTAMP,
    "endtime" TIMESTAMP,
    "storetime" TIMESTAMP,
    "itemid" INTEGER,
    "amount" REAL,
    "amountuom" TEXT,
    "rate" REAL,
    "rateuom" TEXT,
    "orderid" INTEGER,
    "linkorderid" INTEGER,
    "statusdescription" TEXT,
    "originalamount" REAL,
    "originalrate" REAL
);

INSERT INTO "ingredientevents" (
    "subject_id",
    "hadm_id",
    "stay_id",
    "caregiver_id",
    "starttime",
    "endtime",
    "storetime",
    "itemid",
    "amount",
    "amountuom",
    "rate",
    "rateuom",
    "orderid",
    "linkorderid",
    "statusdescription",
    "originalamount",
    "originalrate"
) VALUES (
    1,
    1,
    1,
    1,
    '2023-10-01 00:00:00',
    '2023-10-01 01:00:00',
    '2023-10-01 02:00:00',
    1,
    100.0,
    'mg',
    10.0,
    'ml/h',
    1,
    1,
    'active',
    100.0,
    10.0
);

CREATE TABLE "patients" (
    "subject_id" INTEGER);

INSERT INTO "patients" ("subject_id") VALUES (1);

