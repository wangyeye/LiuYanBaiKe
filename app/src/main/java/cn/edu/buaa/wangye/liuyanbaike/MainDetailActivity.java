package cn.edu.buaa.wangye.liuyanbaike;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class MainDetailActivity extends ActionBarActivity {

    private String url;
    private String title;
    private String con;
    private TextView content;
    private String contentText;
    private SwipeRefreshLayout swipeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_detail);
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        con = getIntent().getStringExtra("con");
        setTitle("["+con+"]"+title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);
        content = (TextView)findViewById(R.id.textView);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadDataThread();
                    }
                }, 0);
            }
        });

        loadDataThread();
    }

    Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            content.setText(Html.fromHtml(contentText));
            swipeView.setRefreshing(false);
            super.handleMessage(msg);
        }
    };

    private void loadDataThread(){
        swipeView.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setContent();
                myHandler.sendEmptyMessage(0);
            }
        }).start();
    }


    public void setContent() {
        Document doc;
        try {
            doc = Jsoup.connect(url).timeout(60000).get();
            Element rumorTitle = doc.getElementsByClass("rumor-title").first();
            Element rumorDesc = doc.getElementsByClass("rumor-desc").first();
            Element rumorTruth = doc.getElementsByClass("rumor-truth").first();
            Element rumorContent = doc.getElementsByClass("rumor-content").first();
            contentText = rumorTitle.toString()
                    +rumorDesc.toString()
                    +rumorTruth.toString()
                    +rumorContent.toString();
            System.out.println(contentText);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
