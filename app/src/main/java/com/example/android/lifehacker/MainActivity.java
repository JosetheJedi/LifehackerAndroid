package com.example.android.lifehacker;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    private String url = "http://lifehacker.com/";
    private ArrayList<Entry> entries;
    private String nextPage = "";
    private int entryNumber = 0;
    private Stack<String> lastUrl = null;

    private TextView summary = null;
    private TextView header = null;
    private Button next = null;
    private Button last = null;
    private Button open = null;
    private Uri uriUrl = null;
    private Intent launchBrowser = null;
    private WebView web = null;
    private String imgUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        summary = (TextView)findViewById(R.id.summaryT);
        summary.setMovementMethod(new ScrollingMovementMethod());
        header = (TextView)findViewById(R.id.headerT);
        next = (Button)findViewById(R.id.nextB);
        last = (Button)findViewById(R.id.lastB);
        open = (Button)findViewById(R.id.openB);
        web = (WebView)findViewById(R.id.WebView1);
        lastUrl = new Stack<>();
        entryNumber = 0;


        WebSettings settings = web.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        new Scrape().execute();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(entryNumber<19)
                {
                    entryNumber++;
                    header.setText(entries.get(entryNumber).getHeader());
                    summary.setText(entries.get(entryNumber).getSummary());
                    imgUrl = entries.get(entryNumber).getImgURL();
                    web.loadUrl(imgUrl);
                }
                else if(entryNumber == 19)
                {
                    lastUrl.push(url);
                    url = getNextLink();
                    entryNumber = 0;
                    new Scrape().execute();
                }
            }
        });

        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(entryNumber>0)
                {
                    entryNumber--;
                    header.setText(entries.get(entryNumber).getHeader());
                    summary.setText(entries.get(entryNumber).getSummary());
                    imgUrl = entries.get(entryNumber).getImgURL();
                    web.loadUrl(imgUrl);
                }
                else if(!lastUrl.isEmpty() && entryNumber == 0)
                {
                    url = lastUrl.pop();
                    entryNumber = 19;
                    new Scrape().execute();
                }
            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uriUrl = Uri.parse(entries.get(entryNumber).getLink());
                launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                Toast.makeText(MainActivity.this, "Opening Article", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public String getNextLink()
    {
        String baseURL = "http://lifehacker.com/";
        url = baseURL + nextPage;
        return url;
    }


    public class Scrape extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            Document doc;
            Elements media, links;
            Entry en;
            int size;
            try
            {
                doc = Jsoup.connect(url).get();

                media = doc.select("article.postlist__item");
                size = media.size();
                entries = new ArrayList<>(size);


                // to get the next lifehacker page
                links = doc.select("div.load-more__button");

                nextPage = links.select("a").attr("href");

                for(int i = 0; i < size; i++)
                {
                    en = new Entry();
                    en.setHeader(media.get(i).select(".headline").text());
                    en.setSummary(media.get(i).getElementsByTag("p").text());
                    en.setDatePosted(media.get(i).select(".updated").text());
                    en.setLink(media.get(i).select("h1 a[href]").attr("abs:href"));
                    en.setImgURL(media.get(i).getElementsByTag("source").attr("data-srcset"));

                    entries.add(en);
                }
            }
            catch(IOException e)
            {
                System.out.println("nothing to show");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            header.setText(entries.get(entryNumber).getHeader());
            summary.setText(entries.get(entryNumber).getSummary());
            //****** OPEN IMAGE ******//

            imgUrl = entries.get(entryNumber).getImgURL();
            web.loadUrl(imgUrl);
        }
    }
}
