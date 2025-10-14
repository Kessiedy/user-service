package com.userservice;

import com.userservice.dao.UserDao;
import com.userservice.dao.UserDaoImpl;
import com.userservice.entity.User;
import com.userservice.util.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class TestUserDao {

    public static void main(String[] args) {
        System.out.println("===Тестирование UserDao===");

        UserDao userDao = new UserDaoImpl();
        userDao.deleteAll();

        try {
            System.out.println("1. Тест CREATE\n");

            User user1 = new User("Николас Кейдж", "nick@gambler.com", 13);
            User user2 = new User("Райан Гослинг", "drive@gmail.com", 99);
            User user3 = new User("Виктор Цой", "bloodgroup@mail.ru", 33);

            user1 = userDao.save(user1);
            user2 = userDao.save(user2);
            user3 = userDao.save(user3);

            System.out.println("Созданы пользователи:");
            System.out.println("  - " + user1);
            System.out.println("  - " + user2);
            System.out.println("  - " + user3);
            System.out.println();

            System.out.println("2. Тест COUNT\n");

            long count = userDao.count();
            System.out.println("Всего пользователей в БД: " + count);
            System.out.println();

            System.out.println("3. Тест FIND BY ID\n");

            Optional<User> foundUser = userDao.findById(user1.getId());
            if (foundUser.isPresent()) {
                System.out.println("Найден пользователь: " + foundUser.get());
            } else {
                System.out.println("Пользователь не найден");
            }
            System.out.println();

            System.out.println("4. Тест FIND BY EMAIL\n");

            Optional<User> foundByEmail = userDao.findByEmail("drive@gmail.com");
            if (foundByEmail.isPresent()) {
                System.out.println("Найден пользователь: " + foundByEmail.get());
            } else {
                System.out.println("Пользователь не найден");
            }
            System.out.println();

            System.out.println("5. Тест EXISTS BY EMAIL\n");

            boolean exists = userDao.existsByEmail("nick@gambler.com");
            System.out.println("Email alex@example.com существует: " + exists);
            boolean notExists = userDao.existsByEmail("notfound@bruh.com");
            System.out.println("Email notfound@example.com существует: " + notExists);
            System.out.println();

            System.out.println("6. Тест FIND ALL\n");

            user1.setName("Ghost rider");
            user1.setAge(29);
            userDao.update(user1);
            Optional<User> updatedUser = userDao.findById(user1.getId());
            if (updatedUser.isPresent()) {
                System.out.println("Обновленный пользователь: " + updatedUser.get());
            }
            System.out.println();

            System.out.println("8. Тест DELETE\n");
            boolean deleted = userDao.delete(user3.getId());
            System.out.println("Пользователь удален: " + deleted);
            count = userDao.count();
            System.out.println("Осталось пользователей: " + count);
            System.out.println();

            System.out.println("9. Тест FIND ALL после удаления");

            List<User> allUsers = userDao.findAll();
            System.out.println("✓ Пользователей в БД: " + allUsers.size());
            allUsers.forEach(u -> System.out.println("  - " + u));
            System.out.println();

            System.out.println("====Все тесты пройдены====");
        } catch (Exception e) {
            System.err.println("Ошибка при тестировании DAO:");
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }


}