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

public class SuggestedRecipesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        List<Recipe> matches = new DatabaseHelper(this).suggested();

        TextView headerText = findViewById(R.id.suggestedHeader);
        if (headerText != null) {
            headerText.setText("Available Recipes (" + matches.size() + ")");
        }

        View noMatchesCard = findViewById(R.id.noMatchesCard);
        if (noMatchesCard != null) {
            noMatchesCard.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
        }

        RecyclerView list = findViewById(R.id.recipeList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new RecipeAdapter(matches, recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("id", recipe.id);
            startActivity(intent);
        }));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
