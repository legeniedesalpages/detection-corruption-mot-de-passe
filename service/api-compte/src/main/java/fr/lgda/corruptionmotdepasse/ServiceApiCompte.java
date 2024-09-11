package fr.lgda.corruptionmotdepasse;

import fr.lgda.corruptionmotdepasse.service.Revocateur;
import fr.lgda.corruptionmotdepasse.service.VerificateurDeCorruption;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class ServiceApiCompte implements VerificateurDeCorruption, Revocateur {

    private static final Logger LOG = Logger.getLogger(ServiceApiCompte.class.getName());

    @Override
    public void revoquer(String idEns) {
        LOG.info("Revoquer le mot de passe de l'utilisateur " + idEns);
    }

    @Override
    public boolean verifierSiMotDePasseCorrompu(String idEns, String motDePasse) {
        LOG.info("Vérifier si le mot de passe de l'utilisateur " + idEns + " est corrompu");
        return true;
    }
}
