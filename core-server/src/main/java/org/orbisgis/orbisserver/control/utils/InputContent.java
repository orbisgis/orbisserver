package org.orbisgis.orbisserver.control.utils;

import java.util.Map;

/**
 * Class containing the necessary information about an input for the html UI.
 *
 * @author Sylvain PALOMINOS
 */
public class InputContent {
    private String title;
    private String name;
    private String id;
    private String type;
    private Map<String, Object> attributes;

    public InputContent(String title, String name, String id, String type, Map<String, Object> attributes){
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
