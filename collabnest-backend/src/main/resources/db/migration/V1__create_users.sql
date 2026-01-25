CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255),
                       auth_provider VARCHAR(50),
                       role VARCHAR(50),
                       enabled BOOLEAN NOT NULL,
                       created_at TIMESTAMP NOT NULL
);

