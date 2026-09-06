package com.courses.api.service;

import com.courses.api.dto.PageResponse;
import com.courses.api.model.Course;
import com.courses.api.repository.CourseRepository;
import com.courses.api.util.NotFoundException;
import com.courses.api.util.ValidationException;

public class CourseService {
    private static final int TITLE_MAX = 15;
    private static final int DESCRIPTION_MAX = 50;

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public PageResponse listCourses(int page, int size) {
        validatePagination(page, size);

        long totalItems = courseRepository.countAll();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);

        return new PageResponse(
                courseRepository.findPage(page, size),
                page,
                size,
                totalItems,
                totalPages
        );
    }

    public Course getCourse(long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + id));
    }

    public Course createCourse(Course course) {
        validateCourse(course);
        return courseRepository.create(course);
    }

    public Course updateCourse(long id, Course course) {
        validateCourse(course);

        boolean updated = courseRepository.update(id, course);
        if (!updated) {
            throw new NotFoundException("Course not found with id: " + id);
        }

        course.setId(id);
        return course;
    }

    public void deleteCourse(long id) {
        boolean deleted = courseRepository.delete(id);
        if (!deleted) {
            throw new NotFoundException("Course not found with id: " + id);
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 1) {
            throw new ValidationException("page must be greater than or equal to 1");
        }
        if (size < 1 || size > 100) {
            throw new ValidationException("size must be between 1 and 100");
        }
    }

    private void validateCourse(Course course) {
        if (course == null) {
            throw new ValidationException("request body is required");
        }

        String title = course.getTitle();
        if (title == null || title.isBlank()) {
            throw new ValidationException("title must not be blank");
        }
        if (title.length() > TITLE_MAX) {
            throw new ValidationException("title must not exceed " + TITLE_MAX + " characters");
        }

        String description = course.getDescription();
        if (description != null && description.length() > DESCRIPTION_MAX) {
            throw new ValidationException("description must not exceed " + DESCRIPTION_MAX + " characters");
        }

        Integer capacity = course.getCapacity();
        if (capacity == null) {
            throw new ValidationException("capacity is required");
        }
        if (capacity <= 0) {
            throw new ValidationException("capacity must be greater than zero");
        }
    }
}
