package org.orbisgis.orbisserver.control.utils;

/**
 * Object containing basic information used by the HTML UI.
 *
 * @author Sylvain PALOMINOS
 */
public class ProcessContent {
    private String htmlId;
    private String wpsId;
    private String title;

    public ProcessContent(String htmlId, String wpsId, String title){
        this.htmlId = htmlId;
        this.wpsId = wpsId;
        this.title = title;
    }

    public String getHtmlId() {
        return htmlId;
    }

    public String getWpsId() {
        return wpsId;
    }

    public String getTitle() {
        return title;
    }
}
