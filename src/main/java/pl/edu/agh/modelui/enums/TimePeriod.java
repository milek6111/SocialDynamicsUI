package pl.edu.agh.modelui.enums;

public enum TimePeriod {
    HUNDRED_MILIS("100 milisekund"),
    FIVE_HUNDRED_MILIS("500 milisekund"),
    ONE_SEC("1 sekunda"),
    THREE_SECS("3 sekundy"),
    FIVE_SECS("5 sekundy");

    String value;

    TimePeriod(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public static TimePeriod fromString(String value){
        for(var e : TimePeriod.values()){
            if(e.getValue().equals(value))
                return e;
        }

        return null;
    }
}
