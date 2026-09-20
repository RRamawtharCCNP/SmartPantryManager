package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantryActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private View emptyCard;
    private TextView emptyTitle, emptySubtitle;

    private TextView statTotalCount, statExpiringCount, statFreshCount;
    private EditText searchEditText;
    private ImageView clearSearchButton;
    private ChipGroup filterChipGroup;

    private List<PantryItem> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        db = new DatabaseHelper(this);
        adapter = new PantryAdapter(this);

        statTotalCount = findViewById(R.id.statTotalCount);
        statExpiringCount = findViewById(R.id.statExpiringCount);
        statFreshCount = findViewById(R.id.statFreshCount);

        searchEditText = findViewById(R.id.searchEditText);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        filterChipGroup = findViewById(R.id.filterChipGroup);

        emptyCard = findViewById(R.id.emptyCardView);
        emptyTitle = findViewById(R.id.emptyTitle);
        emptySubtitle = findViewById(R.id.emptySubtitle);

        RecyclerView list = findViewById(R.id.pantryList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        ExtendedFloatingActionButton fab = findViewById(R.id.addButton);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, IngredientFormActivity.class)));

        View emptyAddBtn = findViewById(R.id.emptyAddButton);
        if (emptyAddBtn != null) {
            emptyAddBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, IngredientFormActivity.class)));
        }

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

        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) fab.shrink();
                else if (dy < 0) fab.extend();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (clearSearchButton != null) {
                    clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (clearSearchButton != null) {
            clearSearchButton.setOnClickListener(v -> searchEditText.setText(""));
        }

        if (filterChipGroup != null) {
            filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        allItems = db.pantry();
        updateDashboardStats(allItems);
        applyFilters();
    }

    private void updateDashboardStats(List<PantryItem> items) {
        int total = items.size();
        int expiring = 0;
        int fresh = 0;

        for (PantryItem item : items) {
            if (item.expiry != null && !item.expiry.trim().isEmpty()) {
                Date expDate = parseDate(item.expiry.trim());
                if (expDate != null) {
                    long days = getDaysDifference(expDate);
                    if (days <= 3) {
                        expiring++;
                    } else {
                        fresh++;
                    }
                } else {
                    fresh++;
                }
            } else {
                fresh++;
            }
        }

        if (statTotalCount != null) statTotalCount.setText(String.valueOf(total));
        if (statExpiringCount != null) statExpiringCount.setText(String.valueOf(expiring));
        if (statFreshCount != null) statFreshCount.setText(String.valueOf(fresh));
    }

    private void applyFilters() {
        String query = searchEditText != null ? searchEditText.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        int checkedChipId = filterChipGroup != null ? filterChipGroup.getCheckedChipId() : R.id.chipAll;

        List<PantryItem> filtered = new ArrayList<>();

        for (PantryItem item : allItems) {
            if (!query.isEmpty() && !item.name.toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }

            if (checkedChipId == R.id.chipExpiring) {
                if (item.expiry == null || item.expiry.trim().isEmpty()) continue;
                Date expDate = parseDate(item.expiry.trim());
                if (expDate == null || getDaysDifference(expDate) > 3) continue;
            } else if (checkedChipId == R.id.chipLowStock) {
                boolean isLow = false;
                String unit = item.unit.toLowerCase(Locale.ROOT);
                if (unit.contains("g") || unit.contains("ml")) {
                    isLow = item.quantity <= 100;
                } else {
                    isLow = item.quantity <= 1;
                }
                if (!isLow) continue;
            }

            filtered.add(item);
        }

        adapter.setItems(filtered);

        if (emptyCard != null) {
            if (filtered.isEmpty()) {
                emptyCard.setVisibility(View.VISIBLE);
                if (allItems.isEmpty()) {
                    if (emptyTitle != null) emptyTitle.setText("Your pantry is empty");
                    if (emptySubtitle != null) emptySubtitle.setText("Start adding items to track your stock and get recipe suggestions.");
                } else {
                    if (emptyTitle != null) emptyTitle.setText("No matching items found");
                    if (emptySubtitle != null) emptySubtitle.setText("Try adjusting your search query or selected filter chips.");
                }
            } else {
                emptyCard.setVisibility(View.GONE);
            }
        }
    }

    private Date parseDate(String dateStr) {
        String[] formats = {"yyyy-MM-dd", "yyyy/MM/dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy.MM.dd"};
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private long getDaysDifference(Date expiryDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar exp = Calendar.getInstance();
        exp.setTime(expiryDate);
        exp.set(Calendar.HOUR_OF_DAY, 0);
        exp.set(Calendar.MINUTE, 0);
        exp.set(Calendar.SECOND, 0);
        exp.set(Calendar.MILLISECOND, 0);

        long diffMs = exp.getTimeInMillis() - today.getTimeInMillis();
        return diffMs / (24 * 60 * 60 * 1000);
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
