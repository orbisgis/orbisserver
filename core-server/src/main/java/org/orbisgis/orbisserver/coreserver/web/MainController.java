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

    @View("Home")
    Template home;

    @Route(method = HttpMethod.GET, uri = "/")
    public Result Home() {
        return ok(render(home));
    }

    @View("signIn")
    Template signIn;

    @Route(method = HttpMethod.GET, uri = "/signIn")
    public Result signIn() {
        return ok(render(signIn));
    }
}
