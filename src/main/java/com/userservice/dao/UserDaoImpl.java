package com.userservice.dao;

import com.userservice.entity.User;
import com.userservice.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) {
        logger.debug("Попытка сохранить пользователя", user);

        Transaction transaction = null;
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.save(user);

            transaction.commit();
            logger.info("Пользователь успешно сохранен с ID", user.getId());

            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                logger.error("Транзакция отменена при сохранении пользователя", e);
                user = null;
            }
            logger.error("Ошибка при сохранении пользователя", e);
        } finally {
            if (session != null) {
                session.close();
            }

        }
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Поиск пользователя по ID", id);

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = session.get(User.class, id);

            if (user != null) {
                logger.info("Пользователь найден", user);
            } else {
                logger.info("Не найден пользователь с ID", id);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по ID", id, e);
            throw new RuntimeException("Не удалось найти пользователя", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<User> findAll() {
        logger.debug("Получение всех пользователей");

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query<User> query = session.createQuery("FROM User", User.class);
            List<User> users = query.list();

            logger.info("Найдено пользователей", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Ошибка при получении всех пользователей", e);
            return new ArrayList<>();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public User update(User user) {
        logger.debug("Попытка обновить пользователя", user);

        Transaction transaction = null;
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.update(user);

            transaction.commit();
            logger.info("Пользователь успешно обновлен", user);

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                logger.error("Ошибка при обновлении пользователя", e);
                throw new RuntimeException(e);
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return user;
    }

    @Override
    public boolean delete(Long id) {
        logger.debug("Попытка удалить пользователя с ID", id);

        Transaction transaction = null;
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);

            if (user != null) {
                session.delete(user);
                transaction.commit();
                logger.info("Пользователь удален");
                return true;
            } else {
                transaction.commit();
                logger.warn("Пользователь не найден");
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                logger.error("Ошибка при удалении пользователя", e);
            }
            throw new RuntimeException("Не удалось удалить пользователя", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Поиск пользователя по email");

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);

            User user = query.uniqueResult();

            if (user != null) {
                logger.info("Пользователь найден", email);
            } else {
                logger.info("Пользователь не найден", email);
            }

            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по email", email, e);
            throw new RuntimeException("Не удалось найти пользователя по email", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        logger.debug("Поиск пользователя по email", email);

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class);
            query.setParameter("email", email);

            Long count = query.uniqueResult();
            boolean exists = count != null && count > 0;

            logger.debug("Пользователь с email{} существует:{}", email, exists);

            return exists;
        } catch (Exception e) {
            logger.error("Ошибка при проверке существования пользователя по email {}", email, e);
            throw new RuntimeException("Не удалось проверить существование email", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Long count() {
        logger.debug("Подсчет количества пользователей");

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM User", Long.class);

            Long count = query.uniqueResult();

            logger.info("Всего пользователей {}", count);

            return count != null ? count : 0L;
        } catch (Exception e) {
            logger.error("Ошибка при подсчете пользователей", e);
            return 0L;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void deleteAll() {
        logger.warn("Удаление ВСЕХ ПОЛЬЗОВАТЕЛЕЙ из БД");

        Transaction transaction = null;
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Query query = session.createQuery("DELETE FROM User");
            int deletedCount = query.executeUpdate();

            transaction.commit();
            logger.info("Удалено {} пользователей", deletedCount);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                logger.error("Не удалось удалить всех пользователей", e);
            }
            throw new RuntimeException("Не удалось удалить всех пользователей");
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
