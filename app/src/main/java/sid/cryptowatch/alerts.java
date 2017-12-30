package sid.cryptowatch;

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
    static String jsonText;

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
                new String[]{"First Line", "Second Line"},
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
                try {
                    refreshPriceList(prices, listItems, adapter);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "There might be a problem with your connection.",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "There was a problem in parsing the JSON.",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
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

        SimpleDatabaseHelper db = new SimpleDatabaseHelper(getApplicationContext());

    }
    public void decodeToParsable() throws JSONException{
        JSONObject stats = jsonObject.getJSONObject("stats");
        JSONObject prices = jsonObject.getJSONObject("prices");
        JSONObject currencyStatsBTC = stats.getJSONObject("BTC");
        JSONObject currencyStatsETH = stats.getJSONObject("ETH");
        JSONObject currencyStatsXRP = stats.getJSONObject("XRP");
        JSONObject currencyStatsLTC = stats.getJSONObject("LTC");
        JSONObject currencyStatsBCH = stats.getJSONObject("BCH");
        koinexJSONTicker.prices.BTC = prices.getString("BTC");
        Toast.makeText(getApplicationContext(), (String)koinexJSONTicker.prices.BTC,
                Toast.LENGTH_LONG).show();
        koinexJSONTicker.prices.ETH = prices.getString("ETH");
        koinexJSONTicker.prices.XRP = prices.getString("XRP");
        koinexJSONTicker.prices.BCH = prices.getString("BCH");
        koinexJSONTicker.prices.LTC = prices.getString("LTC");
        koinexJSONTicker.stats.BCH.last_traded_price = currencyStatsBCH.getString("last_traded_price");
        koinexJSONTicker.stats.BTC.last_traded_price = currencyStatsBTC.getString("last_traded_price");
        koinexJSONTicker.stats.ETH.last_traded_price = currencyStatsETH.getString("last_traded_price");
        koinexJSONTicker.stats.XRP.last_traded_price = currencyStatsXRP.getString("last_traded_price");
        koinexJSONTicker.stats.LTC.last_traded_price = currencyStatsLTC.getString("last_traded_price");
        koinexJSONTicker.stats.BCH.lowest_ask = currencyStatsBCH.getString("lowest_ask");
        koinexJSONTicker.stats.BTC.lowest_ask = currencyStatsBTC.getString("lowest_ask");
        koinexJSONTicker.stats.ETH.lowest_ask = currencyStatsETH.getString("lowest_ask");
        koinexJSONTicker.stats.XRP.lowest_ask = currencyStatsXRP.getString("lowest_ask");
        koinexJSONTicker.stats.LTC.lowest_ask = currencyStatsLTC.getString("lowest_ask");
        koinexJSONTicker.stats.BCH.highest_bid = currencyStatsBCH.getString("highest_bid");
        koinexJSONTicker.stats.BTC.highest_bid = currencyStatsBTC.getString("highest_bid");
        koinexJSONTicker.stats.ETH.highest_bid = currencyStatsETH.getString("highest_bid");
        koinexJSONTicker.stats.XRP.highest_bid = currencyStatsXRP.getString("highest_bid");
        koinexJSONTicker.stats.LTC.highest_bid = currencyStatsLTC.getString("highest_bid");
        koinexJSONTicker.stats.BCH.max_24hrs = currencyStatsBCH.getString("max_24hrs");
        koinexJSONTicker.stats.BTC.max_24hrs = currencyStatsBTC.getString("max_24hrs");
        koinexJSONTicker.stats.ETH.max_24hrs = currencyStatsETH.getString("max_24hrs");
        koinexJSONTicker.stats.XRP.max_24hrs = currencyStatsXRP.getString("max_24hrs");
        koinexJSONTicker.stats.LTC.max_24hrs = currencyStatsLTC.getString("max_24hrs");
        koinexJSONTicker.stats.BCH.min_24hrs = currencyStatsBCH.getString("min_24hrs");
        koinexJSONTicker.stats.BTC.min_24hrs = currencyStatsBTC.getString("min_24hrs");
        koinexJSONTicker.stats.ETH.min_24hrs = currencyStatsETH.getString("min_24hrs");
        koinexJSONTicker.stats.XRP.min_24hrs = currencyStatsXRP.getString("min_24hrs");
        koinexJSONTicker.stats.LTC.min_24hrs = currencyStatsLTC.getString("min_24hrs");
        koinexJSONTicker.stats.BCH.vol_24hrs = currencyStatsBCH.getString("vol_24hrs");
        koinexJSONTicker.stats.BTC.vol_24hrs = currencyStatsBTC.getString("vol_24hrs");
        koinexJSONTicker.stats.ETH.vol_24hrs = currencyStatsETH.getString("vol_24hrs");
        koinexJSONTicker.stats.XRP.vol_24hrs = currencyStatsXRP.getString("vol_24hrs");
        koinexJSONTicker.stats.LTC.vol_24hrs = currencyStatsLTC.getString("vol_24hrs");
    }
    /*

     */

    void getData() throws IOException, JSONException {
        String jsonText = String.valueOf(new RetrieveDataTask().execute("https://koinex.in/api/ticker"));
        if(jsonText.isEmpty())
            throw new IOException();
        System.out.println(">>> " + jsonText + " <<<");
        jsonObject = new JSONObject(jsonText);
    }
    public void refreshPriceList(HashMap<String, String> prices, List<HashMap<String, String>>listItems, SimpleAdapter adapter) throws IOException, JSONException {
        getData();
        decodeToParsable();

        prices.put("Bitcoin", koinexJSONTicker.prices.BTC);
        prices.put("Ether", koinexJSONTicker.prices.ETH);
        prices.put("Ripple", koinexJSONTicker.prices.XRP);
        prices.put("Litecoin", koinexJSONTicker.prices.LTC);
        prices.put("Bitcoin Cash", koinexJSONTicker.prices.BCH);

        Iterator it = prices.entrySet().iterator();
        listItems.clear();
        while(it.hasNext()) {
            HashMap<String, String> resultsMap = new HashMap<>();
            Map.Entry pair = (Map.Entry) it.next();
            resultsMap.put("First Line", pair.getKey().toString());
            resultsMap.put("Second Line", pair.getValue().toString());
            listItems.add(resultsMap);
        }
        adapter.notifyDataSetChanged();
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
    private static class RetrieveDataTask extends AsyncTask<String, Void, String> { /* Params, Progress, Result */

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
            try {
                InputStream is = new URL(url[0]).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                jsonText = readAll(rd);
                //System.out.println(jsonText);
                return jsonText;
            } catch (Exception e) {
                return "";
            }
        }

    }
}
