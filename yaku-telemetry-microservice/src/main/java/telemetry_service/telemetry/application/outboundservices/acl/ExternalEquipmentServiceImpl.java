package telemetry_service.telemetry.application.outboundservices.acl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ExternalEquipmentServiceImpl implements ExternalEquipmentService {

    private final RestTemplate restTemplate;
    private final String equipmentServiceUrl = System.getenv().getOrDefault("EQUIPMENT_SERVICE_URL", "http://equipment-service:8080") + "/api/v1";

    public ExternalEquipmentServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getSpeciesByPondId(Long pondId) {
        try {
            var response = restTemplate.getForObject(equipmentServiceUrl + "/ponds/" + pondId, Map.class);
            if (response != null && response.get("species") != null) {
                return response.get("species").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "TILAPIA"; // Fallback
    }

    @Override
    public Long getUserIdByPondId(Long pondId) {
        try {
            var pondResponse = restTemplate.getForObject(equipmentServiceUrl + "/ponds/" + pondId, Map.class);
            if (pondResponse != null && pondResponse.get("farmId") != null) {
                Long farmId = Long.valueOf(pondResponse.get("farmId").toString());
                var farmResponse = restTemplate.getForObject(equipmentServiceUrl + "/farms/" + farmId, Map.class);
                if (farmResponse != null && farmResponse.get("ownerId") != null) {
                    return Long.valueOf(farmResponse.get("ownerId").toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1L; // Fallback
    }

    @Override
    public Long getOperatorIdByPondId(Long pondId) {
        try {
            var pondResponse = restTemplate.getForObject(equipmentServiceUrl + "/ponds/" + pondId, Map.class);
            if (pondResponse != null) {
                if (pondResponse.get("assignedOperatorId") != null) {
                    return Long.valueOf(pondResponse.get("assignedOperatorId").toString());
                } else {
                    return getUserIdByPondId(pondId); // Fallback to owner/admin if no operator assigned
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1L; // Fallback
    }

    @Override
    public Long getPondIdByDeviceId(String deviceId) {
        try {
            var response = restTemplate.getForObject(equipmentServiceUrl + "/equipment/physical-code/" + deviceId, Map.class);
            if (response != null && response.get("pondId") != null) {
                return Long.valueOf(response.get("pondId").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Device not found or not linked to any pond: " + deviceId);
    }
}
