/**
 * OrbisServer is an OSGI web application to expose OGC services.
 *
 * OrbisServer is part of the OrbisGIS platform
 *
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 *
 * OrbisServer is distributed under LGPL 3 license.
 *
 * Copyright (C) 2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * OrbisServer is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisServer is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * OrbisServer. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.orbisgis.orbisserver.control.xml;

import org.junit.Assert;
import org.junit.Test;
import org.wisdom.api.http.Result;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;
import org.wisdom.test.parents.WisdomUnitTest;

import static org.wisdom.test.parents.Action.action;

/**
 * A couple of unit tests.
 */
public class WpsOperationControllerTest extends WisdomUnitTest {
    /**
     * Checks that the WpsOperationController is returning OK, and returning the good response corresponding to the GetCapabilities method.
     */
    @Test
    public void testGetCapabilitiesController() throws Exception {
        // Instance of WpsOperationController
        final WpsOperationController controller = new WpsOperationController();

        // Test of GetCapabilities with the correct parameters
        Action.ActionResult result = action(new Invocation(){
          @Override
          public Result invoke() throws Throwable {
              return controller.displayXML("WPS", "2.0.0", "GetCapabilities", null);
          }
        }).invoke();

        Assert.assertEquals(status(result), OK);
        Assert.assertTrue(toString(result).contains("net.opengis.wps._2_0.WPSCapabilitiesType@"));

        // Test of  GetCapabilities, when the service parameter is missing
        result = action(new Invocation(){
          @Override
          public Result invoke() throws Throwable {
              return controller.displayXML("", "2.0.0", "GetCapabilities","");
          }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("You need to enter a service to do queries, it should be WPS here"));

        // Test of GetCapabilities, when the service parameter is wrong
        result = action(new Invocation(){
          @Override
          public Result invoke() throws Throwable {
              return controller.displayXML("WP", "2.0.0", "GetCapabilities","");
          }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("The service was not properly written, it should be WPS here"));

        // Test of GetCapabilities, when the version parameter is missing
        result = action(new Invocation(){
          @Override
          public Result invoke() throws Throwable {
              return controller.displayXML("WPS", "", "GetCapabilities","");
          }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("You need to enter the version of WPS to get the corresponding xml file"));

        // Test of GetCapabilities, when the version parameter is wrong
        result = action(new Invocation(){
          @Override
          public Result invoke() throws Throwable {
              return controller.displayXML("WPS", "2.0.1", "GetCapabilities","");
          }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("Please enter a good version of WPS, it should be 2.0.0"));

        // Test of GetCapabilities, when the request parameter is missing
        result = action(new Invocation(){
          @Override
          public Result invoke() throws Throwable {
              return controller.displayXML("WPS", "2.0.0", "","");
          }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("You need to enter the request to get the corresponding xml file"));

        // Test of GetCapabilities, when the request parameter is wrong
        result = action(new Invocation(){
          @Override
          public Result invoke() throws Throwable {
              return controller.displayXML("WPS", "2.0.0", "GetCapabilites","");
          }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("The request was not properly written"));
    }

    /**
     * Checks that the WpsOperationController is returning OK, and returning the good response corresponding to the DescribeProcess method.
     */
    @Test
    public void testDescribeProcessController() throws Exception {
        // Instance of WpsOperationController
        final WpsOperationController controller = new WpsOperationController();

        // Test of GetCapabilities with the correct parameters
        Action.ActionResult result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("WPS", "2.0.0", "DescribeProcess", "ALL");
            }
        }).invoke();

        Assert.assertEquals(status(result), OK);
        //Assert.assertTrue(toString(result).contains("net.opengis.wps._2_0.WPSCapabilitiesType@"));

        // Test of GetCapabilities with the correct parameters
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("WPS", "2.0.0", "DescribeProcess", "tmp");
            }
        }).invoke();

        Assert.assertEquals(status(result), OK);
        //Assert.assertTrue(toString(result).contains("net.opengis.wps._2_0.WPSCapabilitiesType@"));

        // Test of  GetCapabilities, when the service parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("", "2.0.0", "DescribeProcess","ALL");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("You need to enter a service to do queries, it should be WPS here"));

        // Test of GetCapabilities, when the service parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("WP", "2.0.0", "DescribeProcess","ALL");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("The service was not properly written, it should be WPS here"));

        // Test of GetCapabilities, when the version parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("WPS", "", "DescribeProcess","ALL");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("You need to enter the version of WPS to get the corresponding xml file"));

        // Test of GetCapabilities, when the version parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("WPS", "2.0.1", "DescribeProcess","ALL");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("Please enter a good version of WPS, it should be 2.0.0"));

        // Test of GetCapabilities, when the request parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("WPS", "2.0.0", "","ALL");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("You need to enter the request to get the corresponding xml file"));

        // Test of GetCapabilities, when the request parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("WPS", "2.0.0", "DescribeProces","ALL");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("The request was not properly written"));

        // Test of GetCapabilities, when the request parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("WPS", "2.0.0", "DescribeProcess","");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("The request was not properly written"));

        // Test of GetCapabilities, when the request parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return controller.displayXML("WPS", "2.0.0", "DescribeProcess","ALLE");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(toString(result).contains("The request was not properly written"));
    }
}
