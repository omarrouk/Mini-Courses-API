# Mini-Courses-API

Simple Java API for training courses. Uses HttpServer, JDBC, and MySQL.

I built this to practice:
- CRUD endpoints under `/api/v1`
- saving data in MySQL
- reading DB settings from a config file
- pagination
- simple ROLE header checks (ADMIN / USER / TEACHER)

## Project structure

```
config/db.properties.example
schema.sql
src/main/java/com/courses/api/
  App.java
  config/
  model/
  dto/
  repository/
  service/
  handler/
  util/
postman/
```

## How to set up the database

1. Open MySQL Workbench and connect (`127.0.0.1`, port `3306`, user `root`).
2. Create the database:

```sql
CREATE DATABASE mini;
```

3. Select `mini`, then run `schema.sql`.
4. Copy the example config and put your password:

```bash
copy config\db.properties.example config\db.properties
```

```properties
db.url=jdbc:mysql://127.0.0.1:3306/mini?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=YOUR_PASSWORD
```

## How to run

Need JDK 17+ and MySQL.

```bash
mvnw.cmd clean package
mvnw.cmd exec:java
```

Or:

```bash
java -jar target/mini-courses-api-1.0.0.jar
```

App runs on `http://localhost:8080`.

## Endpoints

| Method | URL | ROLE | Success |
|--------|-----|------|---------|
| GET | `/api/v1/courses?page=&size=` | none | 200 |
| GET | `/api/v1/courses/{id}` | USER | 200 |
| POST | `/api/v1/courses` | ADMIN | 201 |
| PUT | `/api/v1/courses/{id}` | TEACHER or ADMIN | 200 |
| DELETE | `/api/v1/courses/{id}` | ADMIN | 204 |

### Notes
- No ROLE on a protected route → 401
- Wrong ROLE → 403
- Bad input → 400
- Course not found → 404

### Validation
- title: required, max 15 chars
- description: optional, max 50 chars
- capacity: must be > 0

### Error example

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "title must not be blank"
}
```

## Testing with Postman

Import `postman/Mini_Courses_API.postman_collection.json` and try:
1. list courses (no ROLE)
2. create with ADMIN
3. get one with USER
4. update with TEACHER
5. delete with ADMIN
6. check 401 / 403 / 400 / 404 cases

## Screenshots

Put Postman screenshots in `screenshots/`:
- create 201
- list 200
- update 200
- delete 204
- one error (400 or 404)
