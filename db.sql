CREATE DATABASE IF NOT EXISTS survival;

CREATE USER IF NOT EXISTS 'minecraft'@'localhost' IDENTIFIED BY 'TaSqTQ2AfLwn&4a&rKZW';
GRANT ALL PRIVILEGES ON survival.* TO 'minecraft'@'localhost';

USE survival;

DROP TABLE IF EXISTS players_bank;
DROP TABLE IF EXISTS players;

CREATE TABLE IF NOT EXISTS players (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(16) NOT NULL,
  UUID VARCHAR(36) NOT NULL,
  last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX uuid_index (UUID)
);

CREATE TABLE IF NOT EXISTS players_bank (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  UUID VARCHAR(36) NOT NULL,
  balance DOUBLE(12, 2) NOT NULL DEFAULT 0.00,
  last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_uuid FOREIGN KEY (UUID) REFERENCES players(UUID)
);
