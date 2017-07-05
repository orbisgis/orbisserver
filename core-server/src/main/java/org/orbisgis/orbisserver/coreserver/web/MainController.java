package org.orbisgis.orbisserver.coreserver.web;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

/**
 * Main orbisserver controller
 *
 * @author Sylvain PALOMINOS
 */
@Controller
public class MainController extends DefaultController {

    /*
    @View("welcome")
    Template welcome;

    @Route(method = HttpMethod.GET, uri = "/")
    public Result welcome() {
        WpsServerManager.setDataSource(ds);
        return ok(render(welcome));
    }
    */
}
