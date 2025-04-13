package io.cdap.wrangler.api.parser;

import java.util.List;
public class Arguments {
    private List<Argument> arguments;
    public Arguments(List<Argument> arguments) {
        this.arguments = arguments;
    }
    public List<Argument> get() {
        return arguments;
    }
    public String value(String name){
        for (Argument arg : arguments) {
            if (arg.name().equals(name)) {
                return arg.value();
            }
        }
        return null;
    }
}
