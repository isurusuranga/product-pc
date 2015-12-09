/**
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.wso2.carbon.pc.analytics.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.pc.analytics.core.clients.AnalyticsRestClient;
import org.wso2.carbon.pc.analytics.core.models.AggregateField;
import org.wso2.carbon.pc.analytics.core.models.AggregateQuery;
import org.wso2.carbon.pc.analytics.core.utils.AnalyticsUtils;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * UserLevelMonitor holds all the functionalities for the user level monitoring
 */
public class UserLevelMonitor {
	private static final Log log = LogFactory.getLog(UserLevelMonitor.class);

	/**
	 * perform query: SELECT assignUser, SUM(duration) AS totalInvolvedTime FROM
	 *                USER_INVOLVE_SUMMARY WHERE <date range> GROUP BY assignUser;
	 *
	 * @param filters is used to filter the result
	 * @return the result as a JSON string
	 */
	public String getTotalInvolvedTimeVsUserId(String filters) {
		String sortedResult = "";
		try {
			if (AnalyticsUtils.isDASAnalyticsActivated()) {
				JSONObject filterObj = new JSONObject(filters);
				long from = filterObj.getLong(AnalyticsConstants.START_TIME);
				long to = filterObj.getLong(AnalyticsConstants.END_TIME);
				String order = filterObj.getString(AnalyticsConstants.ORDER);
				int userCount = filterObj.getInt(AnalyticsConstants.NUM_COUNT);

				AggregateField sumField = new AggregateField();
				sumField.setFieldName(AnalyticsConstants.DURATION);
				sumField.setAggregate(AnalyticsConstants.SUM);
				sumField.setAlias(AnalyticsConstants.TOTAL_INVOLVED_TIME);

				ArrayList<AggregateField> aggregateFields = new ArrayList<>();
				aggregateFields.add(sumField);

				AggregateQuery query = new AggregateQuery();
				query.setTableName(AnalyticsConstants.USER_INVOLVE_TABLE);
				query.setGroupByField(AnalyticsConstants.ASSIGN_USER);
				if (from != 0 && to != 0) {
					query.setQuery(AnalyticsUtils.getDateRangeQuery(
							AnalyticsConstants.COLUMN_FINISHED_TIME, from, to));
				}
				query.setAggregateFields(aggregateFields);

				if (log.isDebugEnabled()) {
					log.debug(AnalyticsUtils.getJSONString(query));
				}

				String result = AnalyticsRestClient
								.post(AnalyticsUtils.getURL(AnalyticsConstants.ANALYTICS_AGGREGATE),
						        AnalyticsUtils.getJSONString(query));

				JSONArray unsortedResultArray = new JSONArray(result);
				Hashtable<String, Double> table = new Hashtable<>();

				if (unsortedResultArray.length() != 0) {
					for (int i = 0; i < unsortedResultArray.length(); i++) {
						JSONObject jsonObj = unsortedResultArray.getJSONObject(i);
						JSONObject values = jsonObj.getJSONObject("values");
						String assignee = values.getJSONArray("assignUser").getString(0);
						double totalInvolvedTime = values.getDouble("totalInvolvedTime");
						table.put(assignee, totalInvolvedTime);
					}
					sortedResult = AnalyticsUtils
							.getDoubleValueSortedList(table, "assignUser", "totalInvolvedTime",
							                          order, userCount);
				}
			}
		} catch (Exception e) {
			String errMsg = "PC Analytics core UserLevelMonitoring error.";
			log.error(errMsg, e);
		}
		if (log.isDebugEnabled()) {
			log.debug("Result = " + sortedResult);
		}
		return sortedResult;
	}

	/**
	 * perform query: SELECT assignUser, COUNT(*) AS completedTotalTasks FROM
	 *                USER_INVOLVE_SUMMARY_DATA WHERE <date range> GROUP BY assignUser;
	 *
	 * @param filters is used to filter the result
	 * @return the result as a JSON string
	 */
	public String getTotalCompletedTasksVsUserId(String filters) {
		String sortedResult = "";
		try {
			if (AnalyticsUtils.isDASAnalyticsActivated()) {
				JSONObject filterObj = new JSONObject(filters);
				long from = filterObj.getLong(AnalyticsConstants.START_TIME);
				long to = filterObj.getLong(AnalyticsConstants.END_TIME);
				String order = filterObj.getString(AnalyticsConstants.ORDER);
				int userCount = filterObj.getInt(AnalyticsConstants.NUM_COUNT);

				AggregateField countField = new AggregateField();
				countField.setFieldName(AnalyticsConstants.ALL);
				countField.setAggregate(AnalyticsConstants.COUNT);
				countField.setAlias(AnalyticsConstants.COMPLETED_TOTAL_TASKS);

				ArrayList<AggregateField> aggregateFields = new ArrayList<>();
				aggregateFields.add(countField);

				AggregateQuery query = new AggregateQuery();
				query.setTableName(AnalyticsConstants.USER_INVOLVE_TABLE);
				query.setGroupByField(AnalyticsConstants.ASSIGN_USER);
				if (from != 0 && to != 0) {
					query.setQuery(AnalyticsUtils.getDateRangeQuery(
							AnalyticsConstants.COLUMN_FINISHED_TIME, from, to));
				}
				query.setAggregateFields(aggregateFields);

				if (log.isDebugEnabled()) {
					log.debug(AnalyticsUtils.getJSONString(query));
				}

				String result = AnalyticsRestClient
								.post(AnalyticsUtils.getURL(AnalyticsConstants.ANALYTICS_AGGREGATE),
						        AnalyticsUtils.getJSONString(query));

				JSONArray unsortedResultArray = new JSONArray(result);
				Hashtable<String, Integer> table = new Hashtable<>();

				if (unsortedResultArray.length() != 0) {
					for (int i = 0; i < unsortedResultArray.length(); i++) {
						JSONObject jsonObj = unsortedResultArray.getJSONObject(i);
						JSONObject values = jsonObj.getJSONObject("values");
						String assignee = values.getJSONArray("assignUser").getString(0);
						int totalInvolvedTime = values.getInt("completedTotalTasks");
						table.put(assignee, totalInvolvedTime);
					}
					sortedResult = AnalyticsUtils
							.getIntegerValueSortedList(table, "assignUser", "completedTotalTasks",
							                           order, userCount);
				}
			}
		} catch (Exception e) {
			String errMsg = "PC Analytics core UserLevelMonitoring error.";
			log.error(errMsg, e);
		}
		if (log.isDebugEnabled()) {
			log.debug("Result = " + sortedResult);
		}
		return sortedResult;
	}

	/**
	 * perform query: SELECT processDefKey, SUM(duration) AS totalInvolvedTime FROM
	 *                USER_INVOLVE_SUMMARY WHERE <assignee> AND <date range> GROUP BY processDefKey;
	 *
	 * @param filters is used to filter the result
	 * @return the result as a JSON string
	 */
	public String getTotalInvolvedTimeVsProcessId(String filters) {
		String sortedResult = "";
		try {
			if (AnalyticsUtils.isDASAnalyticsActivated()) {
				JSONObject filterObj = new JSONObject(filters);
				long from = filterObj.getLong(AnalyticsConstants.START_TIME);
				long to = filterObj.getLong(AnalyticsConstants.END_TIME);
				String userId = filterObj.getString(AnalyticsConstants.USER_ID);
				String order = filterObj.getString(AnalyticsConstants.ORDER);
				int count = filterObj.getInt(AnalyticsConstants.NUM_COUNT);

				AggregateField sumField = new AggregateField();
				sumField.setFieldName(AnalyticsConstants.DURATION);
				sumField.setAggregate(AnalyticsConstants.SUM);
				sumField.setAlias(AnalyticsConstants.TOTAL_INVOLVED_TIME);

				ArrayList<AggregateField> aggregateFields = new ArrayList<>();
				aggregateFields.add(sumField);

				AggregateQuery query = new AggregateQuery();
				query.setTableName(AnalyticsConstants.USER_INVOLVE_TABLE);
				query.setGroupByField(AnalyticsConstants.PROCESS_DEFINITION_KEY);
				String queryStr = "assignee:" + "\"'" + userId + "'\"";
				if (from != 0 && to != 0) {
					queryStr += " AND " +
					            AnalyticsUtils
							            .getDateRangeQuery(AnalyticsConstants.COLUMN_FINISHED_TIME,
							                               from, to);
				}
				query.setQuery(queryStr);
				query.setAggregateFields(aggregateFields);

				if (log.isDebugEnabled()) {
					log.debug(AnalyticsUtils.getJSONString(query));
				}

				String result = AnalyticsRestClient
								.post(AnalyticsUtils.getURL(AnalyticsConstants.ANALYTICS_AGGREGATE),
						        AnalyticsUtils.getJSONString(query));

				JSONArray unsortedResultArray = new JSONArray(result);
				Hashtable<String, Double> table = new Hashtable<>();

				if (unsortedResultArray.length() != 0) {
					for (int i = 0; i < unsortedResultArray.length(); i++) {
						JSONObject jsonObj = unsortedResultArray.getJSONObject(i);
						JSONObject values = jsonObj.getJSONObject("values");
						String processDefKey = values.getJSONArray("processDefKey").getString(0);
						double totalInvolvedTime = values.getDouble("totalInvolvedTime");
						table.put(processDefKey, totalInvolvedTime);
					}
					sortedResult = AnalyticsUtils
							.getDoubleValueSortedList(table, "processDefKey", "totalInvolvedTime",
							                          order, count);
				}
			}
		} catch (Exception e) {
			String errMsg = "PC Analytics core UserLevelMonitoring error.";
			log.error(errMsg, e);
		}
		if (log.isDebugEnabled()) {
			log.debug("Result = " + sortedResult);
		}
		return sortedResult;
	}

	/**
	 * perform query: SELECT processDefKey, COUNT(*) AS totalInstanceCount FROM
	 *                USER_INVOLVE_SUMMARY WHERE <assignee> AND <date range> GROUP BY
	 *                processDefKey;
	 *
	 * @param filters is used to filter the result
	 * @return the result as a JSON string
	 */
	public String getTotalInvolvedInstanceCountVsProcessId(String filters) {
		String sortedResult = "";
		try {
			if (AnalyticsUtils.isDASAnalyticsActivated()) {
				JSONObject filterObj = new JSONObject(filters);
				long from = filterObj.getLong(AnalyticsConstants.START_TIME);
				long to = filterObj.getLong(AnalyticsConstants.END_TIME);
				String userId = filterObj.getString(AnalyticsConstants.USER_ID);
				String order = filterObj.getString(AnalyticsConstants.ORDER);
				int count = filterObj.getInt(AnalyticsConstants.NUM_COUNT);

				AggregateField countField = new AggregateField();
				countField.setFieldName(AnalyticsConstants.ALL);
				countField.setAggregate(AnalyticsConstants.COUNT);
				countField.setAlias(AnalyticsConstants.TOTAL_INSTANCE_COUNT);

				ArrayList<AggregateField> aggregateFields = new ArrayList<>();
				aggregateFields.add(countField);

				AggregateQuery query = new AggregateQuery();
				query.setTableName(AnalyticsConstants.USER_INVOLVE_TABLE);
				query.setGroupByField(AnalyticsConstants.PROCESS_DEFINITION_KEY);
				String queryStr = "assignee:" + "\"'" + userId + "'\"";
				if (from != 0 && to != 0) {
					queryStr += " AND " +
					            AnalyticsUtils
							            .getDateRangeQuery(AnalyticsConstants.COLUMN_FINISHED_TIME,
							                               from, to);
				}
				query.setQuery(queryStr);
				query.setAggregateFields(aggregateFields);

				if (log.isDebugEnabled()) {
					log.debug(AnalyticsUtils.getJSONString(query));
				}

				String result = AnalyticsRestClient
								.post(AnalyticsUtils.getURL(AnalyticsConstants.ANALYTICS_AGGREGATE),
						        AnalyticsUtils.getJSONString(query));

				JSONArray unsortedResultArray = new JSONArray(result);
				Hashtable<String, Integer> table = new Hashtable<>();

				if (unsortedResultArray.length() != 0) {
					for (int i = 0; i < unsortedResultArray.length(); i++) {
						JSONObject jsonObj = unsortedResultArray.getJSONObject(i);
						JSONObject values = jsonObj.getJSONObject("values");
						String processDefKey = values.getJSONArray("processDefKey").getString(0);
						int instanceCount = values.getInt("totalInstanceCount");
						table.put(processDefKey, instanceCount);
					}
					sortedResult = AnalyticsUtils
							.getIntegerValueSortedList(table, "processDefKey", "totalInstanceCount",
							                           order, count);
				}
			}
		} catch (Exception e) {
			String errMsg = "PC Analytics core UserLevelMonitoring error.";
			log.error(errMsg, e);
		}

		if (log.isDebugEnabled()) {
			log.debug("Result = " + sortedResult);
		}

		return sortedResult;
	}

	/**
	 * perform query: SELECT taskDefinitionKey, COUNT(taskInstanceId) AS taskInstanceCount FROM
	 *                USER_INVOLVE_SUMMARY_DATA WHERE <assignee> GROUP BY taskDefinitionKey;
	 *
	 * @param filters is used to filter the result
	 * @return the result as a JSON string
	 */
	public String getUserLevelTaskInstanceCountVsTaskId(String filters) {
		String sortedResult = "";
		try {
			if (AnalyticsUtils.isDASAnalyticsActivated()) {
				JSONObject filterObj = new JSONObject(filters);
				String userId = filterObj.getString(AnalyticsConstants.USER_ID);
				String order = filterObj.getString(AnalyticsConstants.ORDER);
				int taskCount = filterObj.getInt(AnalyticsConstants.NUM_COUNT);

				AggregateField countField = new AggregateField();
				countField.setFieldName(AnalyticsConstants.ALL);
				countField.setAggregate(AnalyticsConstants.COUNT);
				countField.setAlias(AnalyticsConstants.TASK_INSTANCE_COUNT);

				ArrayList<AggregateField> aggregateFields = new ArrayList<>();
				aggregateFields.add(countField);

				AggregateQuery query = new AggregateQuery();
				query.setTableName(AnalyticsConstants.USER_INVOLVE_TABLE);
				query.setGroupByField(AnalyticsConstants.TASK_DEFINITION_KEY);
				query.setQuery("assignee:" + "\"'" + userId + "'\"");
				query.setAggregateFields(aggregateFields);

				if (log.isDebugEnabled()) {
					log.debug(AnalyticsUtils.getJSONString(query));
				}

				String result = AnalyticsRestClient
								.post(AnalyticsUtils.getURL(AnalyticsConstants.ANALYTICS_AGGREGATE),
						        AnalyticsUtils.getJSONString(query));

				JSONArray unsortedResultArray = new JSONArray(result);
				Hashtable<String, Integer> table = new Hashtable<>();

				if (unsortedResultArray.length() != 0) {
					for (int i = 0; i < unsortedResultArray.length(); i++) {
						JSONObject jsonObj = unsortedResultArray.getJSONObject(i);
						JSONObject values = jsonObj.getJSONObject("values");
						String processDefKey = values.getJSONArray("taskDefId").getString(0);
						int processInstanceCount = values.getInt("taskInstanceCount");
						table.put(processDefKey, processInstanceCount);
					}
					sortedResult = AnalyticsUtils
							.getIntegerValueSortedList(table, "taskDefId", "taskInstanceCount",
							                           order, taskCount);
				}
			}
		} catch (Exception e) {
			String errMsg = "PC Analytics core UserLevelMonitoring error.";
			log.error(errMsg, e);
		}
		if (log.isDebugEnabled()) {
			log.debug("Result = " + sortedResult);
		}
		return sortedResult;
	}

	/**
	 * perform query: SELECT taskDefinitionKey, AVG(duration) AS avgExecutionTime FROM
	 *                USER_INVOLVE_SUMMARY_DATA WHERE <assignee> GROUP BY taskDefinitionKey;
	 *
	 * @param filters is used to filter the result
	 * @return the result as a JSON string
	 */
	public String getUserLevelAvgExecuteTimeVsTaskId(String filters) {
		String sortedResult = "";
		try {
			if (AnalyticsUtils.isDASAnalyticsActivated()) {
				JSONObject filterObj = new JSONObject(filters);
				String userId = filterObj.getString(AnalyticsConstants.USER_ID);
				String order = filterObj.getString(AnalyticsConstants.ORDER);
				int taskCount = filterObj.getInt(AnalyticsConstants.NUM_COUNT);

				AggregateField avgField = new AggregateField();
				avgField.setFieldName(AnalyticsConstants.DURATION);
				avgField.setAggregate(AnalyticsConstants.AVG);
				avgField.setAlias(AnalyticsConstants.AVG_EXECUTION_TIME);

				ArrayList<AggregateField> aggregateFields = new ArrayList<>();
				aggregateFields.add(avgField);

				AggregateQuery query = new AggregateQuery();
				query.setTableName(AnalyticsConstants.USER_INVOLVE_TABLE);
				query.setGroupByField(AnalyticsConstants.TASK_DEFINITION_KEY);
				query.setQuery("assignee:" + "\"'" + userId + "'\"");
				query.setAggregateFields(aggregateFields);

				if (log.isDebugEnabled()) {
					log.debug(AnalyticsUtils.getJSONString(query));
				}

				String result = AnalyticsRestClient
								.post(AnalyticsUtils.getURL(AnalyticsConstants.ANALYTICS_AGGREGATE),
						        AnalyticsUtils.getJSONString(query));

				JSONArray unsortedResultArray = new JSONArray(result);
				Hashtable<String, Double> table = new Hashtable<>();

				if (unsortedResultArray.length() != 0) {
					for (int i = 0; i < unsortedResultArray.length(); i++) {
						JSONObject jsonObj = unsortedResultArray.getJSONObject(i);
						JSONObject values = jsonObj.getJSONObject("values");
						String taskDefKey = values.getJSONArray("taskDefId").getString(0);
						double avgExecTime = values.getDouble("avgExecutionTime");
						table.put(taskDefKey, avgExecTime);
					}
					sortedResult = AnalyticsUtils
							.getDoubleValueSortedList(table, "taskDefId", "avgExecutionTime", order,
							                          taskCount);
				}
			}
		} catch (Exception e) {
			String errMsg = "PC Analytics core UserLevelMonitoring error.";
			log.error(errMsg, e);
		}
		if (log.isDebugEnabled()) {
			log.debug("Result = " + sortedResult);
		}
		return sortedResult;
	}

	/**
	 * Get user id list
	 *
	 * @return user id list as a JSON array string
	 */
	public String getUserList() {
		String userIdList = "";
		try {
			if (AnalyticsUtils.isDASAnalyticsActivated()) {
				AggregateField countField = new AggregateField();
				countField.setFieldName(AnalyticsConstants.ALL);
				countField.setAggregate(AnalyticsConstants.COUNT);
				countField.setAlias(AnalyticsConstants.COMPLETED_TOTAL_TASKS);

				ArrayList<AggregateField> aggregateFields = new ArrayList<>();
				aggregateFields.add(countField);

				AggregateQuery query = new AggregateQuery();
				query.setTableName(AnalyticsConstants.USER_INVOLVE_TABLE);
				query.setGroupByField(AnalyticsConstants.ASSIGN_USER);
				query.setAggregateFields(aggregateFields);

				if (log.isDebugEnabled()) {
					log.debug(AnalyticsUtils.getJSONString(query));
				}

				String result = AnalyticsRestClient
								.post(AnalyticsUtils.getURL(AnalyticsConstants.ANALYTICS_AGGREGATE),
						        AnalyticsUtils.getJSONString(query));

				JSONArray array = new JSONArray(result);
				JSONArray resultArray = new JSONArray();

				if (array.length() != 0) {
					for (int i = 0; i < array.length(); i++) {
						JSONObject jsonObj = array.getJSONObject(i);
						JSONObject values = jsonObj.getJSONObject("values");
						String userId = values.getJSONArray("assignUser").getString(0);
						JSONObject o = new JSONObject();
						o.put("assignUser", userId);
						resultArray.put(o);
					}
					userIdList = resultArray.toString();
				}

				if (log.isDebugEnabled()) {
					log.debug("Query = " + query.getQuery());
					log.debug("Result = " + userIdList);
				}
			}
		} catch (Exception e) {
			String errMsg = "PC Analytics core - user id list error.";
			log.error(errMsg, e);
		}
		return userIdList;
	}
}
