package fr.lgda.corruptionmotdepasse.batch;

import fr.lgda.corruptionmotdepasse.usecase.VerifierEtRevoquerMotDePasse;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;

import java.util.Date;

@Configuration
@ComponentScan
public class BatchConfiguration {

}
