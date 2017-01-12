package com.example.android.lifehacker;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.type.Note;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
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
    private WebView web = null;
    private String imgUrl = "";
    private String articleURL = "";
    private String articleHeader = "";
    private StringBuffer articleText = null;

    /****
     * for evernote
     ***/

    private static final String CONSUMER_KEY = "josefhu-15";
    private static final String CONSUMER_SECRET = "7420ea22e43c1583";
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    private EvernoteSession m = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setting up Evernote Session
        m = new EvernoteSession.Builder(this)
                .setEvernoteService(EVERNOTE_SERVICE)
                .setSupportAppLinkedNotebooks(true)
                .build(CONSUMER_KEY, CONSUMER_SECRET)
                .asSingleton();

        summary = (TextView) findViewById(R.id.summaryT);
        summary.setMovementMethod(new ScrollingMovementMethod());
        header = (TextView) findViewById(R.id.headerT);
        next = (Button) findViewById(R.id.nextB);
        last = (Button) findViewById(R.id.lastB);
        web = (WebView) findViewById(R.id.WebView1);
        lastUrl = new Stack<>();
        entryNumber = 0;

        WebSettings settings = web.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        new Scrape().execute();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (entryNumber < 19) {
                    entryNumber++;
                    articleHeader = entries.get(entryNumber).getHeader();
                    header.setText(articleHeader);
                    summary.setText(entries.get(entryNumber).getSummary());
                    imgUrl = entries.get(entryNumber).getImgURL();
                    web.loadUrl(imgUrl);
                } else if (entryNumber == 19) {
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
                if (entryNumber > 0) {
                    entryNumber--;
                    articleHeader = entries.get(entryNumber).getHeader();
                    header.setText(articleHeader);
                    summary.setText(entries.get(entryNumber).getSummary());
                    imgUrl = entries.get(entryNumber).getImgURL();
                    web.loadUrl(imgUrl);
                } else if (!lastUrl.isEmpty() && entryNumber == 0) {
                    url = lastUrl.pop();
                    entryNumber = 19;
                    new Scrape().execute();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Uri uriUrl;
        Intent launchBrowser;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_open:
                uriUrl = Uri.parse(entries.get(entryNumber).getLink());
                launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                Toast.makeText(MainActivity.this, "Opening Article", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_evernote:
                if (!EvernoteSession.getInstance().isLoggedIn()) {
                    m.authenticate(MainActivity.this);
                } else {
                    articleURL = entries.get(entryNumber).getLink();

                    new ScrapePage().execute();
                    Toast.makeText(MainActivity.this, "Saving...", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getNextLink() {
        String baseURL = "http://lifehacker.com/";
        url = baseURL + nextPage;
        return url;
    }

    public class ScrapePage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Document doc;
            Elements media, media2;
            int size;
            articleText = new StringBuffer();

            try {
                doc = Jsoup.connect(articleURL).get();

                media = doc.select("article.post");
                size = media.size();
                media2 = media.get(size - 1).getElementsByTag("p");
                String articleLink = media.get(size - 1).select("h1 a[href]").attr("abs:href");

                articleText.append("<p>");
                int msize = media2.size();

                for (int i = 0; i < msize; i++) {
                    String str = media2.get(i).text();
                    String link = media2.get(i).select("p a[href]").attr("abs:href");

                    String pText = media2.get(i).select("p a[href]").text().toString();
                    String absLink = "<a href=\"" + media2.get(i).select("p a[href]").attr("abs:href").toString() + "\">" + pText + "</a>";


                    if (str.contains(pText) && !pText.equals("") && !absLink.isEmpty() && !pText.isEmpty()) {
                        str = str.replace(pText, absLink);
                    }
                    if (str.contains("&")) {
                        str = str.replace("&", "&amp;");
                    }
                    if (link.contains("&")) {
                        link = link.replace("&", "&amp;");
                    }
                    if (str.contains("Read more Read more")) {
                        str = str.replace("Read more Read more", "Read more");
                    }

                    if (str.contains("|") && !link.isEmpty()) {
                        articleText.append("<a href=\"" + link + "\">" + str + "</a>");
                    } else {
                        if (!str.equalsIgnoreCase("Advertisement") && !str.equalsIgnoreCase("Sponsored") && !str.isEmpty()) {
                            articleText.append("<p>" + str + "</p>\n");
                        }
                    }
                }

                articleText.append("\n\n<i><small>Original article: <a href=\"" + articleLink + "\">" + articleLink + "</a></small></i></p>");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();

            Note note = new Note();
            note.setTitle(articleHeader);
            note.setContent(EvernoteUtil.NOTE_PREFIX + articleText + EvernoteUtil.NOTE_SUFFIX);

            noteStoreClient.createNoteAsync(note, new EvernoteCallback<Note>() {
                @Override
                public void onSuccess(Note result) {
                    Toast.makeText(MainActivity.this, "Article Saved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onException(Exception exception) {
                    //Log.e(LOGTAG, "Error creating note", exception);
                    Toast.makeText(MainActivity.this, "Could not save :(", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }


    public class Scrape extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Document doc;
            Elements media, links;
            Entry en;
            int size;
            try {
                doc = Jsoup.connect(url).get();

                media = doc.select("article.postlist__item");
                size = media.size();
                entries = new ArrayList<>(size);


                // to get the next lifehacker page
                links = doc.select("div.load-more__button");

                nextPage = links.select("a").attr("href");

                for (int i = 0; i < size; i++) {
                    en = new Entry();
                    en.setHeader(media.get(i).select(".headline").text());
                    en.setSummary(media.get(i).getElementsByTag("p").text());
                    en.setDatePosted(media.get(i).select(".updated").text());
                    en.setLink(media.get(i).select("h1 a[href]").attr("abs:href"));
                    en.setImgURL(media.get(i).getElementsByTag("source").attr("data-srcset"));

                    entries.add(en);
                }
            } catch (IOException e) {
                System.out.println("nothing to show");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            articleHeader = entries.get(entryNumber).getHeader();
            header.setText(articleHeader);
            summary.setText(entries.get(entryNumber).getSummary());
            //****** OPEN IMAGE ******//
            imgUrl = entries.get(entryNumber).getImgURL();
            web.loadUrl(imgUrl);
        }
    }
}
