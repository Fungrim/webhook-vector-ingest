package io.github.fungrim.storage;

import com.google.gson.Gson;

import io.github.fungrim.conf.MilvusConfig;
import io.github.fungrim.conf.PineconeConfig;
import io.github.fungrim.milvus.Milvus;
import io.github.fungrim.pinecone.Pinecone;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
        if ("pinecone".equals(db) && pineconeConfig.isLegal()) {
            return new Pinecone(pineconeConfig);
        }
        throw new IllegalStateException("Database '" + db + "' not found");
    }
}
