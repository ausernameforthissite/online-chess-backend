DROP TABLE IF EXISTS user_rating;

CREATE TABLE user_rating (
     user_id BIGINT PRIMARY KEY,
     user_rating SMALLINT NOT NULL
);