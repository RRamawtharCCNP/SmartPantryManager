package za.ac.richfield.smartpantry;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Recipe Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long recipeId = getIntent().getLongExtra("id", -1);
        Recipe recipe = new DatabaseHelper(this).recipe(recipeId);

        if (recipe == null) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipe.name);
        }

        TextView nameView = findViewById(R.id.recipeName);
        if (nameView != null) nameView.setText(recipe.name);

        StringBuilder sb = new StringBuilder();
        for (Recipe.Requirement req : recipe.requirements) {
            String qtyStr = (req.quantity % 1 == 0)
                    ? String.format("%.0f %s", req.quantity, req.unit)
                    : String.format("%.1f %s", req.quantity, req.unit);
            sb.append("• ").append(req.name).append(" — ").append(qtyStr).append("\n");
        }

        TextView ingredientsView = findViewById(R.id.ingredientsText);
        if (ingredientsView != null) ingredientsView.setText(sb.toString().trim());

        TextView methodView = findViewById(R.id.methodText);
        if (methodView != null) methodView.setText(recipe.method);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
