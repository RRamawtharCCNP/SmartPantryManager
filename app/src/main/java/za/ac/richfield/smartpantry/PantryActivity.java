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

// Main screen of the app - shows the pantry list with search/filter chips and
// some quick stats at the top (total items, expiring soon, fresh).
public class PantryActivity extends AppCompatActivity implements PantryAdapter.Listener {

    // if something expires within this many days it counts as "expiring soon"
    private static final int EXPIRING_SOON_DAYS = 3;

    // rough thresholds for what counts as "low stock" - not perfect but good enough
    // for now
    private static final double LOW_STOCK_WEIGHT_VOLUME = 100; // grams/ml
    private static final double LOW_STOCK_UNIT_COUNT = 1; // whole units, e.g. "1 onion left"

    // a few common date formats people might type in manually
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd", "yyyy/MM/dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy.MM.dd"
    };

    private DatabaseHelper db;
    private PantryAdapter adapter;

    private View emptyCard;
    private TextView emptyTitle;
    private TextView emptySubtitle;

    private TextView statTotalCount;
    private TextView statExpiringCount;
    private TextView statFreshCount;

    private EditText searchEditText;
    private ImageView clearSearchButton;
    private ChipGroup filterChipGroup;

    // keeps the full unfiltered list in memory so search/filter don't need to hit
    // the DB every time
    private List<PantryItem> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        db = new DatabaseHelper(this);
        adapter = new PantryAdapter(this);

        bindViews();
        setupPantryList();
        setupAddButtons();
        setupBottomNav();
        setupSearchAndFilters();
    }

    private void bindViews() {
        statTotalCount = findViewById(R.id.statTotalCount);
        statExpiringCount = findViewById(R.id.statExpiringCount);
        statFreshCount = findViewById(R.id.statFreshCount);

        searchEditText = findViewById(R.id.searchEditText);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        filterChipGroup = findViewById(R.id.filterChipGroup);

        emptyCard = findViewById(R.id.emptyCardView);
        emptyTitle = findViewById(R.id.emptyTitle);
        emptySubtitle = findViewById(R.id.emptySubtitle);
    }

    private void setupPantryList() {
        RecyclerView list = findViewById(R.id.pantryList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        ExtendedFloatingActionButton fab = findViewById(R.id.addButton);
        fab.setOnClickListener(v -> startActivity(new Intent(this, IngredientFormActivity.class)));

        // shrink the FAB down to just an icon while scrolling so it doesn't block
        // content,
        // then extend it back out once the user scrolls back up
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fab.shrink();
                } else if (dy < 0) {
                    fab.extend();
                }
            }
        });
    }

    private void setupAddButtons() {
        View emptyAddBtn = findViewById(R.id.emptyAddButton);
        if (emptyAddBtn != null) {
            emptyAddBtn.setOnClickListener(v -> startActivity(new Intent(this, IngredientFormActivity.class)));
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_recipes) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return itemId == R.id.nav_pantry;
        });
    }

    private void setupSearchAndFilters() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // don't need this one
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (clearSearchButton != null) {
                    clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // don't need this one either
            }
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
        // reload every time we come back to this screen in case something was
        // added/edited/deleted from another activity
        refresh();
    }

    private void refresh() {
        allItems = db.pantry();
        updateDashboardStats(allItems);
        applyFilters();
    }

    private void updateDashboardStats(List<PantryItem> items) {
        int expiring = 0;
        int fresh = 0;

        for (PantryItem item : items) {
            if (isExpiringSoon(item)) {
                expiring++;
            } else {
                fresh++;
            }
        }

        if (statTotalCount != null)
            statTotalCount.setText(String.valueOf(items.size()));
        if (statExpiringCount != null)
            statExpiringCount.setText(String.valueOf(expiring));
        if (statFreshCount != null)
            statFreshCount.setText(String.valueOf(fresh));
    }

    // an item with no expiry date, or one we can't parse, is just treated as
    // "fresh" -
    // better than crashing or scaring the user with a bad date
    private boolean isExpiringSoon(PantryItem item) {
        if (item.expiry == null || item.expiry.trim().isEmpty()) {
            return false;
        }

        Date expDate = parseDate(item.expiry.trim());
        if (expDate == null) {
            return false;
        }

        return getDaysDifference(expDate) <= EXPIRING_SOON_DAYS;
    }

    private void applyFilters() {
        String query = searchEditText != null
                ? searchEditText.getText().toString().trim().toLowerCase(Locale.ROOT)
                : "";
        int checkedChipId = filterChipGroup != null ? filterChipGroup.getCheckedChipId() : R.id.chipAll;

        List<PantryItem> filtered = new ArrayList<>();

        for (PantryItem item : allItems) {
            if (!matchesSearch(item, query)) {
                continue;
            }
            if (!matchesChip(item, checkedChipId)) {
                continue;
            }
            filtered.add(item);
        }

        adapter.setItems(filtered);
        updateEmptyState(filtered);
    }

    private boolean matchesSearch(PantryItem item, String query) {
        return query.isEmpty() || item.name.toLowerCase(Locale.ROOT).contains(query);
    }

    private boolean matchesChip(PantryItem item, int checkedChipId) {
        if (checkedChipId == R.id.chipExpiring) {
            return isExpiringSoon(item);
        }

        if (checkedChipId == R.id.chipLowStock) {
            return isLowStock(item);
        }

        // chipAll (or nothing selected) - everything passes
        return true;
    }

    // pretty rough heuristic based on unit type - could definitely be smarter
    // (e.g. per-ingredient thresholds) but this covers the common cases fine for
    // now
    private boolean isLowStock(PantryItem item) {
        String unit = item.unit.toLowerCase(Locale.ROOT);
        if (unit.contains("g") || unit.contains("ml")) {
            return item.quantity <= LOW_STOCK_WEIGHT_VOLUME;
        }
        return item.quantity <= LOW_STOCK_UNIT_COUNT;
    }

    private void updateEmptyState(List<PantryItem> filtered) {
        if (emptyCard == null) {
            return;
        }

        if (!filtered.isEmpty()) {
            emptyCard.setVisibility(View.GONE);
            return;
        }

        emptyCard.setVisibility(View.VISIBLE);

        // different message depending on whether the pantry is genuinely empty
        // or just the current search/filter combo has no matches
        if (allItems.isEmpty()) {
            setEmptyText("Your pantry is empty",
                    "Start adding items to track your stock and get recipe suggestions.");
        } else {
            setEmptyText("No matching items found",
                    "Try adjusting your search query or selected filter chips.");
        }
    }

    private void setEmptyText(String title, String subtitle) {
        if (emptyTitle != null)
            emptyTitle.setText(title);
        if (emptySubtitle != null)
            emptySubtitle.setText(subtitle);
    }

    // tries each known format until one works - not the most efficient way to do
    // this
    // but the log table's small so it doesn't matter in practice
    private Date parseDate(String dateStr) {
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (Exception e) {
                // just means this format didn't match, try the next one
            }
        }
        return null;
    }

    private long getDaysDifference(Date expiryDate) {
        Calendar today = Calendar.getInstance();
        zeroOutTime(today);

        Calendar exp = Calendar.getInstance();
        exp.setTime(expiryDate);
        zeroOutTime(exp);

        long diffMs = exp.getTimeInMillis() - today.getTimeInMillis();
        return diffMs / (24 * 60 * 60 * 1000);
    }

    // strips out the time portion so we're comparing whole days, not exact
    // timestamps
    private void zeroOutTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
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