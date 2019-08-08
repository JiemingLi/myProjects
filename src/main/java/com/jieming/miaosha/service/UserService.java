package com.jieming.miaosha.service;

import com.jieming.miaosha.dao.UserDao;
import com.jieming.miaosha.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public User getById(Integer id){
       return  userDao.getById(id);
    }


    @Transactional
    public boolean tx(){
        User u1 = new User();
        u1.setId(2);
        u1.setName("jieming2");
        userDao.insert(u1);
        User u2 = new User();
        u2.setId(1);
        u2.setName("jieming1");
        userDao.insert(u2);
        return true;
    }

}
