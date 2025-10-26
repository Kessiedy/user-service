package com.userservice;

import com.userservice.dao.UserDao;
import com.userservice.dao.UserDaoImpl;
import com.userservice.entity.User;
import com.userservice.exception.UserAlreadyExistsException;
import com.userservice.exception.UserNotFoundException;
import com.userservice.exception.ValidationException;
import com.userservice.service.UserService;
import com.userservice.service.UserServiceImpl;
import com.userservice.util.HibernateUtil;

import java.util.List;

public class TestUserService {

    public static void main(String[] args) {

        UserDao userDao = new UserDaoImpl();
        userDao.deleteAll();
        System.out.println("=== Тестирование Service слоя ===");

        UserService userService = new UserServiceImpl();

        try {
            System.out.println("1. Тест CREATE с валидацией");
            ;

            User user1 = userService.createUser("Анна Смирнова", "anna@example.com", 28);
            System.out.println("Создан: " + user1);

            User user2 = userService.createUser("Петр Иванов", "petr@example.com", 35);
            System.out.println("Создан: " + user2);

            System.out.println();

            System.out.println("2. Тест валидации - пустое имя");
            try {
                userService.createUser("", "test@example.com", 25);
                System.out.println("Валидация не сработала!");
            } catch (ValidationException e) {
                System.out.println("Валидация сработала: " + e.getMessage());
            }
            System.out.println();

            System.out.println("3. Тест валидации - некорректный email");
            try {
                userService.createUser("Тест", "invalid-email", 25);
                System.out.println("Валидация не сработала!");
            } catch (ValidationException e) {
                System.out.println("Валидация сработала: " + e.getMessage());
            }
            System.out.println();

            System.out.println("4. Тест валидации - отрицательный возраст");
            try {
                userService.createUser("Тест", "test@example.com", -5);
                System.out.println("Валидация не сработала!");
            } catch (ValidationException e) {
                System.out.println("Валидация сработала: " + e.getMessage());
            }
            System.out.println();

            System.out.println("5. Тест дубликата email");
            try {
                userService.createUser("Дубликат", "anna@example.com", 30);
                System.out.println("Проверка дубликата не сработала!");
            } catch (UserAlreadyExistsException e) {
                System.out.println("Проверка дубликата сработала: " + e.getMessage());
            }
            System.out.println();

            System.out.println("6. Тест получения пользователя по ID");
            User foundUser = userService.getUserById(user1.getId());
            System.out.println("Найден: " + foundUser);
            System.out.println();

            System.out.println("7. Тест получения несуществующего пользователя");
            try {
                userService.getUserById(9999L);
                System.out.println("Исключение не выброшено!");
            } catch (UserNotFoundException e) {
                System.out.println("Исключение выброшено: " + e.getMessage());
            }
            System.out.println();

            System.out.println("8. Тест получения всех пользователей");
            List<User> allUsers = userService.getAllUsers();
            System.out.println("Найдено пользователей: " + allUsers.size());
            allUsers.forEach(u -> System.out.println("  - " + u));
            System.out.println();

            System.out.println("9. Тест обновления пользователя");
            User updatedUser = userService.updateUser(user1.getId(), "Анна Петрова", null, 29);
            System.out.println("Обновлен: " + updatedUser);
            System.out.println();

            System.out.println("10. Тест поиска по email");
            User foundByEmail = userService.getUserByEmail("petr@example.com");
            System.out.println("Найден по email: " + foundByEmail);
            System.out.println();

            System.out.println("11. Тест подсчета пользователей");
            long count = userService.getUserCount();
            System.out.println("Всего пользователей: " + count);
            System.out.println();

            System.out.println("12. Тест удаления пользователя");
            userService.deleteUser(user2.getId());
            System.out.println("Пользователь удален");

            count = userService.getUserCount();
            System.out.println("Осталось пользователей: " + count);
            System.out.println();

            System.out.println("=== ВСЕ ТЕСТЫ SERVICE УСПЕШНО ПРОЙДЕНЫ! ===");

        } catch (Exception e) {
            System.err.println("Неожиданная ошибка при тестировании:");
            e.printStackTrace();

        } finally {
            HibernateUtil.shutdown();
        }
    }
}