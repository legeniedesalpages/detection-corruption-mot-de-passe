package fr.lgda.corruptionmotdepasse.core;

public class BusinessImpl implements Business {

    private Persitance persitance;

    BusinessImpl(Persitance persitance) {
        this.persitance = persitance;
    }

    public void sayHello() {
        System.out.println("Hello world 3!");
        this.persitance.save();
    }
}
