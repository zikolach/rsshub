# Comments schema

# --- !Ups

CREATE SEQUENCE comments_id_seq;
CREATE TABLE comments (
    id integer NOT NULL DEFAULT nextval('comments_id_seq'),
    post_id integer NOT NULL,
    user_id integer NOT NULL,
    update_date timestamp,
    comment text,
    PRIMARY KEY (id),
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
ALTER TABLE posts ADD COLUMN user_id integer;
ALTER TABLE posts ADD COLUMN source_id integer;
ALTER TABLE posts ADD FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE posts ADD FOREIGN KEY (source_id) REFERENCES sources(id);
ALTER TABLE sources ADD COLUMN user_id integer;
ALTER TABLE sources ADD FOREIGN KEY (user_id) REFERENCES users(id);


# --- !Downs

ALTER TABLE sources DROP COLUMN user_id;
ALTER TABLE posts DROP COLUMN user_id;
ALTER TABLE posts DROP COLUMN source_id;
DROP TABLE comments;
DROP SEQUENCE comments_id_seq;


