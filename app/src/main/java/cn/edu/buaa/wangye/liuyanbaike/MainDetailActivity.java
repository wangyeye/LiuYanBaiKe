package cn.edu.buaa.wangye.liuyanbaike;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_detail);
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        con = getIntent().getStringExtra("con");
        setTitle("["+con+"]"+title);
        content = (TextView)findViewById(R.id.textView);
        loadDataThread();
    }

    Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            content.setText(Html.fromHtml(contentText));
            super.handleMessage(msg);
        }
    };

    private void loadDataThread(){
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

}
