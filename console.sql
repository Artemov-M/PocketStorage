--
DROP TABLE IF EXISTS users;
CREATE TABLE users
(
    id        BIGSERIAL PRIMARY KEY,
    user_hash TEXT UNIQUE NOT NULL
);

DROP TABLE IF EXISTS key;
CREATE TABLE key
(
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT REFERENCES users (id) ON DELETE CASCADE,
    key_hash TEXT
);

DROP TABLE IF EXISTS value;
CREATE TABLE value
(
    id     BIGSERIAL PRIMARY KEY,
--     or
--     val_number INT NOT NULL, PRIMARY KEY (val_number, key_id),
    key_id BIGINT REFERENCES key (id) ON DELETE CASCADE,
    value  TEXT
);

--
INSERT INTO users (user_hash)
VALUES ('938db8c9f82c8cb58d3f3ef4fd250036a48d26a712753d2fde5abd03a85cabf4'),
       ('a953f09a1b6b6725b81956e9ad0b1eb49e3ad40004c04307ef8af6246a054116'),
       ('0b8efa5a3bf104413a725c6ff0459a6be12b1fd33314cbb138745baf39504ae5'),
       ('6cd5b6e51936a442b973660c21553dd22bd72ddc8751132a943475288113b4c0'),
       ('c97550ce8213ef5cf6ed4ba48790c137df3ef6a5da20b48961001a634b6cead2');

INSERT INTO key (user_id, key_hash)
VALUES (1, 'c97550ce8213ef5cf6ed4ba48790c137df3ef6a5da20b48961001a634b6cead2'),
       (2, 'c97550ce8213ef5cf6ed4ba48790c137df3ef6a5da20b48961001a634b6cead2'),
       (5, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'),
       (3, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'),
       (4, 'f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a3'),
       (1, 'f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a3'),
       (1, '7020e57625b6a6695ffd51ed494fbfc56c699eaceca4e77bf7ea590c7ebf3879'),
       (3, '7020e57625b6a6695ffd51ed494fbfc56c699eaceca4e77bf7ea590c7ebf3879'),
       (5, '511f51a9b0063e4f1c918e55b0e8a9b5f38c5d30ae1e055966a7ce7e2249a6c6'),
       (4, '511f51a9b0063e4f1c918e55b0e8a9b5f38c5d30ae1e055966a7ce7e2249a6c6'),
       (2, '511f51a9b0063e4f1c918e55b0e8a9b5f38c5d30ae1e055966a7ce7e2249a6c6');

INSERT INTO value (key_id, value)
VALUES (11, 'asdываfasdf'),
       (2, 'asdfываersdvxc'),
       (5, 'bawыаewfbx'),
       (4, 'asdrываfbawe'),
       (4, '2sdыаf[a3r[af'),
       (6, 'a3rqываq'),
       (2, 'vq2ыва4vq25b'),
       (8, 'qwerqыва3qed'),
       (6, 'qv34vqыаdefavvr3'),
       (11, 'vq12ыва34qvd'),
       (10, 'vbq3ыа4vqdrqw');
