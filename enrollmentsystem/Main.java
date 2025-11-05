// Main.java
// Entry point with console menu and file I/O coordination.

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    // Files in working directory
    private static final Path STUDENTS_FILE = Paths.get("students.txt");
    private static final Path COURSES_FILE = Paths.get("courses.txt");
    private static final Path ENROLLMENTS_FILE = Paths.get("enrollments.txt");

    private static List<Student> students = new ArrayList<>();
    private static List<Course> courses = new ArrayList<>();
    private static List<Enrollment> enrollments = new ArrayList<>();

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadAllData();
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": addStudent(); break;
                case "2": viewAllStudents(); break;
                case "3": deleteStudent(); break;
                case "4": addCourse(); break;
                case "5": viewAllCourses(); break;
                case "6": deleteCourse(); break;
                case "7": enrollStudentInCourse(); break;
                case "8": viewAllEnrollments(); break;
                case "9": dropStudentFromCourse(); break;
                case "10": exportEnrollmentsCsv(); break;
                case "11": saveAllData(); running = false; System.out.println("Saved. Exiting."); break;
                default: System.out.println("Invalid choice. Try again."); break;
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== ONLINE ENROLLMENT SYSTEM =====");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. Delete Student");
        System.out.println("4. Add Course");
        System.out.println("5. View All Courses");
        System.out.println("6. Delete Course");
        System.out.println("7. Enroll Student in Course");
        System.out.println("8. View All Enrollments");
        System.out.println("9. Drop Student from Course");
        System.out.println("10. Export Enrollments to CSV");
        System.out.println("11. Exit and Save");
        System.out.print("Enter choice: ");
    }

    // -------------------- Load & Save --------------------
    private static void loadAllData() {
        students = loadStudents();
        courses = loadCourses();
        enrollments = loadEnrollments();

        // Sync enrollments into student objects
        Map<String, Student> studentMap = students.stream().collect(Collectors.toMap(Student::getStudentId, s -> s));
        for (Enrollment e : enrollments) {
            Student s = studentMap.get(e.getStudentId());
            if (s != null && !s.getEnrolledCourses().contains(e.getCourseId())) {
                s.enrollInCourse(e.getCourseId()); // update in-memory student list
            }
        }
        System.out.println("Data loaded: " + students.size() + " students, " + courses.size() + " courses, " + enrollments.size() + " enrollments.");
    }

    private static void saveAllData() {
        try {
            saveStudents();
            saveCourses();
            saveEnrollments();
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // -------------------- Student operations --------------------
    private static List<Student> loadStudents() {
        List<Student> list = new ArrayList<>();
        if (Files.exists(STUDENTS_FILE)) {
            try {
                List<String> lines = Files.readAllLines(STUDENTS_FILE, StandardCharsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    list.add(Student.fromRecord(line));
                }
            } catch (IOException e) {
                System.out.println("Failed to load students: " + e.getMessage());
            }
        }
        return list;
    }

    private static void saveStudents() throws IOException {
        List<String> lines = students.stream().map(Student::toRecord).collect(Collectors.toList());
        Files.write(STUDENTS_FILE, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void addStudent() {
        System.out.print("Enter Student ID: ");
        String sid = scanner.nextLine().trim();
        if (findStudentById(sid) != null) {
            System.out.println("Student ID already exists.");
            return;
        }
        System.out.print("Enter Student Name: ");
        String name = scanner.nextLine().trim();
        Student s = new Student(sid, name);
        students.add(s);
        try { saveStudents(); System.out.println("Student added and saved."); } catch (IOException e) { System.out.println("Added but failed to save: " + e.getMessage()); }
    }

    private static void viewAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        students.forEach(Student::displayInfo);
    }

    private static void deleteStudent() {
        System.out.print("Enter Student ID to delete: ");
        String sid = scanner.nextLine().trim();
        Student s = findStudentById(sid);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }
        students.remove(s);
        // remove related enrollments
        enrollments.removeIf(e -> e.getStudentId().equals(sid));
        // also remove the course references in other students (shouldn't be needed but safe)
        students.forEach(st -> st.dropCourse(sid)); // no-op normally
        try {
            saveStudents(); saveEnrollments();
            System.out.println("Student and related enrollments deleted.");
        } catch (IOException e) {
            System.out.println("Deleted but failed to save: " + e.getMessage());
        }
    }

    // -------------------- Course operations --------------------
    private static List<Course> loadCourses() {
        List<Course> list = new ArrayList<>();
        if (Files.exists(COURSES_FILE)) {
            try {
                List<String> lines = Files.readAllLines(COURSES_FILE, StandardCharsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    list.add(Course.fromRecord(line));
                }
            } catch (IOException e) {
                System.out.println("Failed to load courses: " + e.getMessage());
            }
        }
        return list;
    }

    private static void saveCourses() throws IOException {
        List<String> lines = courses.stream().map(Course::toRecord).collect(Collectors.toList());
        Files.write(COURSES_FILE, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void addCourse() {
        System.out.print("Enter Course ID: ");
        String cid = scanner.nextLine().trim();
        if (findCourseById(cid) != null) {
            System.out.println("Course ID already exists.");
            return;
        }
        System.out.print("Enter Course Name: ");
        String name = scanner.nextLine().trim();
        int credits = readIntSafe("Enter Credits (integer): ");
        int capacity = readIntSafe("Enter Capacity (integer): ");
        Course c = new Course(cid, name, credits, capacity);
        courses.add(c);
        try { saveCourses(); System.out.println("Course added and saved."); } catch (IOException e) { System.out.println("Added but failed to save: " + e.getMessage()); }
    }

    private static void viewAllCourses() {
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }
        for (Course c : courses) {
            long enrolledCount = enrollments.stream().filter(e -> e.getCourseId().equals(c.getCourseId())).count();
            System.out.printf("%s | %s | Credits: %d | Capacity: %d | Enrolled: %d%n",
                    c.getCourseId(), c.getCourseName(), c.getCredits(), c.getCapacity(), enrolledCount);
        }
    }

    private static void deleteCourse() {
        System.out.print("Enter Course ID to delete: ");
        String cid = scanner.nextLine().trim();
        Course c = findCourseById(cid);
        if (c == null) {
            System.out.println("Course not found.");
            return;
        }
        courses.remove(c);
        // remove related enrollments and remove from student lists
        enrollments.removeIf(e -> e.getCourseId().equals(cid));
        students.forEach(s -> s.dropCourse(cid));
        try {
            saveCourses(); saveEnrollments(); saveStudents();
            System.out.println("Course and related enrollments deleted.");
        } catch (IOException e) {
            System.out.println("Deleted but failed to save: " + e.getMessage());
        }
    }

    // -------------------- Enrollment operations --------------------
    private static List<Enrollment> loadEnrollments() {
        List<Enrollment> list = new ArrayList<>();
        if (Files.exists(ENROLLMENTS_FILE)) {
            try {
                List<String> lines = Files.readAllLines(ENROLLMENTS_FILE, StandardCharsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    Enrollment e = Enrollment.fromRecord(line);
                    if (e != null) list.add(e);
                }
            } catch (IOException e) {
                System.out.println("Failed to load enrollments: " + e.getMessage());
            }
        }
        return list;
    }

    private static void saveEnrollments() throws IOException {
        List<String> lines = enrollments.stream().map(Enrollment::toRecord).collect(Collectors.toList());
        Files.write(ENROLLMENTS_FILE, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void enrollStudentInCourse() {
        System.out.print("Enter Student ID: ");
        String sid = scanner.nextLine().trim();
        Student s = findStudentById(sid);
        if (s == null) {
            System.out.println("Student not found. Add student first.");
            return;
        }
        System.out.print("Enter Course ID: ");
        String cid = scanner.nextLine().trim();
        Course c = findCourseById(cid);
        if (c == null) {
            System.out.println("Course not found. Add course first.");
            return;
        }
        // capacity check
        long enrolledCount = enrollments.stream().filter(e -> e.getCourseId().equals(cid)).count();
        if (enrolledCount >= c.getCapacity()) {
            System.out.println("Course is full. Cannot enroll.");
            return;
        }
        // already enrolled check
        boolean already = enrollments.stream().anyMatch(e -> e.getStudentId().equals(sid) && e.getCourseId().equals(cid));
        if (already) {
            System.out.println("Student already enrolled in this course.");
            return;
        }
        // perform enroll
        s.enrollInCourse(cid);
        enrollments.add(new Enrollment(sid, cid));
        try { saveStudents(); saveEnrollments(); System.out.println("Enrollment successful and saved."); } catch (IOException e) { System.out.println("Enrolled but failed to save: " + e.getMessage()); }
    }

    private static void viewAllEnrollments() {
        if (enrollments.isEmpty()) {
            System.out.println("No enrollments.");
            return;
        }
        for (Enrollment e : enrollments) {
            e.displayInfo();
        }
    }

    private static void dropStudentFromCourse() {
        System.out.print("Enter Student ID: ");
        String sid = scanner.nextLine().trim();
        Student s = findStudentById(sid);
        if (s == null) {
            System.out.println("Student not found.");
            return;
        }
        System.out.print("Enter Course ID to drop: ");
        String cid = scanner.nextLine().trim();
        boolean enrolled = enrollments.stream().anyMatch(e -> e.getStudentId().equals(sid) && e.getCourseId().equals(cid));
        if (!enrolled) {
            System.out.println("That enrollment does not exist.");
            return;
        }
        s.dropCourse(cid);
        enrollments.removeIf(e -> e.getStudentId().equals(sid) && e.getCourseId().equals(cid));
        try { saveStudents(); saveEnrollments(); System.out.println("Dropped and saved."); } catch (IOException e) { System.out.println("Dropped but failed to save: " + e.getMessage()); }
    }

    // -------------------- Utility --------------------
    private static Student findStudentById(String sid) {
        for (Student s : students) if (s.getStudentId().equals(sid)) return s;
        return null;
    }

    private static Course findCourseById(String cid) {
        for (Course c : courses) if (c.getCourseId().equals(cid)) return c;
        return null;
    }

    private static int readIntSafe(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }

    private static void exportEnrollmentsCsv() {
        Path out = Paths.get("export_enrollments.csv");
        List<String> lines = new ArrayList<>();
        lines.add("student_id,course_id");
        for (Enrollment e : enrollments) lines.add(e.toRecord());
        try {
            Files.write(out, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Exported to " + out.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to export: " + e.getMessage());
        }
    }
}
