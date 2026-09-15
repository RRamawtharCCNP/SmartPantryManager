package za.ac.richfield.smartpantry;

import android.os.Bundle;import android.widget.TextView;import androidx.appcompat.app.AppCompatActivity;
public class RecipeDetailActivity extends AppCompatActivity{protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_recipe_detail);Recipe r=new DatabaseHelper(this).recipe(getIntent().getLongExtra("id",-1));if(r==null){finish();return;}((TextView)findViewById(R.id.recipeName)).setText(r.name);StringBuilder s=new StringBuilder();for(Recipe.Requirement q:r.requirements)s.append("• ").append(q.name).append(": ").append(q.quantity).append(" ").append(q.unit).append("\n");((TextView)findViewById(R.id.ingredientsText)).setText(s.toString());((TextView)findViewById(R.id.methodText)).setText(r.method);}}
