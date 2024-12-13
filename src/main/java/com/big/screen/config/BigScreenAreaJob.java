package com.big.screen.config;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class BigScreenAreaJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
        if (mergedJobDataMap.get(BigScreenArea.class.getName()) instanceof BigScreenArea bigScreenArea){
            BigScreenEnum.publish(bigScreenArea);
        }
    }
}
