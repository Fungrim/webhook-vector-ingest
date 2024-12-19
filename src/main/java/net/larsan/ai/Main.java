package net.larsan.ai;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main {

    // this main class is here for easier debugging
    // in vs code
    public static void main(String... args) {
        Quarkus.run(args);
    }
}