package za.ac.richfield.smartpantry;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ActivityReportActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private AuditLogAdapter adapter;
    private View emptyLogsView;
    private TextView totalLogsCount, addedLogsCount, updatedLogsCount, deletedLogsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        emptyLogsView = findViewById(R.id.emptyLogsView);
        totalLogsCount = findViewById(R.id.totalLogsCount);
        addedLogsCount = findViewById(R.id.addedLogsCount);
        updatedLogsCount = findViewById(R.id.updatedLogsCount);
        deletedLogsCount = findViewById(R.id.deletedLogsCount);

        RecyclerView recyclerView = findViewById(R.id.auditRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AuditLogAdapter();
        recyclerView.setAdapter(adapter);

        findViewById(R.id.clearLogButton).setOnClickListener(v -> confirmClearLogs());

        loadData();
    }

    private void loadData() {
        List<AuditLogItem> logs = db.auditLogs();
        adapter.setItems(logs);

        int total = logs.size();
        int added = db.countLogsByAction("ADDED");
        int updated = db.countLogsByAction("UPDATED");
        int deleted = db.countLogsByAction("DELETED");

        totalLogsCount.setText(String.valueOf(total));
        addedLogsCount.setText(String.valueOf(added));
        updatedLogsCount.setText(String.valueOf(updated));
        deletedLogsCount.setText(String.valueOf(deleted));

        if (emptyLogsView != null) {
            emptyLogsView.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void confirmClearLogs() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Audit Log?")
                .setMessage("This will permanently delete all activity log history.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    db.clearAuditLogs();
                    loadData();
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
