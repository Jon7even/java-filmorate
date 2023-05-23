create TABLE IF NOT EXISTS person (
	id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	email VARCHAR(48) NOT NULL,
	login VARCHAR(20) NOT NULL UNIQUE,
	first_name VARCHAR(20) NOT NULL,
	birthday date NOT NULL
);

create type IF NOT EXISTS status_friendship as ENUM (
	'REQUEST', 'APPROVED', 'BLACK LIST'
);

CREATE TABLE IF NOT EXISTS person_friend (
	person_id INTEGER NOT NULL REFERENCES person(id),
	person_friend_id INTEGER NOT NULL REFERENCES person(id),
	friendship status_friendship NOT NULL DEFAULT 'REQUEST',
	PRIMARY KEY (person_id, person_friend_id)
);

create TABLE IF NOT EXISTS genre (
	id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
	name VARCHAR(40) NOT NULL,
	description VARCHAR(200) NOT NULL DEFAULT 'Description coming soon'
);

create type IF NOT EXISTS rating_mpa as ENUM (
	'G', 'PG', 'PG-13', 'R', 'NC-17'
);

CREATE TABLE IF NOT EXISTS film (
	id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
	title VARCHAR(80) NOT NULL,
	description VARCHAR(200) NOT NULL DEFAULT 'Description coming soon',
	release_date date NOT NULL,
	duration INTEGER NOT NULL,
	rating rating_mpa NOT NULL DEFAULT 'NC-17'
);

create TABLE IF NOT EXISTS film_genre (
	film_id INTEGER NOT NULL REFERENCES film(id),
	genre_id INTEGER NOT NULL REFERENCES genre(id),
	PRIMARY KEY (film_id, genre_id)
);

create TABLE IF NOT EXISTS film_likes (
	film_id INTEGER NOT NULL REFERENCES film(id),
	person_id INTEGER NOT NULL REFERENCES person(id),
	PRIMARY KEY (film_id, person_id)
);

INSERT INTO genre (name) VALUES ('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');