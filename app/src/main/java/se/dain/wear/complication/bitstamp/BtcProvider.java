package se.dain.wear.complication.bitstamp;

import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class BtcProvider extends ComplicationProviderService {
    //TODO These probably need to be persisted as this object is recreated often...
    private int lastPrice = -1;
    private long lastUpdate = -1;

    @Override
    public void onComplicationUpdate(final int complicationId, final int type, final ComplicationManager complicationManager) {
        if(System.currentTimeMillis() - lastUpdate < 300000) {
            Log.d("BTC", "Already fresh value, no update needed");
            complicationManager.noUpdateRequired(complicationId);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int price = getPrice();
                    int dollars = price / 100;
                    int cents = price % 100;
                    complicationManager.updateComplicationData(complicationId, new ComplicationData.Builder(type)
                            .setShortTitle(ComplicationText.plainText("BTC"))
                            .setShortText(ComplicationText.plainText(String.format(Locale.US, "$%,d.%02d", dollars, cents)))
                            .build());
                }
            }).start();
        }
    }

    private int getPrice() {
        try {
            URLConnection urlConnection = new URL("https://www.bitstamp.net/api/ticker/").openConnection();
            urlConnection.setRequestProperty("User-Agent", "github.com/dainnilsson/wear-complication-bitstamp Android Wear");
            lastPrice = (int)(new JSONObject(
                    new BufferedReader(
                            new InputStreamReader(
                                    urlConnection.getInputStream()
                            )
                    ).readLine()
            ).getDouble("last") * 100);
            lastUpdate = System.currentTimeMillis();
        } catch (IOException | JSONException e) {
            Log.e("BTC", "Failed getting price", e);
        }
        return lastPrice;
    }
}
