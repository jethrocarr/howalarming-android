package com.jethrocarr.howalarming;

/**
 * Containing the constants used to share registration states within the application.
 */
public class GlobalConstants {

    /**
     * GCM Registration Phases
     */
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String TOKEN = "token";

    /**
     * Alarm phases
     */
    public static final String ALARM_STATUS             = "AlarmStatus";
    public static final String ALARM_STATUS_ALARMING    = "ALARMING";
    public static final String ALARM_STATUS_ARMED       = "Armed";
    public static final String ALARM_STATUS_DISARMED    = "Unarmed/Disarmed";
    public static final String ALARM_STATUS_FAULT       = "Fault";
    public static final String ALARM_STATUS_RECOVERED   = "Recovered/Cleared Alarm";
    public static final String ALARM_STATUS_UNKNOWN     = "Unknown State";
}