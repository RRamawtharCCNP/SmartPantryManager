package za.ac.richfield.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SwitchMaterial expirySwitch = findViewById(R.id.expirySwitch);
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        if (expirySwitch != null) {
            expirySwitch.setChecked(prefs.getBoolean("expiry_alerts", true));
            expirySwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                    prefs.edit().putBoolean("expiry_alerts", isChecked).apply());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
