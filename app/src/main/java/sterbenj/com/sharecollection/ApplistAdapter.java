package sterbenj.com.sharecollection;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * XJB Created by 野良人 on 2018/6/10.
 */
public class ApplistAdapter extends RecyclerView.Adapter<ApplistAdapter.ViewHolder> {

    private Context mContext;
    private List<mApp> mAppList = new ArrayList<>();
    private  returnAppInfo MyreturnAppInfo;

    public ApplistAdapter(List<mApp> mAppList){
        this.mAppList.clear();
        this.mAppList = mAppList;
    }

    public void setMyreturnAppInfo(returnAppInfo myreturnAppInfo) {
        this.MyreturnAppInfo = myreturnAppInfo;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_applist, null, true);
        final ViewHolder holder = new ViewHolder(view);

        switch (BaseActivity.sTheme){
            case R.style.white_transStat:
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardview_light_background));
                break;
            case R.style.Dark:
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardview_dark2));
                break;
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appName;
                byte[] bytes;
                String packageName = holder.packageName;
                List<mApp> temp = LitePal.where("PackageName = ?", packageName).find(mApp.class);
                appName = temp.get(0).getName();
                bytes = temp.get(0).getIcon();
                if ( LitePal.select("PackageName").where("PackageName = ?", packageName).find(Category.class).size() == 0) {
                    Intent intent = new Intent();
                    intent.putExtra("appName", appName);
                    intent.putExtra("appIconByte", bytes);
                    intent.putExtra("packageName", packageName);
                    MyreturnAppInfo.onAppClick(intent);
                }
                else{
                    Toast.makeText(mContext, "已有该分类", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(mAppList.get(position));
    }

    @Override
    public int getItemCount() {
        return mAppList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView appIcon;
        TextView appName;
        String packageName;
        public ViewHolder(View itemView){
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view_applist);
            appIcon = (ImageView)itemView.findViewById(R.id.appIcon);
            appName = (TextView)itemView.findViewById(R.id.appName);
        }
        public void setData(mApp app){
            appName.setText(app.getName());
            appIcon.setImageDrawable(tools.ByteArrayToDrawable(app.getIcon()));
            packageName = app.getPackageName();
        }
    }

    public interface returnAppInfo{
        void onAppClick(Intent intent);
    }
}
