package sid.cryptowatch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class alerts extends AppCompatActivity {

    ListView listViewCurrentPrices;
    FloatingActionButton fabReload, fab, fabShowAlerts;
    HashMap<String, String> prices;
    List<HashMap<String, String>> listItems;
    SimpleAdapter adapter;
    static JSONObject jsonObject = null;
    static KoinexJSONTicker koinexJSONTicker;
    SimpleDatabaseHelper db;
    String jsonText;
    static int notifyCounter;
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    RetrieveDataTask backgroundStuff;
    public static boolean IsClosingActivities = false;

    @Override
    public void onBackPressed(){
        backgroundStuff.cancel(true);
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (alerts.IsClosingActivities) {
            this.finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listViewCurrentPrices = findViewById( R.id.listViewPrices);
        koinexJSONTicker = new KoinexJSONTicker();
        db = new SimpleDatabaseHelper(getApplicationContext());
        backgroundStuff = new RetrieveDataTask(this);
        notifyCounter = 40;

        notificationBuilder = new NotificationCompat.Builder(this);
        Intent notificationIntent = new Intent(this, alerts.class);

        prices = new HashMap<>();
        listItems = new ArrayList<>();
        adapter = new SimpleAdapter(getApplicationContext(), listItems, R.layout.list_item,
                new String[]{"Currency", "Price"},
                new int[]{R.id.textHeader, R.id.textSub});

        getPricesFromLastOpen();


        backgroundStuff.execute("https://koinex.in/api/ticker");
                //new RetrieveDataTask().execute("https://koinex.in/api/ticker");


        listViewCurrentPrices.setAdapter(adapter);

        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(contentIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        listViewCurrentPrices.setClickable(true);
        listViewCurrentPrices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(alerts.this, add_alert.class);
                TextView textViewCoinPrices = (TextView) view.findViewById(R.id.textHeader);
                String coin = textViewCoinPrices.getText().toString();
                //String coin = ((TextView) view.findViewById(R.id.listViewPrices)).getText().toString();
                //String coin = (String) parent.getAdapter().getItem(position);
                intent.putExtra("coin", coin);
                startActivity(intent);
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent k = new Intent(getApplicationContext(), add_alert.class);
                backgroundStuff.cancel(true);
                //finish();
                startActivity(k);

            }
        });
        fabReload = findViewById(R.id.fabReload);
        fabReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //backgroundStuff.cancel(true);
                //backgroundStuff.execute("https://koinex.in/api/ticker");
                callRefresh();
                adapter.notifyDataSetChanged();
            }
        });

        fabShowAlerts = findViewById(R.id.fabShowAlerts);
        fabShowAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent k = new Intent(getApplicationContext(), add_alert.class);
                //finish();
                backgroundStuff.cancel(true);
                startActivity(k);

            }
        });

    }

    /*
    This is really bad programming, I'm already too far into it to change it
    Note to self: always allow for scaling application up and not limiting ANYTHING to
    what your present idea of implementation is. THINK OUT OF THE BOX!
     */
    public void getPricesFromLastOpen(){
        koinexJSONTicker.prices.BTC = db.getCurrent("BTC");
        koinexJSONTicker.prices.XRP = db.getCurrent("XRP");
        koinexJSONTicker.prices.LTC = db.getCurrent("LTC");
        koinexJSONTicker.prices.BCH = db.getCurrent("BCH");
        koinexJSONTicker.prices.ETH = db.getCurrent("ETH");
    }
    public void updateCurrentPrices(){
        db.updateCurrent(koinexJSONTicker.prices.ETH+"", "ETH");
        db.updateCurrent(koinexJSONTicker.prices.BTC+"", "BTC");
        db.updateCurrent(koinexJSONTicker.prices.XRP+"", "XRP");
        db.updateCurrent(koinexJSONTicker.prices.BCH+"", "BCH");
        db.updateCurrent(koinexJSONTicker.prices.LTC+"", "LTC");
    }

    @Override
    public void onRestart(){
        super.onRestart();
    }
    public synchronized void callRefresh() {
        Snackbar.make(findViewById(R.id.coordinatorLayout),
                "Loading data, please wait!", Snackbar.LENGTH_SHORT).show();
        notifyCounter += 1;
        try{
            refreshPriceList(prices, listItems, adapter);
            updateCurrentPrices();
        } catch (IOException e) {
            Toast.makeText(this.getApplicationContext(), "There might be a problem with your connection.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        } catch (JSONException e) {
            Toast.makeText(alerts.this, "There was a problem in parsing the JSON.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        } catch(Exception e) {
            Toast.makeText(alerts.this, "There was a problem.",
                    Toast.LENGTH_LONG);
            return;
        }

        // notificationID allows you to update the notification later on.
        if(notifyCounter > 40) {
            notificationBuilder.setContentTitle("Latest Prices");
            notificationBuilder.setContentText("Check the latest prices on cryptowatch!");
            notificationManager.notify(2364, notificationBuilder.build());
            notifyCounter = 0;
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
        //KoinexJSONTicker koinexJSONTicker = new KoinexJSONTicker();
        koinexJSONTicker.prices.last_BTC = koinexJSONTicker.prices.BTC;
        koinexJSONTicker.prices.BTC = Float.parseFloat(prices.getString("BTC"));
        koinexJSONTicker.prices.last_ETH = koinexJSONTicker.prices.ETH;
        koinexJSONTicker.prices.ETH = Float.parseFloat(prices.getString("ETH"));
        koinexJSONTicker.prices.last_XRP = koinexJSONTicker.prices.XRP;
        koinexJSONTicker.prices.XRP = Float.parseFloat(prices.getString("XRP"));
        koinexJSONTicker.prices.last_BCH = koinexJSONTicker.prices.BCH;
        koinexJSONTicker.prices.BCH = Float.parseFloat(prices.getString("BCH"));
        koinexJSONTicker.prices.last_LTC = koinexJSONTicker.prices.LTC;
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
        // check for alerts and send notification
        String activeAlerts[] = db.getAllAlerts();
        for(int i = 0; i < activeAlerts.length; i++){
            String parts[] = activeAlerts[i].split(":");
            if(checkIfReached(parts[0], parts[1])){
                notificationBuilder.setContentTitle("Update");
                notificationBuilder.setContentText("Price of " + parts[0] + " has reached " + parts[1] +"!");
                // notificationID allows you to update the notification later on.
                notificationManager.notify(getNumberForCurrency(parts[0]), notificationBuilder.build());
                db.deleteAlert(parts[1], parts[0]);
            }
        }
        return koinexJSONTicker;
    }
    int getNumberForCurrency(String c){
        switch (c){
            case "BTC": return 1;
            case "ETH": return 2;
            case "XRP": return 3;
            case "LTC": return 4;
            case "BCH": return 5;
        }
        return 0;
    }
    boolean checkIfReached(String currency, String amt){
        float price = Float.parseFloat(amt);
        switch(currency){
            case "BTC":
                if(koinexJSONTicker.prices.last_BTC < price && price <= koinexJSONTicker.prices.BTC
                    || koinexJSONTicker.prices.last_BTC > price && price >= koinexJSONTicker.prices.BTC)
                    return true;
                break;
            case "XRP":
                if(koinexJSONTicker.prices.last_XRP < price && price <= koinexJSONTicker.prices.XRP
                        || koinexJSONTicker.prices.last_XRP > price && price >= koinexJSONTicker.prices.XRP)
                    return true;
                break;
            case "ETH":
                if(koinexJSONTicker.prices.last_ETH < price && price <= koinexJSONTicker.prices.ETH
                        || koinexJSONTicker.prices.last_ETH > price && price >= koinexJSONTicker.prices.ETH)
                    return true;
                break;
            case "BCH":
                if(koinexJSONTicker.prices.last_BCH < price && price <= koinexJSONTicker.prices.BCH
                        || koinexJSONTicker.prices.last_BCH > price && price >= koinexJSONTicker.prices.BCH)
                    return true;
                break;
            case "LTC":
                if(koinexJSONTicker.prices.last_LTC < price && price <= koinexJSONTicker.prices.LTC
                        || koinexJSONTicker.prices.last_LTC > price && price >= koinexJSONTicker.prices.LTC)
                    return true;
                break;
        }
        return false;
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

        koinexJSONTicker = decodeToParsable();
        //System.out.println("Reached here with " + koinexJSONTicker.prices.BTC);
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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Snackbar.make(findViewById(R.id.coordinatorLayout),
                "Latest prices!", Snackbar.LENGTH_SHORT).show();
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
        BufferedReader rd;

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
            try {
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        "Retrieving latest prices...", Snackbar.LENGTH_SHORT).show();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i = 0 ; i < 500 ; i++) {
                try {
                    InputStream is = new URL(url[0]).openStream();
                    rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    this.myAlert.jsonText = readAll(rd);
                    //System.out.println("IN doInBackground: " + this.myAlert.jsonText);
                    this.myAlert.callRefresh();
                    Thread.sleep(50000);
                } catch (Exception e) {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            "Cannot retrieve latest prices, retrying...", Snackbar.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            //if(1==0) /* Silly workaround for never getting out of here; couldn't make return value void|Void for some reason */
            //    return null;
            return null; // dummy; does nothing (AFAIK!)
        }

        @Override
        protected void onPostExecute(String result) { /* Never called */
            //alerts.jsonText = result;
        }
    }
}
