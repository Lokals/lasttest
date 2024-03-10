package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.model.importfile.ImportLock;
import com.mastertest.lasttest.repository.ImportLockRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@RequiredArgsConstructor
@Service
public class ImportLockService {


    private static final Logger logger = LoggerFactory.getLogger(ImportLockService.class);

    private final ImportLockRepository importLockRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean tryLockImportProcess() {
        ImportLock lock = importLockRepository.findById(1).orElseThrow();
        if (!lock.getIsLocked()) {
            lock.setIsLocked(true);
            lock.setLockedAt(new Timestamp(System.currentTimeMillis()));
            importLockRepository.save(lock);
            return true;
        }
        return false;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void unlockImportProcess() {
        ImportLock lock = importLockRepository.findById(1).orElseThrow();
        lock.setIsLocked(false);
        lock.setLockedAt(null);
        importLockRepository.save(lock);
    }

}
