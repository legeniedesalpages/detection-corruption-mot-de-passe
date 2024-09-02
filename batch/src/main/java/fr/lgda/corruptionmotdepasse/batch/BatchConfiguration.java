package fr.lgda.corruptionmotdepasse.batch;

import fr.lgda.corruptionmotdepasse.core.Business;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
@ImportResource("classpath*:applicationContext.xml")
@Inject
public class BatchConfiguration {

    @Bean
    public Step step(JobRepository jobRepository, JdbcTransactionManager transactionManager, Business business) {
        return new StepBuilder("step", jobRepository).tasklet((contribution, chunkContext) -> {
            business.sayHello();
            return RepeatStatus.FINISHED;
        }, transactionManager).build();
    }

    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("job", jobRepository).start(step).build();
    }
}
