package za.ac.richfield.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.Holder> {

    public interface Listener {
        void edit(PantryItem item);
        void delete(PantryItem item);
    }

    private List<PantryItem> items = new ArrayList<>();
    private final Listener listener;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_pantry_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        PantryItem item = items.get(position);
        holder.name.setText(item.name);
        
        // Display formatted quantity in badge
        String qtyText = (item.quantity % 1 == 0) 
                ? String.format("%.0f %s", item.quantity, item.unit) 
                : String.format("%.1f %s", item.quantity, item.unit);
        holder.badge.setText(qtyText);

        // Display expiry detail text
        if (item.expiry != null && !item.expiry.trim().isEmpty()) {
            holder.details.setText("📅 Expiry: " + item.expiry.trim());
        } else {
            holder.details.setText("📅 Expiry: No expiry date set");
        }

        holder.edit.setOnClickListener(v -> listener.edit(item));
        holder.delete.setOnClickListener(v -> listener.delete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, badge, details;
        Button edit, delete;

        Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            badge = itemView.findViewById(R.id.itemBadge);
            details = itemView.findViewById(R.id.itemDetails);
            edit = itemView.findViewById(R.id.editButton);
            delete = itemView.findViewById(R.id.deleteButton);
        }
    }
}
