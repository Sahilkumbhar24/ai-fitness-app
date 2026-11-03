FROM quay.io/keycloak/keycloak:24.0.5 AS builder

# Set build-time options
ENV KC_DB=dev-mem
ENV KC_HEALTH_ENABLED=false
ENV KC_METRICS_ENABLED=false

# Bake in Quarkus augmentation at image build time (avoids 3-minute runtime build)
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:24.0.5

# Copy pre-built Keycloak from builder stage
COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Copy realm configuration into import directory
COPY realm-export.json /opt/keycloak/data/import/realm-export.json

# Environment settings for low-memory environments (Render Free Tier 512MB limit)
ENV KC_HTTP_PORT=8080
ENV KC_PROXY=edge
ENV KC_HOSTNAME_STRICT=false
ENV KC_HTTP_ENABLED=true
ENV KC_DB=dev-mem
ENV KC_DB_URL="jdbc:h2:mem:keycloak;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=VALUE"
ENV JAVA_OPTS_APPEND="-Xms128m -Xmx240m -XX:MaxMetaspaceSize=128m"

# Expose Keycloak HTTP port
EXPOSE 8080

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized", "--http-port=8080", "--import-realm"]
