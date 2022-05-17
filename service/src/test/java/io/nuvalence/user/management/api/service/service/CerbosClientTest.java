package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.cerbos.models.AddOrUpdatePolicyResponse;
import io.nuvalence.user.management.api.service.cerbos.models.CheckResourceSetResponse;
import io.nuvalence.user.management.api.service.cerbos.models.CheckResourceSetResponseActionEffectMap;
import io.nuvalence.user.management.api.service.cerbos.models.Effect;
import io.nuvalence.user.management.api.service.cerbos.models.GetPolicyResponse;
import io.nuvalence.user.management.api.service.cerbos.models.Policy;
import io.nuvalence.user.management.api.service.cerbos.models.ResourcePolicy;
import io.nuvalence.user.management.api.service.cerbos.models.ResourceRule;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CerbosClientTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CerbosClient client;

    @BeforeEach
    void initTests() {
        ReflectionTestUtils.setField(client, "username", "user");
        ReflectionTestUtils.setField(client, "password", "password");
        client.setHttpClient(restTemplate);
    }

    // check
    @Test
    public void check_returnsTrueIfValid() {
        when(restTemplate.postForEntity(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.<HttpEntity<?>>any(),
                eq(CheckResourceSetResponse.class))
        ).thenReturn(getCheckResourceResponse(Effect.EFFECT_ALLOW));

        Boolean allowed = client.check("default_resource", getUserEntity(), "allow");
        assertEquals(allowed, true);
    }

    @Test
    public void check_returnsFalseIfInvalid() {
        when(restTemplate.postForEntity(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.<HttpEntity<?>>any(),
                eq(CheckResourceSetResponse.class))
        ).thenReturn(getCheckResourceResponse(Effect.EFFECT_DENY));

        Boolean allowed = client.check("default_resource", getUserEntity(), "allow");
        assertEquals(allowed, false);
    }

    @Test
    public void check_returnsFalseIfPermissionDoesNotExist() {
        when(restTemplate.postForEntity(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.<HttpEntity<?>>any(),
                eq(CheckResourceSetResponse.class))
        ).thenReturn(getCheckResourceResponse(Effect.EFFECT_NO_MATCH));

        Boolean allowed = client.check("default_resource", getUserEntity(), "allow");
        assertEquals(allowed, false);
    }

    // updateRolePermissionMappings
    @Test
    public void updateRolePermissionMappings_returnsTrueIfValid() {
        when(restTemplate.exchange(ArgumentMatchers.anyString(), eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<Void>>any(),
                eq(GetPolicyResponse.class))
        ).thenReturn(getPolicyResponse());

        when(restTemplate.postForEntity(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.<HttpEntity<?>>any(),
                eq(AddOrUpdatePolicyResponse.class))
        ).thenReturn(ResponseEntity.ok(new AddOrUpdatePolicyResponse()));

        Boolean success = client.updateRolePermissionMappings("default_resource",
                "ROLE_TO_TEST", new String[] { "actionToTest" });
        assertEquals(success, true);
    }

    // removeRole
    @Test
    public void removeRole_returnsTrueIfValid() {
        when(restTemplate.exchange(ArgumentMatchers.anyString(), eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<Void>>any(),
                eq(GetPolicyResponse.class))
        ).thenReturn(getPolicyResponse());

        when(restTemplate.postForEntity(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.<HttpEntity<?>>any(),
                eq(AddOrUpdatePolicyResponse.class))
        ).thenReturn(ResponseEntity.ok(new AddOrUpdatePolicyResponse()));

        Boolean success = client.removeRole("default_resource", "ROLE_TO_TEST");
        assertEquals(success, true);

    }

    // getRolePermissionMappings
    @Test
    public void getRolePermissionMappings_returnsPermissionsMap() {
        when(restTemplate.exchange(ArgumentMatchers.anyString(), eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<Void>>any(),
                eq(GetPolicyResponse.class))
        ).thenReturn(getPolicyResponse());

        Map<String, String[]> rolePermissionMappings = client
                .getRolePermissionMappings("default_resource");

        assertEquals(!rolePermissionMappings.isEmpty(), true);
    }

    @Test
    public void getRolePermissionMappings_returnsEmptyMapIfNotInitialized() {
        when(restTemplate.exchange(
                ArgumentMatchers.anyString(),
                eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<Void>>any(),
                eq(GetPolicyResponse.class))
        ).thenReturn(ResponseEntity.ok(GetPolicyResponse.builder().build()));

        Map<String, String[]> rolePermissionMappings = client
                .getRolePermissionMappings("default_resource");

        assertEquals(rolePermissionMappings.isEmpty(), true);
    }

    private UserEntity getUserEntity() {
        var userEntity = new UserEntity();
        UserRoleEntity userRole = new UserRoleEntity();
        RoleEntity role = new RoleEntity();
        role.setRoleName("allow");
        userRole.setRole(role);
        userEntity.setDisplayName("allow");
        userEntity.setUserRoleEntities(Arrays.asList(userRole));
        return userEntity;
    }

    private ResponseEntity<GetPolicyResponse> getPolicyResponse() {
        GetPolicyResponse response = GetPolicyResponse.builder()
                .policies(new Policy[] {
                        Policy.builder()
                                .resourcePolicy(
                                        ResourcePolicy.builder()
                                                .resource("default_resource")
                                                .rules(new ResourceRule[] {
                                                        ResourceRule.builder()
                                                                .actions(new String[] { "actionToTest" })
                                                                .roles(new String[] { "ROLE_TO_TEST" })
                                                                .effect(Effect.EFFECT_ALLOW)
                                                                .build()
                                                }).build()
                                ).build()
                }).build();

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<CheckResourceSetResponse> getCheckResourceResponse(Effect effect) {
        CheckResourceSetResponse response = CheckResourceSetResponse.builder()
                .resourceInstances(Map.of("user", CheckResourceSetResponseActionEffectMap.builder()
                                .actions(Map.of("allow", effect))
                        .build()))
                .build();

        return ResponseEntity.ok(response);
    }
}
