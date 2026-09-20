package za.ac.richfield.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// This only shows recipes we've already confirmed the user has ingredients for -
// filtering happens before this list gets built (see DatabaseHelper.suggested()),
// so we don't need to check availability again here.
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.Holder> {

    interface Listener {
        void open(Recipe recipe);
    }

    private final List<Recipe> items;
    private final Listener listener;

    RecipeAdapter(List<Recipe> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_recipe, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Recipe recipe = items.get(position);

        holder.name.setText(recipe.name);
        holder.summary.setText(buildSummary(recipe));

        holder.itemView.setOnClickListener(v -> listener.open(recipe));
    }

    // small thing, but "1 ingredients" always bugged me - worth the extra line for
    // "1 ingredient"
    private String buildSummary(Recipe recipe) {
        int count = recipe.requirements.size();
        String word = count == 1 ? "ingredient" : "ingredients";
        return count + " " + word + " - all available";
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, summary;

        Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipeRowName);
            summary = itemView.findViewById(R.id.recipeRowSummary);
        }
    }
}