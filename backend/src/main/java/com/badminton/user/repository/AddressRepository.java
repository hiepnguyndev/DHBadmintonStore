package com.badminton.user.repository;

import com.badminton.user.entity.Address;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findAllByUserUserIdOrderByAddressIdDesc(Long userId);

  Optional<Address> findByAddressIdAndUserUserId(Long id, Long userId);
}
