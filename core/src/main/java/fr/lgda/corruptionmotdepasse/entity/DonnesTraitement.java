package fr.lgda.corruptionmotdepasse.entity;

import java.time.LocalDateTime;
import java.util.List;

public class DonnesTraitement {

    public final LocalDateTime dateTraitement;

    public final List<Utilisateur> listeDesUtilisateurs;

    public DonnesTraitement(LocalDateTime dateTraitement, List<Utilisateur> listeDesUtilisateurs) {
        this.dateTraitement = dateTraitement;
        this.listeDesUtilisateurs = listeDesUtilisateurs;
    }
}
