package org.orbisgis.orbisserver.coreserver.model;

import java.util.Map;

/**
 * Created by sylvain on 08/07/17.
 */
public class Input {
    private String title;
    private String name;
    private String id;
    private String type;
    private Map<String, Object> attributes;

    public Input(String title, String name, String id, String type, Map<String, Object> attributes){
        this.title = title;
        this.name = name;
        this.id = id;
        this.type = type;
        this.attributes = attributes;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
