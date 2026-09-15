package za.ac.richfield.smartpantry;

import android.content.*; import android.database.Cursor; import android.database.sqlite.*;
import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB="smart_pantry.db"; private static final int VERSION=1;
    public DatabaseHelper(Context c){super(c,DB,null,VERSION);}
    @Override public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE pantry(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,quantity REAL NOT NULL CHECK(quantity>0),unit TEXT NOT NULL,expiry TEXT)");
        db.execSQL("CREATE TABLE recipes(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,method TEXT NOT NULL)");
        db.execSQL("CREATE TABLE requirements(id INTEGER PRIMARY KEY AUTOINCREMENT,recipe_id INTEGER NOT NULL,name TEXT NOT NULL,quantity REAL NOT NULL,unit TEXT NOT NULL,FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");
        seed(db);
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldV,int newV){}
    @Override public void onConfigure(SQLiteDatabase db){super.onConfigure(db);db.setForeignKeyConstraintsEnabled(true);}
    public long addPantry(String n,double q,String u,String e){ContentValues v=values(n,q,u,e);return getWritableDatabase().insertOrThrow("pantry",null,v);}
    public void updatePantry(long id,String n,double q,String u,String e){getWritableDatabase().update("pantry",values(n,q,u,e),"id=?",new String[]{String.valueOf(id)});}
    public void deletePantry(long id){getWritableDatabase().delete("pantry","id=?",new String[]{String.valueOf(id)});}
    private ContentValues values(String n,double q,String u,String e){ContentValues v=new ContentValues();v.put("name",n.trim());v.put("quantity",q);v.put("unit",u);v.put("expiry",e.trim());return v;}
    public PantryItem pantry(long id){try(Cursor c=getReadableDatabase().query("pantry",null,"id=?",new String[]{String.valueOf(id)},null,null,null)){if(c.moveToFirst())return pantryFrom(c);}return null;}
    public List<PantryItem> pantry(){List<PantryItem>x=new ArrayList<>();try(Cursor c=getReadableDatabase().query("pantry",null,null,null,null,null,"name COLLATE NOCASE")){while(c.moveToNext())x.add(pantryFrom(c));}return x;}
    private PantryItem pantryFrom(Cursor c){return new PantryItem(c.getLong(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("name")),c.getDouble(c.getColumnIndexOrThrow("quantity")),c.getString(c.getColumnIndexOrThrow("unit")),c.getString(c.getColumnIndexOrThrow("expiry")));}
    public List<Recipe> suggested(){List<PantryItem> pantry=pantry();List<Recipe> out=new ArrayList<>();for(Recipe r:recipes())if(matches(r,pantry))out.add(r);return out;}
    public boolean matches(Recipe recipe,List<PantryItem> pantry){
        for(Recipe.Requirement req:recipe.requirements){double available=0;String target=IngredientNormalizer.name(req.name);String family=IngredientNormalizer.family(req.unit);for(PantryItem p:pantry)if(IngredientNormalizer.name(p.name).equals(target)&&IngredientNormalizer.family(p.unit).equals(family))available+=IngredientNormalizer.baseQuantity(p.quantity,p.unit);if(available+0.0001<IngredientNormalizer.baseQuantity(req.quantity,req.unit))return false;}return true;
    }
    public Recipe recipe(long id){for(Recipe r:recipes())if(r.id==id)return r;return null;}
    public List<Recipe> recipes(){List<Recipe> out=new ArrayList<>();SQLiteDatabase db=getReadableDatabase();try(Cursor r=db.query("recipes",null,null,null,null,null,"name")){while(r.moveToNext()){long id=r.getLong(0);List<Recipe.Requirement> reqs=new ArrayList<>();try(Cursor q=db.query("requirements",null,"recipe_id=?",new String[]{String.valueOf(id)},null,null,"id")){while(q.moveToNext())reqs.add(new Recipe.Requirement(q.getString(q.getColumnIndexOrThrow("name")),q.getDouble(q.getColumnIndexOrThrow("quantity")),q.getString(q.getColumnIndexOrThrow("unit"))));}out.add(new Recipe(id,r.getString(r.getColumnIndexOrThrow("name")),r.getString(r.getColumnIndexOrThrow("method")),reqs));}}return out;}
    private void seed(SQLiteDatabase db){
        addRecipe(db,"Tomato Toast","Toast the bread. Slice tomato, place it on top and season.","bread|2|unit;tomato|1|unit");
        addRecipe(db,"Cheese Toastie","Fill bread with cheese and toast in a dry pan until golden.","bread|2|unit;cheese|50|g");
        addRecipe(db,"Scrambled Eggs","Whisk eggs with milk and cook gently, stirring until set.","egg|2|unit;milk|30|ml");
        addRecipe(db,"Banana Oats","Simmer oats in milk, then top with sliced banana.","oats|50|g;milk|200|ml;banana|1|unit");
        addRecipe(db,"Tomato Omelette","Whisk eggs, add chopped tomato and cook until set.","egg|2|unit;tomato|1|unit");
        addRecipe(db,"Garlic Pasta","Boil pasta. Gently fry garlic in oil and toss together.","pasta|100|g;garlic|2|unit;oil|15|ml");
        addRecipe(db,"Rice and Beans","Cook rice, warm beans and combine with tomato.","rice|100|g;beans|100|g;tomato|1|unit");
        addRecipe(db,"Potato Hash","Dice potato and onion, then fry in oil until crisp.","potato|2|unit;onion|1|unit;oil|20|ml");
        addRecipe(db,"Tuna Sandwich","Mix tuna with mayonnaise and fill the bread.","bread|2|unit;tuna|100|g;mayonnaise|15|g");
        addRecipe(db,"Fruit Bowl","Slice and combine the apple, banana and orange.","apple|1|unit;banana|1|unit;orange|1|unit");
        addRecipe(db,"Chicken Rice","Cook chicken thoroughly and serve with cooked rice.","chicken|150|g;rice|100|g");
        addRecipe(db,"Yoghurt Banana Cup","Slice banana into yoghurt and serve chilled.","yoghurt|150|g;banana|1|unit");
        addRecipe(db,"Bean Salad","Combine beans, chopped tomato and onion.","beans|150|g;tomato|1|unit;onion|1|unit");
        addRecipe(db,"Egg Fried Rice","Stir-fry cooked rice with egg and oil until hot.","rice|150|g;egg|1|unit;oil|10|ml");
        addRecipe(db,"Cheesy Pasta","Boil pasta, drain, then stir through grated cheese.","pasta|100|g;cheese|50|g");
        addRecipe(db,"Apple Oats","Cook oats with milk and fold in diced apple.","oats|50|g;milk|200|ml;apple|1|unit");
        addRecipe(db,"Tomato Rice","Cook rice and stir through chopped tomato.","rice|100|g;tomato|2|unit");
        addRecipe(db,"Boiled Eggs on Toast","Boil eggs, peel, slice and serve on toast.","egg|2|unit;bread|2|unit");
    }
    private void addRecipe(SQLiteDatabase db,String name,String method,String spec){ContentValues v=new ContentValues();v.put("name",name);v.put("method",method);long id=db.insertOrThrow("recipes",null,v);for(String part:spec.split(";")){String[]a=part.split("\\|");ContentValues q=new ContentValues();q.put("recipe_id",id);q.put("name",a[0]);q.put("quantity",Double.parseDouble(a[1]));q.put("unit",a[2]);db.insertOrThrow("requirements",null,q);}}
}
