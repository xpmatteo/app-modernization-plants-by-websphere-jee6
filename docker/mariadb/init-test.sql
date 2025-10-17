-- Test database initialization script for plantsdb_test
-- Uses shared schema and data files

CREATE DATABASE IF NOT EXISTS plantsdb_test;
GRANT ALL PRIVILEGES ON plantsdb_test.* TO 'pbwuser'@'%';
FLUSH PRIVILEGES;

USE plantsdb_test;

SOURCE /schema.sql;
SOURCE /data.sql;
