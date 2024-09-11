package fr.lgda.corruptionmotdepasse.batch;

import fr.lgda.corruptionmotdepasse.usecase.VerifierEtRevoquerMotDePasse;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
public class TaskRecuperationDonnees {

    @Bean(name = "stepRevocation")
    public Step step(JobRepository jobRepository, JdbcTransactionManager transactionManager, VerifierEtRevoquerMotDePasse verifierEtRevoquerMotDePasse) {
        return new StepBuilder("step", jobRepository).tasklet((contribution, chunkContext) -> {
            verifierEtRevoquerMotDePasse.execute("idEns", "motDePasse");
            return RepeatStatus.FINISHED;
        }, transactionManager).build();
    }
}
