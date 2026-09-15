package za.ac.richfield.smartpantry;

import android.content.Intent;import android.os.Bundle;import android.view.View;import androidx.appcompat.app.AppCompatActivity;import androidx.recyclerview.widget.*;import java.util.*;
public class SuggestedRecipesActivity extends AppCompatActivity{protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_suggested_recipes);List<Recipe>x=new DatabaseHelper(this).suggested();View empty=findViewById(R.id.noMatchesText);empty.setVisibility(x.isEmpty()?View.VISIBLE:View.GONE);RecyclerView list=findViewById(R.id.recipeList);list.setLayoutManager(new LinearLayoutManager(this));list.setAdapter(new RecipeAdapter(x,r->{Intent i=new Intent(this,RecipeDetailActivity.class);i.putExtra("id",r.id);startActivity(i);}));}}
