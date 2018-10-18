package sterbenj.com.sharecollection;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.navigation.NavigationView;
import com.tencent.smtt.sdk.QbSdk;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Boolean hasFinishLoadWeb;

    int load_Index;

    private static HashSet STATIC = new HashSet() {
        {
            add("html");
            add("htm");
            add("js");
            add("ico");
            add("css");
            add("png");
            add("jpg");
            add("jpeg");
            add("gif");
            add("bmp");
            add("ttf");
            add("woff");
            add("woff2");
            add("otf");
            add("eot");
            add("svg");
            add("xml");
            add("swf");
            add("txt");
            add("text");
            add("conf");
            add("webp");
        }
    };

    public ActionMode actionMode;
    private List<Category> categoryList = new ArrayList<>();
    public Set<Integer> positionSet = new HashSet<>();
    CollectionsAdapter collectionsAdapter;
    public final static int NAME = 0;
    public static MainActivity instance;
    private FloatingActionMenu fab;
    private FloatingActionButton fabFromApp;
    private FloatingActionButton fabFromCustom;
    private DrawerLayout drawer;
    private ProgressDialog progressDialog;

    public static final String TAG = "MainActivity";

    @Override
    public void onResume(){
        Log.d(TAG, "onResume: ");
        super.onResume();

        //刷新列表
        categoryList.clear();
        List<Category> newList = LitePal.order("id asc").find(Category.class);
        categoryList.addAll(newList);
        collectionsAdapter.notifyDataSetChanged();

        //判断是否登入账号，登入则开启同步按钮
        invalidateOptionsMenu();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        QbSdk.initX5Environment(getApplicationContext(), null);

        if(LitePal.where("PackageName = ?", "全部收藏").find(Category.class).size() == 0){
            Category category = new Category("全部收藏", tools.DrawableToByteArray(ContextCompat.getDrawable(this, R.drawable.ic_folder_black_24dp)), "全部收藏", "全部收藏");
            category.save();
        }

        /*
        初始化recyclerView和其adapter
         */
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_list);
        collectionsAdapter = new CollectionsAdapter(categoryList);
        collectionsAdapter.setMyClickListener(new CollectionsAdapter.MyClickListener(){
            //长按进入多选编辑模式
            @Override
            public void longClickListener(View v, int position){
                if (actionMode == null  && position != 0) {
                    actionMode = startSupportActionMode(new MyCallback());
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    fab.hideMenu(true);
                    positionSet.add(position);
                    actionMode.setTitle(positionSet.size() + " 已选择项目");
                    collectionsAdapter.notifyItemChanged(position);
                }
            }

            //短按进入笔记编辑，多选模式则选择目标
            @Override
            public void ClickListener(View v, int position, Context mContext, Category category1, int NAME, CheckBox checkBox){
                if (actionMode != null){
                    if (positionSet.contains(position) && position != 0) {
                        // 如果包含，则撤销选择
                        positionSet.remove(position);
                    }
                    else if (!positionSet.contains(position) && position != 0){
                        // 如果不包含，则添加
                        positionSet.add(position);
                    }
                    actionMode.setTitle(positionSet.size() + " 已选择项目");
                        // 更新列表界面
                    collectionsAdapter.notifyItemChanged(position);
                }
                else{
                    Intent intent = new Intent(mContext, CollectionItemListActivity.class);
                    intent.putExtra("Category", category1);
                    intent.putExtra("SpinnerIndex", position);
                    mContext.startActivity(intent);
                }
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(collectionsAdapter);

        //下滑隐藏fab，上滑出现fab
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0){
                    fab.hideMenu(true);
                }
                if(dy < 0 && actionMode == null){
                    fab.showMenu(true);
                }
            }
        });
        /*
        END
        初始化recyclerView和其adapter
         */

        //初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //初始化fab
        fab = (FloatingActionMenu) findViewById(R.id.fab);
        fabFromApp = (FloatingActionButton) findViewById(R.id.fab_fromapp);
        fabFromCustom = (FloatingActionButton) findViewById(R.id.fab_fromcustom);
        fab.setClosedOnTouchOutside(false);

        //从应用列表新建
        fabFromApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ApplistActivity.class);
                startActivityForResult(intent, NAME);
            }
        });

        //自定义新建类
        fabFromCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CategoryEditActivity.class);
                intent.setAction("From Custom");
                startActivity(intent);
            }
        });


        drawer= (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //判断是否登入账号，登入则开启同步按钮
        String accountTemp = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getString("AccountHasLogin", null);
        if (accountTemp != null){
            menu.findItem(R.id.main_menu_upload).setVisible(true);
            menu.findItem(R.id.main_menu_download).setVisible(true);
        }
        else{
            menu.findItem(R.id.main_menu_upload).setVisible(false);
            menu.findItem(R.id.main_menu_download).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.main_menu_upload:
                upload();
                break;
            case R.id.main_menu_download:
                download();
                break;
            case R.id.main_menu_getImage:
                getAllImage();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingActivity.class));
            // Handle the camera action
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /*
    多选编辑模式
     */
    private class MyCallback implements ActionMode.Callback{
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (actionMode == null){
                actionMode = mode;
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.main_delete, menu);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.main_actionMode_delete:
                    return confirmDeleteAll();
                case R.id.main_actionMode_SelectAll:
                    if (positionSet.size() == categoryList.size() - 1){
                        for (int i = 1; i < collectionsAdapter.getItemCount(); i++){
                            positionSet.remove(i);
                        }
                    }
                    else{
                        for (int i = 1; i < collectionsAdapter.getItemCount(); i++){
                            if (!positionSet.contains(i)){
                                positionSet.add(i);
                            }
                        }
                    }
                    actionMode.setTitle(positionSet.size() + " 已选择项目");
                    collectionsAdapter.notifyDataSetChanged();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            actionMode = null;
            positionSet.clear();
            fab.showMenu(true);
            categoryList.clear();
            List<Category> newList = LitePal.order("id asc").find(Category.class);
            categoryList.addAll(newList);
            collectionsAdapter.notifyDataSetChanged();
        }
        /*
        END
        多选编辑模式
         */


        /*
        多选模式删除确认
         */
        public boolean confirmDeleteAll(){


            /*
            确认是否删除
             */
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("");
            dialog.setMessage("确认删除" + positionSet.size() + "个项目吗？");
            dialog.setCancelable(false);

            //确认
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int position : positionSet){
                        LitePal.delete(Category.class, collectionsAdapter.getCategoryList().get(position).getId());
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

    }

    /*
    upload
     */
    private void upload(){

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("");
        progressDialog.setMessage("正在从本机同步到云端");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //判断网络连接
        if (tools.isNetworkConnected(getApplicationContext())){

            //Category表
            //查找用户云BmobCategory所有条目
            BmobQuery<BmobCategory> query1 = new BmobQuery<BmobCategory>();
            query1.addWhereEqualTo("UserId", BmobUser.getCurrentUser(User.class).getObjectId());
            query1.setLimit(500);
            query1.findObjects(new FindListener<BmobCategory>() {
                @Override
                public void done(List<BmobCategory> list, BmobException e) {
                    Log.d(TAG, "done: "+ list.size());
                    if (e == null){
                        Log.d(TAG, "done: step1");
                        deleteBmobCategory(list);
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "上传到云端失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else{
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "没有网络连接", Toast.LENGTH_SHORT).show();
        }
    }

    //删除云BmobCategory
    private void deleteBmobCategory(List<BmobCategory> list){
        List<BmobObject> deleteList = new ArrayList<BmobObject>();
        deleteList.addAll(list);
        list.clear();
        new BmobBatch().deleteBatch(deleteList).doBatch(new QueryListListener<BatchResult>() {
            @Override
            public void done(List<BatchResult> list, BmobException e) {

                Log.d(TAG, "done: step2");
                //删除原有数据成功才同步上去
                if (e == null){
                    uploadBmobCategory();
                }

                //删除失败
                else{
                    progressDialog.dismiss();
                    Log.d(TAG, "done: 111111111111");
                    Toast.makeText(getApplicationContext(), "上传到云端失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        deleteList.clear();
    }

    //上传BmobCategory数据
    private void uploadBmobCategory(){
        List<Category> categories = LitePal.findAll(Category.class);
        categories.remove(0);
        Log.d(TAG, "upload: " + categories.size());
        if (categories.size() > 0){
            List<BmobObject> update = new ArrayList<BmobObject>();
            for (Category category : categories){
                Log.d(TAG, "upload: " + category.getTitle());
                BmobCategory bmobCategory = new BmobCategory();

                bmobCategory.setUserId(BmobUser.getCurrentUser(User.class).getObjectId());

                bmobCategory.setTitle(category.getTitle());

                bmobCategory.setContext(category.getContext());

                Byte[] bytes = new Byte[category.getIcon().length];
                for (int i = 0; i < category.getIcon().length; i++){
                    bytes[i] = category.getIcon()[i];
                }
                bmobCategory.setIcon(bytes);

                bmobCategory.setPackageName(category.getPackageName());

                update.add(bmobCategory);
            }
            new BmobBatch().insertBatch(update).doBatch(new QueryListListener<BatchResult>() {
                @Override
                public void done(List<BatchResult> list, BmobException e) {
                    if (e == null){
                        Log.d(TAG, "done: setp3-1");
                        searchBmobCollectionItem_up();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "上传到云端失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Log.d(TAG, "done: setp3-2");
            searchBmobCollectionItem_up();
        }

    }

    //CollectionItem表
    private void searchBmobCollectionItem_up(){

        //查找用户云BmobCollectionItem所有条目
        BmobQuery<BmobCollectionItem> query2 = new BmobQuery<BmobCollectionItem>();
        query2.addWhereEqualTo("UserId", BmobUser.getCurrentUser(User.class).getObjectId());
        query2.setLimit(500);
        query2.findObjects(new FindListener<BmobCollectionItem>() {
            @Override
            public void done(List<BmobCollectionItem> list, BmobException e) {
                if (e == null){
                    Log.d(TAG, "done: step4");
                    deleteBmobCollectionItem(list);
                }
                else{
                    progressDialog.dismiss();
                    Log.d(TAG, "done: 3333333333333333333333");
                    Toast.makeText(getApplicationContext(), "上传到云端失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //删除云BmobCategory
    private void deleteBmobCollectionItem(List<BmobCollectionItem> list){
        List<BmobObject> deleteList = new ArrayList<BmobObject>();
        deleteList.addAll(list);
        new BmobBatch().deleteBatch(deleteList).doBatch(new QueryListListener<BatchResult>() {
            @Override
            public void done(List<BatchResult> list, BmobException e) {

                //删除原有数据成功才同步上去
                if (e == null){
                    Log.d(TAG, "done: step5");
                    uploadBmobCollectionItem();
                }

                //删除失败
                else{
                    progressDialog.dismiss();
                    Log.d(TAG, "done: 44444444444444444");
                    Toast.makeText(getApplicationContext(), "上传到云端失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        deleteList.clear();
    }

    //上传BmobCollectionItem数据
    private void uploadBmobCollectionItem(){
        List<CollectionItem> collectionItems = LitePal.findAll(CollectionItem.class);
        if (collectionItems.size() > 0){
            List<BmobObject> update = new ArrayList<BmobObject>();
            Log.d(TAG, "uploadBmobCollectionItem: "+collectionItems.size());
            for (CollectionItem collectionItem : collectionItems){
                BmobCollectionItem bmobCollectionItem = new BmobCollectionItem();

                bmobCollectionItem.setUserId(BmobUser.getCurrentUser(User.class).getObjectId());

                bmobCollectionItem.setTitle(collectionItem.getTitle());

                bmobCollectionItem.setContext(collectionItem.getContext());

                bmobCollectionItem.setmUri(collectionItem.getmUri());

                bmobCollectionItem.setParentCategory(collectionItem.getParentCategory());

                update.add(bmobCollectionItem);
            }
            new BmobBatch().insertBatch(update).doBatch(new QueryListListener<BatchResult>() {
                @Override
                public void done(List<BatchResult> list, BmobException e) {
                    if (e == null){
                        Log.d(TAG, "done: step6-1");
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "上传到云端成功", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "上传到云端失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Log.d(TAG, "uploadBmobCollectionItem: step6-2");
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "上传到云端成功", Toast.LENGTH_SHORT).show();
        }

    }

    /*
    END
    upload
     */

    /*
    download
     */
    private void download(){

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("");
        progressDialog.setMessage("正在从云端同步到本机");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (tools.isNetworkConnected(getApplicationContext())){

            //Category表
            //查找用户云BmobCategory所有条目
            BmobQuery<BmobCategory> query1 = new BmobQuery<BmobCategory>();
            query1.addWhereEqualTo("UserId", BmobUser.getCurrentUser(User.class).getObjectId());
            query1.setLimit(500);
            query1.findObjects(new FindListener<BmobCategory>() {
                @Override
                public void done(List<BmobCategory> list, BmobException e) {
                    if (e == null){
                        //删除本地Category
                        LitePal.deleteAll(Category.class, "PackageName != ?", "全部收藏");

                        downloadBmobCategory(list);
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "同步至本机失败", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "done: aaaaaaaaaaaaaaaaaa" + e);
                    }
                }
            });

        }
        else{
            progressDialog.dismiss();
            Log.d(TAG, "download: llllllllllllllllllll");
            Toast.makeText(getApplicationContext(), "没有网络连接", Toast.LENGTH_SHORT).show();
        }
    }

    //把云BmobCategory数据同步到本地Category
    private void downloadBmobCategory(List<BmobCategory> list){
        for (BmobCategory bmobCategory : list){

            Log.d(TAG, "downloadBmobCategory: " + list.size());
            Category category = new Category();

            category.setTitle(bmobCategory.getTitle());

            category.setContext(bmobCategory.getContext());

            category.setPackageName(bmobCategory.getPackageName());

            byte[] bytes = new byte[bmobCategory.getIcon().length];
            for (int i = 0; i < bmobCategory.getIcon().length; i++){
                bytes[i] = bmobCategory.getIcon()[i];
            }
            category.setIcon(bytes);

            category.save();
        }

        list.clear();

        searchBmobCollectionItem_down();
    }

    //CollectionItem表
    private void searchBmobCollectionItem_down(){
        //        //查找用户云BmobCollectionItem所有条目
        BmobQuery<BmobCollectionItem> query2 = new BmobQuery<BmobCollectionItem>();
        query2.addWhereEqualTo("UserId", BmobUser.getCurrentUser().getObjectId());
        query2.setLimit(500);
        query2.findObjects(new FindListener<BmobCollectionItem>() {
            @Override
            public void done(List<BmobCollectionItem> list, BmobException e) {
                if (e == null){
                    downloadBmobCollectionItem(list);
                }
                else{
                    Log.d(TAG, "done: bbbbbbbbbbbbbbbbbb");
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "同步至本机失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //把云BmobCollectionItem数据同步到本地CollectionItem
    private void downloadBmobCollectionItem(List<BmobCollectionItem> list){
        LitePal.deleteAll(CollectionItem.class);
        for (BmobCollectionItem bmobCollectionItem : list){
            CollectionItem collectionItem = new CollectionItem();

            collectionItem.setTitle(bmobCollectionItem.getTitle());

            collectionItem.setContext(bmobCollectionItem.getContext());

            collectionItem.setmUri(bmobCollectionItem.getmUri());

            collectionItem.setImage(tools.DrawableToByteArray(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_close_black_24dp)));

            collectionItem.setParentCategory(bmobCollectionItem.getParentCategory());

            collectionItem.save();
        }
        list.clear();

        //刷新列表
        categoryList.clear();
        List<Category> newList = LitePal.order("id asc").find(Category.class);
        categoryList.addAll(newList);
        collectionsAdapter.notifyDataSetChanged();

        Log.d(TAG, "downloadBmobCollectionItem: cccccccccccccccccc");
        progressDialog.dismiss();

        Toast.makeText(getApplicationContext(), "同步至本机成功", Toast.LENGTH_SHORT).show();
    }

    /*
    END
    download
     */

    //一键获取缩略图
    private void getAllImage(){
        class LoadImage extends AsyncTask<Void, Integer, Void>{

            private ProgressDialog progressDialog;
            private List<CollectionItem> collectionItemList;
            private int success;
            private int fail;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                success = 0;
                fail = 0;
                collectionItemList = LitePal.order("id desc").find(CollectionItem.class);
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setTitle("正在获取缩略图");
                progressDialog.setMessage("可以挂到后台等待");
                progressDialog.setMax(collectionItemList.size());
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            //获取缩略图实现
            @Override
            protected Void doInBackground(Void... voids) {
                PackageManager packageManager = getPackageManager();
                for (int i = 0; i < collectionItemList.size(); i++){
                    CollectionItem temp = collectionItemList.get(i);
                    byte[] bytes = getImagebyte(temp.getmUri());
                    if (bytes == null){
                        fail++;
                        temp.setImage(tools.DrawableToByteArray(ContextCompat.getDrawable(getApplication(), R.drawable.ic_close_black_24dp)));
                        temp.update(temp.getId());
                        publishProgress(i + 1);
                        continue;
                    }
                    else{
                        success++;
                        temp.setImage(bytes);
                        temp.update(temp.getId());
                        Log.d(TAG, "doInBackground: " + temp.getTitle());
                        publishProgress(i + 1);
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                progressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                collectionItemList.clear();
                progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "获取缩略图完成，成功:" + success + "，失败:" + fail, Toast.LENGTH_LONG).show();
            }
        }
        if (tools.isNetworkConnected(getApplicationContext())){
            new LoadImage().execute();
        }
        else {
            Toast.makeText(getApplicationContext(), "获取缩略图失败，没有网络连接", Toast.LENGTH_SHORT).show();
        }
    }

    //解析uri获取图片uri
    private byte[] getImagebyte(String uri){
        Document document;
        Elements images;
        Elements ImageUri;
        String single = null;
        try{
            document = Jsoup.connect(uri).timeout(10000).get();

            //微信公众号
            if (uri.indexOf("weixin.qq.com") != -1){
                images = document.getElementsByTag("img");
                ImageUri = images.select("[data-src$=jpeg],[data-src$=png]");
                Log.d("++++++++++++++++++++", "run: " + ImageUri.size() + "and" + images.size());
                if (ImageUri.size() != 0) {
                    single = ImageUri.first().attr("abs:data-src");
                }
                Log.d("Accept", "run: weixin");
            }

            //bilibili
            else if (uri.indexOf("bilibili.com") != -1){

                //获取所有meta标签数据
                images = document.getElementsByTag("meta");

                //获取图片uri
                ImageUri = images.select("[content$=.jpg]");
                if (ImageUri.size() != 0){
                    single = ImageUri.first().attr("abs:content");
                }
                else {
                    ImageUri = images.select("[content$=.png]");
                    if (ImageUri.size() != 0){
                        single = ImageUri.first().attr("abs:content");
                    }
                }
            }

            //知乎
            else if (uri.indexOf("zhihu.com") != -1){
                images = document.getElementsByTag("link");
                ImageUri = images.select("[rel=shortcut icon]");
                if (images.size() != 0){
                    single = ImageUri.attr("abs:href");
                }
            }

            //酷安
            else if (uri.indexOf("www.coolapk.com") != -1){
                images = document.getElementsByTag("img");
                ImageUri = images.select("[src$=under_logo.png]");
                if (images.size() != 0){
                    single = ImageUri.first().attr("abs:src");
                }
            }

            //普通微博
            else if (uri.indexOf("m.weibo.cn") != -1){
                images = document.getElementsByTag("script");
                String[] jsData = images.get(1).data().toString().split("var");
                for (String str : jsData){
                    if (str.contains("$render_data")){
                        String[] ineed = str.split("\",");
                        for (String str2 : ineed){
                            if (str2.trim().contains("original_pic")){
                                Log.d("MainActivity233", "run: "+ str2.trim() + "  " + str2);
                                single = str2.substring(str2.indexOf("http"));
                            }
                        }
                    }
                }
            }

            //微博图文
            else if (uri.indexOf("media.weibo.cn") != -1){
                images = document.getElementsByTag("script");
                String[] jsData = images.get(1).data().toString().split("var");
                for (String str : jsData){
                    if (str.contains("$render_data")){

                        //图片
                        String[] ineed = str.split("\"|\":");
                        for (String str2 : ineed){
                            if (str2.trim().contains(".jpg")){
                                Log.d("MainActivity233", "run: "+ str2.trim() + "  " + str2);
                                single = str2.trim();
                                break;
                            }
                        }
                    }
                }
            }

            //taptap游戏
            else if (uri.indexOf("https://www.taptap.com/app") == 0 || uri.indexOf("www.taptap.com/app") == 0){
                images = document.getElementsByTag("meta");
                single = images.select("[property=og:image]").attr("content");
            }

            //taptap图文
            else if (uri.indexOf("https://www.taptap.com/story") == 0 || uri.indexOf("www.taptap.com/story") == 0){
                single = document.getElementsByTag("meta").select("[property=og:image]").attr("content");
            }

            //MIUI论坛
            else if (uri.indexOf("http://www.miui.com/thread") == 0 || uri.indexOf("www.miui.com/thread") == 0){
                single = document.getElementsByTag("ignore_js_op").first().select("img").attr("zoomfile");
            }

            //一加社区
            else if (uri.indexOf("http://www.oneplusbbs.com/thread") == 0 || uri.indexOf("www.oneplusbbs.com/thread") == 0){
                single = document.getElementsByTag("ignore_js_op").first().select("img").attr("zoomfile");
            }

            //A站
            else if (uri.indexOf("http://www.acfun.cn/a/") == 0 || uri.indexOf("www.acfun.cn/a/") == 0){
                images = document.getElementsByTag("img");
                for (Element element : images){
                    Log.d(TAG, "run: " + element.toString());
                    String str = element.attr("src");
                    if (str.indexOf("http://cdn.aixifan.com") != 0){
                        Log.d(TAG, "run: " + str);
                        single = str;
                        break;
                    }
                }
            }

            //A站2
            else if(uri.indexOf("http://m.acfun.cn/v/") == 0 || uri.indexOf("m.acfun.cn/v/") == 0){
                Log.d("ff", "run: " + uri.substring(uri.indexOf("/v/?ac=") + 7, uri.length()));
                uri = "http://www.acfun.cn/a/ac" + uri.substring(uri.indexOf("/v/?ac=") + 7, uri.length());
                Log.d("fff", "run: " + uri);
                document = Jsoup.connect(uri).timeout(10000).get();
                images = document.getElementsByTag("img");
                for (Element element : images){
                    Log.d(TAG, "run: " + element.toString());
                    String str = element.attr("src");
                    if (str.indexOf("http://cdn.aixifan.com") != 0){
                        Log.d(TAG, "run: " + str);
                        single = str;
                        break;
                    }
                }
            }

            //默认情况
            else{
                //获取所有img标签数据
                images = document.getElementsByTag("img");
                //筛选有可能有图片的标签
                ImageUri = images.select("[data-src$=jpeg],[data-src$=png],[src$=jpeg],[src$=png]");
                //筛选到时
                if (ImageUri.size() != 0){
                    single = ImageUri.first().attr("abs:src");
                    Log.d("Accept", "In run: not 0");
                }
                //筛选不到时
                else{
                    single = null;
                    Log.d("Accept", "In run: 0");
                }
                Log.d("Accept", "run: normal");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Bitmap bitmap = getImageBitmap(single);
        if (bitmap == null){
            return null;
        }
        else{
            return tools.BittmapToByteArray(bitmap);
        }
    }

    //解析图片uri获取bitmap
    private Bitmap getImageBitmap(String url) {
        URL imgurl = null;
        Bitmap bitmap = null;

        HttpURLConnection urlConnection;
        try {
            imgurl = new URL(url);
            urlConnection = (HttpURLConnection)
                    imgurl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(35000);
            urlConnection.setConnectTimeout(35000);
            urlConnection.connect();
            InputStream is = urlConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return getZoomImage(bitmap, 1024);
    }

    public static Bitmap getZoomImage(Bitmap bitmap, double maxSize) {
        if (null == bitmap) {
            return null;
        }
        if (bitmap.isRecycled()) {
            return null;
        }

        //byte换算kb
        double currentSize = tools.BitmapToByteArray(bitmap, false).length / 1024;

        //若大于最大大小则压缩
        while (currentSize > maxSize) {
            double multiple = currentSize / maxSize;
            bitmap = getZoomImage(bitmap, bitmap.getWidth() / Math.sqrt(multiple), bitmap.getHeight() / Math.sqrt(multiple));
            currentSize = tools.BitmapToByteArray(bitmap, false).length / 1024;
        }
        return bitmap;
    }

    //按比例重绘bitmap
    public static Bitmap getZoomImage(Bitmap orgBitmap, double newWidth, double newHeight) {
        if (null == orgBitmap) {
            return null;
        }
        if (orgBitmap.isRecycled()) {
            return null;
        }
        if (newWidth <= 0 || newHeight <= 0) {
            return null;
        }

        // 获取图片的宽和高
        float width = orgBitmap.getWidth();
        float height = orgBitmap.getHeight();

        // 创建操作图片的matrix对象
        Matrix matrix = new Matrix();

        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(orgBitmap, 0, 0, (int) width, (int) height, matrix, true);
        return bitmap;
    }
}