CREATE DATABASE IF NOT EXISTS url_db;

USE url_db;

CREATE TABLE data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    click_count INTEGER NOT NULL,
    creation_time DATETIME(6) NOT NULL,
    expiration_time DATETIME(6),
    shortened_url VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;


CREATE EVENT delete_expired_urls
ON SCHEDULE EVERY 1 MINUTE
DO
  DELETE FROM data
  WHERE expirationTime IS NOT NULL AND expiration_time < NOW();