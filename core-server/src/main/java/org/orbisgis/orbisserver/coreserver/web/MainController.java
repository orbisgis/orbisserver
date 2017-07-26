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
import org.orbisgis.orbisserver.coreserver.model.*;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

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

    @View("HomeContent")
    Template homeContent;

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

    @View("DataLeftNav")
    Template dataLeftNav;

    @View("Data")
    Template data;

    @View("Import")
    Template tImport;

    @View("Export")
    Template export;

    @View("Process")
    Template process;

    @View("ProcessLeftNav")
    Template leftNavContent;

    @View("User")
    Template user;

    @View("UserSettings")
    Template userSettings;

    @View("DatabaseView")
    Template databaseView;

    @Route(method = HttpMethod.GET, uri = "/")
    public Result home() {
        return ok(render(home));
    }

    @Route(method = HttpMethod.GET, uri = "/home")
    public Result homeContent(@Parameter("token") String token) {
        return ok(render(homeContent));
    }

    @Route(method = HttpMethod.GET, uri = "/user/logOut")
    public Result logOut(@Parameter("token") String token) {
        Session session = null;
        for(Session s : sessionList){
            if(s.getToken().toString().equals(token)){
                session = s;
            }
        }
        if(session != null) {
            sessionList.remove(session);
            return ok();
        }
        else {
            return badRequest("Unexisting session.");
        }
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
            return badRequest("Unrecognized credits.");
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
        Session session = null;
        for(Session s : sessionList) {
            if (s.getToken().toString().equals(token)) {
                session = s;
                Operation op = session.getOperation(id);
                return ok(render(describeProcess, "operation", op, "session", session));
            }
        }
        return badRequest(render(homeContent));
    }

    @Route(method = HttpMethod.POST, uri = "/execute")
    public Result execute() throws IOException {
        for(Session session : sessionList) {
            String urlContent = URLDecoder.decode(context().reader().readLine(), "UTF-8");
            String[] split = urlContent.split("&");
            String token = "";
            for(String str : split){
                String[] val = str.split("=");
                if(val[0].equals("token")){
                    token = val[1];
                }
            }
            if (session.getToken().toString().equals(token)) {
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

    @Route(method = HttpMethod.POST, uri = "/uploading")
    public Result upload(@FormParameter("upload") FileItem uploaded) {
        if (uploaded != null) {
            return ok();
        }
        return badRequest(render(homeContent));
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
        return ok(render(homeContent));
    }

    @Route(method = HttpMethod.POST, uri = "/register")
    public Result signIn() throws IOException {
        String urlContent = URLDecoder.decode(context().reader().readLine(), "UTF-8");
        String[] split = urlContent.split("&");
        Session session = CoreServerController.createSession(split[0].replaceAll(".*=", ""),
                split[1].replaceAll(".*=", ""));
        if(session != null) {
            sessionList.add(session);
            return ok(session.getToken().toString());
        }
        else {
            return badRequest("Can not create user.");
        }
    }

    @Route(method = HttpMethod.GET, uri = "/workspace")
    public Result workspace() {
        return ok(render(workspace));
    }

    @Route(method = HttpMethod.GET, uri = "/data")
    public Result data(@Parameter("token") String token) {
        for (Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                return ok(render(data));
            }
        }
        return badRequest(render(data));
    }

    @Route(method = HttpMethod.GET, uri = "/dataleftnav")
    public Result dataLeftNav(@Parameter("token") String token) {
        for (Session session : sessionList) {
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
                return ok(render(dataLeftNav,"processList", importList ));
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

        return badRequest(render(homeContent));
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

        return badRequest(render(homeContent));
    }

    @Route(method = HttpMethod.GET, uri = "/process")
    public Result process(@Parameter("token") String token) {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                return ok(render(process));
            }
        }
        return badRequest(render(process));
    }


    @Route(method = HttpMethod.GET, uri = "/process/leftNavContent")
    public Result leftNavContent(@Parameter("token") String token) {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                return ok(render(leftNavContent));
            }
        }
        return badRequest(render(process));
    }

    @Route(method = HttpMethod.GET, uri = "/user")
    public Result user(@Parameter("token") String token) {
        for(Session session : sessionList) {
            if (session.getToken().toString().equals(token)) {
                return ok(render(user, "session", session));
            }
        }
        return ok(render(user, "session", null));
    }

    @Route(method = HttpMethod.POST, uri = "/user/changePwd")
    public Result changePwd() throws IOException {
        String urlContent = URLDecoder.decode(context().reader().readLine(), "UTF-8");
        String newPassword = "";
        String newPasswordRepeat = null;
        String token = null;
        String[] split = urlContent.split("&");
        for(String argument : split){
            String[] splitArg = argument.split("=");
            switch (splitArg[0]) {
                case "pwd":
                    newPassword = splitArg[1];
                    break;
                case "pwd_repeat":
                    newPasswordRepeat = splitArg[1];
                    break;
                case "token":
                    token = splitArg[1];
                    break;
            }
        }
        if(newPassword.equals(newPasswordRepeat) && token != null) {
            CoreServerController.changePassword(token, newPassword);
            return ok("Password changed.");
        }
        else{
            return badRequest("The two passwords are not the same.");
        }
    }

    @Route(method = HttpMethod.GET, uri = "/user/settings")
    public Result settings(@Parameter("token") String token) {
        Session session = null;
        for(Session s : sessionList){
            if(s.getToken().toString().equals(token)){
                session = s;
            }
        }
        if(session != null) {
            sessionList.remove(session);
            return ok(render(userSettings, "session", session));
        }
        else {
            return badRequest("Unexisting session.");
        }
    }

    @Route(method = HttpMethod.GET, uri = "/data/database")
    public Result database(@Parameter("token") String token) {
        Session session = null;
        for(Session s : sessionList){
            if(s.getToken().toString().equals(token)){
                session = s;
            }
        }
        if(session != null) {
            DatabaseContent dbContent = session.getDatabaseContent();
            int maxSize = 0;
            for(DatabaseTable dbTable : dbContent.getTableList()){
                maxSize = Math.max(maxSize, dbTable.getFieldList().size()+1);
            }
            return ok(render(databaseView,
                    "databaseContent", dbContent,
                    "cell_width_percent", (float)(100)/maxSize));
        }
        else {
            return badRequest("Unexisting session.");
        }
    }
}
