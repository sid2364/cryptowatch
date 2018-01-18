package sid.cryptowatch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ActiveAlerts extends AppCompatActivity {
    SimpleDatabaseHelper db;
    ListView listView;
    AlertDialog alert;
    AlertDialog.Builder builder;
    static String coinToRemove = null;
    static String valueToRemove = null;
    static int positionToRemove = -1;
    ArrayList<String> listItems;
    //List listItems;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_alerts);
        Toolbar toolbar = findViewById(R.id.toolbarAA);
        toolbar.setTitle("Active Alerts");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        int i = 0;
        db = new SimpleDatabaseHelper(getApplicationContext());
        listView = findViewById(R.id.lvAlerts);
        String[] tempAlerts = db.getAllAlerts();
        HashMap<Float, String> hashMapAlerts = new HashMap<>();
        for(String alert: tempAlerts){
            String parts[] = alert.split(":");
            hashMapAlerts.put(Float.parseFloat(parts[1]), parts[0]); // reversing it for sorting, can't have multiple values for same key (obv.)
        }

        //Arrays.sort(tempAlerts);
        //hashMapAlerts = sortHashMapByValues(hashMapAlerts);

        Map<Float, String> map = new TreeMap<>(hashMapAlerts);
        Arrays.fill(tempAlerts, null );

        for(Map.Entry<Float, String> entry: map.entrySet()) {
            tempAlerts[i++] = entry.getValue() + ":" + entry.getKey();
        }
        listItems = new ArrayList<>(Arrays.asList(tempAlerts));
        adapter = new ArrayAdapter<>(this, R.layout.list_item_aa, R.id.textView, listItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub
                String[] value = adapter.getItem(position).split(":");
                //Toast.makeText(getApplicationContext(),value,Toast.LENGTH_SHORT).show();
                coinToRemove = value[0];
                valueToRemove = value[1];
                positionToRemove = position;
                alert.show();
            }
        });
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Delete alert?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO make use of the radio button selection to determine sign of the magnitude of debt amount
                if(coinToRemove == null || coinToRemove.isEmpty() || valueToRemove == null || valueToRemove.isEmpty() || positionToRemove == -1){
                    Toast.makeText(getApplicationContext(), "Never gonna happen. If you're seeing this, good for you." ,Toast.LENGTH_SHORT).show();
                    return;
                }
                db.deleteAlert(valueToRemove, coinToRemove);

                //listItems.remove(coinToRemove+":"+valueToRemove);
                listItems.remove(positionToRemove);
                adapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), "Removed notification for "+coinToRemove+" at "+valueToRemove+"." ,Toast.LENGTH_SHORT).show();
                //finish(); // too lazy to notify change to adapter
                //Intent k = new Intent(getApplicationContext(), ActiveAlerts.class);
                //
                // startActivity(k);
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert = builder.create();
    }
    public HashMap<String, Float> sortHashMapByValues(
            HashMap<String, Float> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Float> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        HashMap<String, Float> sortedMap =
                new HashMap<>();

        Iterator<Float> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            float val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                float comp1 = passedMap.get(key);
                float comp2 = val;

                if (comp1 == comp2) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
