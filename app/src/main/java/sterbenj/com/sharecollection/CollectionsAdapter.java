package sterbenj.com.sharecollection;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * XJB Created by 野良人 on 2018/5/25.
 */
public class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.ViewHolder>{
    
    public static final String TAG = "CollectionsAdapter";

    public static Boolean needReflash;
    private List<Category> categoryList;
    private  Context mContext;
    public final static int NAME = 1;
    private MyClickListener mListener;

    public CollectionsAdapter(List<Category> categoryList){
        this.categoryList = categoryList;
    }

    public void setMyClickListener(MyClickListener mListener){
        this.mListener = mListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list, null, true);
        final ViewHolder holder = new ViewHolder(view);

            //设置cardView颜色
        switch (BaseActivity.sTheme){
                case R.style.white_transStat:
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardview_light_background));
                    holder.line.setBackgroundColor(ContextCompat.getColor(mContext, R.color.Line_Light));
                    holder.title.setTextColor(ContextCompat.getColor(mContext, android.R.color.tertiary_text_light));
                    break;
                case R.style.Dark:
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardview_dark2));
                    holder.line.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.tertiary_text_dark));
                    holder.title.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent_second));
                    break;
            }

        holder.cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Log.d(TAG, "onClick: ");
                    int position = holder.getAdapterPosition();
                    Category category1 = categoryList.get(position);
                    mListener.ClickListener(v, position, mContext, category1, NAME, holder.checkBox);
                }
            });

        holder.cardView.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                Log.d(TAG, "onLongClick: ");
                int position = holder.getAdapterPosition();
                mListener.longClickListener(v, position);
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(CollectionsAdapter.ViewHolder holder, int position){
        Log.d(TAG, "onBindViewHolder: ");
        holder.setData(categoryList.get(position), position);
    }

    @Override
    public int getItemCount(){
        Log.d(TAG, "getItemCount: ");
        return categoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView title;
        ImageView imageView;
        CheckBox checkBox;
        View line;
        public ViewHolder(View itemView){
            super(itemView);
            line= (View)itemView.findViewById(R.id.main_line);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            title = (TextView)itemView.findViewById(R.id.main_appName);
            imageView = (ImageView)itemView.findViewById(R.id.main_appIcon);
            checkBox = (CheckBox)itemView.findViewById(R.id.main_appCheckbox);
        }
        public void setData(Category category, int position){
            Log.d(TAG, "setData: ");
            Set<Integer> positionSet = MainActivity.instance.positionSet;
            if (positionSet.contains(position)) {
                this.checkBox.setVisibility(View.VISIBLE);
                this.checkBox.setChecked(true);
            } else {
                this.checkBox.setVisibility(View.GONE);
                this.checkBox.setChecked(false);
            }
            this.checkBox.setClickable(false);
            this.title.setText(category.getTitle());
            this.imageView.setImageDrawable(tools.ByteArrayToDrawable(category.getIcon()));
        }
    }

    public interface MyClickListener{
        void longClickListener(View v, int position);
        void ClickListener(View v, int position, Context mContext, Category category1, int NAME, CheckBox checkBox);
    }

    public List<Category> getCategoryList(){
        return this.categoryList;
    }

}
