package telemetry_service.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.atteo.evo.inflector.English;

public class SnakeCaseWithPluralizedTablePhysicalNamingStrategy implements PhysicalNamingStrategy {
    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) { return null; }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) { return this.toSnakeCase(identifier); }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) { return this.toSnakeCase(this.toPlural(identifier)); }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) { return this.toSnakeCase(identifier); }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) { return this.toSnakeCase(identifier); }

    private Identifier toSnakeCase(final Identifier identifier) {
        if (identifier == null) return null;
        return Identifier.toIdentifier(identifier.getText().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase());
    }

    private Identifier toPlural(final Identifier identifier) {
        return Identifier.toIdentifier(English.plural(identifier.getText()));
    }
}