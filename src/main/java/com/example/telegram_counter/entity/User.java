package com.example.telegram_counter.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "user_tg")
public class User {

    @Id
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "msg_numb", nullable = false)
    private int msg_numb;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messageList = new ArrayList<>();
}

