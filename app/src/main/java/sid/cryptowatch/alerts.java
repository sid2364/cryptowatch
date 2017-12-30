package sid.cryptowatch;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class alerts extends AppCompatActivity {

    ListView listViewActiveAlerts;
    FloatingActionButton fabReload, fab, fabShowAlerts;
    HashMap<String, String> prices;
    List<HashMap<String, String>> listItems;
    SimpleAdapter adapter;
    static JSONObject jsonObject = null;
    static KoinexJSONTicker koinexJSONTicker;
    String jsonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        listViewActiveAlerts = findViewById( R.id.listViewPrices);
        koinexJSONTicker = new KoinexJSONTicker();
        setSupportActionBar(toolbar);

        prices = new HashMap<>();
        listItems = new ArrayList<>();
        adapter = new SimpleAdapter(getApplicationContext(), listItems, R.layout.list_item,
                new String[]{"Currency", "Price"},
                new int[]{R.id.textHeader, R.id.textSub});
        listViewActiveAlerts.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent k = new Intent(getApplicationContext(), add_alert.class);
                finish();
                startActivity(k);

            }
        });
        fabReload = (FloatingActionButton) findViewById(R.id.fabReload);
        fabReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callRefresh();
                adapter.notifyDataSetChanged();
            }
        });

        fabShowAlerts = (FloatingActionButton) findViewById(R.id.fabShowAlerts);
        fabShowAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent k = new Intent(getApplicationContext(), add_alert.class);
                finish();
                startActivity(k);

            }
        });

        RetrieveDataTask backgroundStuff = new RetrieveDataTask(this);
        backgroundStuff.execute("https://koinex.in/api/ticker");
        //new RetrieveDataTask().execute("https://koinex.in/api/ticker");
        SimpleDatabaseHelper db = new SimpleDatabaseHelper(getApplicationContext());

    }
    public void callRefresh() {
        try{
            refreshPriceList(prices, listItems, adapter);
        } catch (IOException e) {
            Toast.makeText(this.getApplicationContext(), "There might be a problem with your connection.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (JSONException e) {
            Toast.makeText(alerts.this, "There was a problem in parsing the JSON.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch(Exception e) {
            Toast.makeText(alerts.this, "There was a problem.",
                    Toast.LENGTH_LONG);
        }
    }
    public KoinexJSONTicker decodeToParsable() throws JSONException{
        JSONObject stats = jsonObject.getJSONObject("stats");
        JSONObject prices = jsonObject.getJSONObject("prices");
        JSONObject currencyStatsBTC = stats.getJSONObject("BTC");
        JSONObject currencyStatsETH = stats.getJSONObject("ETH");
        JSONObject currencyStatsXRP = stats.getJSONObject("XRP");
        JSONObject currencyStatsLTC = stats.getJSONObject("LTC");
        JSONObject currencyStatsBCH = stats.getJSONObject("BCH");
        KoinexJSONTicker koinexJSONTicker = new KoinexJSONTicker();
        koinexJSONTicker.prices.BTC = Float.parseFloat(prices.getString("BTC"));
        koinexJSONTicker.prices.ETH = Float.parseFloat(prices.getString("ETH"));
        koinexJSONTicker.prices.XRP = Float.parseFloat(prices.getString("XRP"));
        koinexJSONTicker.prices.BCH = Float.parseFloat(prices.getString("BCH"));
        koinexJSONTicker.prices.LTC = Float.parseFloat(prices.getString("LTC"));
        koinexJSONTicker.stats.BCH.last_traded_price = Float.parseFloat(currencyStatsBCH.getString("last_traded_price"));
        koinexJSONTicker.stats.BTC.last_traded_price = Float.parseFloat(currencyStatsBTC.getString("last_traded_price"));
        koinexJSONTicker.stats.ETH.last_traded_price = Float.parseFloat(currencyStatsETH.getString("last_traded_price"));
        koinexJSONTicker.stats.XRP.last_traded_price = Float.parseFloat(currencyStatsXRP.getString("last_traded_price"));
        koinexJSONTicker.stats.LTC.last_traded_price = Float.parseFloat(currencyStatsLTC.getString("last_traded_price"));
        koinexJSONTicker.stats.BCH.lowest_ask = Float.parseFloat(currencyStatsBCH.getString("lowest_ask"));
        koinexJSONTicker.stats.BTC.lowest_ask = Float.parseFloat(currencyStatsBTC.getString("lowest_ask"));
        koinexJSONTicker.stats.ETH.lowest_ask = Float.parseFloat(currencyStatsETH.getString("lowest_ask"));
        koinexJSONTicker.stats.XRP.lowest_ask = Float.parseFloat(currencyStatsXRP.getString("lowest_ask"));
        koinexJSONTicker.stats.LTC.lowest_ask = Float.parseFloat(currencyStatsLTC.getString("lowest_ask"));
        koinexJSONTicker.stats.BCH.highest_bid = Float.parseFloat(currencyStatsBCH.getString("highest_bid"));
        koinexJSONTicker.stats.BTC.highest_bid = Float.parseFloat(currencyStatsBTC.getString("highest_bid"));
        koinexJSONTicker.stats.ETH.highest_bid = Float.parseFloat(currencyStatsETH.getString("highest_bid"));
        koinexJSONTicker.stats.XRP.highest_bid = Float.parseFloat(currencyStatsXRP.getString("highest_bid"));
        koinexJSONTicker.stats.LTC.highest_bid = Float.parseFloat(currencyStatsLTC.getString("highest_bid"));
        koinexJSONTicker.stats.BCH.max_24hrs = Float.parseFloat(currencyStatsBCH.getString("max_24hrs"));
        koinexJSONTicker.stats.BTC.max_24hrs = Float.parseFloat(currencyStatsBTC.getString("max_24hrs"));
        koinexJSONTicker.stats.ETH.max_24hrs = Float.parseFloat(currencyStatsETH.getString("max_24hrs"));
        koinexJSONTicker.stats.XRP.max_24hrs = Float.parseFloat(currencyStatsXRP.getString("max_24hrs"));
        koinexJSONTicker.stats.LTC.max_24hrs = Float.parseFloat(currencyStatsLTC.getString("max_24hrs"));
        koinexJSONTicker.stats.BCH.min_24hrs = Float.parseFloat(currencyStatsBCH.getString("min_24hrs"));
        koinexJSONTicker.stats.BTC.min_24hrs = Float.parseFloat(currencyStatsBTC.getString("min_24hrs"));
        koinexJSONTicker.stats.ETH.min_24hrs = Float.parseFloat(currencyStatsETH.getString("min_24hrs"));
        koinexJSONTicker.stats.XRP.min_24hrs = Float.parseFloat(currencyStatsXRP.getString("min_24hrs"));
        koinexJSONTicker.stats.LTC.min_24hrs = Float.parseFloat(currencyStatsLTC.getString("min_24hrs"));
        koinexJSONTicker.stats.BCH.vol_24hrs = Float.parseFloat(currencyStatsBCH.getString("vol_24hrs"));
        koinexJSONTicker.stats.BTC.vol_24hrs = Float.parseFloat(currencyStatsBTC.getString("vol_24hrs"));
        koinexJSONTicker.stats.ETH.vol_24hrs = Float.parseFloat(currencyStatsETH.getString("vol_24hrs"));
        koinexJSONTicker.stats.XRP.vol_24hrs = Float.parseFloat(currencyStatsXRP.getString("vol_24hrs"));
        koinexJSONTicker.stats.LTC.vol_24hrs = Float.parseFloat(currencyStatsLTC.getString("vol_24hrs"));

        return koinexJSONTicker;
    }
    /*

     */

    public void getData() throws IOException, JSONException, ExecutionException, InterruptedException {
        if(this.jsonText == null) {
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.coordinatorLayout),
                    "No internet connection!", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
            throw new IOException(); /* No internet connection? */
        }
        jsonObject = new JSONObject(this.jsonText);
    }

    public void refreshPriceList(HashMap<String, String> prices, List<HashMap<String, String>>listItems, final SimpleAdapter adapter) throws IOException, JSONException {
        Snackbar.make(findViewById(R.id.coordinatorLayout),
                "Loading data, please wait!", Snackbar.LENGTH_SHORT).show();
        try{
            getData();
        }catch(IOException e){
            Toast.makeText(alerts.this, "You don't have an active internet connection.", Toast.LENGTH_LONG);
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Snackbar.make(findViewById(R.id.coordinatorLayout),
                "Latest prices!", Snackbar.LENGTH_SHORT).show();

        koinexJSONTicker = decodeToParsable();
        System.out.println("Reached here with " + koinexJSONTicker.prices.BTC);
        prices.put("Bitcoin", koinexJSONTicker.prices.BTC+"");
        prices.put("Ether", koinexJSONTicker.prices.ETH+"");
        prices.put("Ripple", koinexJSONTicker.prices.XRP+"");
        prices.put("Litecoin", koinexJSONTicker.prices.LTC+"");
        prices.put("Bitcoin Cash", koinexJSONTicker.prices.BCH+"");

        Iterator it = prices.entrySet().iterator();
        listItems.clear();
        while(it.hasNext()) {
            HashMap<String, String> resultsMap = new HashMap<>();
            Map.Entry pair = (Map.Entry) it.next();
            resultsMap.put("Currency", pair.getKey().toString());
            resultsMap.put("Price", pair.getValue().toString());
            listItems.add(resultsMap);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alerts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private class RetrieveDataTask extends AsyncTask<String, Void, String> { /* Params, Progress, Result */
        public alerts myAlert;
        public RetrieveDataTask(alerts alert){
            this.myAlert = alert;
        }

        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        @Override
        protected String doInBackground(String... url) {
            while(true) {
                try {
                    InputStream is = new URL(url[0]).openStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    this.myAlert.jsonText = readAll(rd);
                    System.out.println("IN doInBackground: " + this.myAlert.jsonText);
                    this.myAlert.callRefresh();
                    Thread.sleep(15000);
                } catch (Exception e) {
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.coordinatorLayout),
                            "Cannot retrieve latest prices, retrying...", Snackbar.LENGTH_SHORT);
                }
                if(1==0) /* Silly workaround for never getting out of here; couldn't make return value void|Void for some reason */
                    return null;
            }


        }

        @Override
        protected void onPostExecute(String result) { /* Never called */
            //alerts.jsonText = result;
        }
    }
}
