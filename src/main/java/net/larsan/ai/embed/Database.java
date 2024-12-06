package net.larsan.ai.embed;

@FunctionalInterface
public interface Database {

    void upsert(Upsert req);

}