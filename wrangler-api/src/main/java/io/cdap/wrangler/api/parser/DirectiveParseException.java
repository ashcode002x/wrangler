package io.cdap.wrangler.api.parser;

public class DirectiveParseException {
    public DirectiveParseException(String msg){
        super();
        // Log the error message
        System.err.println("DirectiveParseException: " + msg);

    }
}
