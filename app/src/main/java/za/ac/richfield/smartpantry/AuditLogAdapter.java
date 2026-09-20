package za.ac.richfield.smartpantry;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AuditLogAdapter extends RecyclerView.Adapter<AuditLogAdapter.ViewHolder> {

    private final List<AuditLogItem> items = new ArrayList<>();

    public void setItems(List<AuditLogItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_audit_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuditLogItem item = items.get(position);
        holder.logItemName.setText(item.itemName);
        holder.logDetails.setText(item.details);
        holder.logTimestamp.setText(item.timestamp);
        holder.actionBadge.setText(item.actionType);

        int badgeColor;
        if ("ADDED".equalsIgnoreCase(item.actionType)) {
            badgeColor = Color.parseColor("#2E7D32");
        } else if ("UPDATED".equalsIgnoreCase(item.actionType)) {
            badgeColor = Color.parseColor("#E65100");
        } else if ("DELETED".equalsIgnoreCase(item.actionType)) {
            badgeColor = Color.parseColor("#C62828");
        } else {
            badgeColor = Color.parseColor("#616161");
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);
        shape.setColor(badgeColor);
        holder.actionBadge.setBackground(shape);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView actionBadge;
        final TextView logItemName;
        final TextView logDetails;
        final TextView logTimestamp;

        ViewHolder(View itemView) {
            super(itemView);
            actionBadge = itemView.findViewById(R.id.actionBadge);
            logItemName = itemView.findViewById(R.id.logItemName);
            logDetails = itemView.findViewById(R.id.logDetails);
            logTimestamp = itemView.findViewById(R.id.logTimestamp);
        }
    }
}
