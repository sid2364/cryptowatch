package sid.cryptowatch;

/**
 * Created by siddhs on 30-12-2017.
 */

class Prices{
    String BTC;
    String XRP;
    String ETH;
    String BCH;
    String LTC;
    String MIOTA;
    String OMG;
    String GNT;
}
class Crypto{
    String last_traded_price;
    String lowest_ask;
    String highest_bid;
    String min_24hrs;
    String max_24hrs;
    String vol_24hrs;
}
class Stats{
    Crypto BTC;
    Crypto XRP;
    Crypto ETH;
    Crypto LTC;
    Crypto BCH;
}

public class KoinexJSONTicker {
    Prices prices;
    Stats stats;
}
