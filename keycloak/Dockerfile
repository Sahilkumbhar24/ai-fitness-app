FROM quay.io/keycloak/keycloak:24.0.5

# Copy realm configuration into import directory
COPY realm-export.json /opt/keycloak/data/import/realm-export.json

# Environment settings for low-memory environments (Render Free Tier 512MB limit)
ENV KC_HTTP_PORT=8080
ENV KC_PROXY=edge
ENV KC_HOSTNAME_STRICT=false
ENV KC_HTTP_ENABLED=true
ENV KC_DB_URL="jdbc:h2:mem:keycloak;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=VALUE"
ENV JAVA_OPTS_APPEND="-Xms128m -Xmx240m -XX:MaxMetaspaceSize=128m"

# Expose Keycloak HTTP port
EXPOSE 8080

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev", "--http-port=8080", "--import-realm"]
