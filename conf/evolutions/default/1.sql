# Posts schema

# --- !Ups

CREATE SEQUENCE posts_id_seq;
CREATE TABLE posts (
    id integer NOT NULL DEFAULT nextval('posts_id_seq'),
    name varchar(255),
    text text,
    fingerprint text
);

# --- !Downs

DROP TABLE posts;
DROP SEQUENCE posts_id_seq;