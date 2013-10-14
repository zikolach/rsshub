# Comments schema

# --- !Ups

CREATE SEQUENCE comments_id_seq;
CREATE TABLE comments (
    id integer NOT NULL DEFAULT nextval('comments_id_seq'),
    post_id integer NOT NULL,
    update_date timestamp,
    comment text,
    PRIMARY KEY (id),
    FOREIGN KEY (post_id) REFERENCES posts(id)
);

# --- !Downs

DROP TABLE comments;
DROP SEQUENCE comments_id_seq;
