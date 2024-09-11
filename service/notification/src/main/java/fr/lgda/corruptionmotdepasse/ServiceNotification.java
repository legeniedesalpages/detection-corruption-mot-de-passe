package fr.lgda.corruptionmotdepasse;

import fr.lgda.corruptionmotdepasse.service.Notificateur;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component
public class ServiceNotification implements Notificateur {

    private static final Logger LOG = Logger.getLogger(ServiceNotification.class.getName());
    @Override
    public void notifier(String idEns, LocalDateTime dateDeTraitement, String statutDeNotification) {
        LOG.info("Notifier l'utilisateur " + idEns + " le " + dateDeTraitement + " avec le statut " + statutDeNotification);
    }
}
