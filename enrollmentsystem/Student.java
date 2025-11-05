// Student.java
// Student extends Person (Inheritance). Encapsulation for fields and methods.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Student extends Person {
    private List<String> enrolledCourses; // list of course IDs

    public Student(String studentId, String name) {
        super(studentId, name);
        this.enrolledCourses = new ArrayList<>();
    }

    // Constructor used when loading from file with existing courses
    public Student(String studentId, String name, List<String> courses) {
        super(studentId, name);
        this.enrolledCourses = new ArrayList<>(courses);
    }

    public String getStudentId() {
        return getId();
    }

    // Encapsulation: return copy to avoid external mutation
    public List<String> getEnrolledCourses() {
        return new ArrayList<>(enrolledCourses);
    }

    // Behavior methods
    public boolean enrollInCourse(String courseId) {
        if (enrolledCourses.contains(courseId)) return false;
        enrolledCourses.add(courseId);
        return true;
    }

    public boolean dropCourse(String courseId) {
        return enrolledCourses.remove(courseId);
    }

    @Override
    public void displayInfo() {
        String courses = enrolledCourses.isEmpty() ? "(none)" : String.join(", ", enrolledCourses);
        System.out.printf("Student ID: %s | Name: %s | Enrolled: %s%n", getStudentId(), getName(), courses);
    }

    // Text storage helpers
    // Format: studentId,name,course1|course2|course3
    public String toRecord() {
        String coursesPart = enrolledCourses.isEmpty() ? "" : enrolledCourses.stream().collect(Collectors.joining("|"));
        return String.format("%s,%s,%s", getStudentId(), escape(getName()), escape(coursesPart));
    }

    // Parse record: expects exactly 3 parts but tolerates missing courses
    public static Student fromRecord(String line) {
        // naive CSV-like split (we keep it simple; names shouldn't contain commas for this project)
        String[] parts = line.split(",", 3);
        String sid = parts.length > 0 ? parts[0].trim() : "";
        String name = parts.length > 1 ? unescape(parts[1].trim()) : "";
        List<String> courses = new ArrayList<>();
        if (parts.length > 2) {
            String coursePart = unescape(parts[2].trim());
            if (!coursePart.isEmpty()) {
                courses = Arrays.stream(coursePart.split("\\|"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        }
        return new Student(sid, name, courses);
    }

    // simple escaping for commas/pipes (very basic)
    private static String escape(String s) {
        return s.replace("\n", " ").replace("\r", " ");
    }

    private static String unescape(String s) {
        return s;
    }
}
