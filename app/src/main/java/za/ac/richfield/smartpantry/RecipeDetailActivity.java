package za.ac.richfield.smartpantry;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

// Simple read-only screen - just pulls one recipe by id and lays out its
// name, ingredient list, and method text.
public class RecipeDetailActivity extends AppCompatActivity {

    private static final String TAG = "RecipeDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        setupToolbar();

        long recipeId = getIntent().getLongExtra("id", -1);
        Recipe recipe = new DatabaseHelper(this).recipe(recipeId);

        if (recipe == null) {
            // shouldn't normally happen since we only ever get here from a valid recipe
            // list,
            // but bail out gracefully rather than crashing on a null recipe
            Log.w(TAG, "no recipe found for id " + recipeId);
            finish();
            return;
        }

        bindRecipe(recipe);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Recipe Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void bindRecipe(Recipe recipe) {
        // once we actually have the recipe, swap the generic toolbar title for its name
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipe.name);
        }

        TextView nameView = findViewById(R.id.recipeName);
        if (nameView != null) {
            nameView.setText(recipe.name);
        }

        TextView ingredientsView = findViewById(R.id.ingredientsText);
        if (ingredientsView != null) {
            ingredientsView.setText(buildIngredientsList(recipe));
        }

        TextView methodView = findViewById(R.id.methodText);
        if (methodView != null) {
            methodView.setText(recipe.method);
        }
    }

    // builds something like "• egg — 2 unit\n• milk — 30 ml" for the ingredients
    // box
    private String buildIngredientsList(Recipe recipe) {
        StringBuilder sb = new StringBuilder();

        for (Recipe.Requirement req : recipe.requirements) {
            sb.append("• ")
                    .append(req.name)
                    .append(" — ")
                    .append(formatQuantity(req.quantity, req.unit))
                    .append("\n");
        }

        return sb.toString().trim();
    }

    // same "no pointless decimal" formatting used on the pantry list, just
    // duplicated here
    // for now - could pull this into a shared util class if it ends up needed
    // elsewhere
    private String formatQuantity(double quantity, String unit) {
        if (quantity % 1 == 0) {
            return String.format("%.0f %s", quantity, unit);
        }
        return String.format("%.1f %s", quantity, unit);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}