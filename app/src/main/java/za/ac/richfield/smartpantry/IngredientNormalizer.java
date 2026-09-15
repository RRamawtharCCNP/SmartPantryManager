package za.ac.richfield.smartpantry;

import java.util.Locale;
public final class IngredientNormalizer {
    private IngredientNormalizer() {}
    public static String name(String value) {
        String n=value.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z ]", "").replaceAll("\\s+", " ");
        if (n.endsWith("ies") && n.length()>3) n=n.substring(0,n.length()-3)+"y";
        else if (n.endsWith("oes")) n=n.substring(0,n.length()-2);
        else if (n.endsWith("es") && !n.endsWith("cheese")) n=n.substring(0,n.length()-2);
        else if (n.endsWith("s") && !n.endsWith("ss")) n=n.substring(0,n.length()-1);
        return n;
    }
    public static double baseQuantity(double quantity, String unit) {
        switch(unit.toLowerCase(Locale.ROOT)) { case "kg": case "l": return quantity*1000; default: return quantity; }
    }
    public static String family(String unit) {
        switch(unit.toLowerCase(Locale.ROOT)) { case "g": case "kg": return "weight"; case "ml": case "l": return "volume"; default: return "count"; }
    }
}
