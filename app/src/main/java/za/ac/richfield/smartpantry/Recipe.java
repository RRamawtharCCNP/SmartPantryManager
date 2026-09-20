package za.ac.richfield.smartpantry;

import java.util.List;

public class Recipe {
    public long id;
    public String name;
    public String method;
    public List<Requirement> requirements;

    public Recipe(long id, String name, String method, List<Requirement> requirements) {
        this.id = id;
        this.name = name;
        this.method = method;
        this.requirements = requirements;
    }

    public static class Requirement {
        public String name;
        public double quantity;
        public String unit;

        public Requirement(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}
