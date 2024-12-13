package com.big.screen.util;

import com.big.screen.config.BigScreenEnum;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.*;

@Slf4j
public class QuartzUtil {
    private static Scheduler scheduler;

    public static void init() throws Exception {
        if (scheduler != null) {
            return;
        }
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Map<String,Map<String,String>> map = new HashMap<>();
                try {
                    Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
                    for (JobKey jobKey : jobKeys) {
                        Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
                        Map<String,String> detail = new HashMap<>();
                        map.put(jobKey.getName(),detail);
                        detail.put("group",jobKey.getGroup());
                        detail.put("previousFireTime",DateFormatUtils.format(trigger.getPreviousFireTime(),"yyyy-MM-dd HH:mm:ss"));
                        detail.put("nextFireTime",DateFormatUtils.format(trigger.getNextFireTime(),"yyyy-MM-dd HH:mm:ss"));
                    }
                } catch (SchedulerException e) {
                    log.error("Error get job list", e);
                }
                log.info(JacksonUtil.toJson(map));
                log.info(BigScreenEnum.getSubscribeInfo());
            }
        }, 0, 5 * 1000);
    }

    public static void startJob(JobDetail jobDetail, Trigger trigger) throws Exception {
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public static void removeJob(JobKey jobKey) throws Exception {
        scheduler.deleteJob(jobKey);
    }

}
