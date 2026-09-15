package za.ac.richfield.smartpantry;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class IngredientFormActivity extends AppCompatActivity {

    private EditText name, quantity, expiry;
    private Spinner unit;
    private DatabaseHelper db;
    private long id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_form);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        name = findViewById(R.id.nameInput);
        quantity = findViewById(R.id.quantityInput);
        expiry = findViewById(R.id.expiryInput);
        unit = findViewById(R.id.unitInput);

        String[] units = {"unit", "g", "kg", "ml", "l"};
        unit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units));

        id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Ingredient");
            }
            TextView title = findViewById(R.id.formTitle);
            if (title != null) title.setText("Edit Pantry Ingredient");

            PantryItem item = db.pantry(id);
            if (item != null) {
                name.setText(item.name);
                quantity.setText(String.valueOf(item.quantity));
                expiry.setText(item.expiry);
                for (int i = 0; i < units.length; i++) {
                    if (units[i].equalsIgnoreCase(item.unit)) {
                        unit.setSelection(i);
                    }
                }
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Ingredient");
            }
        }

        findViewById(R.id.saveButton).setOnClickListener(v -> save());
    }

    private void save() {
        String n = name.getText().toString().trim();
        String q = quantity.getText().toString().trim();
        String e = expiry.getText().toString().trim();

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
                LocalDate.parse(e);
            } catch (DateTimeParseException ex) {
                expiry.setError("Use YYYY-MM-DD format (e.g. 2026-10-15)");
                expiry.requestFocus();
                return;
            }
        }

        String u = unit.getSelectedItem().toString();
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
