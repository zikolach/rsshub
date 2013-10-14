# Sources schema

# --- !Ups

CREATE SEQUENCE sources_id_seq;
CREATE TABLE sources (
    id integer NOT NULL DEFAULT nextval('sources_id_seq'),
    name varchar(255),
    url varchar(1000),
    fetch_date timestamp,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE sources;
DROP SEQUENCE sources_id_seq;