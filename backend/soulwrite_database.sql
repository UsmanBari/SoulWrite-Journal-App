-- ================================================
-- SOULWRITE APP DATABASE SCHEMA
-- Database: 4604428_barisoulwrite
-- Host: fdb1029.awardspace.net
-- ================================================
-- Execute this script in phpMyAdmin to create all tables
-- ================================================

-- Drop existing tables if you want a fresh start (CAUTION: This will delete all data!)
-- Uncomment the lines below only if you want to start fresh
-- DROP TABLE IF EXISTS followers;
-- DROP TABLE IF EXISTS journals;
-- DROP TABLE IF EXISTS users;

-- ================================================
-- 1. CREATE USERS TABLE
-- ================================================
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `password` VARCHAR(255) NOT NULL,
  `profile_image_url` VARCHAR(500) DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 2. CREATE JOURNALS TABLE
-- ================================================
CREATE TABLE IF NOT EXISTS `journals` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT(11) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `thumbnail_url` VARCHAR(500) DEFAULT NULL,
  `is_public` TINYINT(1) NOT NULL DEFAULT '0',
  `date` BIGINT(20) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_date` (`date`),
  KEY `idx_public` (`is_public`),
  KEY `idx_title` (`title`),
  CONSTRAINT `journals_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 3. CREATE FOLLOWERS TABLE
-- ================================================
CREATE TABLE IF NOT EXISTS `followers` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `follower_id` INT(11) NOT NULL,
  `following_id` INT(11) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_follow` (`follower_id`,`following_id`),
  KEY `idx_follower` (`follower_id`),
  KEY `idx_following` (`following_id`),
  CONSTRAINT `followers_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `followers_ibfk_2` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- VERIFICATION QUERIES
-- Run these after import to verify tables were created
-- ================================================

-- Show all tables
-- SHOW TABLES;

-- Check users table structure
-- DESCRIBE users;

-- Check journals table structure
-- DESCRIBE journals;

-- Check followers table structure
-- DESCRIBE followers;

-- Count records in each table
-- SELECT 'users' as table_name, COUNT(*) as record_count FROM users
-- UNION ALL
-- SELECT 'journals', COUNT(*) FROM journals
-- UNION ALL
-- SELECT 'followers', COUNT(*) FROM followers;

-- ================================================
-- SAMPLE TEST DATA (OPTIONAL)
-- Uncomment to insert test data for testing
-- ================================================

/*
-- Insert test users
INSERT INTO `users` (`name`, `email`, `phone`, `password`) VALUES
('John Doe', 'john@example.com', '1234567890', MD5('password123')),
('Jane Smith', 'jane@example.com', '0987654321', MD5('password123')),
('Bob Wilson', 'bob@example.com', '5555555555', MD5('password123'));

-- Insert test journals
INSERT INTO `journals` (`user_id`, `title`, `content`, `is_public`, `date`) VALUES
(1, 'My First Journal', 'This is my first journal entry. It is a public entry.', 1, UNIX_TIMESTAMP() * 1000),
(1, 'Private Thoughts', 'This is a private journal entry.', 0, UNIX_TIMESTAMP() * 1000),
(2, 'Travel Diary', 'Today I visited a beautiful place.', 1, UNIX_TIMESTAMP() * 1000),
(3, 'Daily Notes', 'Just some daily notes.', 1, UNIX_TIMESTAMP() * 1000);

-- Insert test follow relationships
-- User 1 follows User 2 and User 3
INSERT INTO `followers` (`follower_id`, `following_id`) VALUES
(1, 2),
(1, 3),
(2, 1);
*/

-- ================================================
-- USEFUL ADMIN QUERIES
-- ================================================

-- Get all users with their journal count
-- SELECT u.id, u.name, u.email, COUNT(j.id) as journal_count
-- FROM users u
-- LEFT JOIN journals j ON u.id = j.user_id
-- GROUP BY u.id
-- ORDER BY journal_count DESC;

-- Get all users with their follower count
-- SELECT u.id, u.name, u.email, COUNT(f.id) as follower_count
-- FROM users u
-- LEFT JOIN followers f ON u.id = f.following_id
-- GROUP BY u.id
-- ORDER BY follower_count DESC;

-- Get all public journals with author info
-- SELECT j.id, j.title, j.content, j.date, u.name as author_name, u.email as author_email
-- FROM journals j
-- JOIN users u ON j.user_id = u.id
-- WHERE j.is_public = 1
-- ORDER BY j.date DESC;

-- Get follow relationships
-- SELECT
--   u1.name as follower_name,
--   u2.name as following_name,
--   f.created_at
-- FROM followers f
-- JOIN users u1 ON f.follower_id = u1.id
-- JOIN users u2 ON f.following_id = u2.id
-- ORDER BY f.created_at DESC;

-- ================================================
-- END OF SQL SCRIPT
-- ================================================
-- Import Instructions:
-- 1. Go to phpMyAdmin
-- 2. Select your database (4604428_barisoulwrite)
-- 3. Click on "Import" tab
-- 4. Choose this file
-- 5. Click "Go" to import
--
-- OR
--
-- 1. Click on "SQL" tab
-- 2. Copy and paste this entire file content
-- 3. Click "Go" to execute
-- ================================================

