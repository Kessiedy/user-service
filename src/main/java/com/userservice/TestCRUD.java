package com.userservice;

import com.userservice.entity.User;
import com.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class TestCRUD {

    public static void main(String[] args) {
        System.out.println("===Общий тест===\n");

        System.out.println("1. Create");
        Long userId1 = createUser("Кулаков Семен", "kulka@rambler.ru", 22);
        Long userId2 = createUser("Маршал Жуков", "zh_ivvv@gmail.com", 20);
        System.out.println("Созданы пользователи с ID:" + userId1 + "и" + userId2);

        System.out.println("2. Read");
        User user = findUserById(userId1);
        System.out.println("Найден пользователь" + user);

        System.out.println("3. Read all");
        List<User> users = findAllUsers();
        System.out.println("Найдено" + users.size() + "пользователей");
        users.forEach(u -> System.out.println("-" + u));
        System.out.println();

        System.out.println("4. Update");
        updateUser(userId2, "Primo Victoria", 19);
        User updatedUser = findUserById(userId2);
        System.out.println("Обновлен пользователь" + updatedUser);

        System.out.println("5. Delete");
        deleteUser(userId2);
        System.out.println(findAllUsers());

        System.out.println("Конец теста");
        HibernateUtil.shutdown();
    }

    private static Long createUser(String name, String email, Integer age) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        User user = new User(name, email, age);
        session.save(user);

        transaction.commit();
        session.close();

        return user.getId();
    }

    private static User findUserById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = session.get(User.class, id);
        session.close();
        return user;
    }

    private static List<User> findAllUsers() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<User> users = session.createQuery("FROM User", User.class).list();
        session.close();
        return users;
    }

    private static void updateUser(Long id, String newName, Integer newAge) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        User user = session.get(User.class, id);
        user.setName(newName);
        user.setAge(newAge);
        session.update(user);

        transaction.commit();
        session.close();
    }

    private static void deleteUser(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        User user = session.get(User.class, id);
        session.delete(user);

        transaction.commit();
        session.close();
    }
}