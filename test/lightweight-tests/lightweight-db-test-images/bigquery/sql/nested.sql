DROP TABLE IF EXISTS nested.company_data_arrays;
CREATE TABLE nested.company_data_arrays (
    id integer NOT NULL,
    days array<timestamp>,
    income array<integer>,
    workers array<struct<names array<string>>>,
    managers array<json>
) as SELECT * from (
  (SELECT 1 as id,  [TIMESTAMP '2023-01-01 18:00:00', TIMESTAMP '2023-01-15 18:00:00', TIMESTAMP '2023-01-29 12:00:00'] as days, [10000, 18000, 13000] as income, [STRUCT(['Sam', 'Cynthia']), STRUCT(['Bob']), STRUCT(['Jim'])] as workers, [JSON '{"firstName": "Mary", "lastName": "Jane", "age": 28}', JSON '{"firstName": "Carlos", "lastName": "Carlson", "age": 45}', JSON '{"firstName": "John", "lastName": "Moriarty", "age": 60}'] as managers)
  UNION ALL (SELECT 2 as id,  [TIMESTAMP '2023-02-12 18:00:00', TIMESTAMP '2023-02-26 18:00:00'] as days, [14000, 0] as income, [STRUCT(['Jim', 'Cynthia']), STRUCT(NULL)] as workers, [JSON '{"firstName": "Helena", "lastName": "of Troy"}', JSON '{"firstName": "Robert", "lastName": "Smith", "age": 48}'] as managers)
  UNION ALL (SELECT 3 as id,  [TIMESTAMP '2023-03-12 18:00:00', TIMESTAMP '2023-03-26 18:00:00'] as days, [15000, 20000] as income, [STRUCT(['Carl', 'Bob', 'Cynthia']), STRUCT(['Jim', 'Bob'])] as workers, [JSON '{"firstName": "Joseph", "lastName": "Grey"}', JSON '{"firstName": "Godfrey", "lastName": "Hamilton", "age": 59}'] as managers)
  UNION ALL (SELECT 4 as id,  NULL as days, NULL as income, NULL as workers, NULL as managers)
);