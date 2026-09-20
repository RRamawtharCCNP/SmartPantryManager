package za.ac.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, quantity REAL NOT NULL CHECK(quantity > 0), unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, method TEXT NOT NULL)");
        db.execSQL("CREATE TABLE requirements(id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS activity_log(id INTEGER PRIMARY KEY AUTOINCREMENT, action_type TEXT NOT NULL, item_name TEXT NOT NULL, details TEXT NOT NULL, timestamp TEXT NOT NULL)");
        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS activity_log(id INTEGER PRIMARY KEY AUTOINCREMENT, action_type TEXT NOT NULL, item_name TEXT NOT NULL, details TEXT NOT NULL, timestamp TEXT NOT NULL)");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public void logAction(String actionType, String itemName, String details) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        ContentValues values = new ContentValues();
        values.put("action_type", actionType);
        values.put("item_name", itemName);
        values.put("details", details);
        values.put("timestamp", timestamp);

        try {
            getWritableDatabase().insert("activity_log", null, values);
        } catch (Exception ignored) {}
    }

    public long addPantry(String name, double quantity, String unit, String expiry) {
        ContentValues values = buildPantryValues(name, quantity, unit, expiry);
        long id = getWritableDatabase().insertOrThrow("pantry", null, values);

        String expiryStr = (expiry != null && !expiry.trim().isEmpty()) ? ", Exp: " + expiry.trim() : "";
        logAction("ADDED", name.trim(), "Added " + quantity + " " + unit + expiryStr);
        return id;
    }

    public void updatePantry(long id, String name, double quantity, String unit, String expiry) {
        PantryItem oldItem = pantry(id);
        getWritableDatabase().update("pantry", buildPantryValues(name, quantity, unit, expiry), "id=?", new String[]{String.valueOf(id)});

        String expiryStr = (expiry != null && !expiry.trim().isEmpty()) ? ", Exp: " + expiry.trim() : "";
        String details = "Updated to " + quantity + " " + unit + expiryStr;
        if (oldItem != null) {
            details = "Changed from " + oldItem.quantity + " " + oldItem.unit + " to " + quantity + " " + unit + expiryStr;
        }
        logAction("UPDATED", name.trim(), details);
    }

    public void deletePantry(long id) {
        PantryItem item = pantry(id);
        getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
        if (item != null) {
            logAction("DELETED", item.name, "Removed " + item.quantity + " " + item.unit + " from pantry");
        }
    }

    private ContentValues buildPantryValues(String name, double quantity, String unit, String expiry) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit);
        values.put("expiry", expiry.trim());
        return values;
    }

    public PantryItem pantry(long id) {
        try (Cursor cursor = getReadableDatabase().query("pantry", null, "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return pantryFrom(cursor);
            }
        }
        return null;
    }

    public List<PantryItem> pantry() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("pantry", null, null, null, null, null, "name COLLATE NOCASE")) {
            while (cursor.moveToNext()) {
                list.add(pantryFrom(cursor));
            }
        }
        return list;
    }

    private PantryItem pantryFrom(Cursor cursor) {
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                cursor.getString(cursor.getColumnIndexOrThrow("expiry"))
        );
    }

    public List<AuditLogItem> auditLogs() {
        List<AuditLogItem> list = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("activity_log", null, null, null, null, null, "id DESC")) {
            while (cursor.moveToNext()) {
                list.add(new AuditLogItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("action_type")),
                        cursor.getString(cursor.getColumnIndexOrThrow("item_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("details")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }

    public void clearAuditLogs() {
        try {
            getWritableDatabase().delete("activity_log", null, null);
        } catch (Exception ignored) {}
    }

    public int countLogsByAction(String actionType) {
        int count = 0;
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM activity_log WHERE action_type=?", new String[]{actionType})) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception ignored) {}
        return count;
    }

    public List<Recipe> suggested() {
        List<PantryItem> pantryItems = pantry();
        List<Recipe> matchingRecipes = new ArrayList<>();
        for (Recipe recipe : recipes()) {
            if (matches(recipe, pantryItems)) {
                matchingRecipes.add(recipe);
            }
        }
        return matchingRecipes;
    }

    public boolean matches(Recipe recipe, List<PantryItem> pantryItems) {
        for (Recipe.Requirement req : recipe.requirements) {
            double available = 0;
            String target = IngredientNormalizer.name(req.name);
            String family = IngredientNormalizer.family(req.unit);

            for (PantryItem item : pantryItems) {
                if (IngredientNormalizer.name(item.name).equals(target) && IngredientNormalizer.family(item.unit).equals(family)) {
                    available += IngredientNormalizer.baseQuantity(item.quantity, item.unit);
                }
            }
            if (available + 0.0001 < IngredientNormalizer.baseQuantity(req.quantity, req.unit)) {
                return false;
            }
        }
        return true;
    }

    public Recipe recipe(long id) {
        for (Recipe r : recipes()) {
            if (r.id == id) return r;
        }
        return null;
    }

    public List<Recipe> recipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query("recipes", null, null, null, null, null, "name")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                List<Recipe.Requirement> requirements = new ArrayList<>();
                try (Cursor reqCursor = db.query("requirements", null, "recipe_id=?", new String[]{String.valueOf(id)}, null, null, "id")) {
                    while (reqCursor.moveToNext()) {
                        requirements.add(new Recipe.Requirement(
                                reqCursor.getString(reqCursor.getColumnIndexOrThrow("name")),
                                reqCursor.getDouble(reqCursor.getColumnIndexOrThrow("quantity")),
                                reqCursor.getString(reqCursor.getColumnIndexOrThrow("unit"))
                        ));
                    }
                }
                list.add(new Recipe(
                        id,
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("method")),
                        requirements
                ));
            }
        }
        return list;
    }

    private void seed(SQLiteDatabase db) {
        addRecipe(db, "Tomato Toast", "Toast the bread. Slice tomato, place it on top and season.", "bread|2|unit;tomato|1|unit");
        addRecipe(db, "Cheese Toastie", "Fill bread with cheese and toast in a dry pan until golden.", "bread|2|unit;cheese|50|g");
        addRecipe(db, "Scrambled Eggs", "Whisk eggs with milk and cook gently, stirring until set.", "egg|2|unit;milk|30|ml");
        addRecipe(db, "Banana Oats", "Simmer oats in milk, then top with sliced banana.", "oats|50|g;milk|200|ml;banana|1|unit");
        addRecipe(db, "Tomato Omelette", "Whisk eggs, add chopped tomato and cook until set.", "egg|2|unit;tomato|1|unit");
        addRecipe(db, "Garlic Pasta", "Boil pasta. Gently fry garlic in oil and toss together.", "pasta|100|g;garlic|2|unit;oil|15|ml");
        addRecipe(db, "Rice and Beans", "Cook rice, warm beans and combine with tomato.", "rice|100|g;beans|100|g;tomato|1|unit");
        addRecipe(db, "Potato Hash", "Dice potato and onion, then fry in oil until crisp.", "potato|2|unit;onion|1|unit;oil|20|ml");
        addRecipe(db, "Tuna Sandwich", "Mix tuna with mayonnaise and fill the bread.", "bread|2|unit;tuna|100|g;mayonnaise|15|g");
        addRecipe(db, "Fruit Bowl", "Slice and combine the apple, banana and orange.", "apple|1|unit;banana|1|unit;orange|1|unit");
        addRecipe(db, "Chicken Rice", "Cook chicken thoroughly and serve with cooked rice.", "chicken|150|g;rice|100|g");
        addRecipe(db, "Yoghurt Banana Cup", "Slice banana into yoghurt and serve chilled.", "yoghurt|150|g;banana|1|unit");
        addRecipe(db, "Bean Salad", "Combine beans, chopped tomato and onion.", "beans|150|g;tomato|1|unit;onion|1|unit");
        addRecipe(db, "Egg Fried Rice", "Stir-fry cooked rice with egg and oil until hot.", "rice|150|g;egg|1|unit;oil|10|ml");
        addRecipe(db, "Cheesy Pasta", "Boil pasta, drain, then stir through grated cheese.", "pasta|100|g;cheese|50|g");
        addRecipe(db, "Apple Oats", "Cook oats with milk and fold in diced apple.", "oats|50|g;milk|200|ml;apple|1|unit");
        addRecipe(db, "Tomato Rice", "Cook rice and stir through chopped tomato.", "rice|100|g;tomato|2|unit");
        addRecipe(db, "Boiled Eggs on Toast", "Boil eggs, peel, slice and serve on toast.", "egg|2|unit;bread|2|unit");
    }

    private void addRecipe(SQLiteDatabase db, String name, String method, String spec) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("method", method);
        long recipeId = db.insertOrThrow("recipes", null, values);

        for (String part : spec.split(";")) {
            String[] parts = part.split("\\|");
            ContentValues reqValues = new ContentValues();
            reqValues.put("recipe_id", recipeId);
            reqValues.put("name", parts[0]);
            reqValues.put("quantity", Double.parseDouble(parts[1]));
            reqValues.put("unit", parts[2]);
            db.insertOrThrow("requirements", null, reqValues);
        }
    }
}
