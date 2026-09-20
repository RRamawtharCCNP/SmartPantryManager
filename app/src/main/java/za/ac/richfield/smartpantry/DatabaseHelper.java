package za.ac.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    // small tolerance for float comparisons, otherwise 0.30000000004 != 0.3 breaks
    // recipe matching
    private static final double QTY_TOLERANCE = 0.0001;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL CHECK(quantity > 0), " +
                "unit TEXT NOT NULL, " +
                "expiry TEXT)");

        db.execSQL("CREATE TABLE recipes(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "method TEXT NOT NULL)");

        db.execSQL("CREATE TABLE requirements(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");

        // added in v2 - keeping the IF NOT EXISTS just in case onCreate ever runs twice
        db.execSQL("CREATE TABLE IF NOT EXISTS activity_log(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "action_type TEXT NOT NULL, " +
                "item_name TEXT NOT NULL, " +
                "details TEXT NOT NULL, " +
                "timestamp TEXT NOT NULL)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // v1 -> v2 just added the activity log table, nothing else changed
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS activity_log(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "action_type TEXT NOT NULL, " +
                    "item_name TEXT NOT NULL, " +
                    "details TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL)");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // needed for the requirements FK cascade to actually work
    }

    // ---------------------------------------------------------------
    // Activity log
    // ---------------------------------------------------------------

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
        } catch (Exception e) {
            // logging shouldn't crash the app if it fails, but we still want to know about
            // it
            Log.e(TAG, "failed to write activity log", e);
        }
    }

    public List<AuditLogItem> auditLogs() {
        List<AuditLogItem> list = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "activity_log", null, null, null, null, null, "id DESC")) {

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String action = cursor.getString(cursor.getColumnIndexOrThrow("action_type"));
                String item = cursor.getString(cursor.getColumnIndexOrThrow("item_name"));
                String details = cursor.getString(cursor.getColumnIndexOrThrow("details"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

                list.add(new AuditLogItem(id, action, item, details, time));
            }
        } catch (Exception e) {
            Log.e(TAG, "couldn't load audit logs", e);
        }
        return list;
    }

    public void clearAuditLogs() {
        try {
            getWritableDatabase().delete("activity_log", null, null);
        } catch (Exception e) {
            Log.e(TAG, "couldn't clear audit logs", e);
        }
    }

    public int countLogsByAction(String actionType) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM activity_log WHERE action_type=?";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, new String[] { actionType })) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "couldn't count logs for " + actionType, e);
        }
        return count;
    }

    // ---------------------------------------------------------------
    // Pantry
    // ---------------------------------------------------------------

    public long addPantry(String name, double quantity, String unit, String expiry) {
        ContentValues values = buildPantryValues(name, quantity, unit, expiry);
        long id = getWritableDatabase().insertOrThrow("pantry", null, values);

        logAction("ADDED", name.trim(), "Added " + quantity + " " + unit + expiryNote(expiry));
        return id;
    }

    public void updatePantry(long id, String name, double quantity, String unit, String expiry) {
        PantryItem oldItem = pantry(id); // grab this before we overwrite it, so we can log what changed

        getWritableDatabase().update(
                "pantry",
                buildPantryValues(name, quantity, unit, expiry),
                "id=?",
                new String[] { String.valueOf(id) });

        String details;
        if (oldItem != null) {
            details = "Changed from " + oldItem.quantity + " " + oldItem.unit +
                    " to " + quantity + " " + unit + expiryNote(expiry);
        } else {
            // shouldn't really happen, but just in case the id was already gone
            details = "Updated to " + quantity + " " + unit + expiryNote(expiry);
        }

        logAction("UPDATED", name.trim(), details);
    }

    public void deletePantry(long id) {
        PantryItem item = pantry(id);
        getWritableDatabase().delete("pantry", "id=?", new String[] { String.valueOf(id) });

        if (item != null) {
            logAction("DELETED", item.name, "Removed " + item.quantity + " " + item.unit + " from pantry");
        }
    }

    private String expiryNote(String expiry) {
        if (expiry == null || expiry.trim().isEmpty()) {
            return "";
        }
        return ", Exp: " + expiry.trim();
    }

    private ContentValues buildPantryValues(String name, double quantity, String unit, String expiry) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit);
        values.put("expiry", expiry == null ? "" : expiry.trim());
        return values;
    }

    public PantryItem pantry(long id) {
        try (Cursor cursor = getReadableDatabase().query(
                "pantry", null, "id=?", new String[] { String.valueOf(id) }, null, null, null)) {

            if (cursor.moveToFirst()) {
                return pantryFrom(cursor);
            }
        }
        return null;
    }

    public List<PantryItem> pantry() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "pantry", null, null, null, null, null, "name COLLATE NOCASE")) {

            while (cursor.moveToNext()) {
                list.add(pantryFrom(cursor));
            }
        }
        return list;
    }

    private PantryItem pantryFrom(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
        String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
        String expiry = cursor.getString(cursor.getColumnIndexOrThrow("expiry"));
        return new PantryItem(id, name, quantity, unit, expiry);
    }

    // ---------------------------------------------------------------
    // Recipes
    // ---------------------------------------------------------------

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

    // checks if we have enough of every ingredient a recipe needs
    public boolean matches(Recipe recipe, List<PantryItem> pantryItems) {
        for (Recipe.Requirement req : recipe.requirements) {
            double available = 0;
            String target = IngredientNormalizer.name(req.name);
            String family = IngredientNormalizer.family(req.unit);

            for (PantryItem item : pantryItems) {
                boolean sameIngredient = IngredientNormalizer.name(item.name).equals(target);
                boolean sameUnitFamily = IngredientNormalizer.family(item.unit).equals(family);

                if (sameIngredient && sameUnitFamily) {
                    available += IngredientNormalizer.baseQuantity(item.quantity, item.unit);
                }
            }

            double required = IngredientNormalizer.baseQuantity(req.quantity, req.unit);
            if (available + QTY_TOLERANCE < required) {
                return false; // missing this ingredient, no point checking the rest
            }
        }
        return true;
    }

    public Recipe recipe(long id) {
        for (Recipe r : recipes()) {
            if (r.id == id) {
                return r;
            }
        }
        return null;
    }

    public List<Recipe> recipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query("recipes", null, null, null, null, null, "name")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String method = cursor.getString(cursor.getColumnIndexOrThrow("method"));

                list.add(new Recipe(id, name, method, requirementsFor(db, id)));
            }
        }
        return list;
    }

    private List<Recipe.Requirement> requirementsFor(SQLiteDatabase db, long recipeId) {
        List<Recipe.Requirement> requirements = new ArrayList<>();
        try (Cursor cursor = db.query(
                "requirements", null, "recipe_id=?", new String[] { String.valueOf(recipeId) }, null, null, "id")) {

            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
                requirements.add(new Recipe.Requirement(name, qty, unit));
            }
        }
        return requirements;
    }

    // just some starter recipes so the app isn't empty on first launch
    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Tomato Toast", "Toast the bread. Slice tomato, place it on top and season.");
        addIngredient(db, "Tomato Toast", "bread", 2, "unit");
        addIngredient(db, "Tomato Toast", "tomato", 1, "unit");

        addRecipe(db, "Cheese Toastie", "Fill bread with cheese and toast in a dry pan until golden.");
        addIngredient(db, "Cheese Toastie", "bread", 2, "unit");
        addIngredient(db, "Cheese Toastie", "cheese", 50, "g");

        addRecipe(db, "Scrambled Eggs", "Whisk eggs with milk and cook gently, stirring until set.");
        addIngredient(db, "Scrambled Eggs", "egg", 2, "unit");
        addIngredient(db, "Scrambled Eggs", "milk", 30, "ml");

        addRecipe(db, "Banana Oats", "Simmer oats in milk, then top with sliced banana.");
        addIngredient(db, "Banana Oats", "oats", 50, "g");
        addIngredient(db, "Banana Oats", "milk", 200, "ml");
        addIngredient(db, "Banana Oats", "banana", 1, "unit");

        addRecipe(db, "Tomato Omelette", "Whisk eggs, add chopped tomato and cook until set.");
        addIngredient(db, "Tomato Omelette", "egg", 2, "unit");
        addIngredient(db, "Tomato Omelette", "tomato", 1, "unit");

        addRecipe(db, "Garlic Pasta", "Boil pasta. Gently fry garlic in oil and toss together.");
        addIngredient(db, "Garlic Pasta", "pasta", 100, "g");
        addIngredient(db, "Garlic Pasta", "garlic", 2, "unit");
        addIngredient(db, "Garlic Pasta", "oil", 15, "ml");

        addRecipe(db, "Rice and Beans", "Cook rice, warm beans and combine with tomato.");
        addIngredient(db, "Rice and Beans", "rice", 100, "g");
        addIngredient(db, "Rice and Beans", "beans", 100, "g");
        addIngredient(db, "Rice and Beans", "tomato", 1, "unit");

        addRecipe(db, "Potato Hash", "Dice potato and onion, then fry in oil until crisp.");
        addIngredient(db, "Potato Hash", "potato", 2, "unit");
        addIngredient(db, "Potato Hash", "onion", 1, "unit");
        addIngredient(db, "Potato Hash", "oil", 20, "ml");

        addRecipe(db, "Tuna Sandwich", "Mix tuna with mayonnaise and fill the bread.");
        addIngredient(db, "Tuna Sandwich", "bread", 2, "unit");
        addIngredient(db, "Tuna Sandwich", "tuna", 100, "g");
        addIngredient(db, "Tuna Sandwich", "mayonnaise", 15, "g");

        addRecipe(db, "Fruit Bowl", "Slice and combine the apple, banana and orange.");
        addIngredient(db, "Fruit Bowl", "apple", 1, "unit");
        addIngredient(db, "Fruit Bowl", "banana", 1, "unit");
        addIngredient(db, "Fruit Bowl", "orange", 1, "unit");

        addRecipe(db, "Chicken Rice", "Cook chicken thoroughly and serve with cooked rice.");
        addIngredient(db, "Chicken Rice", "chicken", 150, "g");
        addIngredient(db, "Chicken Rice", "rice", 100, "g");

        addRecipe(db, "Yoghurt Banana Cup", "Slice banana into yoghurt and serve chilled.");
        addIngredient(db, "Yoghurt Banana Cup", "yoghurt", 150, "g");
        addIngredient(db, "Yoghurt Banana Cup", "banana", 1, "unit");

        addRecipe(db, "Bean Salad", "Combine beans, chopped tomato and onion.");
        addIngredient(db, "Bean Salad", "beans", 150, "g");
        addIngredient(db, "Bean Salad", "tomato", 1, "unit");
        addIngredient(db, "Bean Salad", "onion", 1, "unit");

        addRecipe(db, "Egg Fried Rice", "Stir-fry cooked rice with egg and oil until hot.");
        addIngredient(db, "Egg Fried Rice", "rice", 150, "g");
        addIngredient(db, "Egg Fried Rice", "egg", 1, "unit");
        addIngredient(db, "Egg Fried Rice", "oil", 10, "ml");

        addRecipe(db, "Cheesy Pasta", "Boil pasta, drain, then stir through grated cheese.");
        addIngredient(db, "Cheesy Pasta", "pasta", 100, "g");
        addIngredient(db, "Cheesy Pasta", "cheese", 50, "g");

        addRecipe(db, "Apple Oats", "Cook oats with milk and fold in diced apple.");
        addIngredient(db, "Apple Oats", "oats", 50, "g");
        addIngredient(db, "Apple Oats", "milk", 200, "ml");
        addIngredient(db, "Apple Oats", "apple", 1, "unit");

        addRecipe(db, "Tomato Rice", "Cook rice and stir through chopped tomato.");
        addIngredient(db, "Tomato Rice", "rice", 100, "g");
        addIngredient(db, "Tomato Rice", "tomato", 2, "unit");

        addRecipe(db, "Boiled Eggs on Toast", "Boil eggs, peel, slice and serve on toast.");
        addIngredient(db, "Boiled Eggs on Toast", "egg", 2, "unit");
        addIngredient(db, "Boiled Eggs on Toast", "bread", 2, "unit");
    }

    private void addRecipe(SQLiteDatabase db, String name, String method) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("method", method);
        db.insertOrThrow("recipes", null, values);
    }

    private void addIngredient(SQLiteDatabase db, String recipeName, String ingredient, double qty, String unit) {
        // seeding runs before we have a Recipe object to work with, so just look the id
        // up by name
        long recipeId = -1;
        try (Cursor cursor = db.query("recipes", new String[] { "id" }, "name=?", new String[] { recipeName }, null,
                null, null)) {
            if (cursor.moveToFirst()) {
                recipeId = cursor.getLong(0);
            }
        }

        if (recipeId == -1) {
            Log.e(TAG, "tried to seed ingredient for unknown recipe: " + recipeName);
            return;
        }

        ContentValues values = new ContentValues();
        values.put("recipe_id", recipeId);
        values.put("name", ingredient);
        values.put("quantity", qty);
        values.put("unit", unit);
        db.insertOrThrow("requirements", null, values);
    }
}