package cn.edu.buaa.wangye.liuyanbaike;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Wang on 2014/12/31.
 */
public class ListViewAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<FeedItem> feedItemList;

    public ListViewAdapter(Context ctx, List<FeedItem> list){
        this.mContext = ctx;
        this.feedItemList = list;
        this.mInflater = LayoutInflater.from(ctx);
    }


    @Override
    public int getCount() {
        return feedItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return feedItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {

            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.list_item, null);
            holder.title = (TextView) convertView
                    .findViewById(R.id.list_item_title);
            holder.con = (TextView) convertView
                    .findViewById(R.id.list_item_con);
            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        FeedItem item = feedItemList.get(position);

        holder.title.setText(item.getTitle());
        holder.con.setText(item.getCon());
        return convertView;
    }



    class ViewHolder {
        public TextView title;
        public TextView con;
    }
}
