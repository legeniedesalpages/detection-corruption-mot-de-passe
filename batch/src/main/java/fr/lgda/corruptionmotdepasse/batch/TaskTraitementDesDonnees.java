package fr.lgda.corruptionmotdepasse.batch;

import fr.lgda.corruptionmotdepasse.service.LecteurDonnees;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.retry.policy.NeverRetryPolicy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

@Configuration
public class TaskTraitementDesDonnees {

    private final String inputUri;

    TaskTraitementDesDonnees(@Value("${app.input-uri}") String inputUri) {
        this.inputUri = inputUri;
    }

    @Bean(name = "stepTraitementDesDonnees")
    public Step step(JobRepository jobRepository, JdbcTransactionManager transactionManager, @Qualifier("itemReader") ItemStreamReader<String> itemReader, @Qualifier("itemWiter") ItemWriter<String> itemWriter) {

        return new StepBuilder("sampleStep", jobRepository)
                .<String, String>chunk(1, transactionManager)
                .reader(itemReader)
                .writer(itemWriter)
                .faultTolerant()
                .retryPolicy(new NeverRetryPolicy())
                .build();
    }

    @Bean(name = "itemReader")
    public ItemStreamReader<String> produceItemReader(LecteurDonnees lecteurDonnees) {
        return new ItemStreamReader<String>() {

            private Iterator<String> stringIterator;

            @Override
            public void open(ExecutionContext executionContext) throws ItemStreamException {
                try {
                    stringIterator = lecteurDonnees.lireDonnees(new URI(inputUri)).iterator();
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Fichier non trouvé: " + inputUri, e);
                }
            }

            @Override
            public String read() throws Exception {
                if (stringIterator.hasNext()) {
                    return stringIterator.next();
                }
                return null;
            }
        };
    }

    @Bean(name = "itemWiter")
    public ItemWriter<String> produceItemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println("item = " + item);
            }
        };
    }
}
