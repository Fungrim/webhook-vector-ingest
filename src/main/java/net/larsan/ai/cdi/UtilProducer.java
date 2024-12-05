package net.larsan.ai.cdi;

import com.google.gson.Gson;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

public class UtilProducer {

    @Produces
    @Singleton
    Gson gson() {
        return new Gson();
    }
}
