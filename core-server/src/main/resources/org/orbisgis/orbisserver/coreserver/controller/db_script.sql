-- Script of the initiation of the database.
DROP TABLE IF EXISTS session_table;
CREATE TABLE session_table (username VARCHAR(50), password VARCHAR(50), expirationTime LONG);
INSERT INTO session_table VALUES ('admin', 'admin', 20000);