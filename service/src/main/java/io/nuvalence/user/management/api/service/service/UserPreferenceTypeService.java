package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.entity.UserPreferenceTypeEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceTypeDTO;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.repository.UserPreferenceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

/**
 * Service for User Preference Types.
 */
@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceTypeService {

    private final UserPreferenceTypeRepository userPreferenceTypeRepository;

    /**
     * Gets a list of all the User Preference types.
     * @return User Preference Types.
     */
    public ResponseEntity<List<UserPreferenceTypeDTO>> getAllUserPreferenceTypes() {
        List<UserPreferenceTypeEntity> allUserPreferenceTypes = userPreferenceTypeRepository.findAll();

        List<UserPreferenceTypeDTO> userPreferenceTypes = allUserPreferenceTypes
                .stream().map(MapperUtils::mapUserPreferenceTypeEntityToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userPreferenceTypes);
    }
}
