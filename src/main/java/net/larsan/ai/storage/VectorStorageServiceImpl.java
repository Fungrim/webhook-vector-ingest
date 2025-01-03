package net.larsan.ai.storage;

import com.google.gson.Gson;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.larsan.ai.conf.MilvusConfig;
import net.larsan.ai.conf.PineconeConfig;
import net.larsan.ai.milvus.Milvus;
import net.larsan.ai.pinecone.Pinecone;

@Singleton
public class VectorStorageServiceImpl implements VectorStorageService {

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

    @Override
    public StorageFacade getDatabase(String db) {
        if ("milvus".equals(db) && milvusConfig.isLegal()) {
            return new Milvus(milvusConfig, gson);
        }
        if ("pinecone".equals(db) && milvusConfig.isLegal()) {
            return new Pinecone(pineconeConfig);
        }
        throw new IllegalStateException("Database '" + db + "' not found");
    }
}
