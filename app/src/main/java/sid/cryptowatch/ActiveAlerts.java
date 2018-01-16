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
import java.util.List;

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

        db = new SimpleDatabaseHelper(getApplicationContext());
        listView = findViewById(R.id.lvAlerts);
        String[] tempAlerts = db.getAllAlerts();
        Arrays.sort(tempAlerts);
        listItems = new ArrayList<String>(Arrays.asList(tempAlerts));
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
}
