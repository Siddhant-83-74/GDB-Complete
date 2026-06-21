package com.gdb.transactions.repository;

import com.gdb.transactions.domain.enums.PrivilegeLevel;
import com.gdb.transactions.domain.model.TransferLimit;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for transfer limit operations.
 */
public interface TransferLimitRepository {
    
    /**
     * Find transfer limit by privilege level.
     */
    Optional<TransferLimit> findByPrivilege(PrivilegeLevel privilege);
    
    /**
     * Find all transfer limits.
     */
    List<TransferLimit> findAll();
    
    /**
     * Save or update transfer limit.
     */
    TransferLimit save(TransferLimit transferLimit);
    
    /**
     * Delete transfer limit by privilege.
     */
    void deleteByPrivilege(PrivilegeLevel privilege);
}