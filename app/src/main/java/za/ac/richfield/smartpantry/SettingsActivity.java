package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    // just using a plain SharedPreferences file for now - there's only one setting
    // right now so it's not worth pulling in a whole preferences screen for it
    private static final String PREFS_NAME = "settings";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();
        setupExpirySwitch();
        setupAuditLogCard();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupExpirySwitch() {
        SwitchMaterial expirySwitch = findViewById(R.id.expirySwitch);
        if (expirySwitch == null) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // default to true - most people probably want expiry alerts on out of the box
        expirySwitch.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));

        expirySwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());
    }

    private void setupAuditLogCard() {
        View auditLogCard = findViewById(R.id.auditLogCard);
        if (auditLogCard == null) {
            return;
        }

        auditLogCard.setOnClickListener(v -> startActivity(new Intent(this, ActivityReportActivity.class)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}