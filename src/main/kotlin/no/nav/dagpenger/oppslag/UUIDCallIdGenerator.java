package no.nav.dagpenger.oppslag;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

public class UUIDCallIdGenerator {

    private final String callIdKey;

    public UUIDCallIdGenerator(String callIdKey) {
        this.callIdKey = callIdKey;
    }

    public String getOrCreate() {
        return Optional.ofNullable(MDC.get(callIdKey)).orElse(create());
    }

    public String create() {
        return UUID.randomUUID().toString();
    }

    public String getCallIdKey() {
        return callIdKey;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [callIdKey=" + callIdKey + "]";
    }
}
