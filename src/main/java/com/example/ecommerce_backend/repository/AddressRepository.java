package com.example.ecommerce_backend.repository;

import com.example.ecommerce_backend.entity.Address;
import com.example.ecommerce_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    Optional<Address> findByUserAndIsDefaultTrue(User user);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    
    boolean existsByUserId(Long userId);
    
    long countByUserId(Long userId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.id != :addressId")
    void setOtherAddressesNonDefault(@Param("userId") Long userId, @Param("addressId") Long addressId);
    
    Optional<Address> findByIdAndUserId(Long id, Long userId);
    
    void deleteByUserId(Long userId);
}