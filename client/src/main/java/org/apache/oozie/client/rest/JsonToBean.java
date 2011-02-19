/**
 * Copyright (c) 2011 Yahoo! Inc. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. See accompanying LICENSE file.
 */
package org.apache.oozie.client.rest;

import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.client.CoordinatorJob;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON to bean converter for {@link WorkflowAction}, {@link WorkflowJob}, {@link CoordinatorAction}
 * and {@link CoordinatorJob}.
 * <p/>
 * It uses JDK dynamic proxy to create bean instances.
 */
public class JsonToBean {

    private static class Property {
        String label;
        Class type;
        boolean isList;

        public Property(String label, Class type) {
            this(label, type, false);
        }

        public Property(String label, Class type, boolean isList) {
            this.label = label;
            this.type = type;
            this.isList = isList;
        }
    }

    private static final Map<String, Property> WF_JOB = new HashMap<String, Property>();
    private static final Map<String, Property> WF_ACTION = new HashMap<String, Property>();
    private static final Map<String, Property> COORD_JOB = new HashMap<String, Property>();
    private static final Map<String, Property> COORD_ACTION = new HashMap<String, Property>();

    static {
        WF_ACTION.put("getId", new Property(JsonTags.WORKFLOW_ACTION_ID, String.class));
        WF_ACTION.put("getName", new Property(JsonTags.WORKFLOW_ACTION_NAME, String.class));
        WF_ACTION.put("getType", new Property(JsonTags.WORKFLOW_ACTION_TYPE, String.class));
        WF_ACTION.put("getConf", new Property(JsonTags.WORKFLOW_ACTION_CONF, String.class));
        WF_ACTION.put("getStatus", new Property(JsonTags.WORKFLOW_ACTION_STATUS, WorkflowAction.Status.class));
        WF_ACTION.put("getRetries", new Property(JsonTags.WORKFLOW_ACTION_RETRIES, Integer.TYPE));
        WF_ACTION.put("getStartTime", new Property(JsonTags.WORKFLOW_ACTION_START_TIME, Date.class));
        WF_ACTION.put("getEndTime", new Property(JsonTags.WORKFLOW_ACTION_END_TIME, Date.class));
        WF_ACTION.put("getTransition", new Property(JsonTags.WORKFLOW_ACTION_TRANSITION, String.class));
        WF_ACTION.put("getData", new Property(JsonTags.WORKFLOW_ACTION_DATA, String.class));
        WF_ACTION.put("getExternalId", new Property(JsonTags.WORKFLOW_ACTION_EXTERNAL_ID, String.class));
        WF_ACTION.put("getExternalStatus", new Property(JsonTags.WORKFLOW_ACTION_EXTERNAL_STATUS, String.class));
        WF_ACTION.put("getTrackerUri", new Property(JsonTags.WORKFLOW_ACTION_TRACKER_URI, String.class));
        WF_ACTION.put("getConsoleUrl", new Property(JsonTags.WORKFLOW_ACTION_CONSOLE_URL, String.class));
        WF_ACTION.put("getErrorCode", new Property(JsonTags.WORKFLOW_ACTION_ERROR_CODE, String.class));
        WF_ACTION.put("getErrorMessage", new Property(JsonTags.WORKFLOW_ACTION_ERROR_MESSAGE, String.class));

        WF_JOB.put("getAppPath", new Property(JsonTags.WORKFLOW_APP_PATH, String.class));
        WF_JOB.put("getAppName", new Property(JsonTags.WORKFLOW_APP_NAME, String.class));
        WF_JOB.put("getId", new Property(JsonTags.WORKFLOW_ID, String.class));
        WF_JOB.put("getConf", new Property(JsonTags.WORKFLOW_CONF, String.class));
        WF_JOB.put("getStatus", new Property(JsonTags.WORKFLOW_STATUS, WorkflowJob.Status.class));
        WF_JOB.put("getLastModifiedTime", new Property(JsonTags.WORKFLOW_LAST_MOD_TIME, Date.class));
        WF_JOB.put("getCreatedTime", new Property(JsonTags.WORKFLOW_CREATED_TIME, Date.class));
        WF_JOB.put("getStartTime", new Property(JsonTags.WORKFLOW_CREATED_TIME, Date.class));
        WF_JOB.put("getEndTime", new Property(JsonTags.WORKFLOW_END_TIME, Date.class));
        WF_JOB.put("getUser", new Property(JsonTags.WORKFLOW_USER, String.class));
        WF_JOB.put("getGroup", new Property(JsonTags.WORKFLOW_GROUP, String.class));
        WF_JOB.put("getRun", new Property(JsonTags.WORKFLOW_RUN, Integer.TYPE));
        WF_JOB.put("getConsoleUrl", new Property(JsonTags.WORKFLOW_CONSOLE_URL, String.class));
        WF_JOB.put("getActions", new Property(JsonTags.WORKFLOW_ACTIONS, WorkflowAction.class, true));


        COORD_ACTION.put("getId", new Property(JsonTags.COORDINATOR_ACTION_ID, String.class));
        COORD_ACTION.put("getJobId", new Property(JsonTags.COORDINATOR_JOB_ID, String.class));
        COORD_ACTION.put("getActionNumber", new Property(JsonTags.COORDINATOR_ACTION_NUMBER, Integer.TYPE));
        COORD_ACTION.put("getCreatedConf", new Property(JsonTags.COORDINATOR_ACTION_CREATED_CONF, String.class));
        COORD_ACTION.put("getCreatedTime", new Property(JsonTags.COORDINATOR_ACTION_CREATED_TIME, Date.class));
        COORD_ACTION.put("getNominalTime", new Property(JsonTags.COORDINATOR_ACTION_NOMINAL_TIME, Date.class));
        COORD_ACTION.put("getExternalId", new Property(JsonTags.COORDINATOR_ACTION_EXTERNALID, String.class));
        COORD_ACTION.put("getStatus", new Property(JsonTags.COORDINATOR_ACTION_STATUS, CoordinatorAction.Status.class));
        COORD_ACTION.put("getRunConf", new Property(JsonTags.COORDINATOR_ACTION_RUNTIME_CONF, String.class));
        COORD_ACTION
                .put("getLastModifiedTime", new Property(JsonTags.COORDINATOR_ACTION_LAST_MODIFIED_TIME, Date.class));
        COORD_ACTION
                .put("getMissingDependencies", new Property(JsonTags.COORDINATOR_ACTION_MISSING_DEPS, String.class));
        COORD_ACTION.put("getExternalStatus", new Property(JsonTags.COORDINATOR_ACTION_EXTERNAL_STATUS, String.class));
        COORD_ACTION.put("getTrackerUri", new Property(JsonTags.COORDINATOR_ACTION_TRACKER_URI, String.class));
        COORD_ACTION.put("getConsoleUrl", new Property(JsonTags.COORDINATOR_ACTION_CONSOLE_URL, String.class));
        COORD_ACTION.put("getErrorCode", new Property(JsonTags.COORDINATOR_ACTION_ERROR_CODE, String.class));
        COORD_ACTION.put("getErrorMessage", new Property(JsonTags.COORDINATOR_ACTION_ERROR_MESSAGE, String.class));

        COORD_JOB.put("getAppPath", new Property(JsonTags.COORDINATOR_JOB_PATH, String.class));
        COORD_JOB.put("getAppName", new Property(JsonTags.COORDINATOR_JOB_NAME, String.class));
        COORD_JOB.put("getId", new Property(JsonTags.COORDINATOR_JOB_ID, String.class));
        COORD_JOB.put("getConf", new Property(JsonTags.COORDINATOR_JOB_CONF, String.class));
        COORD_JOB.put("getStatus", new Property(JsonTags.COORDINATOR_JOB_STATUS, CoordinatorJob.Status.class));
        COORD_JOB.put("getExecutionOrder",
                      new Property(JsonTags.COORDINATOR_JOB_EXECUTIONPOLICY, CoordinatorJob.Execution.class));
        COORD_JOB.put("getFrequency", new Property(JsonTags.COORDINATOR_JOB_FREQUENCY, Integer.TYPE));
        COORD_JOB.put("getTimeUnit", new Property(JsonTags.COORDINATOR_JOB_TIMEUNIT, CoordinatorJob.Timeunit.class));
        COORD_JOB.put("getTimeZone", new Property(JsonTags.COORDINATOR_JOB_TIMEZONE, String.class));
        COORD_JOB.put("getConcurrency", new Property(JsonTags.COORDINATOR_JOB_CONCURRENCY, Integer.TYPE));
        COORD_JOB.put("getTimeout", new Property(JsonTags.COORDINATOR_JOB_TIMEOUT, Integer.TYPE));
        COORD_JOB.put("getLastActionTime", new Property(JsonTags.COORDINATOR_JOB_LAST_ACTION_TIME, Date.class));
        COORD_JOB.put("getNextMaterializedTime",
                      new Property(JsonTags.COORDINATOR_JOB_NEXT_MATERIALIZED_TIME, Date.class));
        COORD_JOB.put("getStartTime", new Property(JsonTags.COORDINATOR_JOB_START_TIME, Date.class));
        COORD_JOB.put("getEndTime", new Property(JsonTags.COORDINATOR_JOB_END_TIME, Date.class));
        COORD_JOB.put("getUser", new Property(JsonTags.COORDINATOR_JOB_USER, String.class));
        COORD_JOB.put("getGroup", new Property(JsonTags.COORDINATOR_JOB_GROUP, String.class));
        COORD_JOB.put("getConsoleUrl", new Property(JsonTags.COORDINATOR_JOB_CONSOLE_URL, String.class));
        COORD_JOB.put("getActions", new Property(JsonTags.COORDINATOR_ACTIONS, CoordinatorAction.class, true));
    }

    /**
     * The dynamic proxy invocation handler used to convert JSON values to bean properties using a mapping.
     */
    private static class JsonInvocationHandler implements InvocationHandler {
        private Map<String, Property> mapping;
        private JSONObject json;

        /**
         * Invocation handler constructor.
         *
         * @param mapping property to JSON/type-info mapping.
         * @param json the json object to back the property values.
         */
        public JsonInvocationHandler(Map<String, Property> mapping, JSONObject json) {
            this.mapping = mapping;
            this.json = json;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            Property prop = mapping.get(method.getName());
            if (prop == null) {
                throw new RuntimeException("Undefined method mapping: " + method.getName());
            }
            if (prop.isList) {
                if (prop.type == WorkflowAction.class) {
                    return createWorkflowActionList((JSONArray) json.get(prop.label));
                }
                else if (prop.type == CoordinatorAction.class) {
                    return createCoordinatorActionList((JSONArray) json.get(prop.label));
                }
                else {
                    throw new RuntimeException("Unsupported list type : " + prop.type.getSimpleName());
                }
            }
            else {
                return parseType(prop.type, json.get(prop.label));
            }
        }

        @SuppressWarnings("unchecked")
        private Object parseType(Class type, Object obj) {
            if (type == String.class) {
                return obj;
            }
            else if (type == Integer.TYPE) {
                return (obj != null) ? new Integer(((Long) obj).intValue()) : new Integer(0);
            }
            else if (type == Long.TYPE) {
                return (obj != null) ? obj : new Long(0);
            }
            else if (type == Date.class) {
                return JsonUtils.parseDateRfc822((String) obj);
            }
            else if (type.isEnum()) {
                return Enum.valueOf(type, (String) obj);
            }
            else if (type == WorkflowAction.class) {
                return createWorkflowAction((JSONObject) obj);
            }
            else {
                throw new RuntimeException("Unsupported type : " + type.getSimpleName());
            }
        }
    }

    /**
     * Creates a workflow action bean from a JSON object.
     * 
     * @param json json object.
     * @return a workflow action bean populated with the JSON object values.
     */
    public static WorkflowAction createWorkflowAction(JSONObject json) {
        return (WorkflowAction) Proxy.newProxyInstance(JsonToBean.class.getClassLoader(),
                                                       new Class[]{WorkflowAction.class},
                                                       new JsonInvocationHandler(WF_ACTION, json));
    }

    /**
     * Creates a list of workflow action beans from a JSON array.
     * 
     * @param json json array.
     * @return a list of workflow action beans from a JSON array.
     */
    public static List<WorkflowAction> createWorkflowActionList(JSONArray json) {
        List<WorkflowAction> list = new ArrayList<WorkflowAction>();
        for (Object obj : json) {
            list.add(createWorkflowAction((JSONObject) obj));
        }
        return list;
    }

    /**
     * Creates a workflow job bean from a JSON object.
     * 
     * @param json json object.
     * @return a workflow job bean populated with the JSON object values.
     */
    public static WorkflowJob createWorkflowJob(JSONObject json) {
        return (WorkflowJob) Proxy.newProxyInstance(JsonToBean.class.getClassLoader(),
                                                    new Class[]{WorkflowJob.class},
                                                    new JsonInvocationHandler(WF_JOB, json));
    }

    /**
     * Creates a list of workflow job beans from a JSON array.
     * 
     * @param json json array.
     * @return a list of workflow job beans from a JSON array.
     */
    public static List<WorkflowJob> createWorkflowJobList(JSONArray json) {
        List<WorkflowJob> list = new ArrayList<WorkflowJob>();
        for (Object obj : json) {
            list.add(createWorkflowJob((JSONObject) obj));
        }
        return list;
    }

    /**
     * Creates a coordinator action bean from a JSON object.
     * 
     * @param json json object.
     * @return a coordinator action bean populated with the JSON object values.
     */
    public static CoordinatorAction createCoordinatorAction(JSONObject json) {
        return (CoordinatorAction) Proxy.newProxyInstance(JsonToBean.class.getClassLoader(),
                                                          new Class[]{CoordinatorAction.class},
                                                          new JsonInvocationHandler(COORD_ACTION, json));
    }

    /**
     * Creates a list of coordinator action beans from a JSON array.
     * 
     * @param json json array.
     * @return a list of coordinator action beans from a JSON array.
     */
    public static List<CoordinatorAction> createCoordinatorActionList(JSONArray json) {
        List<CoordinatorAction> list = new ArrayList<CoordinatorAction>();
        for (Object obj : json) {
            list.add(createCoordinatorAction((JSONObject) obj));
        }
        return list;
    }

    /**
     * Creates a coordinator job bean from a JSON object.
     * 
     * @param json json object.
     * @return a coordinator job bean populated with the JSON object values.
     */
    public static CoordinatorJob createCoordinatorJob(JSONObject json) {
        return (CoordinatorJob) Proxy.newProxyInstance(JsonToBean.class.getClassLoader(),
                                                       new Class[]{CoordinatorJob.class},
                                                       new JsonInvocationHandler(COORD_JOB, json));
    }

    /**
     * Creates a list of coordinator job beans from a JSON array.
     * 
     * @param json json array.
     * @return a list of coordinator job beans from a JSON array.
     */
    public static List<CoordinatorJob> createCoordinatorJobList(JSONArray json) {
        List<CoordinatorJob> list = new ArrayList<CoordinatorJob>();
        for (Object obj : json) {
            list.add(createCoordinatorJob((JSONObject) obj));
        }
        return list;
    }

}
