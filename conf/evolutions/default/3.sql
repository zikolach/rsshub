# Tags schema

# --- !Ups

CREATE SEQUENCE tags_id_seq;
CREATE TABLE tags (
    id integer NOT NULL DEFAULT nextval('tags_id_seq'),
    name varchar(255),
    PRIMARY KEY (id)
);
CREATE TABLE post_tags (
    post_id integer NOT NULL,
    tag_id integer NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (tag_id) REFERENCES tags(id),
    PRIMARY KEY (post_id, tag_id)
);

# --- !Downs

DROP TABLE post_tags;
DROP TABLE tags;
DROP SEQUENCE tags_id_seq;
