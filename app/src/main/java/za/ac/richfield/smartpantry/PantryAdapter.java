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

    // days-left threshold for showing the orange "expiring soon" badge instead of
    // green
    private static final int EXPIRING_SOON_DAYS = 3;

    // date formats we try to accept - covers most of what a user might type
    // manually
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd", "yyyy/MM/dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy.MM.dd"
    };

    // badge colors - grabbed these from the Material color tool, might revisit
    // later for dark mode
    private static final int COLOR_NEUTRAL_BG = Color.parseColor("#E0E4DE");
    private static final int COLOR_NEUTRAL_TEXT = Color.parseColor("#414941");
    private static final int COLOR_EXPIRED_BG = Color.parseColor("#FFDAD6");
    private static final int COLOR_EXPIRED_TEXT = Color.parseColor("#BA1A1A");
    private static final int COLOR_WARNING_BG = Color.parseColor("#FFDDB3");
    private static final int COLOR_WARNING_TEXT = Color.parseColor("#8A4A00");
    private static final int COLOR_FRESH_BG = Color.parseColor("#D1E8D3");
    private static final int COLOR_FRESH_TEXT = Color.parseColor("#0C1F12");

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
        holder.categoryIcon.setText(getCategoryIcon(item.name));
        holder.badge.setText(formatQuantity(item.quantity, item.unit));

        bindExpiryStatus(holder.expiryBadge, item.expiry);

        holder.edit.setOnClickListener(v -> listener.edit(item));
        holder.delete.setOnClickListener(v -> listener.delete(item));
    }

    // whole numbers look cleaner without a decimal, e.g. "2 unit" instead of "2.0
    // unit"
    private String formatQuantity(double quantity, String unit) {
        if (quantity % 1 == 0) {
            return String.format(Locale.getDefault(), "%.0f %s", quantity, unit);
        }
        return String.format(Locale.getDefault(), "%.1f %s", quantity, unit);
    }

    private void bindExpiryStatus(TextView badge, String expiryStr) {
        if (expiryStr == null || expiryStr.trim().isEmpty()) {
            applyBadge(badge, "📅 No expiry date set", COLOR_NEUTRAL_BG, COLOR_NEUTRAL_TEXT);
            return;
        }

        Date expiryDate = parseDate(expiryStr.trim());
        if (expiryDate == null) {
            // couldn't parse it, just show it raw rather than hiding the info
            applyBadge(badge, "📅 Expiry: " + expiryStr.trim(), COLOR_NEUTRAL_BG, COLOR_NEUTRAL_TEXT);
            return;
        }

        long daysDiff = getDaysDifference(expiryDate);

        if (daysDiff < 0) {
            applyBadge(badge, expiredLabel(daysDiff), COLOR_EXPIRED_BG, COLOR_EXPIRED_TEXT);
        } else if (daysDiff == 0) {
            applyBadge(badge, "⏳ Expires Today!", COLOR_WARNING_BG, COLOR_WARNING_TEXT);
        } else if (daysDiff <= EXPIRING_SOON_DAYS) {
            applyBadge(badge, expiringSoonLabel(daysDiff), COLOR_WARNING_BG, COLOR_WARNING_TEXT);
        } else {
            applyBadge(badge, "🟢 Fresh · " + daysDiff + " days left", COLOR_FRESH_BG, COLOR_FRESH_TEXT);
        }
    }

    private String expiredLabel(long daysDiff) {
        long ago = Math.abs(daysDiff);
        return ago == 1 ? "⚠️ Expired yesterday" : "⚠️ Expired " + ago + " days ago";
    }

    private String expiringSoonLabel(long daysDiff) {
        return daysDiff == 1 ? "⏳ Expires tomorrow" : "⏳ Expires in " + daysDiff + " days";
    }

    private void applyBadge(TextView badge, String text, int backgroundColor, int textColor) {
        badge.setText(text);
        badge.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        badge.setTextColor(textColor);
    }

    // tries each format in turn - first one that parses wins
    private Date parseDate(String dateStr) {
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (Exception e) {
                // wrong format, move on to the next one
            }
        }
        return null;
    }

    private long getDaysDifference(Date expiryDate) {
        Calendar today = Calendar.getInstance();
        zeroOutTime(today);

        Calendar exp = Calendar.getInstance();
        exp.setTime(expiryDate);
        zeroOutTime(exp);

        long diffMs = exp.getTimeInMillis() - today.getTimeInMillis();
        return diffMs / (24 * 60 * 60 * 1000);
    }

    private void zeroOutTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    // pretty basic keyword matching for now - works fine for the seeded
    // recipes/pantry items,
    // could swap this for a proper category field on PantryItem later if it needs
    // to scale
    private String getCategoryIcon(String name) {
        if (name == null)
            return "📦";

        String n = name.toLowerCase(Locale.ROOT).trim();

        if (n.contains("milk") || n.contains("cheese") || n.contains("yoghurt") || n.contains("butter")
                || n.contains("cream")) {
            return "🧀";
        } else if (n.contains("apple") || n.contains("banana") || n.contains("orange") || n.contains("fruit")
                || n.contains("berry")) {
            return "🍎";
        } else if (n.contains("tomato") || n.contains("onion") || n.contains("potato") || n.contains("garlic")
                || n.contains("carrot") || n.contains("salad")) {
            return "🥕";
        } else if (n.contains("bread") || n.contains("toast") || n.contains("flour") || n.contains("cake")
                || n.contains("bake")) {
            return "🍞";
        } else if (n.contains("chicken") || n.contains("beef") || n.contains("pork") || n.contains("meat")
                || n.contains("bacon")) {
            return "🍗";
        } else if (n.contains("tuna") || n.contains("fish") || n.contains("salmon") || n.contains("seafood")) {
            return "🐟";
        } else if (n.contains("egg")) {
            return "🥚";
        } else if (n.contains("rice") || n.contains("pasta") || n.contains("oats") || n.contains("bean")
                || n.contains("cereal") || n.contains("noodle")) {
            return "🌾";
        } else if (n.contains("oil") || n.contains("sauce") || n.contains("mayo") || n.contains("ketchup")
                || n.contains("spice") || n.contains("salt") || n.contains("sugar")) {
            return "🥫";
        } else if (n.contains("water") || n.contains("juice") || n.contains("soda") || n.contains("drink")
                || n.contains("coffee") || n.contains("tea")) {
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