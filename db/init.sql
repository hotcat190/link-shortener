CREATE DATABASE IF NOT EXISTS url_db;

USE url_db;

CREATE TABLE data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    url TEXT NOT NULL,
    shortened_url VARCHAR(255) NOT NULL,
    click_count INTEGER NOT NULL,
    creation_time DATETIME(6) NOT NULL,
    expiration_time DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;


CREATE EVENT delete_expired_urls
ON SCHEDULE EVERY 1 MINUTE
DO
  DELETE FROM data
  WHERE expiration_time IS NOT NULL AND expiration_time < NOW();