//package com.example.telegram_counter.repository;
//
//import com.example.telegram_counter.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import javax.transaction.Transactional;
//
//public interface UserRepository extends JpaRepository<User, Long> {
//
//    @Transactional
//    @Modifying
//    @Query("update User t set t.msg_numb = t.msg_numb + 1 where t.id is not null and t.id = :id")
//    void updateMsgNumberByUserId(@Param("id") long id);
//
//    @Transactional
//    @Query("select name from User where  msg_numb = (SELECT MAX(msg_numb) FROM User)")
//    String mostPosts();
//
//    @Transactional
//    @Query("select msg_numb from User where msg_numb = (SELECT MAX(msg_numb) FROM User)")
//    int sendMessage();
//
//    @Transactional
//    @Query("select name, msg_numb  from User order by msg_numb DESC ")
//    String findTop10UsersByMsgNumb();
//
//}
