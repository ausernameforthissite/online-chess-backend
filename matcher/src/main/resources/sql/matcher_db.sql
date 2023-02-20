DROP TABLE IF EXISTS user_rating;

CREATE TABLE user_rating (
     username VARCHAR(50) PRIMARY KEY,
     user_rating SMALLINT NOT NULL
);