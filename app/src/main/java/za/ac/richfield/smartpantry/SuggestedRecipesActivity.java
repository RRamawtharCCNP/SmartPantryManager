package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Shows recipes the user can actually make right now, based on what's currently
// in the pantry - the actual matching logic lives in DatabaseHelper.suggested().
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private List<Recipe> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        db = new DatabaseHelper(this);

        setupToolbar();
        loadSuggestions();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadSuggestions() {
        matches = db.suggested();

        updateHeader();
        updateEmptyState();
        setupRecipeList();
    }

    private void updateHeader() {
        TextView headerText = findViewById(R.id.suggestedHeader);
        if (headerText != null) {
            headerText.setText("Available Recipes (" + matches.size() + ")");
        }
    }

    private void updateEmptyState() {
        View noMatchesCard = findViewById(R.id.noMatchesCard);
        if (noMatchesCard != null) {
            // if nothing matches, this card probably tells the user to add more ingredients
            noMatchesCard.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void setupRecipeList() {
        RecyclerView list = findViewById(R.id.recipeList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new RecipeAdapter(matches, this::openRecipeDetail));
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("id", recipe.id);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}