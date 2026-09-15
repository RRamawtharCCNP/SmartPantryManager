package za.ac.richfield.smartpantry;

import android.content.*; import android.os.Bundle; import android.view.View; import androidx.appcompat.app.*; import androidx.recyclerview.widget.*;
public class PantryActivity extends AppCompatActivity implements PantryAdapter.Listener{
    private DatabaseHelper db;private PantryAdapter adapter;private View empty;
    protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_pantry);db=new DatabaseHelper(this);empty=findViewById(R.id.emptyText);adapter=new PantryAdapter(this);RecyclerView list=findViewById(R.id.pantryList);list.setLayoutManager(new LinearLayoutManager(this));list.setAdapter(adapter);findViewById(R.id.addButton).setOnClickListener(v->startActivity(new Intent(this,IngredientFormActivity.class)));findViewById(R.id.suggestButton).setOnClickListener(v->startActivity(new Intent(this,SuggestedRecipesActivity.class)));findViewById(R.id.settingsButton).setOnClickListener(v->startActivity(new Intent(this,SettingsActivity.class)));}
    protected void onResume(){super.onResume();refresh();}private void refresh(){java.util.List<PantryItem>x=db.pantry();adapter.setItems(x);empty.setVisibility(x.isEmpty()?View.VISIBLE:View.GONE);}public void edit(PantryItem i){Intent x=new Intent(this,IngredientFormActivity.class);x.putExtra("id",i.id);startActivity(x);}public void delete(PantryItem i){new AlertDialog.Builder(this).setTitle("Delete "+i.name+"?").setMessage("This cannot be undone.").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{db.deletePantry(i.id);refresh();}).show();}
}
