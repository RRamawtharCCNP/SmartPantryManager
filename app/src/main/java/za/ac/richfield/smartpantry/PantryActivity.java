package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Smart Pantry Manager");
            getSupportActionBar().setElevation(4f);
        }

        db = new DatabaseHelper(this);
        emptyCard = findViewById(R.id.emptyCardView);
        adapter = new PantryAdapter(this);

        RecyclerView list = findViewById(R.id.pantryList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.addButton).setOnClickListener(v ->
                startActivity(new Intent(this, IngredientFormActivity.class)));

        View emptyAddBtn = findViewById(R.id.emptyAddButton);
        if (emptyAddBtn != null) {
            emptyAddBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, IngredientFormActivity.class)));
        }

        findViewById(R.id.suggestButton).setOnClickListener(v ->
                startActivity(new Intent(this, SuggestedRecipesActivity.class)));

        findViewById(R.id.settingsButton).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
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
