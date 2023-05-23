CREATE TABLE IF NOT EXISTS "user" (
	"id" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	"email" VARCHAR(48) NOT NULL UNIQUE,
	"login" VARCHAR(20) NOT NULL UNIQUE,
	"first_name" VARCHAR(20) NOT NULL,
	"berthday" date NOT NULL
);

CREATE TYPE IF NOT EXISTS "status_friendship" AS ENUM (
	'REQUEST', 'APPROVED', 'BLACK LIST'
);

CREATE TABLE IF NOT EXISTS "user_friend" (
	"user_id" INTEGER NOT NULL REFERENCES "user"("id"),
	"user_friend_id" INTEGER NOT NULL REFERENCES "user"("id"),
	"friendship" "status_friendship" NOT NULL DEFAULT 'REQUEST',
	PRIMARY KEY ("user_id", "user_friend_id")
);

CREATE TABLE IF NOT EXISTS "genre" (
	"id" INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
	"name" VARCHAR(40) NOT NULL,
	"description" VARCHAR(200) NOT NULL DEFAULT 'Description coming soon'
);

CREATE TYPE IF NOT EXISTS "rating_mpa" AS ENUM (
	'G', 'PG', 'PG-13', 'R', 'NC-17'
);

CREATE TABLE IF NOT EXISTS "film" (
	"id" INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
	"title" VARCHAR(80) NOT NULL,
	"description" VARCHAR(200) NOT NULL DEFAULT 'Description coming soon',
	"release_date" date NOT NULL,
	"duration" INTEGER NOT NULL,
	"rating" "rating_mpa" NOT NULL DEFAULT 'NC-17'
);

CREATE TABLE IF NOT EXISTS "film_genre" (
	"film_id" INTEGER NOT NULL REFERENCES "film"("id"),
	"genre_id" INTEGER NOT NULL REFERENCES "genre"("id"),
	PRIMARY KEY ("film_id", "genre_id")
);

CREATE TABLE IF NOT EXISTS "film_likes" (
	"film_id" INTEGER NOT NULL REFERENCES "film"("id"),
	"user_id" INTEGER NOT NULL REFERENCES "user"("id"),
	PRIMARY KEY ("film_id", "user_id")
);

INSERT INTO "genre" ("name") VALUES ('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');