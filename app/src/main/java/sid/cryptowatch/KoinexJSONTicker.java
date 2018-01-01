package sid.cryptowatch;

/**
 * Created by siddhs on 30-12-2017.
 */

class Prices{
    float BTC;
    float last_BTC;
    float XRP;
    float last_XRP;
    float ETH;
    float last_ETH;
    float BCH;
    float last_BCH;
    float LTC;
    float last_LTC;
    Prices(){
        BTC = 0;
        XRP = 0;
        ETH = 0;
        BCH = 0;
        LTC = 0;
        last_BCH = 0;
        last_BTC = 0;
        last_LTC = 0;
        last_ETH = 0;
        last_XRP = 0;
    }

}
class Crypto{
    float last_traded_price;
    float lowest_ask;
    float highest_bid;
    float min_24hrs;
    float max_24hrs;
    float vol_24hrs;
    Crypto(){
        last_traded_price = 0;
        lowest_ask = 0;
        highest_bid = 0;
        min_24hrs = 0;
        max_24hrs = 0;
        vol_24hrs = 0;
    }
}
class Stats{
    Crypto BTC;
    Crypto XRP;
    Crypto ETH;
    Crypto LTC;
    Crypto BCH;
    Stats(){
        BTC = new Crypto();
        XRP = new Crypto();
        ETH = new Crypto();
        LTC = new Crypto();
        BCH = new Crypto();
    }
}

public class KoinexJSONTicker {
    Prices prices;
    Stats stats;
    KoinexJSONTicker(){
        prices = new Prices();
        stats = new Stats();
    }
}
