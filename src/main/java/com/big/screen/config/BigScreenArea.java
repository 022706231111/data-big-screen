package com.big.screen.config;

import com.big.screen.util.QuartzUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.quartz.*;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class BigScreenArea implements Serializable {

    private String name;

    private String cron;

    private BigScreenEnum bigScreenEnum;

    public void startJob() throws Exception {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BigScreenArea.class.getName(), this);
        JobDetail jobDetail = JobBuilder.newJob(BigScreenAreaJob.class)
                .withIdentity(name).setJobData(jobDataMap)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
        QuartzUtil.startJob(jobDetail, trigger);
    }

    public void removeJob() throws Exception {
        QuartzUtil.removeJob(JobKey.jobKey(name));
    }
}
