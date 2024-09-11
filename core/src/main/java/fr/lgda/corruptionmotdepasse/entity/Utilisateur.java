package fr.lgda.corruptionmotdepasse.entity;

public class Utilisateur {

    public final String idEns;
    public final String motDePasse;

    public Utilisateur(String idEns, String motDePasse) {
        this.idEns = idEns;
        this.motDePasse = motDePasse;
    }
}
