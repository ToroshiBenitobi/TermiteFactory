package org.anar.termitefactory.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttType {
    public final static String ALLOCATION = "allocation";
    public final static String CHECK = "check";
    public final static String RESTORE_QUEST = "restoreQuest";
    public final static String RESTORE = "restore";
    public final static String STATUS = "status";
    public final static String OPERATION_STATUS_CHANGED = "operationStatusChanged";
    public final static String COMMENCE = "commence";
    public final static String CLEAR_FINISHED_OPERATION = "clearFinishedOperation";
    public final static String DELETE = "delete";
}
