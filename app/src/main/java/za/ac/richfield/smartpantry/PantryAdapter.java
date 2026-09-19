package za.ac.richfield.smartpantry;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        
        // Display category icon avatar
        holder.categoryIcon.setText(getCategoryIcon(item.name));

        // Format quantity in badge
        String qtyText = (item.quantity % 1 == 0) 
                ? String.format(Locale.getDefault(), "%.0f %s", item.quantity, item.unit) 
                : String.format(Locale.getDefault(), "%.1f %s", item.quantity, item.unit);
        holder.badge.setText(qtyText);

        // Bind expiry status badge
        bindExpiryStatus(holder.expiryBadge, item.expiry);

        holder.edit.setOnClickListener(v -> listener.edit(item));
        holder.delete.setOnClickListener(v -> listener.delete(item));
    }

    private void bindExpiryStatus(TextView badge, String expiryStr) {
        if (expiryStr == null || expiryStr.trim().isEmpty()) {
            badge.setText("📅 No expiry date set");
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E4DE")));
            badge.setTextColor(Color.parseColor("#414941"));
            return;
        }

        Date expiryDate = parseDate(expiryStr.trim());
        if (expiryDate == null) {
            badge.setText("📅 Expiry: " + expiryStr.trim());
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E4DE")));
            badge.setTextColor(Color.parseColor("#414941"));
            return;
        }

        long daysDiff = getDaysDifference(expiryDate);

        if (daysDiff < 0) {
            long ago = Math.abs(daysDiff);
            String label = ago == 1 ? "⚠️ Expired yesterday" : "⚠️ Expired " + ago + " days ago";
            badge.setText(label);
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFDAD6"))); // Light red
            badge.setTextColor(Color.parseColor("#BA1A1A")); // Dark red
        } else if (daysDiff == 0) {
            badge.setText("⏳ Expires Today!");
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFDDB3"))); // Light orange
            badge.setTextColor(Color.parseColor("#8A4A00")); // Dark orange
        } else if (daysDiff <= 3) {
            String label = daysDiff == 1 ? "⏳ Expires tomorrow" : "⏳ Expires in " + daysDiff + " days";
            badge.setText(label);
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFDDB3"))); // Light orange
            badge.setTextColor(Color.parseColor("#8A4A00")); // Dark orange
        } else {
            badge.setText("🟢 Fresh · " + daysDiff + " days left");
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1E8D3"))); // Light green
            badge.setTextColor(Color.parseColor("#0C1F12")); // Dark green
        }
    }

    private Date parseDate(String dateStr) {
        String[] formats = {"yyyy-MM-dd", "yyyy/MM/dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy.MM.dd"};
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private long getDaysDifference(Date expiryDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar exp = Calendar.getInstance();
        exp.setTime(expiryDate);
        exp.set(Calendar.HOUR_OF_DAY, 0);
        exp.set(Calendar.MINUTE, 0);
        exp.set(Calendar.SECOND, 0);
        exp.set(Calendar.MILLISECOND, 0);

        long diffMs = exp.getTimeInMillis() - today.getTimeInMillis();
        return diffMs / (24 * 60 * 60 * 1000);
    }

    private String getCategoryIcon(String name) {
        if (name == null) return "📦";
        String n = name.toLowerCase(Locale.ROOT).trim();
        if (n.contains("milk") || n.contains("cheese") || n.contains("yoghurt") || n.contains("butter") || n.contains("cream")) {
            return "🧀";
        } else if (n.contains("apple") || n.contains("banana") || n.contains("orange") || n.contains("fruit") || n.contains("berry")) {
            return "🍎";
        } else if (n.contains("tomato") || n.contains("onion") || n.contains("potato") || n.contains("garlic") || n.contains("carrot") || n.contains("salad")) {
            return "🥕";
        } else if (n.contains("bread") || n.contains("toast") || n.contains("flour") || n.contains("cake") || n.contains("bake")) {
            return "🍞";
        } else if (n.contains("chicken") || n.contains("beef") || n.contains("pork") || n.contains("meat") || n.contains("bacon")) {
            return "🍗";
        } else if (n.contains("tuna") || n.contains("fish") || n.contains("salmon") || n.contains("seafood")) {
            return "🐟";
        } else if (n.contains("egg")) {
            return "🥚";
        } else if (n.contains("rice") || n.contains("pasta") || n.contains("oats") || n.contains("bean") || n.contains("cereal") || n.contains("noodle")) {
            return "🌾";
        } else if (n.contains("oil") || n.contains("sauce") || n.contains("mayo") || n.contains("ketchup") || n.contains("spice") || n.contains("salt") || n.contains("sugar")) {
            return "🥫";
        } else if (n.contains("water") || n.contains("juice") || n.contains("soda") || n.contains("drink") || n.contains("coffee") || n.contains("tea")) {
            return "🧃";
        }
        return "📦";
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, badge, expiryBadge, categoryIcon;
        View avatarContainer;
        Button edit, delete;

        Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            badge = itemView.findViewById(R.id.itemBadge);
            expiryBadge = itemView.findViewById(R.id.expiryBadge);
            categoryIcon = itemView.findViewById(R.id.itemCategoryIcon);
            avatarContainer = itemView.findViewById(R.id.itemAvatarContainer);
            edit = itemView.findViewById(R.id.editButton);
            delete = itemView.findViewById(R.id.deleteButton);
        }
    }
}
