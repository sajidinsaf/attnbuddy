-- Email verification and login tracking
ALTER TABLE app_user ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE app_user ADD COLUMN verification_token VARCHAR(64);
ALTER TABLE app_user ADD COLUMN verification_expiry TIMESTAMP;
ALTER TABLE app_user ADD COLUMN last_login_at TIMESTAMP;

-- Grandfather existing users as verified
UPDATE app_user SET email_verified = TRUE;
