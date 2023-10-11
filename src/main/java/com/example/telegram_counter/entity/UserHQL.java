package com.example.telegram_counter.entity;

import com.example.telegram_counter.hibernateConfig.HibernateConfig;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import net.bytebuddy.asm.Advice;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import static com.example.telegram_counter.entity.QUser.user;
import static com.example.telegram_counter.entity.QMessage.message;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserHQL {

    /**
     * Возврат всех участников по имени и количеству сообщений + сортировка от мах -> мин
     */
    public List<Tuple> topUserMostPostsALL() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            var fetch = new JPAQuery<User>(session)
                    .select(user.name, user.msg_numb)
                    .from(user)
                    .orderBy(user.msg_numb.desc())
                    .fetch();
            return fetch;
        }
    }

    /**
     * Возврат всех участников по имени и количеству сообщений + сортировка от мах -> мин ЗА ДЕНЬ
     */
    public List<Tuple> topUserMostPostsPerDay() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            LocalDateTime newDatetime = localDateTime.minusHours(24);
            return new JPAQuery<Tuple>(session)
                    .select(user.name, message.count().intValue())
                    .from(message)
                    .join(message.user, user)
                    .where(message.localDateTime.after(newDatetime))
                    .orderBy(message.count().desc())
                    .groupBy(user.name)
                    .fetch();
        }
    }

    /**
     * Возврат всех участников по имени и количеству сообщений + сортировка от мах -> мин ЗА НЕДЕЛЮ
     */
    public List<Tuple> topUserMostPostsPerWeek() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            LocalDateTime newDatetime = localDateTime.minusWeeks(1);
            return new JPAQuery<Tuple>(session)
                    .select(user.name, message.count().intValue())
                    .from(message)
                    .join(message.user, user)
                    .where(message.localDateTime.after(newDatetime))
                    .orderBy(message.count().desc())
                    .groupBy(user.name)
                    .fetch();
        }
    }


    //находиим и вызываем юзера, редактируем его и апдейтим
    public void updateMsgNumberByUserId(long id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            user.setMsg_numb(user.getMsg_numb() + 1);
            session.update(user);
            transaction.commit();
        }
    }

    // возвращает юзера
    public User findByUser(long userId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return session.get(User.class, userId);
        }
    }

    //проверяем, есть ли пользователь в БД
    public boolean findById(long id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            return user != null;
        }
    }

    public void saveMSG(Message message) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(message);
            session.update(message);

            transaction.commit();
        }
    }

    //сейв пользователя
    public void saveUser(User user) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
        }
    }
}
