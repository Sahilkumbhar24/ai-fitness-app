# Keycloak Identity Server for Production
FROM quay.io/keycloak/keycloak:24.0.5

# Copy realm configuration
COPY realm-export.json /opt/keycloak/data/import/realm-export.json

# Build optimized Keycloak
RUN /opt/keycloak/bin/kc.sh build

# Production entrypoint with realm import
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", \
     "--optimized", \
     "--hostname-strict=false", \
     "--http-enabled=true", \
     "--proxy=edge", \
     "--import-realm"]

EXPOSE 8080

