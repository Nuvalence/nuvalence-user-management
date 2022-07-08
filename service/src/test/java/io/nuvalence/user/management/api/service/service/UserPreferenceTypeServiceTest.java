package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.entity.UserPreferenceOptionEntity;
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
import java.util.UUID;

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
        List<UserPreferenceTypeEntity> userPreferenceTypeEntities = List.of(createUserPreferenceTypeEntity());
        UserPreferenceTypeDTO typeDto = createUserPreferenceTypeDto();
        typeDto.setId(userPreferenceTypeEntities.get(0).getId());

        when(userPreferenceTypeRepository.findAll()).thenReturn(userPreferenceTypeEntities);
        ResponseEntity<List<UserPreferenceTypeDTO>> res = userPreferenceTypeService.getAllUserPreferenceTypes();
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody(), List.of(typeDto));
    }

    private UserPreferenceTypeEntity createUserPreferenceTypeEntity() {
        UserPreferenceOptionEntity emailOption = new UserPreferenceOptionEntity();
        emailOption.setId(UUID.fromString("0e3929a9-fabc-4d73-8cfe-14c89c51b531"));
        emailOption.setValue("email");

        UserPreferenceOptionEntity phoneOption = new UserPreferenceOptionEntity();
        phoneOption.setId(UUID.fromString("3505d910-a479-423b-b3f8-a3d16798a651"));
        phoneOption.setValue("phone");

        UserPreferenceTypeEntity userPreferenceTypeEntity = new UserPreferenceTypeEntity();
        userPreferenceTypeEntity.setId(UUID.randomUUID());
        userPreferenceTypeEntity.setName("communication");
        emailOption.setUserPreferenceType(userPreferenceTypeEntity);
        phoneOption.setUserPreferenceType(userPreferenceTypeEntity);
        userPreferenceTypeEntity.setUserPreferenceOptionEntities(List.of(emailOption, phoneOption));

        return userPreferenceTypeEntity;
    }

    private UserPreferenceTypeDTO createUserPreferenceTypeDto() {
        UserPreferenceTypeDTO userPreferenceTypeEntity = new UserPreferenceTypeDTO();
        userPreferenceTypeEntity.setName("communication");
        userPreferenceTypeEntity.setId(UUID.randomUUID());
        userPreferenceTypeEntity.setOptions(List.of("email", "phone"));

        return userPreferenceTypeEntity;
    }

}
