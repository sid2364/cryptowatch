package sid.cryptowatch;

/**
 * Created by siddhs on 04-01-2018.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class StabiliseAdapter extends ArrayAdapter<Model>{
        Model[] modelItems = null;
        Context context;
        public StabiliseAdapter(Context context, Model[] resource) {
            super(context, R.layout.list_item, resource);
            this.context = context;
            this.modelItems = resource;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            TextView coin = convertView.findViewById(R.id.textHeader);
            TextView price = convertView.findViewById(R.id.textSub);
            CheckBox cb = convertView.findViewById(R.id.checkBoxCheckIfStable);
            coin.setText(modelItems[position].getName());
            price.setText(modelItems[position].getPrice()+"");
            if(modelItems[position].getValue() == 1)
                cb.setChecked(true);
            else
                cb.setChecked(false);
            return convertView;
        }
}

