package za.ac.richfield.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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
        holder.summary.setText(recipe.requirements.size() + " ingredients - all available");
        holder.itemView.setOnClickListener(v -> listener.open(recipe));
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
