FROM quay.io/keycloak/keycloak:24.0.5

# Copy realm configuration into import directory
COPY realm-export.json /opt/keycloak/data/import/realm-export.json

ENV KC_HTTP_PORT=8080
ENV KC_PROXY=edge
ENV KC_HOSTNAME_STRICT=false
ENV KC_HTTP_ENABLED=true

# Expose Keycloak HTTP port
EXPOSE 8080

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev", "--http-port=8080", "--import-realm"]
