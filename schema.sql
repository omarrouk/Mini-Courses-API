-- courses table for the API

CREATE TABLE IF NOT EXISTS courses (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(15)  NOT NULL,
    description VARCHAR(50)  NULL,
    capacity    INT          NOT NULL
);
