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
import java.util.ArrayList;
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

    private List<Session> sessionList = new ArrayList<>();

    @View("Home")
    Template home;

    @View("BaseLog_In")
    Template logIn;

    @View("BaseLog_Out")
    Template logOut;

    @View("ProcessList")
    Template processListTemplate;

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

    @View("ProcessLeftNav")
    Template leftNavContent;

    @View("User")
    Template user;

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
        Session session = CoreServerController.getSession(split[0].replaceAll(".*=", ""),
                split[1].replaceAll(".*=", ""));
        if(session != null) {
            sessionList.add(session);
            return ok(session.getToken().toString());
        }
        else {
            return badRequest();
        }
    }

    @Route(method = HttpMethod.GET, uri = "/process/processList")
    public Result processList(@Parameter("token") String token) throws IOException {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                List<Operation> processList = session.getOperationList();
                List<Operation> importExportList = new ArrayList<Operation>();

                for(Operation op : processList){
                    for(String keyword :  op.getKeyWord()){
                        if(keyword.equals("Export") || keyword.equals("Import")){
                            importExportList.add(op);
                        }
                    }
                }
                processList.removeAll(importExportList);
                return ok(render(processListTemplate, "processList", processList));
            }
        }
        return badRequest(render(processListTemplate));
    }

    @Route(method = HttpMethod.GET, uri = "/describeProcess")
    public Result describeProcess(@Parameter("id") String id, @Parameter("token") String token) throws IOException {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                Operation op = session.getOperation(id);
                return ok(render(describeProcess, "operation", op));
            }
        }
        return badRequest(render(describeProcess));
    }

    @Route(method = HttpMethod.POST, uri = "/execute")
    public Result execute(@Parameter("token") String token) throws IOException {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                String urlContent = URLDecoder.decode(context().reader().readLine(), "UTF-8");
                String[] split = urlContent.split("&");
                Map<String, String> inputData = new HashMap<>();
                String id = "";
                for (String str : split) {
                    String[] val = str.split("=");
                    if (val[0].equals("processId")) {
                        id = val[1];
                    } else {
                        if (val.length == 1) {
                            inputData.put(val[0], "");
                        } else {
                            inputData.put(val[0], val[1]);
                        }
                    }
                }
                session.executeOperation(id, inputData);
                return ok();
            }
        }
        return badRequest();
    }

    @Route(method = HttpMethod.GET, uri = "/jobs")
    public Result jobs(@Parameter("token") String token) throws IOException {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                long timeMillisNow = System.currentTimeMillis();
                List<StatusInfo> statusInfoToRefreshList = session.getAllStatusInfoToRefresh();
                List<StatusInfo> statusInfoList = session.getAllStatusInfo();
                long minRefresh = Long.MAX_VALUE;

                for (StatusInfo statusInfo : statusInfoToRefreshList) {
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
                    if (statusInfo.getNextPoll() != null) {
                        long timeMillisPoll = statusInfo.getNextPoll().toGregorianCalendar().getTime().getTime();
                        statusInfo.setNextRefreshMillis(timeMillisPoll - timeMillisNow);
                    }
                    if (statusInfo.getNextRefreshMillis() >= 0) {
                        minRefresh = Math.min(statusInfo.getNextRefreshMillis(), minRefresh);
                    }
                }

                for (StatusInfo statusInfo : statusInfoList) {
                    if (statusInfo.getNextRefreshMillis() >= 0) {
                        minRefresh = Math.min(statusInfo.getNextRefreshMillis(), minRefresh);
                    }
                }

                if (minRefresh == Long.MAX_VALUE) {
                    minRefresh = -1;
                }
                return ok(render(jobs, "jobList", session.getAllStatusInfo(), "nextRefresh", minRefresh));
            }
        }
        return badRequest(render(jobs));
    }

    @Route(method = HttpMethod.GET, uri = "/signIn")
    public Result signIn() {return ok(render(signIn));}

    @Route(method = HttpMethod.GET, uri = "/workspace")
    public Result workspace() {
        return ok(render(workspace));
    }

    @Route(method = HttpMethod.GET, uri = "/data")
    public Result data(@Parameter("token") String token) {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                List<Operation> opList = session.getOperationList();
                List<Operation> importList = new ArrayList<Operation>();

                for(Operation op : opList){
                    for(String keyword :  op.getKeyWord()){
                        if(keyword.equals("Import")){
                            importList.add(op);
                        }
                    }
                }
                return ok(render(data, "processList", importList));
            }
        }
        return badRequest(render(data));
    }

    @Route(method = HttpMethod.GET, uri = "/data/import")
    public Result Import(@Parameter("token") String token) {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                List<Operation> opList = session.getOperationList();
                List<Operation> importList = new ArrayList<Operation>();

                for(Operation op : opList){
                    for(String keyword :  op.getKeyWord()){
                        if(keyword.equals("Import")){
                            importList.add(op);
                        }
                    }
                }
                return ok(render(tImport, "processList", importList));
            }
        }

        return badRequest(render(tImport));
    }

    @Route(method = HttpMethod.GET, uri = "/data/export")
    public Result export(@Parameter("token") String token) {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                List<Operation> opList = session.getOperationList();
                List<Operation> exportList = new ArrayList<Operation>();

                for(Operation op : opList){
                    for(String keyword :  op.getKeyWord()){
                        if(keyword.equals("Export")){
                            exportList.add(op);
                        }
                    }
                }
                return ok(render(export, "processList", exportList));
            }
        }

        return badRequest(render(export));
    }

    @Route(method = HttpMethod.GET, uri = "/process/leftNavContent")
    public Result leftNavContent() {return ok(render(leftNavContent));}

    @Route(method = HttpMethod.GET, uri = "/user")
    public Result user(@Parameter("token") String token) {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                return ok(render(user, "session", session));
            }
        }
        return badRequest(render(user));
    }
}
