package cn.edu.buaa.wangye.liuyanbaike;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private List<FeedItem> feedList;
    private int page = 1;
    private ListView listView;
    private ListViewAdapter adapter;
    private SwipeRefreshLayout swipeView;
    private String url;
    private boolean isSearching = false;
    private String keyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("流言百科");
        feedList = new ArrayList<>();
        listView = (ListView)findViewById(R.id.listView);
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);
        adapter = new ListViewAdapter(this, feedList);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lastItemIndex;//当前ListView中最后一个Item的索引

            //当ListView不在滚动，并且ListView的最后一项的索引等于adapter的项数减一时则自动加载（因为索引是从0开始的）
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        && lastItemIndex == adapter.getCount() - 1) {
                    //加载数据代码，此处省略了
                    page++;
                    loadDataThread();

                }
            }

            //这三个int类型的参数可以自行Log打印一下就知道是什么意思了
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                //ListView 的FooterView也会算到visibleItemCount中去，所以要再减去一
                lastItemIndex = firstVisibleItem + visibleItemCount - 1;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent();
                i.setClass(MainActivity.this, MainDetailActivity.class);
                i.putExtra("url", feedList.get(position).getUrl());
                i.putExtra("title", feedList.get(position).getTitle());
                i.putExtra("con", feedList.get(position).getCon());
                startActivity(i);

            }
        });
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        page = 1;
                        refreshDataThread();
                    }
                }, 0);
            }
        });
        loadDataThread();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        //SearchView searchView = (SearchView) searchItem.getActionView();
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO : query is the text from the search view
                // after you clicked search
                System.out.println(query);
                keyword = query;
                isSearching = true;
                page = 1;
                refreshDataThread();
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO : newText is the text from the search view
                // (event triggered every time the text changes)
                // search(newText);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                page = 1;
                setTitle("流言百科");
                isSearching = false;
                refreshDataThread();
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTitle("");
            }
        });
        return true;
    }




    Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            System.out.println("myHandler");
            adapter.notifyDataSetChanged();
            swipeView.setRefreshing(false);
            super.handleMessage(msg);
        }
    };

    private void loadDataThread(){
        swipeView.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(isSearching) {
                    feedList.addAll(loadSearchData(page, keyword));
                }else{
                    feedList.addAll(loadDefaultData(page));
                }
                myHandler.sendEmptyMessage(0);
            }
        }).start();
    }
    private void refreshDataThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                feedList.clear();
                if(isSearching){
                    feedList.addAll(loadSearchData(page, keyword));
                }else{
                    feedList.addAll(loadDefaultData(page));
                }


                myHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    private List<FeedItem> loadDefaultData(int page){
        List<FeedItem> feedList = new ArrayList<>();
        String rs = null;
        url = "http://www.liuyanbaike.com/category/?page="+page;

        Document doc;
        try {
            doc = Jsoup.connect(url).timeout(60000).get();
            Elements elements = doc.getElementsByClass("rumor_list").first().getElementsByTag("li");
            for (Element element : elements){
                //System.out.println(element);
                FeedItem item = new FeedItem();
                item.setCon(element.child(0).text());
                item.setTitle(element.child(1).text());
                item.setUrl("http://www.liuyanbaike.com" + element.child(1).child(0).attr("href"));
                //System.out.println(element.child(0).text());
                System.out.println(element.child(1).text());
                //System.out.println(element.child(1).child(0).attr("href"));
                feedList.add(item);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return feedList;
    }
    private List<FeedItem> loadSearchData(int page, String keyword){
        List<FeedItem> feedList = new ArrayList<>();
        String rs = null;
        url = "http://www.liuyanbaike.com/search/?page="+page+"&wd="+ URLEncoder.encode(keyword);

        Document doc;
        try {
            doc = Jsoup.connect(url).timeout(60000).get();
            Elements elements = doc.getElementsByClass("title");
            for (Element element : elements){
                //System.out.println(element);
                FeedItem item = new FeedItem();
                item.setCon(element.ownText().replace("]","").replace("[", ""));
                item.setTitle(element.child(0).text());
                item.setUrl("http://www.liuyanbaike.com"+element.child(0).attr("href"));
                System.out.println(element.ownText().replace("]","").replace("[",""));
                System.out.println(element.child(0).text());
                System.out.println("http://www.liuyanbaike.com"+element.child(0).attr("href"));
                feedList.add(item);
            }



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return feedList;
    }
}