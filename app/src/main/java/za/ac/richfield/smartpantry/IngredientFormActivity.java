package za.ac.richfield.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Handles both "add new ingredient" and "edit existing ingredient" -
// we just check if an id was passed in via the intent to know which mode we're in.
public class IngredientFormActivity extends AppCompatActivity {

    private static final String TAG = "IngredientFormActivity";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String[] UNITS = { "unit", "g", "kg", "ml", "l" };

    private EditText name;
    private EditText quantity;
    private EditText expiry;
    private AutoCompleteTextView unit;

    private DatabaseHelper db;
    private long id = -1; // -1 means we're adding a new item, not editing one

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_form);

        setupToolbar();
        bindViews();
        setupUnitDropdown();

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            loadExistingItem();
        }

        // clicking either the field or its wrapping layout should open the date picker,
        // depending on how the layout XML ends up structured
        expiry.setOnClickListener(v -> showDatePicker());
        if (findViewById(R.id.expiryInputLayout) != null) {
            findViewById(R.id.expiryInputLayout).setOnClickListener(v -> showDatePicker());
        }

        findViewById(R.id.saveButton).setOnClickListener(v -> save());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void bindViews() {
        db = new DatabaseHelper(this);
        name = findViewById(R.id.nameInput);
        quantity = findViewById(R.id.quantityInput);
        expiry = findViewById(R.id.expiryInput);
        unit = findViewById(R.id.unitInput);
    }

    private void setupUnitDropdown() {
        unit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, UNITS));
        unit.setText(UNITS[0], false); // default to "unit" so the field isn't blank
    }

    // pulls in the existing item's data when we're editing rather than adding
    private void loadExistingItem() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Ingredient");
        }

        TextView title = findViewById(R.id.formTitle);
        if (title != null) {
            title.setText("Edit Pantry Ingredient");
        }

        PantryItem item = db.pantry(id);
        if (item == null) {
            // this really shouldn't happen unless the item got deleted somewhere else,
            // but better to log it than silently show a blank form
            Log.w(TAG, "no pantry item found for id " + id);
            return;
        }

        name.setText(item.name);
        quantity.setText(String.valueOf(item.quantity));
        expiry.setText(item.expiry);
        unit.setText(item.unit, false);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // if there's already a date typed in, open the picker on that date instead of
        // today
        String currentExpiry = expiry.getText().toString().trim();
        if (!currentExpiry.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
                c.setTime(sdf.parse(currentExpiry));
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            } catch (ParseException e) {
                // bad/partial date typed in manually - just fall back to today, not worth
                // bothering the user here
                Log.d(TAG, "couldn't parse existing expiry date, defaulting to today");
            }
        }

        new DatePickerDialog(
                this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", yearSelected, monthOfYear + 1,
                            dayOfMonth);
                    expiry.setText(selectedDate);
                },
                year, month, day).show();
    }

    private void save() {
        String n = name.getText().toString().trim();
        String q = quantity.getText().toString().trim();
        String e = expiry.getText().toString().trim();
        String u = unit.getText().toString().trim();

        if (!isNameValid(n))
            return;

        Double amount = parseQuantity(q);
        if (amount == null)
            return;

        if (!isExpiryValid(e))
            return;

        if (id == -1) {
            db.addPantry(n, amount, u, e);
        } else {
            db.updatePantry(id, n, amount, u, e);
        }
        finish();
    }

    private boolean isNameValid(String n) {
        if (n.length() < 2) {
            name.setError("Enter at least 2 letters for the ingredient name");
            name.requestFocus();
            return false;
        }
        return true;
    }

    // returns null if invalid, sets the field error itself so save() doesn't have
    // to
    private Double parseQuantity(String q) {
        try {
            double amount = Double.parseDouble(q);
            if (amount <= 0) {
                throw new NumberFormatException("quantity must be positive");
            }
            return amount;
        } catch (NumberFormatException ex) {
            quantity.setError("Enter a quantity greater than zero");
            quantity.requestFocus();
            return null;
        }
    }

    private boolean isExpiryValid(String e) {
        if (e.isEmpty()) {
            return true; // expiry is optional
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
            sdf.setLenient(false);
            sdf.parse(e);
            return true;
        } catch (ParseException ex) {
            expiry.setError("Use YYYY-MM-DD format (e.g. 2026-10-15)");
            expiry.requestFocus();
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}