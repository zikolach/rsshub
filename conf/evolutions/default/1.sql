# Posts schema

# --- !Ups

CREATE SEQUENCE posts_id_seq;
CREATE TABLE posts (
    id integer NOT NULL DEFAULT nextval('posts_id_seq'),
    name varchar(255),
    text varchar(1000)
);

# --- !Downs

DROP TABLE posts;
DROP SEQUENCE posts_id_seq;