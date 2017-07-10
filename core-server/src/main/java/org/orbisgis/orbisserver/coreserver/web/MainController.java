/*
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
package org.orbisgis.orbisserver.coreserver.web;

import org.orbisgis.orbisserver.coreserver.controller.CoreServerController;
import org.orbisgis.orbisserver.coreserver.model.Operation;
import org.orbisgis.orbisserver.coreserver.model.Session;
import org.orbisgis.orbisserver.coreserver.model.StatusInfo;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main orbisserver controller
 *
 * @author Sylvain PALOMINOS
 */
@Controller
public class MainController extends DefaultController {

    private Session session;

    @View("Home")
    Template home;

    @View("BaseLog_In")
    Template logIn;

    @View("BaseLog_Out")
    Template logOut;

    @View("Process")
    Template process;

    @View("ProcessList")
    Template processList;

    @View("Describe")
    Template describeProcess;

    @View("Jobs")
    Template jobs;

    @View("SignIn")
    Template signIn;

    @View("Workspace")
    Template workspace;

    @View("Data")
    Template data;

    @View("Import")
    Template tImport;

    @View("Export")
    Template export;

    @Route(method = HttpMethod.GET, uri = "/")
    public Result home() {
        return ok(render(home));
    }

    @Route(method = HttpMethod.GET, uri = "/logout")
    public Result logOut() {
        return ok(render(logOut));
    }

    @Route(method = HttpMethod.POST, uri = "/login")
    public Result login() throws IOException {
        String urlContent = URLDecoder.decode(context().reader().readLine(), "UTF-8");
        String[] split = urlContent.split("&");
        session = CoreServerController.getSession(split[0].replaceAll(".*=", ""),
                split[1].replaceAll(".*=", ""));
        if(session != null) {
            return ok(render(home, "userName", session.getUsername()));
        }
        else {
            return ok(render(home));
        }
    }

    @Route(method = HttpMethod.GET, uri = "/process")
    public Result process() throws IOException {
        return ok(render(process));
    }

    @Route(method = HttpMethod.GET, uri = "/processList")
    public Result processList() throws IOException {
        return ok(render(processList, "processList", session.getOperationList()));
    }

    @Route(method = HttpMethod.GET, uri = "/describeProcess")
    public Result describeProcess(@Parameter("id") String id) throws IOException {
        Operation op = session.getOperation(id);
        return ok(render(describeProcess, "operation", op));
    }

    @Route(method = HttpMethod.POST, uri = "/execute")
    public Result execute() throws IOException {
        String urlContent = URLDecoder.decode(context().reader().readLine(), "UTF-8");
        String[] split = urlContent.split("&");
        Map<String, String> inputData = new HashMap<>();
        String id = "";
        for(String str : split){
            String[] val = str.split("=");
            if(val[0].equals("processId")){
                id = val[1];
            }
            else {
                if(val.length==1) {
                    inputData.put(val[0], "");
                }
                else {
                    inputData.put(val[0], val[1]);
                }
            }
        }
        session.executeOperation(id, inputData);
        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "/jobs")
    public Result jobs() throws IOException {
        long timeMillisNow = System.currentTimeMillis();
        List<StatusInfo> statusInfoToRefreshList = session.getAllStatusInfoToRefresh();
        List<StatusInfo> statusInfoList = session.getAllStatusInfo();
        long minRefresh = Long.MAX_VALUE;

        for(StatusInfo statusInfo : statusInfoToRefreshList){
            StatusInfo info = session.refreshStatus(statusInfo.getJobId());
            statusInfo.setStatus(info.getStatus());
            if (info.getPercentCompleted() != null) {
                statusInfo.setPercentCompleted(info.getPercentCompleted());
            }
            statusInfo.setNextPoll(info.getNextPoll());
            if (info.getEstimatedCompletion() != null) {
                statusInfo.setEstimatedCompletion(info.getEstimatedCompletion());
            }
            statusInfo.setNextRefreshMillis(-1);
            if(statusInfo.getNextPoll() != null){
                long timeMillisPoll = statusInfo.getNextPoll().toGregorianCalendar().getTime().getTime();
                statusInfo.setNextRefreshMillis(timeMillisPoll - timeMillisNow);
            }
            if(statusInfo.getNextRefreshMillis() >= 0) {
                minRefresh = Math.min(statusInfo.getNextRefreshMillis(), minRefresh);
            }
        }

        for(StatusInfo statusInfo : statusInfoList){
            if(statusInfo.getNextRefreshMillis() >= 0) {
                minRefresh = Math.min(statusInfo.getNextRefreshMillis(), minRefresh);
            }
        }

        if(minRefresh == Long.MAX_VALUE){
            minRefresh = -1;
        }
        return ok(render(jobs, "jobList", session.getAllStatusInfo(), "nextRefresh", minRefresh));
    }

    @Route(method = HttpMethod.GET, uri = "/signIn")
    public Result signIn() {return ok(render(signIn));}

    @Route(method = HttpMethod.GET, uri = "/workspace")
    public Result workspace() {
        return ok(render(workspace));
    }

    @Route(method = HttpMethod.GET, uri = "/data")
    public Result data() {return ok(render(data));}

    @Route(method = HttpMethod.GET, uri = "/data/import")
    public Result tImport() {return ok(render(tImport));}

    @Route(method = HttpMethod.GET, uri = "/data/export")
    public Result export() {return ok(render(export));}
}
