-- Script of the initiation of the database.
DROP TABLE IF EXISTS users_table;
CREATE TABLE users_table (username VARCHAR(50), password VARCHAR(50));
INSERT INTO users_table VALUES ('admin', 'admin');