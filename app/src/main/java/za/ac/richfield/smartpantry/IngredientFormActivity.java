package za.ac.richfield.smartpantry;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class IngredientFormActivity extends AppCompatActivity {

    private EditText name, quantity, expiry;
    private AutoCompleteTextView unit;
    private DatabaseHelper db;
    private long id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_form);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        name = findViewById(R.id.nameInput);
        quantity = findViewById(R.id.quantityInput);
        expiry = findViewById(R.id.expiryInput);
        unit = findViewById(R.id.unitInput);

        String[] units = {"unit", "g", "kg", "ml", "l"};
        unit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, units));
        unit.setText(units[0], false);

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            getSupportActionBar().setTitle("Edit Ingredient");
            TextView title = findViewById(R.id.formTitle);
            if (title != null) title.setText("Edit Pantry Ingredient");

            PantryItem item = db.pantry(id);
            if (item != null) {
                name.setText(item.name);
                quantity.setText(String.valueOf(item.quantity));
                expiry.setText(item.expiry);
                unit.setText(item.unit, false);
            }
        }

        findViewById(R.id.saveButton).setOnClickListener(v -> save());
    }

    private void save() {
        String n = name.getText().toString().trim();
        String q = quantity.getText().toString().trim();
        String e = expiry.getText().toString().trim();
        String u = unit.getText().toString().trim();

        if (n.length() < 2) {
            name.setError("Enter at least 2 letters for the ingredient name");
            name.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(q);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            quantity.setError("Enter a quantity greater than zero");
            quantity.requestFocus();
            return;
        }

        if (!e.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                sdf.setLenient(false);
                sdf.parse(e);
            } catch (ParseException ex) {
                expiry.setError("Use YYYY-MM-DD format (e.g. 2026-10-15)");
                expiry.requestFocus();
                return;
            }
        }

        if (id == -1) {
            db.addPantry(n, amount, u, e);
        } else {
            db.updatePantry(id, n, amount, u, e);
        }
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
