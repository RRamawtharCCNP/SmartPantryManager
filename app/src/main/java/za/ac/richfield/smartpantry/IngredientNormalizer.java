package za.ac.richfield.smartpantry;

import java.util.Locale;

public final class IngredientNormalizer {

    private IngredientNormalizer() {}

    public static String name(String value) {
        if (value == null) return "";
        String normalized = value.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z ]", "")
                .replaceAll("\\s+", " ");

        if (normalized.endsWith("ies") && normalized.length() > 3) {
            normalized = normalized.substring(0, normalized.length() - 3) + "y";
        } else if (normalized.endsWith("oes")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("es") && !normalized.endsWith("cheese")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("s") && !normalized.endsWith("ss")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    public static double baseQuantity(double quantity, String unit) {
        if (unit == null) return quantity;
        switch (unit.toLowerCase(Locale.ROOT)) {
            case "kg":
            case "l":
                return quantity * 1000;
            default:
                return quantity;
        }
    }

    public static String family(String unit) {
        if (unit == null) return "count";
        switch (unit.toLowerCase(Locale.ROOT)) {
            case "g":
            case "kg":
                return "weight";
            case "ml":
            case "l":
                return "volume";
            default:
                return "count";
        }
    }
}
