package com.userservice.service;

import com.userservice.dao.UserDao;
import com.userservice.entity.User;
import com.userservice.exception.UserAlreadyExistsException;
import com.userservice.exception.UserNotFoundException;
import com.userservice.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", 30);
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now());
    }


    @Test
    @DisplayName("Должен успешно создать пользователя с валидными данными")
    void shouldCreateUserWithValidData() {

        when(userDao.existsByEmail(anyString())).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser("John Doe", "john@example.com", 30);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("John Doe");
        assertThat(createdUser.getEmail()).isEqualTo("john@example.com");
        assertThat(createdUser.getAge()).isEqualTo(30);

        verify(userDao, times(1)).existsByEmail("john@example.com");
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Должен создать пользователя без возраста")
    void shouldCreateUserWithoutAge() {

        User userWithoutAge = new User("Jane Doe", "jane@example.com", null);
        userWithoutAge.setId(2L);

        when(userDao.existsByEmail(anyString())).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(userWithoutAge);

        User createdUser = userService.createUser("Jane Doe", "jane@example.com", null);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getAge()).isNull();

        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при дубликате email")
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        when(userDao.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.createUser("John Doe", "john@example.com", 30)
        )
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("john@example.com");

        verify(userDao, times(1)).existsByEmail("john@example.com");
        verify(userDao, never()).save(any(User.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Должен выбросить ValidationException при пустом имени")
    void shouldThrowValidationExceptionWhenNameIsBlank(String invalidName) {

        assertThatThrownBy(() ->
                userService.createUser(invalidName, "test@example.com", 25)
        )
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Имя не может быть пустым");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен выбросить ValidationException при коротком имени")
    void shouldThrowValidationExceptionWhenNameIsTooShort() {

        assertThatThrownBy(() ->
                userService.createUser("A", "test@example.com", 25)
        )
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("минимум 2 символа");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен выбросить ValidationException при слишком длинном имени")
    void shouldThrowValidationExceptionWhenNameIsTooLong() {

        String longName = "A".repeat(101);

        assertThatThrownBy(() ->
                userService.createUser(longName, "test@example.com", 25)
        )
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не может быть длиннее 100 символов");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@", "@example.com", "test@.com", "test..@example.com"})
    @DisplayName("Должен выбросить ValidationException при некорректном email")
    void shouldThrowValidationExceptionWhenEmailIsInvalid(String invalidEmail) {

        assertThatThrownBy(() ->
                userService.createUser("John Doe", invalidEmail, 25)
        )
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Некорректный формат email");
    }

    @Test
    @DisplayName("Должен выбросить ValidationException при отрицательном возрасте")
    void shouldThrowValidationExceptionWhenAgeIsNegative() {

        assertThatThrownBy(() ->
                userService.createUser("John Doe", "john@example.com", -5)
        )
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не может быть отрицательным");
    }

    @Test
    @DisplayName("Должен выбросить ValidationException при возрасте больше 150")
    void shouldThrowValidationExceptionWhenAgeIsTooHigh() {

        assertThatThrownBy(() ->
                userService.createUser("John Doe", "john@example.com", 151)
        )
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не может быть больше 150");
    }

    @Test
    @DisplayName("Должен найти пользователя по ID")
    void shouldFindUserById() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserById(1L);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(1L);
        assertThat(foundUser.getName()).isEqualTo("John Doe");

        verify(userDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Должен выбросить UserNotFoundException когда пользователь не найден")
    void shouldThrowExceptionWhenUserNotFoundById() {

        when(userDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userDao, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Должен выбросить ValidationException при null ID")
    void shouldThrowValidationExceptionWhenIdIsNull() {

        assertThatThrownBy(() -> userService.getUserById(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("ID");

        verify(userDao, never()).findById(any());
    }

    @Test
    @DisplayName("Должен выбросить ValidationException при отрицательном ID")
    void shouldThrowValidationExceptionWhenIdIsNegative() {

        assertThatThrownBy(() -> userService.getUserById(-1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("положительным");

        verify(userDao, never()).findById(any());
    }


    @Test
    @DisplayName("Должен вернуть всех пользователей")
    void shouldReturnAllUsers() {

        User user2 = new User("Jane Doe", "jane@example.com", 25);
        user2.setId(2L);

        List<User> users = Arrays.asList(testUser, user2);
        when(userDao.findAll()).thenReturn(users);

        List<User> foundUsers = userService.getAllUsers();

        assertThat(foundUsers).hasSize(2);
        assertThat(foundUsers).containsExactly(testUser, user2);

        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("Должен вернуть пустой список когда нет пользователей")
    void shouldReturnEmptyListWhenNoUsers() {

        when(userDao.findAll()).thenReturn(Arrays.asList());

        List<User> foundUsers = userService.getAllUsers();

        assertThat(foundUsers).isEmpty();

        verify(userDao, times(1)).findAll();
    }


    @Test
    @DisplayName("Должен обновить имя пользователя")
    void shouldUpdateUserName() {

        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.update(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.updateUser(1L, "John Smith", null, null);

        assertThat(updatedUser.getName()).isEqualTo("John Smith");

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("Должен обновить email пользователя")
    void shouldUpdateUserEmail() {

        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userDao.update(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.updateUser(1L, null, "newemail@example.com", null);

        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");

        verify(userDao, times(1)).existsByEmail("newemail@example.com");
        verify(userDao, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении email на существующий")
    void shouldThrowExceptionWhenUpdatingToExistingEmail() {

        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.updateUser(1L, null, "existing@example.com", null)
        )
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Должен обновить возраст пользователя")
    void shouldUpdateUserAge() {

        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.update(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.updateUser(1L, null, null, 35);

        assertThat(updatedUser.getAge()).isEqualTo(35);

        verify(userDao, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("Не должен обновлять если нет изменений")
    void shouldNotUpdateWhenNoChanges() {

        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.updateUser(1L, null, null, null);

        assertThat(result).isEqualTo(testUser);

        verify(userDao, never()).update(any(User.class));
    }


    @Test
    @DisplayName("Должен удалить пользователя")
    void shouldDeleteUser() {

        when(userDao.delete(1L)).thenReturn(true);

        assertThatCode(() -> userService.deleteUser(1L))
                .doesNotThrowAnyException();

        verify(userDao, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Должен выбросить исключение при удалении несуществующего пользователя")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {

        when(userDao.delete(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userDao, times(1)).delete(999L);
    }


    @Test
    @DisplayName("Должен найти пользователя по email")
    void shouldFindUserByEmail() {

        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserByEmail("john@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("john@example.com");

        verify(userDao, times(1)).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Должен выбросить исключение когда email не найден")
    void shouldThrowExceptionWhenEmailNotFound() {

        when(userDao.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("notfound@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("notfound@example.com");
    }


    @Test
    @DisplayName("Должен вернуть количество пользователей")
    void shouldReturnUserCount() {

        when(userDao.count()).thenReturn(5L);

        long count = userService.getUserCount();

        assertThat(count).isEqualTo(5L);

        verify(userDao, times(1)).count();
    }


    @Test
    @DisplayName("Должен удалить всех пользователей")
    void shouldDeleteAllUsers() {

        doNothing().when(userDao).deleteAll();

        assertThatCode(() -> userService.deleteAllUsers())
                .doesNotThrowAnyException();

        verify(userDao, times(1)).deleteAll();
    }
}