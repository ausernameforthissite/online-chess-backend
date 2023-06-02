DROP TABLE IF EXISTS current_user_rating;
DROP TABLE IF EXISTS chess_game_user_ratings_record;

CREATE TABLE current_user_rating (
     username VARCHAR,
     chess_game_type VARCHAR,
     rating INT NOT NULL,
     games_played INT NOT NULL,
     k INT NOT NULL,
     PRIMARY KEY(username, chess_game_type)
);

CREATE TABLE chess_game_user_ratings_record (
     game_id VARCHAR PRIMARY KEY,
     chess_game_type VARCHAR NOT NULL,
     started_at TIMESTAMPTZ NOT NULL,
     finished BOOLEAN NOT NULL,
     white_username VARCHAR NOT NULL,
     white_initial_rating INT NOT NULL,
     white_rating_change INT,
     black_username VARCHAR NOT NULL,
     black_initial_rating INT NOT NULL,
     black_rating_change INT,
     technical_finish BOOLEAN NOT NULL,
     draw BOOLEAN NOT NULL,
     winner_color VARCHAR
);