package sterbenj.com.sharecollection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * XJB Created by 野良人 on 2018/6/10.
 */
public class ApplistActivity extends BaseActivity{

    public static ApplistActivity applistactivity;
    private ApplistAdapter applistAdapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private List<mApp> mAppList = new ArrayList<>();
    public static boolean HasAddApp = false;

    //读取应用列表的子线程
    class LoadApp extends AsyncTask<Void, Integer, Void>{

        private ProgressDialog progressDialog;
        private  List<ResolveInfo> resolveInfoList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            resolveInfoList = getRES();
            progressDialog = new ProgressDialog(ApplistActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("正在刷新应用列表");
            progressDialog.setMax(resolveInfoList.size());
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PackageManager packageManager = getPackageManager();
            LitePal.deleteAll(mApp.class);
            for (int i = 0; i < resolveInfoList.size(); i++){
                ResolveInfo resolveInfo = resolveInfoList.get(i);
                mApp app = new mApp(resolveInfo.activityInfo.loadLabel(packageManager).toString(), resolveInfo.activityInfo.packageName, tools.DrawableToByteArray(resolveInfo.activityInfo.loadIcon(packageManager)));
                app.save();
                publishProgress(i + 1);
            }
            HasAddApp = true;
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
            mAppList.clear();
            List<mApp> newList = LitePal.order("Name asc").find(mApp.class);
            mAppList.addAll(newList);
            progressDialog.dismiss();
            applistAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        //每次打开应用时刷新应用列表数据库
        if (!HasAddApp){
            new LoadApp().execute();
        }

        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist);
        applistactivity = this;

        //从数据库中读取应用列表
        mAppList.clear();
        List<mApp> newList = LitePal.order("Name asc").find(mApp.class);
        mAppList.addAll(newList);

        //初始化列表适配器
        applistAdapter = new ApplistAdapter(mAppList);
        applistAdapter.setMyreturnAppInfo(new ApplistAdapter.returnAppInfo() {
            @Override
            public void onAppClick(Intent intent) {
                Category category = new Category(intent.getStringExtra("appName"), intent.getByteArrayExtra("appIconByte"), intent.getStringExtra("packageName"));
                category.save();
                finish();
            }
        });

        //创建布局管理对象
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        //初始化列表
        recyclerView = (RecyclerView)findViewById(R.id.recy_applist);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(applistAdapter);

        //初始化toolbar
        toolbar = (Toolbar)findViewById(R.id.toolbar_applist);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        //加载布局
        getMenuInflater().inflate(R.menu.applist_menu, menu);

        //初始化搜索按钮
        MenuItem item = menu.findItem(R.id.applist_menu_search);
        SearchView searchView = (SearchView) item.getActionView();

        //搜索框提示
        searchView.setQueryHint("请输入App名称");

        //默认关闭搜索
        searchView.setIconified(true);

        //显示提交按钮
        searchView.setSubmitButtonEnabled(true);

        //搜索内容监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(String query) {

                //列表刷新成搜索内容
                mAppList.clear();
                List<mApp> newList = LitePal.order("Name asc").where("Name LIKE ?", "%"+query+"%").find(mApp.class);
                mAppList.addAll(newList);
                applistAdapter.notifyDataSetChanged();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //列表刷新成搜索内容
                mAppList.clear();
                List<mApp> newList = LitePal.order("Name asc").where("Name LIKE ?", "%"+newText+"%").find(mApp.class);
                mAppList.addAll(newList);
                applistAdapter.notifyDataSetChanged();

                return false;
            }
        });

        //关闭搜索监听
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                //恢复列表
                mAppList.clear();
                List<mApp> newList = LitePal.order("Name asc").find(mApp.class);
                mAppList.addAll(newList);
                applistAdapter.notifyDataSetChanged();

                return false;
            }
        });


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return false;
    }

    //获取全部应用信息包
    public List<ResolveInfo> getRES() {
        PackageManager manager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allAppList = manager.queryIntentActivities(intent, 0);
        return allAppList;
    }


    //获取包管理器
    public PackageManager getMyPackageManager() {
        PackageManager manager = getPackageManager();
        return manager;
    }


}
