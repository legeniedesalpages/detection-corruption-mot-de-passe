package fr.lgda.corruptionmotdepasse.service;

import java.time.LocalDateTime;

public interface Notificateur {

    void notifier(String idEns, LocalDateTime dateDeTraitement, String statutDeNotification);
}
