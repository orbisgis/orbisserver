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

import net.opengis.ows._2.ExceptionReport;
import net.opengis.ows._2.ExceptionType;
import org.junit.Assert;
import org.junit.Test;
import org.orbisgis.orbisserver.manager.Wps_2_0_0_Operations;
import org.wisdom.api.http.Result;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;
import org.wisdom.test.parents.WisdomTest;

import org.orbisgis.orbisserver.control.web.*;
import org.orbisgis.orbisserver.control.wps.*;

import javax.inject.Inject;

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
    GetCapabilitiesController getCapabilitiesController;

    @Inject
    WelcomeController welcomeController;

    @Inject
    ExecuteController executeController;

    @Inject
    WpsOperationController wpsOperationController;

    /**
     * Checks that the index page content is good.
     */
    @Test
    public void testIndexPageContent() {
        // Call the action method as follows
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return getCapabilitiesController.getCapabilities();
            }
        }).invoke();

        //It returns a redirection to the index.html page
        Assert.assertEquals(status(result), OK);

        Assert.assertTrue(toString(result).contains("Variable distance buffer"));
        Assert.assertTrue(toString(result).contains("Fixed distance buffer"));
        Assert.assertTrue(toString(result).contains("Extract center"));
        Assert.assertTrue(toString(result).contains("Create a grid of points"));
        Assert.assertTrue(toString(result).contains("Create a grid of polygons"));
        Assert.assertTrue(toString(result).contains("Fixed extrude polygons"));
        Assert.assertTrue(toString(result).contains("Variable extrude polygons"));
        Assert.assertTrue(toString(result).contains("Geometry properties"));
        Assert.assertTrue(toString(result).contains("Reproject geometries"));
        Assert.assertTrue(toString(result).contains("Point table from CSV"));
        Assert.assertTrue(toString(result).contains("Import a CSV file"));
        Assert.assertTrue(toString(result).contains("Import a DBF file"));
        Assert.assertTrue(toString(result).contains("Import a GPX file"));
        Assert.assertTrue(toString(result).contains("Import a GeoJSON file"));
        Assert.assertTrue(toString(result).contains("Import a OSM file"));
        Assert.assertTrue(toString(result).contains("Import a shapeFile"));
        Assert.assertTrue(toString(result).contains("Import a TSV file"));
        Assert.assertTrue(toString(result).contains("Create a graph"));
        Assert.assertTrue(toString(result).contains("Delete columns"));
        Assert.assertTrue(toString(result).contains("Delete rows"));
        Assert.assertTrue(toString(result).contains("Describe columns"));
        Assert.assertTrue(toString(result).contains("Insert values in a table"));
        Assert.assertTrue(toString(result).contains("Tables join"));
    }

    /**
     * Checks that the form page content is good.
     */
    @Test
    public void testFormPageContent() {
        // Call the action method as follows
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return executeController.execute();
            }
        }).invoke();

        //It returns a redirection to the welcome.html page
        Assert.assertEquals(status(result), OK);
        Assert.assertTrue(toString(result).contains("Execute Parameters"));
        Assert.assertTrue(toString(result).contains("Identifier"));
        Assert.assertTrue(toString(result).contains("Response"));
        Assert.assertTrue(toString(result).contains("Mode"));
        Assert.assertTrue(toString(result).contains("Input"));
        Assert.assertTrue(toString(result).contains("Output"));
    }

    /**
     * Checks that the WpsOperationController is returning OK,
     * and returning the good response corresponding to the GetCapabilities method.
     */
    @Test
    public void testGetCapabilitiesRequest() throws Exception {

        // Test of GetCapabilities with the correct parameters
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0", "GetCapabilities", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), OK);
        Assert.assertTrue(toString(result).contains("net.opengis.wps._2_0.WPSCapabilitiesType@"));

        // Test of  GetCapabilities, when the service parameter is missing
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("", "2.0.0", "GetCapabilities", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        ExceptionReport report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("MissingParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("The service parameter should be WPS",
                report.getException().get(0).getExceptionText().get(0));

        // Test of GetCapabilities, when the service parameter is wrong
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WP", "2.0.0", "GetCapabilities", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("InvalidParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("The service parameter should be WPS",
                report.getException().get(0).getExceptionText().get(0));

        // Test of GetCapabilities, when the version parameter is missing
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "", "GetCapabilities", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("MissingParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("The version parameter should set",
                report.getException().get(0).getExceptionText().get(0));

        // Test of GetCapabilities, when the version parameter is wrong
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "0.0.0", "GetCapabilities", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("InvalidParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("Unsupported version.",
                report.getException().get(0).getExceptionText().get(0));

        // Test of GetCapabilities, when the request parameter is missing
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0", "", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("MissingParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("The request parameter should set",
                report.getException().get(0).getExceptionText().get(0));

        // Test of GetCapabilities, when the request parameter is wrong
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0", "GetCapabilites", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("InvalidParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("Invalid request.",
                report.getException().get(0).getExceptionText().get(0));
    }

    /**
     * Checks that the WpsOperationController is returning OK,
     * and returning the good response corresponding to the DescribeProcess method.
     */
    @Test
    public void testDescribeProcessRequest() throws Exception {

        // Test of DescribeProcess with the correct parameters
        Action.ActionResult result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0",
                        "DescribeProcess", "orbisgis:wps:official:deleteRows",null);
            }
        }).invoke();

        Assert.assertEquals(status(result), OK);
        Assert.assertTrue(toString(result).contains("net.opengis.wps._2_0.ProcessOfferings@"));

        // Test of  DescribeProcess, when the service parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("", "2.0.0",
                        "DescribeProcess", "orbisgis:wps:official:deleteRows",null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        ExceptionReport report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("MissingParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("The service parameter should be WPS",
                report.getException().get(0).getExceptionText().get(0));

        // Test of DescribeProcess, when the service parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WP", "2.0.0",
                        "DescribeProcess", "orbisgis:wps:official:deleteRows",null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("InvalidParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("The service parameter should be WPS",
                report.getException().get(0).getExceptionText().get(0));

        // Test of DescribeProcess, when the version parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "",
                        "DescribeProcess", "orbisgis:wps:official:deleteRows",null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("MissingParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("The version parameter should set",
                report.getException().get(0).getExceptionText().get(0));

        // Test of DescribeProcess, when the version parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.1",
                        "DescribeProcess", "orbisgis:wps:official:deleteRows",null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("InvalidParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("Unsupported version.",
                report.getException().get(0).getExceptionText().get(0));

        // Test of DescribeProcess, when the request parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0", "", "orbisgis:wps:official:deleteRows",null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("MissingParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("The request parameter should set",
                report.getException().get(0).getExceptionText().get(0));

        // Test of DescribeProcess, when the request parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0",
                        "DescribeProces", "orbisgis:wps:official:deleteRows",null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
        report = (ExceptionReport) result.getResult().getRenderable().content();
        Assert.assertFalse(report.getException().isEmpty());
        Assert.assertEquals("InvalidParameterValue", report.getException().get(0).getExceptionCode());
        Assert.assertFalse(report.getException().get(0).getExceptionText().isEmpty());
        Assert.assertEquals("Invalid request.",
                report.getException().get(0).getExceptionText().get(0));

        // Test of DescribeProcess, when the request parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0", "DescribeProcess","",null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of DescribeProcess, when the request parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0",
                        "DescribeProcess", "orbgis:wps:official:deleteRows",null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 200);
    }

    /**
     * Checks that the WpsOperationController is returning OK,
     * and returning the good response corresponding to the Execute method.
     */
    @Test
    public void testExecuteRequest() throws Exception {

        // Test of Execute with the correct parameters
        Action.ActionResult result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXMLForExecute("orbisgis:wps:official:deleteRows",
                        "document", "auto", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), OK);
        Assert.assertTrue(toString(result).contains("net.opengis.wps._2_0.StatusInfo@"));

        // Test of  Execute, when the identifier parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXMLForExecute("orbisis:wps:official:deleteRows",
                        "document", "auto", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionType);

        // Test of Execute, when the response parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXMLForExecute("orbisgis:wps:official:deleteRows",
                        null, "auto", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);

        // Test of Execute, when the response parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXMLForExecute("orbisgis:wps:official:deleteRows",
                        "docment", "auto", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionType);

        // Test of Execute, when the mode parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXMLForExecute("orbisgis:wps:official:deleteRows",
                        "document", null, null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);

        // Test of Execute, when the mode parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXMLForExecute("orbisgis:wps:official:deleteRows",
                        "document", "aut", null, null);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionType);
    }

    /**
     * Checks that the WpsOperationController is returning OK,
     * and returning the good response corresponding to the GetStatus method. It makes the Execute method before.
     */
    @Test
    public void testGetStatusRequest() throws Exception {
        //Execution of the Execute method with a process
        wpsOperationController.displayXMLForExecute("orbisgis:wps:official:deleteRows", "document", "auto", null, null);
        final String jobId = Wps_2_0_0_Operations.getLastJobId();

        // Test of GetStatus with the correct parameters
        Action.ActionResult result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0",
                        "GetStatus", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), OK);
        Assert.assertTrue(toString(result).contains("net.opengis.wps._2_0.StatusInfo@"));

        // Test of  GetStatus, when the service parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("", "2.0.0",
                        "GetStatus", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetStatus, when the service parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WP", "2.0.0",
                        "GetStatus", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetStatus, when the version parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "",
                        "GetStatus", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetStatus, when the version parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.1",
                        "GetStatus", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetStatus, when the request parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0", "", null,
                        jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetStatus, when the request parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0",
                        "GetStatu", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetStatus, when the jobId parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0", "GetStatus",null,
                        "");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
    }

    /**
     * Checks that the WpsOperationController is returning OK,
     * and returning the good response corresponding to the GetResult method. It makes the Execute method before.
     */
    @Test
    public void testGetResultRequest() throws Exception {
        //Execution of the Execute method with a process

        wpsOperationController.displayXMLForExecute("orbisgis:wps:official:deleteRows", "document", "auto", null, null);
        final String jobId = Wps_2_0_0_Operations.getLastJobId();

        // Test of GetStatus with the correct parameters
        Action.ActionResult result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0",
                        "GetResult", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), OK);
        Assert.assertTrue(toString(result).contains("net.opengis.wps._2_0.Result@"));

        // Test of  GetResult, when the service parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("", "2.0.0",
                        "GetResult", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetResult, when the service parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WP", "2.0.0",
                        "GetResult", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetResult, when the version parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "",
                        "GetResult", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetResult, when the version parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.1",
                        "GetResult", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetResult, when the request parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0", "", null,
                        jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetResult, when the request parameter is wrong
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0",
                        "GetResul", null, jobId);
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);

        // Test of GetResult, when the jobId parameter is missing
        result = action(new Invocation(){
            @Override
            public Result invoke() throws Throwable {
                return wpsOperationController.displayXML("WPS", "2.0.0", "GetResult",null,
                        "");
            }
        }).invoke();

        Assert.assertEquals(status(result), 400);
        Assert.assertTrue(result.getResult().getRenderable().content() instanceof ExceptionReport);
    }
}
