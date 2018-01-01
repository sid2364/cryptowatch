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
            String CREATE_TABLE_ALERTS = "create table if not exists " + TABLE_NAME_ALERTS + "("
                    + KEY_CURRENCY + " text not null, "
                    + KEY_AMOUNT + " text not null)";
            db.execSQL(CREATE_TABLE_ALERTS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
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
        String sql = "insert into " + TABLE_NAME_ALERTS + " values('" + token + "', '" + amt + "')";
        db.execSQL(sql);
        db.close();
    }
    public void deleteAlert(String amt, String token){
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME_ALERTS + " where " + KEY_AMOUNT + "='" +amt + "' and " + KEY_CURRENCY+"='" + token + "';");
        db.close();
    }
    public String[] getAllAlerts() {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select distinct " + KEY_AMOUNT + ", " + KEY_CURRENCY + " from " + TABLE_NAME_ALERTS, null);
        if (cursor.getCount() == 0) return new String[0];

        String[] alarms = new String[cursor.getCount()];

        // cursor = db.rawQuery("select distinct " + KEY_TIME + " from " + TABLE_NAME_ALARMS, null);
        cursor.moveToFirst();
        int i = 0;
        while (!cursor.isAfterLast()) {
            alarms[i++] = cursor.getString(cursor.getColumnIndex(KEY_CURRENCY)) + ":" + cursor.getString(cursor.getColumnIndex(KEY_AMOUNT));
            cursor.moveToNext();
        }
        db.close();
        return alarms;
    }
}