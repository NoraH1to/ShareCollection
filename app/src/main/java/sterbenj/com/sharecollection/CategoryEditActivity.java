package sterbenj.com.sharecollection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

/**
 * XJB Created by 野良人 on 2018/5/26.
 */

public class CategoryEditActivity extends BaseActivity {

    private TextInputEditText title;
    private TextInputEditText context;
    private Category category;
    private Toolbar toolbar;
    private String packageName = null;
    private Intent intent;
    private cacheData cache = new cacheData("", "");

    //缓存原有数据
    static class cacheData{
        String Title;
        String Context;

        public cacheData(String title, String context){
            Context = context;
            Title = title;
        }

        public String getContext() {
            return Context;
        }

        public String getTitle() {
            return Title;
        }

        public void setContext(String context) {
            Context = context;
        }

        public void setTitle(String title) {
            Title = title;
        }
    }

    @Override
    public void onBackPressed() {
        confirmBack();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_edit);
        init();

        //初始化toolbar
        setSupportActionBar(toolbar);

        //初始toolbar化返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //初始化
        title = (TextInputEditText)findViewById(R.id.edit_title);
        context = (TextInputEditText)findViewById(R.id.edit_context);

        //从intent中得到传入活动类型
        intent = getIntent();
        if (intent.getAction().equals("From CategoryList")){
            this.category = (Category) intent.getSerializableExtra("category");
            title.setText(category.getTitle());
            context.setText(category.getContext());
            packageName = category.getPackageName();

            //初始化cacheData
            cache = new cacheData(title.getText().toString(), context.getText().toString());

        }

        else if (intent.getAction().equals("From Custom")){
            this.category = new Category();
        }

    }

    //加载toolbar布局
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.edit_toolbar_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){

            //保存按钮
            case R.id.saveNote:
                if(saveCategory(category)){
                    Intent newIntent = new Intent();
                    newIntent.putExtra("packageName", category.getPackageName());
                    setResult(RESULT_OK, newIntent);
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            //返回箭头按钮
            case android.R.id.home:
                confirmBack();
                break;
            default:
        }
        return true;
    }


    public boolean saveCategory(Category category){

        //为新建自定义类时
        if (packageName == null){
            if (title.getText().toString().isEmpty()){
                Toast.makeText(this, "类名不能为空", Toast.LENGTH_SHORT).show();
                return false;
            }
            else {
                category.setTitle(title.getText().toString());
                category.setContext(context.getText().toString());
                category.setPackageName(title.getText().toString());
                category.setIcon(tools.DrawableToByteArray(ContextCompat.getDrawable(this, R.drawable.ic_folder_black_24dp)));
                category.save();
                return true;
            }
        }

        //为编辑自定义类时
        else if ((packageName.equals(category.getTitle())) && (packageName != null)){
            if (title.getText().toString().isEmpty()){
                Toast.makeText(this, "类名不能为空", Toast.LENGTH_SHORT).show();
                return false;
            }
            else {
                category.setTitle(title.getText().toString());
                category.setContext(context.getText().toString());
                category.setIcon(tools.DrawableToByteArray(ContextCompat.getDrawable(this, R.drawable.ic_folder_black_24dp)));
                category.updateAll("PackageName = ?", packageName);
                return true;
            }
        }
        //为编辑App分类时
        else{
            if (title.getText().toString().isEmpty()){
                Toast.makeText(this, "类名不能为空", Toast.LENGTH_SHORT).show();
                return false;
            }
            else{
                category.setTitle(title.getText().toString());
                category.setContext(context.getText().toString());
                category.update(category.getId());
                return true;
            }
        }
    }

    //确认是否返回
    public void confirmBack(){
        if (!(cache.getContext().equals(context.getText().toString()) && cache.getTitle().equals(title.getText().toString()))){
            AlertDialog.Builder dialog = new AlertDialog.Builder(CategoryEditActivity.this);
            dialog.setMessage("内容已经更改，是否保存？");
            dialog.setCancelable(true);
            dialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(saveCategory(category)){
                        Intent newIntent = new Intent();
                        newIntent.putExtra("packageName", category.getPackageName());
                        setResult(RESULT_OK, newIntent);
                        Toast.makeText(CategoryEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
            dialog.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.show();
        }
        else{
            finish();
        }
    }

    //初始化控件
    public void init(){
        title = (TextInputEditText)findViewById(R.id.edit_title);
        toolbar = (Toolbar)findViewById(R.id.toolbar_edit);
    }

}