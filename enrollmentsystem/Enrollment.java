// Enrollment.java
// Represents an enrollment record linking student and course.

public class Enrollment {
    private String studentId;
    private String courseId;

    public Enrollment(String studentId, String courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    // File record: studentId,courseId
    public String toRecord() {
        return String.format("%s,%s", studentId, courseId);
    }

    public static Enrollment fromRecord(String line) {
        String[] parts = line.split(",", 2);
        if (parts.length < 2) return null;
        return new Enrollment(parts[0].trim(), parts[1].trim());
    }

    public void displayInfo() {
        System.out.printf("Student: %s | Course: %s%n", studentId, courseId);
    }
}
