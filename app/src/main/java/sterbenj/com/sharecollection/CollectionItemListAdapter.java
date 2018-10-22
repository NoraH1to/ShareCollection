package sterbenj.com.sharecollection;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * XJB Created by 野良人 on 2018/6/13.
 */
public class CollectionItemListAdapter extends RecyclerView.Adapter<CollectionItemListAdapter.ViewHolder> {

    private List<CollectionItem> collectionItemList = new ArrayList<>();
    private Context mContext;
    private JumpOutRoad MyJumpOutRoad;

    //构造函数
    public CollectionItemListAdapter(List<CollectionItem> collectionItemList){
        this.collectionItemList.clear();
        this.collectionItemList = collectionItemList;
    }

    //接口构造
    public void setMyJumpOutRoad(JumpOutRoad MyJumpOutRoad) {
        this.MyJumpOutRoad = MyJumpOutRoad;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageButton more;
        TextView title;
        View line;
        TextView context;
        String uri;
        String ParentPackageName;
        CheckBox checkBox;
        AppCompatImageView appCompatImageView;
        PopupMenu popupMenu;

        public ViewHolder(View itemView){
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.card_view_collectionitemlist);
            more = (ImageButton) itemView.findViewById(R.id.card_view_collectionitemlist_more);
            title = (TextView)itemView.findViewById(R.id.collectionlistitem_title);
            line = (View)itemView.findViewById(R.id.collectionlistitem_line);
            context = (TextView)itemView.findViewById(R.id.collectionlistitem_context);
            checkBox = (CheckBox)itemView.findViewById(R.id.collectionlistitem_checkbox);
            appCompatImageView = (AppCompatImageView)itemView.findViewById(R.id.collectionlistitem_image);
        }

        public void setData(CollectionItem collectionItem, int position){
            Set<Integer> positionSet = CollectionItemListActivity.instance.positionSet;
            if (positionSet.contains(position)) {
                this.checkBox.setVisibility(View.VISIBLE);
                this.checkBox.setChecked(true);
            } else {
                this.checkBox.setVisibility(View.GONE);
                this.checkBox.setChecked(false);
            }
            this.checkBox.setClickable(false);
            title.setText(collectionItem.getTitle());
            context.setText(collectionItem.getContext());
            uri = collectionItem.getmUri();
            ParentPackageName = collectionItem.getParentCategory();
            if (collectionItem.getImage() != null){
                appCompatImageView.setImageDrawable(tools.ByteArrayToDrawable(collectionItem.getImage()));
            }
            if (CollectionItemListActivity.instance.actionMode != null){
                this.more.setVisibility(View.GONE);
            }else{
                this.more.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        Log.d("+++++++++CLAD", "getItemCount: ");
        return collectionItemList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("+++++++++CLAD", "onCreateViewHolder: ");
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyler_collectionitemlist, null, true);
        final ViewHolder holder = new ViewHolder(view);

        //设置cardView颜色
        switch (BaseActivity.sTheme){
            case R.style.white_transStat:
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardview_light_background));
                holder.title.setTextColor(ContextCompat.getColor(mContext, android.R.color.tertiary_text_light));
                holder.line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.Line_Light));
                holder.context.setTextColor(ContextCompat.getColor(mContext, android.R.color.tertiary_text_light));
                break;
            case R.style.Dark:
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardview_dark2));
                holder.title.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent_second));
                holder.line.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.tertiary_text_dark));
                holder.context.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent_second));
                break;
        }

        holder.checkBox.setVisibility(View.GONE);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d("+++++++++CLAD", "onBindViewHolder: ");
        holder.setData(collectionItemList.get(position), position);

        //卡片点击跳转
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(holder.uri);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                //有网路正常操作，没网络离线页面
                if (tools.NetWork){
                    MyJumpOutRoad.JumpOut(intent, position);
                }
                else {
                    MyJumpOutRoad.JumpOfflineWeb(holder.uri, position);
                }
            }
        });

        //卡片长按多选删除
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MyJumpOutRoad.onCollectionitemLongClick(v, position);
                return true;
            }
        });

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyJumpOutRoad.OpenMenu(position, holder.more, collectionItemList.get(position).getTitle()
                        + "\n" + collectionItemList.get(position).getmUri(), holder.uri, collectionItemList.get(position), holder.checkBox);
            }
        });
    }

    public interface JumpOutRoad{
        void JumpOut(Intent intent, int position);
        void onCollectionitemLongClick(View v, int position);
        void JumpOfflineWeb(String uri, int position);
        void OpenMenu(final int position, ImageView view, final String data, final String uri, final CollectionItem collectionItem, CheckBox checkBox);
    }

    public List<CollectionItem> getCollectionItemList(){
        return this.collectionItemList;
    }
}