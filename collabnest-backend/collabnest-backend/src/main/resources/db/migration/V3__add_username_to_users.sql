-- Add username column (will fail silently if already exists)
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(50);

-- Update existing users with a default username based on email
UPDATE users SET username = SPLIT_PART(email, '@', 1) WHERE username IS NULL;

-- Make it NOT NULL
ALTER TABLE users ALTER COLUMN username SET NOT NULL;

-- Add unique constraint (use DROP IF EXISTS first to be safe)
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_username_unique;
ALTER TABLE users ADD CONSTRAINT users_username_unique UNIQUE (username);
