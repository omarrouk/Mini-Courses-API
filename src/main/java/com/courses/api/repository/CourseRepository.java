package com.courses.api.repository;

import com.courses.api.config.DatabaseConfig;
import com.courses.api.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseRepository {
    private final DatabaseConfig databaseConfig;

    public CourseRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Course create(Course course) {
        String sql = "INSERT INTO courses (title, description, capacity) VALUES (?, ?, ?)";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, course.getTitle());
            statement.setString(2, course.getDescription());
            statement.setInt(3, course.getCapacity());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating course failed: no ID returned");
                }
                course.setId(keys.getLong(1));
                return course;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while creating course", e);
        }
    }

    public Optional<Course> findById(long id) {
        String sql = "SELECT id, title, description, capacity FROM courses WHERE id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapCourse(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while retrieving course", e);
        }
    }

    public List<Course> findPage(int page, int size) {
        String sql = """
                SELECT id, title, description, capacity
                FROM courses
                ORDER BY id ASC
                LIMIT ? OFFSET ?
                """;

        int offset = (page - 1) * size;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, size);
            statement.setInt(2, offset);

            List<Course> courses = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    courses.add(mapCourse(resultSet));
                }
            }
            return courses;
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while listing courses", e);
        }
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM courses";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while counting courses", e);
        }
    }

    public boolean update(long id, Course course) {
        String sql = "UPDATE courses SET title = ?, description = ?, capacity = ? WHERE id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, course.getTitle());
            statement.setString(2, course.getDescription());
            statement.setInt(3, course.getCapacity());
            statement.setLong(4, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while updating course", e);
        }
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM courses WHERE id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while deleting course", e);
        }
    }

    private Course mapCourse(ResultSet resultSet) throws SQLException {
        return new Course(
                resultSet.getLong("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getInt("capacity")
        );
    }
}
