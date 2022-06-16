package io.nuvalence.user.management.api.service.cerbos;

import io.nuvalence.user.management.api.service.cerbos.models.AddOrUpdatePolicyRequest;
import io.nuvalence.user.management.api.service.cerbos.models.AddOrUpdatePolicyResponse;
import io.nuvalence.user.management.api.service.cerbos.models.AttributesMap;
import io.nuvalence.user.management.api.service.cerbos.models.CheckResourceSetRequest;
import io.nuvalence.user.management.api.service.cerbos.models.CheckResourceSetResponse;
import io.nuvalence.user.management.api.service.cerbos.models.CheckResourceSetResponseActionEffectMap;
import io.nuvalence.user.management.api.service.cerbos.models.Effect;
import io.nuvalence.user.management.api.service.cerbos.models.GetPolicyResponse;
import io.nuvalence.user.management.api.service.cerbos.models.Policy;
import io.nuvalence.user.management.api.service.cerbos.models.Principal;
import io.nuvalence.user.management.api.service.cerbos.models.ResourcePolicy;
import io.nuvalence.user.management.api.service.cerbos.models.ResourceRule;
import io.nuvalence.user.management.api.service.cerbos.models.ResourceSet;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.interfaces.Permissionable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Cerbos client.
 */

@Component
@Slf4j
public class CerbosClient implements Permissionable {

    @Value("${cerbos.baseUrl}")
    private String baseUrl;

    @Value("${cerbos.username}")
    private String username;

    @Value("${cerbos.password}")
    private String password;

    private RestTemplate httpClient;

    public CerbosClient() {
        httpClient = new RestTemplate();
    }

    /**
     * Sets the class-level httpClient (to be used for mocking).
     * @param restTemplate The RestTemplate object
     */
    public void setHttpClient(RestTemplate restTemplate) {
        this.httpClient = restTemplate;
    }

    /**
     * Checks if a user with the provided roles has any of the provided permissions.
     *
     * @param resourceName is the type of resource to check
     * @param userEntity is the user the check permissions against
     * @param permissionsToCheck is the list of permissions to check
     * @return whether the check is valid or not
     */
    @Override
    public Boolean check(String resourceName, UserEntity userEntity, String... permissionsToCheck) {
        try {
            resourceName = ensureResourceNameIsInitialized(resourceName);

            CheckResourceSetRequest request = buildCheckRequest(resourceName, userEntity, permissionsToCheck);
            ResponseEntity<CheckResourceSetResponse> response = postJson("/api/check", request,
                    CheckResourceSetResponse.class, false);

            if (response.getStatusCode().is2xxSuccessful()) {
                CheckResourceSetResponse responseBody = response.getBody();
                if ((responseBody != null ? responseBody.getResourceInstances() : null) != null) {
                    for (Map.Entry<String, CheckResourceSetResponseActionEffectMap> user :
                            responseBody.getResourceInstances().entrySet()) {
                        if (user.getValue().getActions() != null) {
                            for (Map.Entry<String, Effect> action : user.getValue().getActions().entrySet()) {
                                if (Arrays.stream(permissionsToCheck)
                                        .anyMatch(p -> p.equalsIgnoreCase(action.getKey())
                                                && action.getValue() == Effect.EFFECT_ALLOW)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return false;
    }

    /**
     * Adds/updates role-permission mappings.
     *
     * @param resourceName The name of the resource
     * @param roleName The name of the role
     * @param permissions The permissions for the role
     * @return whether the role-permission mapping was successful or not
     */
    public Boolean updateRolePermissionMappings(String resourceName, String roleName, String[] permissions) {
        try {
            resourceName = ensureResourceNameIsInitialized(resourceName);

            // find the policy in Cerbos (may not exist yet)
            Optional<Policy> foundPolicy = getPolicyByResourceName(resourceName);

            AddOrUpdatePolicyRequest request = buildAddOrUpdatePolicyRequest(resourceName, roleName, permissions,
                    foundPolicy);
            ResponseEntity<AddOrUpdatePolicyResponse> response = postJson("/admin/policy", request,
                    AddOrUpdatePolicyResponse.class, true);

            return Objects.requireNonNull(response.getBody()).getSuccess() != null;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return false;
    }

    /**
     * Removes the role.
     *
     * @param resourceName The name of the resource
     * @param roleName The name of the role
     * @return whether the role was successfully removed or not
     */
    public Boolean removeRole(String resourceName, String roleName) {
        try {
            resourceName = ensureResourceNameIsInitialized(resourceName);

            // find the policy in Cerbos (may not exist yet)
            Optional<Policy> foundPolicy = getPolicyByResourceName(resourceName);

            AddOrUpdatePolicyRequest request = buildAddOrUpdatePolicyRequest(resourceName, roleName, new String[] { },
                    foundPolicy);
            ResponseEntity<AddOrUpdatePolicyResponse> response = postJson("/admin/policy", request,
                    AddOrUpdatePolicyResponse.class, true);

            return Objects.requireNonNull(response.getBody()).getSuccess() != null;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return false;
    }

    /**
     * Get the role-permission mappings for the provided resource.
     *
     * @param resourceName The name of the resource
     * @return The role-permission mappings and status
     */
    public Map<String, String[]> getRolePermissionMappings(String resourceName) {
        try {
            resourceName = ensureResourceNameIsInitialized(resourceName);

            Optional<Policy> foundPolicy = getPolicyByResourceName(resourceName);
            if (foundPolicy.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<String, String[]> rolePermissionMap = new HashMap<>();

            for (ResourceRule rule : foundPolicy.get().getResourcePolicy().getRules()) {
                rolePermissionMap.put(rule.getRoles()[0], lowerCaseStringArray(rule.getActions()));
            }

            return rolePermissionMap;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return Collections.emptyMap();
    }

    private String ensureResourceNameIsInitialized(String resourceName) {
        if (resourceName == null || resourceName.length() == 0) {
            return "default_resource";
        }

        // replace all non-alphanumeric characters with an underscore
        // and then remove leading/trailing non-alphanumeric characters
        return resourceName
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("(?U)^\\P{Alpha}+|\\P{Alnum}+$|[^\\p{Alnum}_.-]", "")
                .toLowerCase();
    }

    private HttpHeaders getHeaders(Boolean isAdminCall) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (isAdminCall) {
            headers.setBasicAuth(username, password);
        }
        return headers;
    }

    private <TInput, TOutput> ResponseEntity<TOutput> postJson(String endpoint, TInput requestBody,
                                                               Class<TOutput> responseType, Boolean isAdminCall) {
        HttpHeaders headers = getHeaders(isAdminCall);
        HttpEntity<TInput> requestEntity = new HttpEntity<>(requestBody, headers);

        return httpClient.postForEntity(baseUrl + endpoint, requestEntity, responseType);
    }

    private <T> ResponseEntity<T> getJson(String endpoint, Class<T> responseType, Boolean isAdminCall) {
        HttpEntity<Void> headers = new HttpEntity<>(getHeaders(isAdminCall));
        return httpClient.exchange(baseUrl + endpoint, HttpMethod.GET, headers, responseType);
    }

    private Optional<Policy> getPolicyByResourceName(String resourceName) {
        ResponseEntity<GetPolicyResponse> getPolicyResponse =
                getJson("/admin/policy?id=resource." + resourceName + ".vdefault",
                        GetPolicyResponse.class, true);
        if (getPolicyResponse.getStatusCode().is2xxSuccessful()) {
            return Arrays.stream(Objects.requireNonNull(getPolicyResponse.getBody()).getPolicies())
                    .filter(p -> resourceName.equalsIgnoreCase(p.getResourcePolicy().getResource()))
                    .findFirst();
        }

        return Optional.empty();
    }

    private CheckResourceSetRequest buildCheckRequest(String resourceName, UserEntity userEntity,
                                                      String... permissionsToCheck) {
        // TODO: Populate "123" from elsewhere (will most likely be username or some other unique user identifier)
        return CheckResourceSetRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .principal(Principal.builder()
                        .id("123")
                        .roles(userEntity.getUserRoleEntities().stream()
                                .map(ur -> ur.getRole().getRoleName()).toArray(String[]::new))
                        .build())
                .resource(ResourceSet.builder()
                        .kind(resourceName)
                        .instances(Map.of(userEntity.getDisplayName(), new AttributesMap()))
                        .build())
                .actions(lowerCaseStringArray(permissionsToCheck))
                .build();
    }

    private AddOrUpdatePolicyRequest buildAddOrUpdatePolicyRequest(String resourceName, String roleName,
                                                                   String[] permissions, Optional<Policy> policy) {
        ArrayList<ResourceRule> rules = new ArrayList<>();

        // add the existing rules to the list
        policy.ifPresent(value -> rules.addAll(List.of(value.getResourcePolicy().getRules())));

        // remove the rule with the matching role name
        rules.removeIf(rule -> Arrays.stream(rule.getRoles()).anyMatch(role -> role.equalsIgnoreCase(roleName)));

        // only add the role as a rule if permissions (actions) are being passed in as well
        if (permissions != null && permissions.length > 0) {
            rules.add(ResourceRule.builder()
                    .roles(new String[] { roleName })
                    .actions(lowerCaseStringArray(permissions))
                    .effect(Effect.EFFECT_ALLOW)
                    .build());
        }

        return AddOrUpdatePolicyRequest.builder()
                .policies(new Policy[] {
                        Policy.builder()
                                .apiVersion("api.cerbos.dev/v1")
                                .resourcePolicy(
                                        ResourcePolicy.builder()
                                                .version("default")
                                                .resource(resourceName)
                                                .rules(rules.toArray(new ResourceRule[0])).build()
                                ).build()
                }).build();
    }

    private String[] lowerCaseStringArray(String[] strAry) {
        if (strAry == null || strAry.length == 0) {
            return new String[] { };
        }

        return Arrays.stream(strAry).map(s -> s.toLowerCase()).toArray(String[]::new);
    }
}
