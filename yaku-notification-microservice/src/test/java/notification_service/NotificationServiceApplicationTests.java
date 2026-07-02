package notification_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void getNotifications_withoutGatewayHeader_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/123/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getNotifications_withMismatchedHeader_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/123/notifications")
                        .header("X-User-Id", 999))
                .andExpect(status().isForbidden());
    }

    @Test
    void getNotifications_withMatchingHeader_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/users/123/notifications")
                        .header("X-User-Id", 123))
                .andExpect(status().isOk());
    }

    @Test
    void markAllAsRead_withoutGatewayHeader_returnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/users/123/notifications/read"))
                .andExpect(status().isForbidden());
    }

    @Test
    void markAllAsRead_withMismatchedHeader_returnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/users/123/notifications/read")
                        .header("X-User-Id", 999))
                .andExpect(status().isForbidden());
    }

    @Test
    void markAllAsRead_withMatchingHeader_returnsOk() throws Exception {
        mockMvc.perform(patch("/api/v1/users/123/notifications/read")
                        .header("X-User-Id", 123))
                .andExpect(status().isOk());
    }

    @Test
    void registerDeviceToken_withoutGatewayHeader_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/users/123/device-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fcmToken\":\"test-token\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerDeviceToken_withMismatchedHeader_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/users/123/device-tokens")
                        .header("X-User-Id", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fcmToken\":\"test-token\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerDeviceToken_withMatchingHeader_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/users/123/device-tokens")
                        .header("X-User-Id", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fcmToken\":\"test-token\"}"))
                .andExpect(status().isCreated());
    }
}
