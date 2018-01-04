/**
 * Created by siddhs on 04-01-2018.
 */

package sid.cryptowatch;

public class Model {
    String coin;
    float price;
    int value; /* 0 -&gt; checkbox disable, 1 -&gt; checkbox enable */

    Model(String coin, float price, int value){
        this.coin = coin;
        this.price = price;
        this.value = value;
    }
    public String getName(){
        return this.coin;
    }
    public float getPrice(){
        return this.price;
    }
    public int getValue(){
        return this.value;
    }

}
