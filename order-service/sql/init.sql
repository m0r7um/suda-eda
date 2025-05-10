ALTER SYSTEM SET max_prepared_transactions = 64;
ALTER SYSTEM SET max_connections = 50;

CREATE DATABASE studs;
CREATE DATABASE analytics;
CREATE DATABASE keycloak;

\c keycloak;
CREATE USER keycloak WITH ENCRYPTED PASSWORD 'keycloak';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
