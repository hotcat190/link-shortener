-- Generic init script for each shard
-- Database name is set via MYSQL_DATABASE env var

CREATE TABLE IF NOT EXISTS data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    url TEXT NOT NULL,
    shortened_url VARCHAR(255) NOT NULL,
    click_count INTEGER NOT NULL,
    creation_time DATETIME(6) NOT NULL,
    expiration_time DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uc_shortened_url (shortened_url),
    INDEX idx_shortened_url (shortened_url)
) ENGINE=InnoDB;

-- Event to clean expired URLs
SET GLOBAL event_scheduler = ON;

CREATE EVENT IF NOT EXISTS delete_expired_urls
ON SCHEDULE EVERY 1 MINUTE
DO
  DELETE FROM data
  WHERE expiration_time IS NOT NULL AND expiration_time < NOW();
