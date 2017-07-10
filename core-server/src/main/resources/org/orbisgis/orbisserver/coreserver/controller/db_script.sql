-- Script of the initiation of the database.
DROP TABLE IF EXISTS USER;
CREATE TABLE USER (username VARCHAR(50), password VARCHAR(50));
INSERT INTO USER VALUES ('admin', 'admin');