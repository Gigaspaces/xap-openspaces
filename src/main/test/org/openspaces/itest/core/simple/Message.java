package org.openspaces.itest.core.simple;

/**
 * @author kimchy
 */
public class Message {

    private String value;

    public Message() {
    }

    public Message(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
