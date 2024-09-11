package fr.lgda.corruptionmotdepasse.launcher;

import fr.lgda.corruptionmotdepasse.batch.BatchLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ImportResource("classpath*:applicationContext.xml")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Component
    public class BatchRunner implements CommandLineRunner {

        private final BatchLauncher batchLauncher;

        public BatchRunner(BatchLauncher batchLauncher) {
            this.batchLauncher = batchLauncher;
        }

        @Override
        public void run(String... args) throws Exception {
            batchLauncher.run();
        }
    }
}
