package com.example.bank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bank.model.User;

public interface UserSearchRepository extends JpaRepository<User, Long> {
    
    /**
     * Поиск пользователей с применением фильтров
     * 
     * @param name часть имени для поиска (будет использоваться LIKE name%)
     * @param email точное значение email
     * @param phone точное значение телефона
     * @param dateOfBirth дата рождения (ищет пользователей с датой рождения > указанной)
     * @param pageable параметры пагинации
     * @return страница с результатами поиска
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN u.emails e " +
           "LEFT JOIN u.phones p " +
           "WHERE (:name IS NULL OR u.name LIKE :namePattern) " +
           "AND (:email IS NULL OR e.email = :email) " +
           "AND (:phone IS NULL OR p.phone = :phone) " +
           "AND (:dateOfBirth IS NULL OR u.dateOfBirth > :dateOfBirth)")
    Page<User> findUsersByFilters(
            @Param("name") String name,
            @Param("namePattern") String namePattern,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("dateOfBirth") String dateOfBirth,
            Pageable pageable);
} 