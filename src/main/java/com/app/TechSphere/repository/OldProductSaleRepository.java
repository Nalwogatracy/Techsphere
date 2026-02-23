package com.app.TechSphere.repository;

import com.app.TechSphere.model.OldProductSale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OldProductSaleRepository extends JpaRepository<OldProductSale, Long> {

    // Sales waiting for admin approval
    List<OldProductSale> findByApprovedFalse();

    // Approved sales only
    List<OldProductSale> findByApprovedTrue();

    // Optional: sales by user
    List<OldProductSale> findByUserId(Long userId);
}
