package sterbenj.com.sharecollection;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.content.DialogInterface.OnClickListener;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * XJB Created by 野良人 on 2018/6/13.
 */
public class CollectionItemListActivity extends BaseActivity {

    public ActionMode actionMode;
    public Set<Integer> positionSet = new HashSet<>();
    public static CollectionItemListActivity instance;

    private int SpinnerIndex;
    private Category category;
    private CollectionItemListAdapter adapter;
    private List<CollectionItem> collectionItemList = new ArrayList<>();

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private TextView mcontext;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private List<String> categoryName = new ArrayList<>();
    private Map<String, Long> categoryData = new ArrayMap<>();
    private String[] categoryName_array;
    private long id;
    private String PackageName;

    @Override
    protected void onResume() {
        super.onResume();

        //从数据库中读取对应类别数据
        if (category.getPackageName().equals("全部收藏")){
            collectionItemList.clear();
            List<CollectionItem> newList = LitePal.order("id desc").find(CollectionItem.class);
            collectionItemList.addAll(newList);

        }
        else {
            collectionItemList.clear();
            List<CollectionItem> newList = LitePal.where("ParentCategory = ?", category.getPackageName())
                    .order("id desc")
                    .find(CollectionItem.class);
            collectionItemList.addAll(newList);
        }
        adapter.notifyDataSetChanged();
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collectionitem_list);

        WebView webView = new WebView(this);

        //获取类别信息
        final Intent intent = getIntent();
        category = (Category)intent.getSerializableExtra("Category");
        SpinnerIndex = intent.getIntExtra("SpinnerIndex", -1);

        //从数据库获取对应类别数据
        if (category.getPackageName().equals("全部收藏")){
            collectionItemList.clear();
            List<CollectionItem> newList = LitePal.order("id desc").find(CollectionItem.class);
            collectionItemList.addAll(newList);

        }
        else {
            collectionItemList.clear();
            List<CollectionItem> newList = LitePal.where("ParentCategory = ?", category.getPackageName())
                    .order("id desc")
                    .find(CollectionItem.class);
            collectionItemList.addAll(newList);
        }

        //初始化Dialog列表数据
        categoryName.clear();
        categoryData.clear();
        int index = 0;
        categoryName_array = new String[LitePal.findAll(Category.class).size()];
        for (Category category : LitePal.findAll(Category.class)){
            categoryName.add(category.getTitle());
            categoryData.put(category.getTitle(), category.getId());
            categoryName_array[index] = category.getTitle();
            index++;
        }

        //初始化适配器
        adapter = new CollectionItemListAdapter(collectionItemList);
        adapter.setMyJumpOutRoad(new CollectionItemListAdapter.JumpOutRoad() {
            @Override
            public void JumpOut(Intent intent, int position) {
                if (actionMode != null){
                    if (positionSet.contains(position)) {
                        // 如果包含，则撤销选择
                        positionSet.remove(position);
                    } else {
                        // 如果不包含，则添加
                        positionSet.add(position);
                    }
                    actionMode.setTitle(positionSet.size() + " 已选择项目");
                    // 更新列表界面
                    adapter.notifyItemChanged(position);
                }
                else{
                    startActivity(intent);
                }
            }

            @Override
            public void JumpOfflineWeb(String uri, int position) {
                if (tools.NetWork){
                    Toast.makeText(getApplicationContext(), "请等待加载完成", Toast.LENGTH_LONG).show();
                }
                if (actionMode != null){
                    if (positionSet.contains(position)) {
                        // 如果包含，则撤销选择
                        positionSet.remove(position);
                    } else {
                        // 如果不包含，则添加
                        positionSet.add(position);
                    }
                    actionMode.setTitle(positionSet.size() + " 已选择项目");
                    // 更新列表界面
                    adapter.notifyItemChanged(position);
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                    intent.putExtra("uri", uri);
                    intent.putExtra("collectionitem", collectionItemList.get(position));
                    startActivity(intent);
                }
            }

            @Override
            public void CopyToShare(String data){
                //获取剪贴板管理器
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", data);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);

                Toast.makeText(getApplicationContext(), "内容已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCollectionitemLongClick(View v, int position) {
                if (actionMode == null){
                    actionMode = startSupportActionMode(new MyCallback());
                    positionSet.add(position);
                    actionMode.setTitle(positionSet.size() + " 已选择项目");
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onCollectionItemClick(CollectionItem collectionItem, int position, View v, Context mContext, CheckBox checkBox) {

                Intent intent1 = new Intent(getApplicationContext(), AcceptCollectionitemAndEditActivity.class);
                intent1.setAction("FROM_IN");
                intent1.putExtra("CollectionItemID", (long)collectionItem.getId());
                intent1.putExtra("SpinnerIndex", SpinnerIndex);
                startActivity(intent1);

            }
        });

        //初始化布局管理
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        //初始化主context
        mcontext = (TextView)findViewById(R.id.collectionlistitem_mainContext);
        mcontext.setText(category.getContext());

        //初始化备注卡片
        CardView cardView = findViewById(R.id.collectionlistitem_context_cardview);
        View cardViewLine = findViewById(R.id.collectionlistitem_line2);

        //初始化toolbar
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.ctoolbar_collectionlist);
        toolbar = (Toolbar)findViewById(R.id.toolbar_collectionlist);
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.ctoolbar_collectionlist);
        collapsingToolbarLayout.setTitle(category.getTitle());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //初始化头图
        AppCompatImageView imageView = (AppCompatImageView)findViewById(R.id.imageView_collectionitemlist);

        Glide.with(this)
                .load(category.getIcon())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(10, 25)))
                .into(imageView);

        //根据主题设置样式
        switch (BaseActivity.sTheme){
            case R.style.white_transStat:
                collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, android.R.color.background_light));
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.cardview_light_background));
                cardViewLine.setBackgroundColor(ContextCompat.getColor(this, R.color.Line_Light));
                break;
            case R.style.Dark:
                collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, android.R.color.black));
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.cardview_dark2));
                cardViewLine.setBackgroundColor(ContextCompat.getColor(this, android.R.color.tertiary_text_dark));
                break;
        }

        //初始化列表
        recyclerView = (RecyclerView) findViewById(R.id.recy_collectionlist);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);


        //初始化fab，设置点击监听
        fab = (FloatingActionButton)findViewById(R.id.collectionlistitem_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryEditActivity.class);
                intent.putExtra("category", category);
                intent.setAction("From CategoryList");
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        //加载布局
        getMenuInflater().inflate(R.menu.collectionitemlist_menu, menu);

        //初始化搜索按钮
        MenuItem item = menu.findItem(R.id.collectionitemlist_menu_search);
        SearchView searchView = (SearchView) item.getActionView();

        //搜索框提示
        searchView.setQueryHint("请输入项目标题");

        //默认关闭搜索
        searchView.setIconified(true);

        //显示提交按钮
        searchView.setSubmitButtonEnabled(true);

        //搜索内容监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(String query) {

                //列表刷新成搜索内容
                collectionItemList.clear();
                List<CollectionItem> newList;
                if (category.getPackageName().equals("全部收藏")){
                    newList = LitePal.where("Title LIKE ?", "%"+query+"%").order("id desc").find(CollectionItem.class);
                }
                else{
                    newList = LitePal.where("Title LIKE ? and ParentCategory = ?", "%"+query+"%", category.getPackageName()).order("id desc").find(CollectionItem.class);
                }
                collectionItemList.addAll(newList);
                adapter.notifyDataSetChanged();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //列表刷新成搜索内容
                collectionItemList.clear();
                List<CollectionItem> newList;
                if (category.getPackageName().equals("全部收藏")){
                    newList = LitePal.where("Title LIKE ?", "%"+newText+"%").order("id desc").find(CollectionItem.class);
                }
                else{
                    newList = LitePal.where("Title LIKE ? and ParentCategory = ?", "%"+newText+"%", category.getPackageName()).order("id desc").find(CollectionItem.class);
                }
                collectionItemList.addAll(newList);
                adapter.notifyDataSetChanged();

                return false;
            }
        });

        //关闭搜索监听
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                //恢复列表
                if (category.getPackageName().equals("全部收藏")){
                    collectionItemList.clear();
                    List<CollectionItem> newList = LitePal.order("id desc").find(CollectionItem.class);
                    collectionItemList.addAll(newList);
                }
                else {
                    collectionItemList.clear();
                    List<CollectionItem> newList = LitePal.where("ParentCategory = ?", category.getPackageName())
                            .order("id desc")
                            .find(CollectionItem.class);
                    collectionItemList.addAll(newList);
                }
                adapter.notifyDataSetChanged();

                return false;
            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK){
                    List<Category> categories = LitePal.where("PackageName = ?", data.getStringExtra("packageName"))
                                                        .find(Category.class);
                    this.category = categories.get(0);
                    reflashLayoutData();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /*
    多选编辑模式
     */
    private class MyCallback implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (actionMode == null){
                fab.setClickable(false);
                actionMode = mode;
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.collectionitemlist_action_mode_menu, menu);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        //多选模式下的操作按钮
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.collectionitemlist_actionMode_delete:
                    return confirmDeleAll();
                case R.id.collectionitemlist_actionMode_move:
                    return moveCollectionItems();
                case R.id.collectionitemlist_actionMode_SelectAll:
                    if (positionSet.size() == collectionItemList.size()){
                        for (int i = 0; i < adapter.getItemCount(); i++){
                            positionSet.remove(i);
                        }
                    }
                    else{
                        for (int i = 0; i < adapter.getItemCount(); i++){
                            if (!positionSet.contains(i)){
                                positionSet.add(i);
                            }
                        }
                    }
                    actionMode.setTitle(positionSet.size() + " 已选择项目");
                    adapter.notifyDataSetChanged();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            fab.setClickable(true);
            positionSet.clear();
            if (category.getPackageName().equals("全部收藏")){
                collectionItemList.clear();
                List<CollectionItem> newList = LitePal.order("id desc").find(CollectionItem.class);
                collectionItemList.addAll(newList);

            }
            else {
                collectionItemList.clear();
                List<CollectionItem> newList = LitePal.where("ParentCategory = ?", category.getPackageName())
                        .order("id desc")
                        .find(CollectionItem.class);
                collectionItemList.addAll(newList);
            }
            adapter.notifyDataSetChanged();
        }
    }
    /*
    END
    多选编辑模式
     */

    void reflashLayoutData(){
        collapsingToolbarLayout.setTitle(category.getTitle());
        mcontext.setText(category.getContext());
    }


    /*
    多选模式删除确认
     */
    private Boolean confirmDeleAll(){

        /*
        确认是否删除
         */
        AlertDialog.Builder dialog = new AlertDialog.Builder(CollectionItemListActivity.this);
        dialog.setTitle("");
        dialog.setMessage("确认删除" + positionSet.size() + "个项目吗？");
        dialog.setCancelable(false);

        //确认
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int position : positionSet){
                    LitePal.delete(CollectionItem.class, adapter.getCollectionItemList().get(position).getId());
                }
                actionMode.finish();
            }
        });

        //取消
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();
        /*
        确认是否删除
         */


        return true;
    }
    /*
    END
    多选模式删除确认
     */

    /*
    多选模式移动
     */
    private Boolean moveCollectionItems(){

        //初始化选择框
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("移动到")
                .setItems(categoryName_array, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //通过类名获取id再获取包名
                        id = categoryData.get(categoryName.get(which));
                        PackageName = LitePal.find(Category.class, id).getPackageName();

                        //移动操作：把夫包名（类别）更改成用户选择的
                        CollectionItem temp;
                        for (int position : positionSet){
                            temp = LitePal.find(CollectionItem.class, adapter.getCollectionItemList().get(position).getId());
                            temp.setParentCategory(PackageName);
                            temp.update(temp.getId());
                        }

                        //刷新操作
                        //从数据库中读取对应类别数据
                        if (category.getPackageName().equals("全部收藏")){
                            collectionItemList.clear();
                            List<CollectionItem> newList = LitePal.order("id desc").find(CollectionItem.class);
                            collectionItemList.addAll(newList);

                        }
                        else {
                            collectionItemList.clear();
                            List<CollectionItem> newList = LitePal.where("ParentCategory = ?", category.getPackageName())
                                    .order("id desc")
                                    .find(CollectionItem.class);
                            collectionItemList.addAll(newList);
                        }
                        adapter.notifyDataSetChanged();

                        //移动成功提示
                        Toast.makeText(getApplicationContext(), "成功移动到" + LitePal.find(Category.class, id).getTitle()
                        , Toast.LENGTH_SHORT).show();

                        dialog.dismiss();

                        actionMode.finish();
                    }
                }).show();


        return true;
    }

    /*
    END
    多选模式移动
     */
}
