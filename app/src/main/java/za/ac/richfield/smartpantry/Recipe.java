package za.ac.richfield.smartpantry;

import java.util.List;
public class Recipe {
    public long id; public String name, method; public List<Requirement> requirements;
    public Recipe(long id, String name, String method, List<Requirement> requirements) { this.id=id; this.name=name; this.method=method; this.requirements=requirements; }
    public static class Requirement { public String name, unit; public double quantity; public Requirement(String n,double q,String u){name=n;quantity=q;unit=u;} }
}
