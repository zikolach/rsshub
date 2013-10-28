# Feeds schema

# --- !Ups

CREATE SEQUENCE feeds_id_seq;
CREATE TABLE feeds (
    id integer NOT NULL DEFAULT nextval('comments_id_seq'),
    user_id integer NOT NULL,
    name varchar(255) NOT NULL,
    description text NOT NULL,
    updated_at timestamp,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE feed_sources (
    feed_id integer NOT NULL,
    source_id integer NOT NULL,
    PRIMARY KEY (feed_id, source_id),
    FOREIGN KEY (feed_id) REFERENCES feeds(id),
    FOREIGN KEY (source_id) REFERENCES sources(id)
);

# --- !Downs

DROP TABLE feed_sources;
DROP TABLE feeds;
DROP SEQUENCE feeds_id_seq;



