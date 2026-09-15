package za.ac.richfield.smartpantry;

import android.view.*; import android.widget.*; import androidx.annotation.NonNull; import androidx.recyclerview.widget.RecyclerView; import java.util.*;
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.Holder>{
    public interface Listener{void edit(PantryItem i);void delete(PantryItem i);} private List<PantryItem> items=new ArrayList<>();private final Listener listener;
    public PantryAdapter(Listener l){listener=l;} public void setItems(List<PantryItem>x){items=x;notifyDataSetChanged();}
    @NonNull public Holder onCreateViewHolder(@NonNull ViewGroup p,int t){return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.row_pantry_item,p,false));}
    public void onBindViewHolder(@NonNull Holder h,int pos){PantryItem i=items.get(pos);h.name.setText(i.name);h.details.setText(i.quantity+" "+i.unit+(i.expiry.isEmpty()?"":" | Expires "+i.expiry));h.edit.setOnClickListener(v->listener.edit(i));h.delete.setOnClickListener(v->listener.delete(i));}
    public int getItemCount(){return items.size();} static class Holder extends RecyclerView.ViewHolder{TextView name,details;Button edit,delete;Holder(View v){super(v);name=v.findViewById(R.id.itemName);details=v.findViewById(R.id.itemDetails);edit=v.findViewById(R.id.editButton);delete=v.findViewById(R.id.deleteButton);}}
}
