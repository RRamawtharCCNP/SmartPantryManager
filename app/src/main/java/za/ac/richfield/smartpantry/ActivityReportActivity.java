package za.ac.richfield.smartpantry;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Shows a history of everything that's happened to the pantry (added/updated/deleted items)
// plus some quick counts at the top so the user can see activity at a glance.
public class ActivityReportActivity extends AppCompatActivity {

    private static final String TAG = "ActivityReportActivity";

    private DatabaseHelper db;
    private AuditLogAdapter adapter;

    private View emptyLogsView;
    private TextView totalLogsCount;
    private TextView addedLogsCount;
    private TextView updatedLogsCount;
    private TextView deletedLogsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        setupToolbar();
        bindViews();
        setupRecyclerView();

        findViewById(R.id.clearLogButton).setOnClickListener(v -> confirmClearLogs());

        loadData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // just so the back arrow shows up in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void bindViews() {
        db = new DatabaseHelper(this);

        emptyLogsView = findViewById(R.id.emptyLogsView);
        totalLogsCount = findViewById(R.id.totalLogsCount);
        addedLogsCount = findViewById(R.id.addedLogsCount);
        updatedLogsCount = findViewById(R.id.updatedLogsCount);
        deletedLogsCount = findViewById(R.id.deletedLogsCount);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.auditRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AuditLogAdapter();
        recyclerView.setAdapter(adapter);
    }

    // pulls the logs from the DB and refreshes everything on screen -
    // called on first load and again after the user clears the log
    private void loadData() {
        List<AuditLogItem> logs = db.auditLogs();
        adapter.setItems(logs);

        // could probably get these counts in one query instead of three,
        // but the log table's tiny so it's not worth the extra complexity
        int added = db.countLogsByAction("ADDED");
        int updated = db.countLogsByAction("UPDATED");
        int deleted = db.countLogsByAction("DELETED");

        totalLogsCount.setText(String.valueOf(logs.size()));
        addedLogsCount.setText(String.valueOf(added));
        updatedLogsCount.setText(String.valueOf(updated));
        deletedLogsCount.setText(String.valueOf(deleted));

        toggleEmptyState(logs.isEmpty());
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (emptyLogsView == null) {
            return;
        }
        emptyLogsView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void confirmClearLogs() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Audit Log?")
                .setMessage("This will permanently delete all activity log history.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (dialog, which) -> {
                    Log.d(TAG, "user cleared the activity log");
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