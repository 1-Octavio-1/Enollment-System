// Person.java
// Abstract superclass demonstrating Abstraction & Encapsulation

public abstract class Person {
    private String id;
    private String name;

    public Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Encapsulation: getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Polymorphism: subclasses override this to display info differently
    public abstract void displayInfo();
}
