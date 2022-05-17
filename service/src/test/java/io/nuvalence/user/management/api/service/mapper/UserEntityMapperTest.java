package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class UserEntityMapperTest {

    @Test
    public void shouldMapUserEntityToUserDto() {
        UserEntity user = new UserEntity();
        user.setDisplayName("John Cena");
        user.setEmail("Invisable@google.com");
        user.setExternalId("48QI42I8CWObQuCvk2uuF3XlyS63");

        UserDTO userModel = UserEntityMapper.INSTANCE.convertUserEntityToUserModel(user);
        assertEquals(user.getDisplayName(), userModel.getDisplayName());
        assertEquals(user.getEmail(), userModel.getEmail());
        assertEquals(user.getExternalId(), userModel.getExternalId());
    }

    @Test
    public void shouldMapUserDtoToUserEntity() {
        UserDTO user = new UserDTO();
        user.setDisplayName("John Cena");
        user.setEmail("Invisable@google.com");
        user.setExternalId("48QI42I8CWObQuCvk2uuF3XlyS63");

        UserEntity userEntity = UserEntityMapper.INSTANCE.convertUserModelToUserEntity(user);
        assertEquals(user.getDisplayName(), userEntity.getDisplayName());
        assertEquals(user.getEmail(), userEntity.getEmail());
        assertEquals(user.getExternalId(), userEntity.getExternalId());
    }
}
