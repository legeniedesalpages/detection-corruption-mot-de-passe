package fr.lgda.corruptionmotdepasse.usecase;

import fr.lgda.corruptionmotdepasse.entity.Utilisateur;
import fr.lgda.corruptionmotdepasse.service.Notificateur;
import fr.lgda.corruptionmotdepasse.service.Revocateur;
import fr.lgda.corruptionmotdepasse.service.VerificateurDeCorruption;

import java.time.LocalDateTime;

public class VerifierEtRevoquerMotDePasse {

    public enum StatutDeRevocation {
        IMPOSSIBLE_DE_VERIFIER,
        NON_CORROMPU,
        CORROMPU_ET_REVOQUE,
        CORROMPU_MAIS_NON_REVOQUE,
    }

    private final Revocateur revocateur;

    private final VerificateurDeCorruption verificateurDeCorruption;

    private final Notificateur notificateur;

    public VerifierEtRevoquerMotDePasse(Revocateur revocateur, VerificateurDeCorruption verificateurDeCorruption, Notificateur notificateur) {
        this.revocateur = revocateur;
        this.verificateurDeCorruption = verificateurDeCorruption;
        this.notificateur = notificateur;
    }

    public StatutDeRevocation execute(String idEns, String motDePasse) {
        Utilisateur utilisateur = new Utilisateur(idEns, motDePasse);
        StatutDeRevocation resultatTraitement = taitement(utilisateur);
        notificateur.notifier(idEns, LocalDateTime.now(), resultatTraitement.toString());
        return resultatTraitement;
    }

    private StatutDeRevocation taitement(Utilisateur utilisateur) {
        boolean estMotDePasseCorrompu;
        try {
            estMotDePasseCorrompu = verificateurDeCorruption.verifierSiMotDePasseCorrompu(utilisateur.idEns, utilisateur.motDePasse);
        } catch (Exception e) {
            return StatutDeRevocation.IMPOSSIBLE_DE_VERIFIER;
        }

        if (estMotDePasseCorrompu) {
            try {
                revocateur.revoquer(utilisateur.idEns);
            } catch (Exception e) {
                return StatutDeRevocation.CORROMPU_MAIS_NON_REVOQUE;
            }

            return StatutDeRevocation.CORROMPU_ET_REVOQUE;
        }

        return StatutDeRevocation.NON_CORROMPU;
    }
}
