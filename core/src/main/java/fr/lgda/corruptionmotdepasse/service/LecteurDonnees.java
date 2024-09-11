package fr.lgda.corruptionmotdepasse.service;

import java.net.URI;
import java.util.stream.Stream;

public interface LecteurDonnees {

    Stream<String> lireDonnees(URI uri);
}
