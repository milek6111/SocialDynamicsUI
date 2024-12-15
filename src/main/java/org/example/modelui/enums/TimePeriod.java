package org.example.modelui.enums;

public enum TimePeriod {
    HUNDRED_MILIS("100 miliseconds"),
    FIVE_HUNDRED_MILIS("500 miliseconds"),
    ONE_SEC("1 second"),
    THREE_SECS("3 seconds"),
    FIVE_SECS("5 seconds");

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
