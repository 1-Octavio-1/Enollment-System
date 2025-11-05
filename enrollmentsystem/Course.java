// Course.java
// Course class with file-record helpers and display method.

public class Course {
    private String courseId;
    private String courseName;
    private int credits;
    private int capacity;

    public Course(String courseId, String courseName, int credits, int capacity) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.capacity = capacity;
    }

    // Getters & Setters (encapsulation)
    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCredits() {
        return credits;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void displayInfo() {
        System.out.printf("Course ID: %s | %s | Credits: %d | Capacity: %d%n",
                courseId, courseName, credits, capacity);
    }

    // File record format: courseId,courseName,credits,capacity
    public String toRecord() {
        return String.format("%s,%s,%d,%d", courseId, courseName.replace(",", " "), credits, capacity);
    }

    public static Course fromRecord(String line) {
        String[] parts = line.split(",", 4);
        String cid = parts.length > 0 ? parts[0].trim() : "";
        String name = parts.length > 1 ? parts[1].trim() : "";
        int credits = 0;
        int capacity = 0;
        try {
            if (parts.length > 2) credits = Integer.parseInt(parts[2].trim());
            if (parts.length > 3) capacity = Integer.parseInt(parts[3].trim());
        } catch (NumberFormatException e) {
            // default to 0 if parse fails
        }
        return new Course(cid, name, credits, capacity);
    }
}
