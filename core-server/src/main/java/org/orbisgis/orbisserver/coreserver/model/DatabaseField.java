package org.orbisgis.orbisserver.coreserver.model;

/**
 * @author Sylvain PALOMINOS
 */
public class DatabaseField {

    private String name;
    private String type;

    public DatabaseField(String name, String type){
        this.name = name;
        this.type = type;
    }

    public String getName(){
        return name;
    }
}
