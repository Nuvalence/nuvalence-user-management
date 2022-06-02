package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.entity.UserPreferenceTypeEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceTypeDTO;
import io.nuvalence.user.management.api.service.repository.UserPreferenceTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserPreferenceTypeServiceTest {

    @Mock
    UserPreferenceTypeRepository userPreferenceTypeRepository;
    
    @InjectMocks
    UserPreferenceTypeService userPreferenceTypeService;

    @Test
    void getAllUserPreferenceTypes() {
        List<UserPreferenceTypeEntity> userPreferenceTypeEntities = List.of(createUserPreferenceEntity());

        when(userPreferenceTypeRepository.findAll()).thenReturn(userPreferenceTypeEntities);
        ResponseEntity<List<UserPreferenceTypeDTO>> res = userPreferenceTypeService.getAllUserPreferenceTypes();
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody(), List.of(createUserPreferenceDto()));
    }

    private UserPreferenceTypeEntity createUserPreferenceEntity() {
        UserPreferenceTypeEntity userPreferenceTypeEntity = new UserPreferenceTypeEntity();
        userPreferenceTypeEntity.setName("language");

        return userPreferenceTypeEntity;
    }

    private UserPreferenceTypeDTO createUserPreferenceDto() {
        UserPreferenceTypeDTO userPreferenceTypeEntity = new UserPreferenceTypeDTO();
        userPreferenceTypeEntity.setName("language");

        return userPreferenceTypeEntity;
    }

}
