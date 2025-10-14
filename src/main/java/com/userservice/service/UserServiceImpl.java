package com.userservice.service;

import com.userservice.dao.UserDao;
import com.userservice.dao.UserDaoImpl;
import com.userservice.entity.User;
import com.userservice.exception.DatabaseException;
import com.userservice.exception.UserAlreadyExistsException;
import com.userservice.exception.ValidationException;
import com.userservice.exception.UserNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserDao userDao;

    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
    }

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User createUser(String name, String email, Integer age) {
        logger.info("Попытка создать пользователя: name={}, email={}, age={}", name, email, age);

        try {
            // Валидация данных
            validateUserData(name, email, age);

            // Проверка на существование email
            if (userDao.existsByEmail(email)) {
                logger.warn("Попытка создать пользователя с существующим email: {}", email);
                throw new UserAlreadyExistsException(email);
            }

            // Создание пользователя
            User user = new User(name, email, age);
            User savedUser = userDao.save(user);

            logger.info("Пользователь успешно создан с ID: {}", savedUser.getId());
            return savedUser;

        } catch (UserAlreadyExistsException | ValidationException e) {
            throw e; // Пробрасываем наши кастомные исключения

        } catch (Exception e) {
            logger.error("Ошибка при создании пользователя", e);
            throw new DatabaseException("Не удалось создать пользователя", e);
        }
    }

    @Override
    public User getUserById(Long id) {
        logger.info("Получение пользователя по ID: {}", id);

        if (id == null || id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }

        try {
            Optional<User> userOptional = userDao.findById(id);

            if (userOptional.isPresent()) {
                logger.info("Пользователь найден: {}", userOptional.get());
                return userOptional.get();
            } else {
                logger.warn("Пользователь с ID {} не найден", id);
                throw new UserNotFoundException(id);
            }

        } catch (UserNotFoundException e) {
            throw e;

        } catch (Exception e) {
            logger.error("Ошибка при получении пользователя по ID: {}", id, e);
            throw new DatabaseException("Не удалось получить пользователя", e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        logger.info("Получение всех пользователей");

        try {
            List<User> users = userDao.findAll();
            logger.info("Получено пользователей: {}", users.size());
            return users;

        } catch (Exception e) {
            logger.error("Ошибка при получении всех пользователей", e);
            throw new DatabaseException("Не удалось получить список пользователей", e);
        }
    }

    @Override
    public User updateUser(Long id, String name, String email, Integer age) {
        logger.info("Обновление пользователя ID: {}", id);

        if (id == null || id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }

        try {
            // Получить существующего пользователя
            Optional<User> userOptional = userDao.findById(id);

            if (!userOptional.isPresent()) {
                logger.warn("Пользователь с ID {} не найден для обновления", id);
                throw new UserNotFoundException(id);
            }

            User user = userOptional.get();
            boolean hasChanges = false;

            // Обновить имя, если указано
            if (name != null && !name.trim().isEmpty()) {
                validateName(name);
                user.setName(name.trim());
                hasChanges = true;
            }

            // Обновить email, если указан
            if (email != null && !email.trim().isEmpty()) {
                validateEmail(email);

                // Проверить, что новый email не занят другим пользователем
                if (!email.equals(user.getEmail()) && userDao.existsByEmail(email)) {
                    logger.warn("Email {} уже используется другим пользователем", email);
                    throw new UserAlreadyExistsException(email);
                }

                user.setEmail(email.trim());
                hasChanges = true;
            }

            // Обновить возраст, если указан
            if (age != null) {
                validateAge(age);
                user.setAge(age);
                hasChanges = true;
            }

            if (!hasChanges) {
                logger.info("Нет изменений для пользователя ID: {}", id);
                return user;
            }

            // Сохранить изменения
            User updatedUser = userDao.update(user);
            logger.info("Пользователь успешно обновлен: {}", updatedUser);

            return updatedUser;

        } catch (UserNotFoundException | ValidationException | UserAlreadyExistsException e) {
            throw e;

        } catch (Exception e) {
            logger.error("Ошибка при обновлении пользователя ID: {}", id, e);
            throw new DatabaseException("Не удалось обновить пользователя", e);
        }
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Удаление пользователя с ID: {}", id);

        if (id == null || id <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }

        try {
            boolean deleted = userDao.delete(id);

            if (!deleted) {
                logger.warn("Пользователь с ID {} не найден для удаления", id);
                throw new UserNotFoundException(id);
            }

            logger.info("Пользователь с ID {} успешно удален", id);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя ID: {}", id, e);
            throw new DatabaseException("Не удалось удалить пользователя", e);
        }
    }

    @Override
    public User getUserByEmail(String email) {
        logger.info("Поиск пользователя по email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email не может быть пустым");
        }

        try {
            Optional<User> userOptional = userDao.findByEmail(email.trim());

            if (userOptional.isPresent()) {
                logger.info("Пользователь с email {} найден", email);
                return userOptional.get();
            } else {
                logger.warn("Пользователь с email {} не найден", email);
                throw new UserNotFoundException("\"Пользователь с email \" + email + \" не найден\"");
            }
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по email: {}", email, e);
            throw new DatabaseException("Не удалось найти пользователя по email", e);
        }
    }

    @Override
    public long getUserCount() {
        logger.info("Получение количества пользователей");

        try {
            long count = userDao.count();
            logger.info("Количество пользователей: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Ошибка при подсчете пользователей", e);
            throw new DatabaseException("Не удалось получить количество пользователей", e);
        }
    }

    @Override
    public void deleteAllUsers() {
        logger.warn("ВНИМАНИЕ: Удаление всех пользователей!");

        try {
            userDao.deleteAll();
            logger.info("Все пользователи удалены");
        } catch (Exception e) {
            logger.error("Ошибка при удалении всех пользователей", e);
            throw new DatabaseException("Не удалось удалить всех пользователей", e);
        }
    }

    private void validateUserData(String name, String email, Integer age) {

    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Имя не может быть пустым");
        }

        if (name.trim().length() < 2) {
            throw new ValidationException("Имя должно содержать минимум 2 символа");
        }

        if (name.trim().length() > 100) {
            throw new ValidationException("Имя не может быть длиннее 100 символов");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Некорректный формат email");
        }

        if (email.trim().length() > 150) {
            throw new ValidationException("Email не может быть длиннее 150 символов");
        }
    }

    private void validateAge(Integer age) {
        if (age == null) {
            return;
        }
        if (age < 0) {
            throw new ValidationException("Возраст не может быть отрицательным");
        }

        if (age > 110) {
            throw new ValidationException("Возраст не может быть больше 110 лет");
        }
    }
}

