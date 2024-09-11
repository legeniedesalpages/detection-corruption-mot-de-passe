package fr.lgda.corruptionmotdepasse.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BatchFlow {

    @Bean
    public Job job(JobRepository jobRepository, @Qualifier("stepTraitementDesDonnees") Step stepRevocation) {
        return new JobBuilder("job-revocation-mot-de-passe", jobRepository)
                .start(stepRevocation)
                .build();
    }

}
