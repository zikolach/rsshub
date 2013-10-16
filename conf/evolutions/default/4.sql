# Comments schema
# Users schema

# --- !Ups

CREATE SEQUENCE comments_id_seq;
CREATE TABLE comments (
    id integer NOT NULL DEFAULT nextval('comments_id_seq'),
    post_id integer NOT NULL,
    update_date timestamp,
    comment text,
    PRIMARY KEY (id),
    FOREIGN KEY (post_id) REFERENCES posts(id)

CREATE SEQUENCE users_id_seq;
CREATE TABLE users (
    id integer NOT NULL DEFAULT nextval('users_id_seq'),
    name varchar(255),
    password varchar(255),
    last_login_date timestamp,
    PRIMARY KEY (id),
    UNIQUE (name)
);
CREATE TABLE tokens (
    token varchar(255),
    user_id integer,
    ip varchar(15),
    PRIMARY KEY (token),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

# --- !Downs

DROP TABLE tokens;
DROP TABLE users;
DROP SEQUENCE users_id_seq;
DROP TABLE comments;
DROP SEQUENCE comments_id_seq;

