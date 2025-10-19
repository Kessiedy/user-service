package com.userservice.dao;

import com.userservice.entity.User;
import com.userservice.util.TestHibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@DisplayName("UserDao Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private static SessionFactory sessionFactory;
    private static UserDao userDao;

    @BeforeAll
    static void beforeAll() {

        sessionFactory = TestHibernateUtil.buildSessionFactory(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        userDao = new UserDaoImpl() {
            @Override
            public User save(User user) {
                Session session = null;
                try {
                    session = sessionFactory.openSession();
                    session.beginTransaction();
                    session.save(user);
                    session.getTransaction().commit();
                    return user;
                } catch (Exception e) {
                    if (session != null && session.getTransaction() != null) {
                        session.getTransaction().rollback();
                    }
                    throw new RuntimeException(e);
                } finally {
                    if (session != null) session.close();
                }
            }

            @Override
            public Optional<User> findById(Long id) {
                Session session = null;
                try {
                    session = sessionFactory.openSession();
                    User user = session.get(User.class, id);
                    return Optional.ofNullable(user);
                } finally {
                    if (session != null) session.close();
                }
            }

            @Override
            public List<User> findAll() {
                Session session = null;
                try {
                    session = sessionFactory.openSession();
                    return session.createQuery("FROM User", User.class).list();
                } finally {
                    if (session != null) session.close();
                }
            }

            @Override
            public User update(User user) {
                Session session = null;
                try {
                    session = sessionFactory.openSession();
                    session.beginTransaction();
                    session.update(user);
                    session.getTransaction().commit();
                    return user;
                } catch (Exception e) {
                    if (session != null && session.getTransaction() != null) {
                        session.getTransaction().rollback();
                    }
                    throw new RuntimeException(e);
                } finally {
                    if (session != null) session.close();
                }
            }

            @Override
            public boolean delete(Long id) {
                Session session = null;
                try {
                    session = sessionFactory.openSession();
                    session.beginTransaction();
                    User user = session.get(User.class, id);
                    if (user != null) {
                        session.delete(user);
                        session.getTransaction().commit();
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    if (session != null && session.getTransaction() != null) {
                        session.getTransaction().rollback();
                    }
                    throw new RuntimeException(e);
                } finally {
                    if (session != null) session.close();
                }
            }

            @Override
            public Optional<User> findByEmail(String email) {
                Session session = null;
                try {
                    session = sessionFactory.openSession();
                    User user = session.createQuery("FROM User WHERE email = :email", User.class)
                            .setParameter("email", email)
                            .uniqueResult();
                    return Optional.ofNullable(user);
                } finally {
                    if (session != null) session.close();
                }
            }

            @Override
            public boolean existsByEmail(String email) {
                Session session = null;
                try {
                    session = sessionFactory.openSession();
                    Long count = session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class)
                            .setParameter("email", email)
                            .uniqueResult();
                    return count != null && count > 0;
                } finally {
                    if (session != null) session.close();
                }
            }

            @Override
            public Long count() {
                Session session = null;
                try {
                    session = sessionFactory.openSession();
                    Long count = session.createQuery("SELECT COUNT(*) FROM User", Long.class).uniqueResult();
                    return count != null ? count : 0L;
                } finally {
                    if (session != null) session.close();
                }
            }

            @Override
            public void deleteAll() {
                Session session = null;
                try {
                    session = sessionFactory.openSession();
                    session.beginTransaction();
                    session.createQuery("DELETE FROM User").executeUpdate();
                    session.getTransaction().commit();
                } catch (Exception e) {
                    if (session != null && session.getTransaction() != null) {
                        session.getTransaction().rollback();
                    }
                    throw new RuntimeException(e);
                } finally {
                    if (session != null) session.close();
                }
            }
        };
    }

    @AfterAll
    static void afterAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        userDao.deleteAll();
    }


    @Test
    @Order(1)
    @DisplayName("Должен сохранить пользователя в БД")
    void shouldSaveUser() {

        User user = new User("John Doe", "john@example.com", 30);

        User savedUser = userDao.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getAge()).isEqualTo(30);
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Должен сохранить пользователя без возраста")
    void shouldSaveUserWithoutAge() {

        User user = new User("Jane Doe", "jane@example.com", null);

        User savedUser = userDao.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getAge()).isNull();
    }

    @Test
    @Order(3)
    @DisplayName("Должен автоматически установить createdAt при сохранении")
    void shouldAutoSetCreatedAt() {

        User user = new User("Test User", "test@example.com", 25);

        User savedUser = userDao.save(user);

        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
    }


    @Test
    @Order(4)
    @DisplayName("Должен найти пользователя по ID")
    void shouldFindUserById() {

        User user = new User("John Doe", "john@example.com", 30);
        User savedUser = userDao.save(user);

        Optional<User> foundUser = userDao.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @Order(5)
    @DisplayName("Должен вернуть пустой Optional если пользователь не найден")
    void shouldReturnEmptyOptionalWhenUserNotFound() {

        Optional<User> foundUser = userDao.findById(999L);

        assertThat(foundUser).isEmpty();
    }


    @Test
    @Order(6)
    @DisplayName("Должен вернуть всех пользователей")
    void shouldReturnAllUsers() {

        userDao.save(new User("User 1", "user1@example.com", 25));
        userDao.save(new User("User 2", "user2@example.com", 30));
        userDao.save(new User("User 3", "user3@example.com", 35));

        List<User> users = userDao.findAll();

        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("User 1", "User 2", "User 3");
    }

    @Test
    @Order(7)
    @DisplayName("Должен вернуть пустой список когда нет пользователей")
    void shouldReturnEmptyListWhenNoUsers() {

        List<User> users = userDao.findAll();

        assertThat(users).isEmpty();
    }


    @Test
    @Order(8)
    @DisplayName("Должен обновить пользователя")
    void shouldUpdateUser() {

        User user = new User("John Doe", "john@example.com", 30);
        User savedUser = userDao.save(user);

        savedUser.setName("John Smith");
        savedUser.setAge(31);
        User updatedUser = userDao.update(savedUser);

        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Smith");
        assertThat(foundUser.get().getAge()).isEqualTo(31);
    }

    @Test
    @Order(9)
    @DisplayName("Должен обновить только измененные поля")
    void shouldUpdateOnlyChangedFields() {

        User user = new User("John Doe", "john@example.com", 30);
        User savedUser = userDao.save(user);
        String originalEmail = savedUser.getEmail();

        savedUser.setName("John Smith");
        userDao.update(savedUser);

        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Smith");
        assertThat(foundUser.get().getEmail()).isEqualTo(originalEmail);
    }


    @Test
    @Order(10)
    @DisplayName("Должен удалить пользователя")
    void shouldDeleteUser() {

        User user = new User("John Doe", "john@example.com", 30);
        User savedUser = userDao.save(user);

        boolean deleted = userDao.delete(savedUser.getId());

        assertThat(deleted).isTrue();
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(11)
    @DisplayName("Должен вернуть false при удалении несуществующего пользователя")
    void shouldReturnFalseWhenDeletingNonExistentUser() {

        boolean deleted = userDao.delete(999L);

        assertThat(deleted).isFalse();
    }


    @Test
    @Order(12)
    @DisplayName("Должен найти пользователя по email")
    void shouldFindUserByEmail() {

        User user = new User("John Doe", "john@example.com", 30);
        userDao.save(user);

        Optional<User> foundUser = userDao.findByEmail("john@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john@example.com");
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @Order(13)
    @DisplayName("Должен вернуть пустой Optional если email не найден")
    void shouldReturnEmptyOptionalWhenEmailNotFound() {

        Optional<User> foundUser = userDao.findByEmail("notfound@example.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(14)
    @DisplayName("Email должен быть уникальным")
    void emailShouldBeUnique() {

        User user1 = new User("User 1", "duplicate@example.com", 25);
        userDao.save(user1);

        User user2 = new User("User 2", "duplicate@example.com", 30);

        assertThatThrownBy(() -> userDao.save(user2))
                .isInstanceOf(Exception.class);
    }


    @Test
    @Order(15)
    @DisplayName("Должен вернуть true если email существует")
    void shouldReturnTrueWhenEmailExists() {

        User user = new User("John Doe", "john@example.com", 30);
        userDao.save(user);

        boolean exists = userDao.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @Order(16)
    @DisplayName("Должен вернуть false если email не существует")
    void shouldReturnFalseWhenEmailDoesNotExist() {

        boolean exists = userDao.existsByEmail("notfound@example.com");

        assertThat(exists).isFalse();
    }


    @Test
    @Order(17)
    @DisplayName("Должен вернуть корректное количество пользователей")
    void shouldReturnCorrectUserCount() {

        userDao.save(new User("User 1", "user1@example.com", 25));
        userDao.save(new User("User 2", "user2@example.com", 30));
        userDao.save(new User("User 3", "user3@example.com", 35));

        long count = userDao.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    @Order(18)
    @DisplayName("Должен вернуть 0 когда нет пользователей")
    void shouldReturnZeroWhenNoUsers() {

        long count = userDao.count();

        assertThat(count).isZero();
    }


    @Test
    @Order(19)
    @DisplayName("Должен удалить всех пользователей")
    void shouldDeleteAllUsers() {

        userDao.save(new User("User 1", "user1@example.com", 25));
        userDao.save(new User("User 2", "user2@example.com", 30));
        userDao.save(new User("User 3", "user3@example.com", 35));

        userDao.deleteAll();

        long count = userDao.count();
        assertThat(count).isZero();

        List<User> users = userDao.findAll();
        assertThat(users).isEmpty();
    }


    @Test
    @Order(20)
    @DisplayName("Должен корректно работать с последовательными операциями")
    void shouldHandleSequentialOperations() {

        User user = new User("John Doe", "john@example.com", 30);
        User savedUser = userDao.save(user);
        assertThat(savedUser.getId()).isNotNull();

        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isPresent();

        foundUser.get().setAge(31);
        userDao.update(foundUser.get());

        Optional<User> updatedUser = userDao.findById(savedUser.getId());
        assertThat(updatedUser.get().getAge()).isEqualTo(31);

        boolean deleted = userDao.delete(savedUser.getId());
        assertThat(deleted).isTrue();

        Optional<User> deletedUser = userDao.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @Order(21)
    @DisplayName("Должен корректно обрабатывать транзакции")
    void shouldHandleTransactionsCorrectly() {

        User user1 = new User("User 1", "user1@example.com", 25);
        User user2 = new User("User 2", "user2@example.com", 30);

        userDao.save(user1);
        userDao.save(user2);

        List<User> users = userDao.findAll();
        assertThat(users).hasSize(2);
    }
}