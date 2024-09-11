package fr.lgda.corruptionmotdepasse.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
public class BatchLauncher {

    private final JobLauncher jobLauncher;
    private final Job job;

    public BatchLauncher(JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    public void run() throws Exception {
        jobLauncher.run(job, new JobParameters());
    }
}