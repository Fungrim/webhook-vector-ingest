package io.github.fungrim.api;

import java.util.Optional;

public record VectorStorageSpec(Optional<String> provider, Optional<String> collection, Optional<String> namespace) {

    public VectorStorageSpec mergeOptional(Optional<VectorStorageSpec> reqStor) {
        if (reqStor.isPresent()) {
            VectorStorageSpec spec = reqStor.get();
            String prov = spec.provider().orElse(this.provider().orElse(null));
            String coll = spec.collection().orElse(this.collection().orElse(null));
            String ns = spec.namespace().orElse(this.namespace().orElse(null));
            return new VectorStorageSpec(
                Optional.ofNullable(prov), 
                Optional.ofNullable(coll), 
                Optional.ofNullable(ns));
        } else {
            return this;
        }
    }
}
