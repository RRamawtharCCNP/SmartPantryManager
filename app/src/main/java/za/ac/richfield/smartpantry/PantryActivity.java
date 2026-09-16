package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PantryActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private View emptyCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        db = new DatabaseHelper(this);
        emptyCard = findViewById(R.id.emptyCardView);
        adapter = new PantryAdapter(this);

        RecyclerView list = findViewById(R.id.pantryList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        // FAB Setup
        ExtendedFloatingActionButton fab = findViewById(R.id.addButton);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, IngredientFormActivity.class)));

        // Empty State Setup
        View emptyAddBtn = findViewById(R.id.emptyAddButton);
        if (emptyAddBtn != null) {
            emptyAddBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, IngredientFormActivity.class)));
        }

        // Bottom Navigation Setup
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_recipes) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return id == R.id.nav_pantry;
        });

        // Hide FAB on scroll
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) fab.shrink();
                else if (dy < 0) fab.extend();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<PantryItem> items = db.pantry();
        adapter.setItems(items);
        if (emptyCard != null) {
            emptyCard.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void edit(PantryItem item) {
        Intent intent = new Intent(this, IngredientFormActivity.class);
        intent.putExtra("id", item.id);
        startActivity(intent);
    }

    @Override
    public void delete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete " + item.name + "?")
                .setMessage("This item will be permanently removed from your pantry.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deletePantry(item.id);
                    refresh();
                })
                .show();
    }
}
