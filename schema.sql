DROP TABLE IF EXISTS likes;
CREATE TABLE IF NOT EXISTS likes (
    steam_id   TEXT NOT NULL,
    coordinate TEXT NOT NULL,
    CONSTRAINT likes_unique UNIQUE (steam_id, coordinate)
);
