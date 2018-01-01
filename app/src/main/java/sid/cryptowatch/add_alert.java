package sid.cryptowatch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class add_alert extends AppCompatActivity implements View.OnClickListener {
    EditText price;
    AlertDialog alert;
    AlertDialog.Builder builder;
    Spinner dropdown;
    String selected_currency;
    Button add_alert_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alert);
        dropdown = findViewById(R.id.spinnerCurrencies);
        price = findViewById(R.id.editTextPrice);
        add_alert_btn = findViewById(R.id.buttonAddAlert);

        add_alert_btn.setOnClickListener(add_alert.this);

        String[] currencies = new String[]{"Bitcoin", "Ether", "Ripple", "Litecoin", "Bitcoin Cash"};
        selected_currency = currencies[0];
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_currency = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Add alert?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO make use of the radio button selection to determine sign of the magnitude of debt amount
                if(price.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Not a valid amount!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    Float.parseFloat(price.getText().toString());
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Not a valid amount!", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDatabaseHelper db = new SimpleDatabaseHelper(getApplicationContext());
                db.addAlert(price.getText().toString(), selected_currency);
                dialog.dismiss();
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.constraintLayout),
                        "Will notify you when " + selected_currency + " reaches Rs. " + price.getText().toString() + "!", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
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
    @Override
    public void onClick(View v){
        alert.show();
    }
}
