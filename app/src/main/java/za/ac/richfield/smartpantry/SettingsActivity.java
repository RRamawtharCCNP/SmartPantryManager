package za.ac.richfield.smartpantry;

import android.os.Bundle;import android.widget.Switch;import androidx.appcompat.app.AppCompatActivity;
public class SettingsActivity extends AppCompatActivity{protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_settings);Switch s=findViewById(R.id.expirySwitch);android.content.SharedPreferences p=getSharedPreferences("settings",MODE_PRIVATE);s.setChecked(p.getBoolean("expiry_alerts",true));s.setOnCheckedChangeListener((v,on)->p.edit().putBoolean("expiry_alerts",on).apply());}}
