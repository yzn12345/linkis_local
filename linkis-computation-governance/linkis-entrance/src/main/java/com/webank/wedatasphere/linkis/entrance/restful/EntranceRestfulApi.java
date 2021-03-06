/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.entrance.restful;

import com.webank.wedatasphere.linkis.common.log.LogUtils;
import com.webank.wedatasphere.linkis.entrance.EntranceServer;
import com.webank.wedatasphere.linkis.entrance.annotation.EntranceServerBeanAnnotation;
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration;
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob;
import com.webank.wedatasphere.linkis.entrance.log.LogReader;
import com.webank.wedatasphere.linkis.entrance.utils.JobHistoryHelper;
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest;
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant;
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo;
import com.webank.wedatasphere.linkis.protocol.utils.ZuulEntranceUtils;
import com.webank.wedatasphere.linkis.rpc.Sender;
import com.webank.wedatasphere.linkis.scheduler.listener.LogListener;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import scala.Option;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Description: an implementation class of EntranceRestfulRemote
 */
@Path("/entrance")
@Component
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EntranceRestfulApi implements EntranceRestfulRemote {

    private EntranceServer entranceServer;

    private static final Logger logger = LoggerFactory.getLogger(EntranceRestfulApi.class);

    @EntranceServerBeanAnnotation.EntranceServerAutowiredAnnotation
    public void setEntranceServer(EntranceServer entranceServer) {
        this.entranceServer = entranceServer;
    }

    /**
     * The execute function handles the request submitted by the user to execute the task, and the execution ID is returned to the user.
     * execute?????????????????????????????????????????????????????????????????????????????????ID
     * json Incoming key-value pair(??????????????????)
     * Repsonse
     */
    @Override
    @POST
    @Path("/execute")
    public Response execute(@Context HttpServletRequest req, Map<String, Object> json) {
        Message message = null;
//        try{
        logger.info("Begin to get an execID");
        json.put(TaskConstant.UMUSER, SecurityFilter.getLoginUsername(req));
        HashMap<String, String> map = (HashMap) json.get(TaskConstant.SOURCE);
        if(map == null){
            map = new HashMap<>();
            json.put(TaskConstant.SOURCE, map);
        }
        String ip = JobHistoryHelper.getRequestIpAddr(req);
        map.put(TaskConstant.REQUEST_IP, ip);
        String execID = entranceServer.execute(json);
        Job job = entranceServer.getJob(execID).get();
        JobRequest jobReq = ((EntranceJob) job).getJobRequest();
        Long taskID = jobReq.getId();
        pushLog(LogUtils.generateInfo("You have submitted a new job, script code (after variable substitution) is"), job);
        pushLog("************************************SCRIPT CODE************************************", job);
        pushLog(jobReq.getExecutionCode(), job);
        pushLog("************************************SCRIPT CODE************************************", job);
        pushLog(LogUtils.generateInfo("Your job is accepted,  jobID is " + execID + " and taskID is " + taskID + " in " + Sender.getThisServiceInstance().toString() + ". Please wait it to be scheduled"), job);
        execID = ZuulEntranceUtils.generateExecID(execID, Sender.getThisServiceInstance().getApplicationName(), new String[]{Sender.getThisInstance()});
        message = Message.ok();
        message.setMethod("/api/entrance/execute");
        message.data("execID", execID);
        message.data("taskID", taskID);
        logger.info("End to get an an execID: {}, taskID: {}", execID, taskID);
//        }catch(ErrorException e){
//            message = Message.error(e.getDesc());
//            message.setStatus(1);
//            message.setMethod("/api/entrance/execute");
//        }
        return Message.messageToResponse(message);

    }

    @Override
    @POST
    @Path("/submit")
    public Response submit(@Context HttpServletRequest req, Map<String, Object> json) {
        Message message = null;
        logger.info("Begin to get an execID");
        json.put(TaskConstant.SUBMIT_USER, SecurityFilter.getLoginUsername(req));
        HashMap<String, String> map = (HashMap) json.get(TaskConstant.SOURCE);
        if(map == null){
            map = new HashMap<>();
            json.put(TaskConstant.SOURCE, map);
        }
        String ip = JobHistoryHelper.getRequestIpAddr(req);
        map.put(TaskConstant.REQUEST_IP, ip);
        String execID = entranceServer.execute(json);
        Job job = entranceServer.getJob(execID).get();
        JobRequest jobRequest = ((EntranceJob) job).getJobRequest();
        Long taskID = jobRequest.getId();
        pushLog(LogUtils.generateInfo("You have submitted a new job, script code (after variable substitution) is"), job);
        pushLog("************************************SCRIPT CODE************************************", job);
        pushLog(jobRequest.getExecutionCode(), job);
        pushLog("************************************SCRIPT CODE************************************", job);
        pushLog(LogUtils.generateInfo("Your job is accepted,  jobID is " + execID + " and taskID is " + taskID + " in " + Sender.getThisServiceInstance().toString() + ". Please wait it to be scheduled"), job);
        execID = ZuulEntranceUtils.generateExecID(execID, Sender.getThisServiceInstance().getApplicationName(), new String[]{Sender.getThisInstance()});
        message = Message.ok();
        message.setMethod("/api/entrance/submit");
        message.data("execID", execID);
        message.data("taskID", taskID);
        logger.info("End to get an an execID: {}, taskID: {}", execID, taskID);
        return Message.messageToResponse(message);
    }

    private void pushLog(String log, Job job) {
        entranceServer.getEntranceContext().getOrCreateLogManager().onLogUpdate(job, log);
    }

    @Override
    @GET
    @Path("/{id}/status")
    public Response status(@PathParam("id") String id, @QueryParam("taskID") String taskID) {
        Message message = null;
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        Option<Job> job = Option.apply(null);
        try {
            job = entranceServer.getJob(realId);
        } catch (Exception e) {
            logger.warn("???????????? {} ?????????????????????", realId, e.getMessage());
            long realTaskID = Long.parseLong(taskID);
            String status = JobHistoryHelper.getStatusByTaskID(realTaskID);
            message = Message.ok();
            message.setMethod("/api/entrance/" + id + "/status");
            message.data("status", status).data("execID", id);
            return Message.messageToResponse(message);
        }
        if (job.isDefined()) {
            message = Message.ok();
            message.setMethod("/api/entrance/" + id + "/status");
            message.data("status", job.get().getState().toString()).data("execID", id);
        } else {
            message = Message.error("ID The corresponding job is empty and cannot obtain the corresponding task status.(ID ?????????job??????????????????????????????????????????)");
        }
        return Message.messageToResponse(message);
    }




    @Override
    @GET
    @Path("/{id}/progress")
    public Response progress(@PathParam("id") String id) {
        Message message = null;
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        Option<Job> job = entranceServer.getJob(realId);
        if (job.isDefined()) {
            JobProgressInfo[] jobProgressInfos = ((EntranceJob) job.get()).getProgressInfo();
            if (jobProgressInfos == null) {
                message = Message.error("Can not get the corresponding progress information, it may be that the corresponding progress information has not been generated(?????????????????????????????????,??????????????????????????????????????????)");
                message.setMethod("/api/entrance/" + id + "/progress");
            } else {
                List<Map<String, Object>> list = new ArrayList<>();
                for (JobProgressInfo jobProgressInfo : jobProgressInfos) {
                    if ("true".equals(EntranceConfiguration.PROGRESS_PUSH().getValue())) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", jobProgressInfo.id());
                        map.put("succeedTasks", jobProgressInfo.succeedTasks());
                        map.put("failedTasks", jobProgressInfo.failedTasks());
                        map.put("runningTasks", jobProgressInfo.runningTasks());
                        map.put("totalTasks", jobProgressInfo.totalTasks());
                        list.add(map);
                    } else if (jobProgressInfo.failedTasks() > 0 || jobProgressInfo.runningTasks() > 0) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", jobProgressInfo.id());
                        map.put("succeedTasks", jobProgressInfo.succeedTasks());
                        map.put("failedTasks", jobProgressInfo.failedTasks());
                        map.put("runningTasks", jobProgressInfo.runningTasks());
                        map.put("totalTasks", jobProgressInfo.totalTasks());
                        list.add(map);
                    }
                }
                message = Message.ok();
                message.setMethod("/api/entrance/" + id + "/progress");
                //TODO ?????????????????????
                message.data("progress", Math.abs(job.get().getProgress())).data("execID", id).data("progressInfo", list);
            }
        } else {
            message = Message.error("The job corresponding to the ID is empty, and the corresponding task progress cannot be obtained.(ID ?????????job??????????????????????????????????????????)");
        }
        return Message.messageToResponse(message);
    }

    @Override
    @GET
    @Path("/{id}/log")
    public Response log(@Context HttpServletRequest req, @PathParam("id") String id) {
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        Option<Job> job = Option.apply(null);
        Message message = null;
        try {
            job = entranceServer.getJob(realId);
        } catch (final Throwable t) {
            message = Message.error("The job you just executed has ended. This interface no longer provides a query. It is recommended that you download the log file for viewing.(??????????????????job????????????????????????????????????????????????????????????????????????????????????)");
            message.setMethod("/api/entrance/" + id + "/log");
            return Message.messageToResponse(message);
        }
        if (job.isDefined()) {
            logger.debug("begin to get log for {}(???????????? {} ?????????)", job.get().getId(),job.get().getId());
            LogReader logReader = entranceServer.getEntranceContext().getOrCreateLogManager().getLogReader(realId);
            int fromLine = 0;
            int size = 100;
            boolean distinctLevel = true;
            if (req != null) {
                String fromLineStr = req.getParameter("fromLine");
                String sizeStr = req.getParameter("size");
                if (StringUtils.isNotBlank(fromLineStr)) {
                    fromLine = Math.max(Integer.parseInt(fromLineStr), 0);
                }
                if (StringUtils.isNotBlank(sizeStr)) {
                    size = Integer.parseInt(sizeStr) >= 0 ? Integer.parseInt(sizeStr) : 10000;
                }
                String distinctLevelStr = req.getParameter("distinctLevel");
                if ("false".equals(distinctLevelStr)) {
                    distinctLevel = false;
                }
            }

            Object retLog = null;
            int retFromLine = 0;
            try {
                if (distinctLevel) {
                    String[] logs = new String[4];
                    retFromLine = logReader.readArray(logs, fromLine, size);
                    retLog = new ArrayList<String>(Arrays.asList(logs));
                } else {
                    StringBuilder sb = new StringBuilder();
                    retFromLine = logReader.read(sb, fromLine, size);
                    retLog = sb.toString();
                }
            } catch (IllegalStateException e) {
                logger.debug("Failed to get log information for :{}(??? {} ??????????????????)", job.get().getId(), job.get().getId(),e);
                message = Message.ok();
                message.setMethod("/api/entrance/" + id + "/log");
                message.data("log", "").data("execID", id).data("fromLine", retFromLine + fromLine);
            } catch (final IllegalArgumentException e) {
                logger.debug("Failed to get log information for :{}(??? {} ??????????????????)", job.get().getId(), job.get().getId(),e);
                message = Message.ok();
                message.setMethod("/api/entrance/" + id + "/log");
                message.data("log", "").data("execID", id).data("fromLine", retFromLine + fromLine);
                return Message.messageToResponse(message);
            } catch (final Exception e1) {
                logger.debug("Failed to get log information for :{}(??? {} ??????????????????)", job.get().getId(), job.get().getId(),e1);
                message = Message.error("Failed to get log information(????????????????????????)");
                message.setMethod("/api/entrance/" + id + "/log");
                message.data("log", "").data("execID", id).data("fromLine", retFromLine + fromLine);
                return Message.messageToResponse(message);
            } finally {
                if (null != logReader && job.get().isCompleted()) {
                    IOUtils.closeQuietly(logReader);
                }
            }
            message = Message.ok();
            message.setMethod("/api/entrance/" + id + "/log");
            message.data("log", retLog).data("execID", id).data("fromLine", retFromLine + fromLine);
            logger.debug("success to get log for {} (?????? {} ????????????)", job.get().getId(),job.get().getId());
        } else {
            message = Message.error("Can't find execID(????????????execID): " + id + "Corresponding job, can not get the corresponding log(?????????job??????????????????????????????)");
            message.setMethod("/api/entrance/" + id + "/log");
        }
        return Message.messageToResponse(message);
    }

    @Override
    @POST
    @Path("/{id}/killJobs")
    public Response killJobs(@Context HttpServletRequest req, JsonNode jsonNode, @PathParam("id") String strongExecId) {
        JsonNode idNode = jsonNode.get("idList");
        JsonNode taskIDNode = jsonNode.get("taskIDList");
        ArrayList<Long> waitToForceKill = new ArrayList<>();
        if(idNode.size() != taskIDNode.size()){
            return Message.messageToResponse(Message.error("The length of the ID list does not match the length of the TASKID list(id??????????????????taskId????????????????????????)"));
        }
        if(!idNode.isArray() || !taskIDNode.isArray()){
            return Message.messageToResponse(Message.error("Request parameter error, please use array(????????????????????????????????????)"));
        }
        ArrayList<Message> messages = new ArrayList<>();
        for(int i = 0; i < idNode.size(); i++){
            String id = idNode.get(i).asText();
            Long taskID = taskIDNode.get(i).asLong();
            String realId = ZuulEntranceUtils.parseExecID(id)[3];
            //??????jobid??????job,???????????????job?????????????????????looparray?????????,??????????????????????????????????????????????????????Cancenlled
            Option<Job> job = Option.apply(null);
            try {
                job = entranceServer.getJob(realId);
            } catch (Exception e) {
                logger.warn("can not find a job in entranceServer, will force to kill it", e.getMessage());
                //?????????????????????????????????????????????????????????????????????????????????????????????????????????
                waitToForceKill.add(taskID);
                Message message = Message.ok("Forced Kill task (??????????????????)");
                message.setMethod("/api/entrance/" + id + "/kill");
                message.setStatus(0);
                messages.add(message);
                continue;
            }
            Message message = null;
            if (job.isEmpty()) {
                logger.warn("can not find a job in entranceServer, will force to kill it");
                waitToForceKill.add(taskID);
                message = Message.ok("Forced Kill task (??????????????????)");
                message.setMethod("/api/entrance/" + id + "/killJobs");
                message.setStatus(0);
                messages.add(message);
            } else {
                try {
                    logger.info("begin to kill job {} ", job.get().getId());
                    job.get().kill();
                    message = Message.ok("Successfully killed the job(??????kill???job)");
                    message.setMethod("/api/entrance/" + id + "/kill");
                    message.setStatus(0);
                    message.data("execID", id);
                    //ensure the job's state is cancelled in database
                    if (job.get() instanceof EntranceJob) {
                        EntranceJob entranceJob = (EntranceJob) job.get();
                        JobRequest jobReq = entranceJob.getJobRequest();
                        entranceJob.updateJobRequestStatus(SchedulerEventState.Cancelled().toString());
                        jobReq.setProgress("1.0f");
                        LogListener logListener = entranceJob.getLogListener().getOrElse(null);
                        if (null != logListener) {
                            logListener.onLogUpdate(entranceJob, "Job " + jobReq.getId() + " was kill by user successfully(??????" + jobReq.getId() + "???????????????)");
                        }
                        this.entranceServer.getEntranceContext().getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(jobReq);
                    }
                    logger.info("end to kill job {} ", job.get().getId());
                } catch (Throwable t) {
                    logger.error("kill job {} failed ", job.get().getId(), t);
                    message = Message.error("An exception occurred while killing the job, kill failed(kill job???????????????????????????kill??????)");
                    message.setMethod("/api/entrance/" + id + "/kill");
                    message.setStatus(1);
                }
            }
            messages.add(message);
        }
        if(!waitToForceKill.isEmpty()){
            JobHistoryHelper.forceBatchKill(waitToForceKill);
        }
        return Message.messageToResponse(Message.ok("??????????????????").data("messages", messages));
    }

    @Override
    @GET
    @Path("/{id}/kill")
    public Response kill(@PathParam("id") String id, @QueryParam("taskID") long taskID) {
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        //??????jobid??????job,???????????????job?????????????????????looparray?????????,??????????????????????????????????????????????????????Cancenlled
        Option<Job> job = Option.apply(null);
        try {
            job = entranceServer.getJob(realId);
        } catch (Exception e) {
            logger.warn("can not find a job in entranceServer, will force to kill it", e);
            //?????????????????????????????????????????????????????????????????????????????????????????????????????????
            JobHistoryHelper.forceKill(taskID);
            Message message = Message.ok("Forced Kill task (??????????????????)");
            message.setMethod("/api/entrance/" + id + "/kill");
            message.setStatus(0);
            return Message.messageToResponse(message);
        }
        Message message = null;
        if (job.isEmpty()) {
            logger.warn("can not find a job in entranceServer, will force to kill it");
            //?????????????????????????????????????????????????????????????????????????????????????????????????????????
            JobHistoryHelper.forceKill(taskID);
            message = Message.ok("Forced Kill task (??????????????????)");
            message.setMethod("/api/entrance/" + id + "/kill");
            message.setStatus(0);
            return Message.messageToResponse(message);
        } else {
            try {
                logger.info("begin to kill job {} ", job.get().getId());
                job.get().kill();
                message = Message.ok("Successfully killed the job(??????kill???job)");
                message.setMethod("/api/entrance/" + id + "/kill");
                message.setStatus(0);
                message.data("execID", id);
                //ensure the job's state is cancelled in database
                if (job.get() instanceof EntranceJob) {
                    EntranceJob entranceJob = (EntranceJob) job.get();
                    JobRequest jobReq = entranceJob.getJobRequest();
                        entranceJob.updateJobRequestStatus(SchedulerEventState.Cancelled().toString());
                    this.entranceServer.getEntranceContext().getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(jobReq);
                }
                logger.info("end to kill job {} ", job.get().getId());
            } catch (Throwable t) {
                logger.error("kill job {} failed ", job.get().getId(), t);
                message = Message.error("An exception occurred while killing the job, kill failed(kill job???????????????????????????kill??????)");
                message.setMethod("/api/entrance/" + id + "/kill");
                message.setStatus(1);
            }
        }
        return Message.messageToResponse(message);
    }

    @Override
    @GET
    @Path("/{id}/pause")
    public Response pause(@PathParam("id") String id) {
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        Option<Job> job = entranceServer.getJob(realId);
        Message message = null;
        if (job.isEmpty()) {
            message = Message.error("can not find the job of exexID :" + id +" can not pause (????????????execID: " + id + "?????????job???????????????pause)");
            message.setMethod("/api/entrance/" + id + "/pause");
            message.setStatus(1);
        } else {
            try {
                //todo job pause ???????????????????????????
                //job.pause();
                logger.info("begin to pause job {} ", job.get().getId());
                message = Message.ok("success to pause job (??????pause???job)");
                message.setStatus(0);
                message.data("execID", id);
                message.setMethod("/api/entrance/" + id + "/pause");
                logger.info("end to pause job {} ", job.get().getId());
            } catch (Throwable t) {
                logger.info("pause job {} failed ", job.get().getId());
                message = Message.error("Abnormal when pausing job, pause failed(pause job???????????????????????????pause??????)");
                message.setMethod("/api/entrance/" + id + "/pause");
                message.setStatus(1);
            }
        }

        return Message.messageToResponse(message);
    }

}
