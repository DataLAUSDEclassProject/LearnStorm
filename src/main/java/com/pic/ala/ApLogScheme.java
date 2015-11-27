package com.pic.ala;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
import org.apache.storm.joda.time.DateTime;
import org.apache.storm.joda.time.format.DateTimeFormat;
import org.apache.storm.joda.time.format.DateTimeFormatter;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ApLogScheme implements Scheme {

	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private String counterColumnName;

	private static final long serialVersionUID = -578815753542323978L;

	public static final String SYSTEM_ID = "aes3g";	// HBase Table name
	public static final String LOG_TYPE = "job";

	public static final String FIELD_LOG_ID = "apLogId";
	public static final String FIELD_HOSTNAME = "hostName";
	public static final String FIELD_AP_ID = "apId";
	public static final String FIELD_EXEC_TIME = "execTime";
	public static final String FIELD_ERROR_LEVEL = "errLevel";
	public static final String FIELD_EXEC_METHOD = "execMethod";
	public static final String FIELD_KEYWORD1 = "keyword1";
	public static final String FIELD_KEYWORD2 = "keyword2";
	public static final String FIELD_KEYWORD3 = "keyword3";
	public static final String FIELD_MESSAGE = "message";

	public static final String AGG_TABLE = "aes3g_agg";	// HBase Table name
	public static final String FIELD_AGG_ID = "aggId";
	public static final String FIELD_DAY_COUNT = "count";
	public static final String YearMonthDay = "yearMonthDay";
	public static final String HourMinute = "hourMinute";

	private static final Logger LOG = Logger.getLogger(ApLogScheme.class);

	public void setCounterColumnName(String counterColumnName) {
		this.counterColumnName = counterColumnName;
	}
	public String getCounterColumnName() {
		return this.counterColumnName;
	}

	public List<Object> deserialize(byte[] bytes) {
		try {
			String LogEntry = new String(bytes, "UTF-8");
			String[] pieces = LogEntry.split("\\$\\$");
//			aes3g-AESRCT1-AES-job-ERROR-2015-01-05 10:50:31,346

			String hostName = cleanup(pieces[0]);
			String apId = cleanup(pieces[1]);
			String execTime = cleanup(pieces[2]);
			String errLevel = cleanup(pieces[3]);
			String execMethod = cleanup(pieces[4]);
			String keyword1 = cleanup(pieces[5]);
			String keyword2 = cleanup(pieces[6]);
			String keyword3 = cleanup(pieces[7]);
			String message = cleanup(pieces[8]);
			String logId = SYSTEM_ID + "-" + hostName + "-" + apId + "-" +
							LOG_TYPE + "-" + execMethod + "-" +
							execTime;

			DateTime dateTime = formatter.parseDateTime(cleanup(pieces[2]));

			int year = dateTime.getYear();
			int month = dateTime.getMonthOfYear();
			int day = dateTime.getDayOfMonth();
			int hour = dateTime.getHourOfDay();
			int minute = dateTime.getMinuteOfHour();

			String hourMinute = String.valueOf(hour) + ":" + String.valueOf(minute);
			String yearMonthDay = String.valueOf(year) + "-" + String.valueOf(month)
									+ "-" + String.valueOf(day);
			String aggId = yearMonthDay;

			setCounterColumnName(hourMinute);

			return new Values(logId, aggId, hostName, execTime, errLevel, execMethod,
								keyword1, keyword2, keyword3, message, aggId);

		} catch (UnsupportedEncodingException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	public Fields getOutputFields() {
		return new Fields(FIELD_LOG_ID, FIELD_HOSTNAME, FIELD_EXEC_TIME,
				FIELD_ERROR_LEVEL, FIELD_EXEC_METHOD, FIELD_KEYWORD1,
				FIELD_KEYWORD2, FIELD_KEYWORD3, FIELD_MESSAGE, FIELD_AGG_ID);
	}

	private String cleanup(String str) {
		if (str != null) {
			return str.trim().replace("\n", "").replace("\t", "");
		} else {
			return str;
		}
	}

}
