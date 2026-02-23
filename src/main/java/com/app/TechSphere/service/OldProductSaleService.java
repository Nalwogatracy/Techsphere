package com.app.TechSphere.service;

import com.app.TechSphere.model.OldProductSale;
import com.app.TechSphere.repository.OldProductSaleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OldProductSaleService {

    private final OldProductSaleRepository oldProductSaleRepository;

    public OldProductSaleService(OldProductSaleRepository oldProductSaleRepository) {
        this.oldProductSaleRepository = oldProductSaleRepository;
    }

    // User submits old product for sale
    public OldProductSale submitSale(OldProductSale sale) {
        sale.setApproved(false); // force pending
        return oldProductSaleRepository.save(sale);
    }

    // Admin: view pending sales
    public List<OldProductSale> getPendingSales() {
        return oldProductSaleRepository.findByApprovedFalse();
    }

    // Admin: approve sale
    public void approveSale(Long saleId) {
        OldProductSale sale = oldProductSaleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        sale.setApproved(true);
        oldProductSaleRepository.save(sale);
    }

    // Admin: reject / delete sale
    public void rejectSale(Long saleId) {
        oldProductSaleRepository.deleteById(saleId);
    }

    // User: view own sales
    public List<OldProductSale> getUserSales(Long userId) {
        return oldProductSaleRepository.findByUserId(userId);
    }

    // Public: view approved old products
    public List<OldProductSale> getApprovedSales() {
        return oldProductSaleRepository.findByApprovedTrue();
    }
    public List<OldProductSale> findPendingSales() {
        return oldProductSaleRepository.findByApprovedFalse();
    }
    public OldProductSale getById(Long id) {
    return oldProductSaleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sale not found"));
}

}
