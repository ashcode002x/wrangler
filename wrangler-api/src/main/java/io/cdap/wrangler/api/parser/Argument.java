package io.cdap.wrangler.api.parser;

public class Argument {
    private final String name;
    private final String value;

    public Argument(String name, String value) {
        this.name = name;
        this.value = value;
    }
    public String name(){
        return name;
    }
    public String value(){
        return value;
    }
}
