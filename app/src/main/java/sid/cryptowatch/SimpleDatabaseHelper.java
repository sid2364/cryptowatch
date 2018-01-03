package sid.cryptowatch;

/**
 * Created by siddhs on 28-12-2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleDatabaseHelper {
    private SQLiteOpenHelper _openHelper;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CryptoWatchSid";
    private static final String TABLE_NAME_ALERTS = "alerts";
    private static final String TABLE_NAME_PRICES = "currentprices";
    //private static final String KEY_CURRENCY = "currency";
    private static final String KEY_LAST_PRICE = "last_price";
    private static final String KEY_ID = "alert_id";
    private static final String KEY_USD = "check_usd";
    private static final String KEY_INR = "check_inr";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_CURRENCY = "currency";


    public SimpleDatabaseHelper(Context context) {
        _openHelper = new SimpleSQLiteOpenHelper(context);
    }

    /* This is an internal class that handles the creation of all database tables */
    class SimpleSQLiteOpenHelper extends SQLiteOpenHelper {
        SimpleSQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME+".db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_TABLE_ALERTS = String.format("create table if not exists %s(%s text not null, %s text not null)",
                    TABLE_NAME_ALERTS, KEY_CURRENCY, KEY_AMOUNT);
            db.execSQL(CREATE_TABLE_ALERTS);
            String CREATE_TABLE_PRICES = String.format("create table if not exists %s(%s text not null, %s text not null)",
                    TABLE_NAME_PRICES, KEY_CURRENCY, KEY_LAST_PRICE);
            db.execSQL(CREATE_TABLE_PRICES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }


    public void updateCurrent(String amt, String coin){
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        db.execSQL(String.format("delete from %s where %s='%s'", TABLE_NAME_PRICES, KEY_CURRENCY, coin));
        db.execSQL(String.format("insert into %s values('%s', '%s')", TABLE_NAME_PRICES, coin, amt));
        db.close();
    }
    public float getCurrent(String coin){
        Float lp;
        String lp_;
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format("select distinct %s, %s from %s", KEY_LAST_PRICE, KEY_CURRENCY, TABLE_NAME_PRICES), null);
        if (cursor.getCount() == 0) {
            updateCurrent("0", coin);
            return 0;
        }
        cursor.moveToFirst();
        lp_ = cursor.getString(cursor.getColumnIndex(KEY_LAST_PRICE));
        cursor.close();
        db.close();
        System.out.println(lp_);
        lp = Float.parseFloat(lp_);
        return lp;

    }
    public void addAlert(String amt, String currency){
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        String token;
        switch(currency){
            case "Bitcoin": token = "BTC"; break;
            case "Ripple": token = "XRP"; break;
            case "Ether": token = "ETH"; break;
            case "Bitcoin Cash": token = "BCH"; break;
            case "Litecoin": token = "LTC"; break;
            default: token = "BTC";
        }
        String sql = String.format("insert into %s values('%s', '%s')", TABLE_NAME_ALERTS, token, amt);
        db.execSQL(sql);
        db.close();
    }
    public void deleteAlert(String amt, String token){
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        db.execSQL(String.format("delete from %s where %s='%s' and %s='%s';", TABLE_NAME_ALERTS, KEY_AMOUNT, amt, KEY_CURRENCY, token));
        db.close();
    }
    public String[] getAllAlerts() {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(String.format("select distinct %s, %s from %s", KEY_AMOUNT, KEY_CURRENCY, TABLE_NAME_ALERTS), null);
        if (cursor.getCount() == 0) return new String[0];

        String[] alarms = new String[cursor.getCount()];

        // cursor = db.rawQuery("select distinct " + KEY_TIME + " from " + TABLE_NAME_ALARMS, null);
        cursor.moveToFirst();
        int i = 0;
        while (!cursor.isAfterLast()) {
            alarms[i++] = cursor.getString(cursor.getColumnIndex(KEY_CURRENCY)) + ":" + cursor.getString(cursor.getColumnIndex(KEY_AMOUNT));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return alarms;
    }
}