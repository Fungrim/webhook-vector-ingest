package net.larsan.ai.storage;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.larsan.ai.milvus.Milvus;
import net.larsan.ai.milvus.MilvusConfig;
import net.larsan.ai.pinecone.Pinecone;
import net.larsan.ai.pinecone.PineconeConfig;

@Singleton
public class VectorStorageService {

    @Inject
    MilvusConfig milvusConfig;

    @Inject
    PineconeConfig pineconeConfig;

    @Inject
    Instance<Milvus> milvus;

    @Inject
    Instance<Pinecone> pinecone;

    @Inject
    Gson gson;

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

    public Storagager getDatabase(String db) {
        if ("milvus".equals(db) && milvusConfig.isLegal()) {
            return new Milvus(milvusConfig, gson);
        }
        if ("pinecone".equals(db) && milvusConfig.isLegal()) {
            return new Pinecone(pineconeConfig);
        }
        throw new IllegalStateException("Database '" + db + "' not found");
    }
}
