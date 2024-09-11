package fr.lgda.corruptionmotdepasse.service;

public interface VerificateurDeCorruption {

    boolean verifierSiMotDePasseCorrompu(String idEns, String motDePasse);
}
