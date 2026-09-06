-- Courses table for Mini Courses API (MySQL)
-- 1) In MySQL Workbench: CREATE DATABASE mini;
-- 2) Double-click schema "mini", then run this script.

CREATE TABLE IF NOT EXISTS courses (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(15)  NOT NULL,
    description VARCHAR(50)  NULL,
    capacity    INT          NOT NULL
);
