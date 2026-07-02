package telemetry_service.telemetry.application.outboundservices.acl;

/**
 * Port for the Telemetry Bounded Context to communicate with external Equipment Context.
 * Resolves necessary information without coupling to Equipment's internal structures.
 */
public interface ExternalEquipmentService {
    
    /**
     * Retrieves the species name assigned to a specific pond.
     *
     * @param pondId The identifier of the pond
     * @return The species name as a plain string (e.g., "TRUCHA", "TILAPIA")
     */
    String getSpeciesByPondId(Long pondId);

    /**
     * Retrieves the user ID of the owner of a specific pond.
     *
     * @param pondId The identifier of the pond
     * @return The user ID of the owner
     */
    Long getUserIdByPondId(Long pondId);

    /**
     * Retrieves the operator ID assigned to a specific pond.
     * If no operator is assigned, it falls back to the owner ID.
     *
     * @param pondId The identifier of the pond
     * @return The user ID of the operator or owner
     */
    Long getOperatorIdByPondId(Long pondId);
}