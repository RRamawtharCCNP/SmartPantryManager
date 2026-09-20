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

    // corner radius for the little colored action badge (ADDED/UPDATED/DELETED)
    private static final float BADGE_CORNER_RADIUS = 16f;

    private final List<AuditLogItem> items = new ArrayList<>();

    public void setItems(List<AuditLogItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        // TODO: switch to DiffUtil at some point, notifyDataSetChanged is fine for now
        // since lists are small
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

        holder.actionBadge.setBackground(buildBadgeBackground(item.actionType));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // rounded pill background for the badge, colored based on what kind of action
    // it was
    private GradientDrawable buildBadgeBackground(String actionType) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(BADGE_CORNER_RADIUS);
        shape.setColor(colorForAction(actionType));
        return shape;
    }

    private int colorForAction(String actionType) {
        if (actionType == null) {
            return Color.parseColor("#616161"); // fallback grey, shouldn't really happen
        }

        switch (actionType.toUpperCase()) {
            case "ADDED":
                return Color.parseColor("#2E7D32"); // green
            case "UPDATED":
                return Color.parseColor("#E65100"); // orange
            case "DELETED":
                return Color.parseColor("#C62828"); // red
            default:
                return Color.parseColor("#616161"); // grey for anything unexpected
        }
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