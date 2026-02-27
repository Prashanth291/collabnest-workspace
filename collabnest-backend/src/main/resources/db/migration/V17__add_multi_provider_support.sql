-- Add separate columns for each OAuth provider ID
ALTER TABLE users ADD COLUMN IF NOT EXISTS github_id VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS google_id VARCHAR(255);

-- Migrate existing provider_id data into the new columns
UPDATE users SET github_id = provider_id WHERE auth_provider = 'GITHUB' AND provider_id IS NOT NULL;
UPDATE users SET google_id = provider_id WHERE auth_provider = 'GOOGLE' AND provider_id IS NOT NULL;

-- Make password_hash explicitly nullable (it already is, but be explicit)
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

-- Add unique indexes on the provider ID columns
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_github_id ON users(github_id) WHERE github_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id) WHERE google_id IS NOT NULL;
