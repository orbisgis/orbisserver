/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.orbisgis.orbisserver.ITTests;

import org.junit.Test;
import org.wisdom.api.http.Result;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;
import org.wisdom.test.parents.WisdomTest;

import org.orbisgis.orbisserver.control.web.*;
import org.orbisgis.orbisserver.control.xml.*;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 * An in-container test checking the application while it's executing.
 */
public class InContainerIT extends WisdomTest {

    /**
     * The @Inject annotation is able to inject (in tests)
     * the bundle context, controllers, services and
     * templates.
     */
    @Inject
    IndexController indexController;

    @Inject
    WelcomeController welcomeController;

    @Inject
    GetCapabilitiesController getCapabilitiesController;

    @Test
    public void testIndexPageContent() {
        // Call the action method as follows
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return indexController.index();
            }
        }).invoke();

        //It returns a redirection to the index.html page
        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).contains("OrbisServer");
        assertThat(toString(result)).contains("Welcome");
        assertThat(toString(result)).contains("Welcome to OrbisServer");

        assertThat(toString(result)).contains("Variable distance buffer");
        assertThat(toString(result)).contains("Fixed distance buffer");
        assertThat(toString(result)).contains("Extract center");
        assertThat(toString(result)).contains("Create a grid of points");
        assertThat(toString(result)).contains("Create a grid of polygons");
        assertThat(toString(result)).contains("Fixed extrude polygons");
        assertThat(toString(result)).contains("Variable extrude polygons");
        assertThat(toString(result)).contains("Geometry properties");
        assertThat(toString(result)).contains("Reproject geometries");
        assertThat(toString(result)).contains("Point table from CSV");
        assertThat(toString(result)).contains("Import a CSV file");
        assertThat(toString(result)).contains("Import a DBF file");
        assertThat(toString(result)).contains("Import a GPX file");
        assertThat(toString(result)).contains("Import a GeoJSON file");
        assertThat(toString(result)).contains("Import a OSM file");
        assertThat(toString(result)).contains("Import a shapeFile");
        assertThat(toString(result)).contains("Import a TSV file");
        assertThat(toString(result)).contains("Create a graph");
        assertThat(toString(result)).contains("Delete columns");
        assertThat(toString(result)).contains("Delete rows");
        assertThat(toString(result)).contains("Describe columns");
        assertThat(toString(result)).contains("Insert values in a table");
        assertThat(toString(result)).contains("Tables join");

        assertThat(toString(result)).contains("GetCapabilities");

    }

    @Test
    public void testWelcomePageContent() {
        // Call the action method as follows
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return welcomeController.welcome();
            }
        }).invoke();

        //It returns a redirection to the welcome.html page
        assertThat(status(result)).isEqualTo(OK);
        assertThat(toString(result)).contains("Welcome to Orbis Server");
        assertThat(toString(result)).contains("Please", "login");
      }
}
