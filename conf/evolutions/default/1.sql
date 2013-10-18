# Posts schema

# --- !Ups

CREATE SEQUENCE posts_id_seq;
CREATE TABLE posts (
    id          integer NOT NULL DEFAULT nextval('posts_id_seq'),
    title       varchar(1000),
    link        varchar(1000),
    description text,
    pub_date    timestamp,
    fingerprint text,
    PRIMARY KEY (id),
    UNIQUE (link)
);

# --- !Downs

DROP TABLE posts;
DROP SEQUENCE posts_id_seq;