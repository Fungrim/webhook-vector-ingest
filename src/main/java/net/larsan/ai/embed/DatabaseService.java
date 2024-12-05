package net.larsan.ai.embed;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatabaseService {

    @Inject
    MilvusConfig milvusConfig;

    @Inject
    PineconeConfig pineconeConfig;

    @Inject
    Instance<Milvus> milvus;

    @Inject
    Instance<Pinecone> pinecone;

    public List<String> getDatabases() {
        List<String> l = new ArrayList<>(2);
        if (milvusConfig.isLegal()) {
            l.add("milvus");
        }
        if (pineconeConfig.isLegal()) {
            l.add("pinecone");
        }
        return l;
    }

    public Database getDatabase(String db) {
        return null;
    }
}
