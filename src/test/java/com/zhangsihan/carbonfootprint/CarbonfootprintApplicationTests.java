package com.zhangsihan.carbonfootprint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CarbonfootprintApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldRejectProtectedEndpointWithoutToken() throws Exception {
		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void shouldRegisterAndLoginSuccessfully() throws Exception {
		String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"username", username,
								"email", username + "@example.com",
								"fullName", "Test User",
								"password", "123456"
						))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.token").isNotEmpty())
				.andExpect(jsonPath("$.data.user.username").value(username));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"account", username,
								"password", "123456"
						))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.tokenType").value("Bearer"));
	}

	@Test
	void shouldCreateRecordAndReturnDashboardAdviceAndCommunityData() throws Exception {
		String token = registerAndGetToken();

		mockMvc.perform(post("/api/records")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"activityType", "TRANSPORT",
								"subType", "TAXI",
								"amount", 200,
								"unit", "km",
								"note", "test record",
								"occurredAt", LocalDateTime.now().toString()
						))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.emissionKg").value(greaterThan(0.0)))
				.andExpect(jsonPath("$.data.activityType").value("TRANSPORT"));

		mockMvc.perform(get("/api/dashboard/summary")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalRecords").value(1))
				.andExpect(jsonPath("$.data.totalEmission").value(greaterThan(0.0)));

		mockMvc.perform(get("/api/advice")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].activityType").value("TRANSPORT"));

		mockMvc.perform(get("/api/community/rankings")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].rank").value(1));
	}

	private String registerAndGetToken() throws Exception {
		String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"username", username,
								"email", username + "@example.com",
								"fullName", "Test User",
								"password", "123456"
						))))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode root = objectMapper.readTree(response);
		return root.path("data").path("token").asText();
	}
}
