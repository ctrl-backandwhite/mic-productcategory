package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.usecase.CategorySyncUseCase;
import com.backandwhite.domain.model.CategorySyncResult;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.infrastructure.client.cj.dto.CjCategoryFirstLevelDto;
import com.backandwhite.infrastructure.client.cj.dto.CjCategorySecondLevelDto;
import com.backandwhite.infrastructure.client.cj.dto.CjCategoryThirdLevelDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategorySyncUseCaseImpl implements CategorySyncUseCase {

    private static final String LOCALE_EN = "en";

    private final DropshippingPort cjClient;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategorySyncResult syncFromCjDropshipping() {
        log.info("Starting CJ Dropshipping category sync...");

        List<CjCategoryFirstLevelDto> cjCategories = cjClient.getCategories();
        SyncCounters counters = new SyncCounters();

        for (CjCategoryFirstLevelDto firstLevel : cjCategories) {
            syncFirstLevel(firstLevel, counters);
        }

        log.info("CJ Dropshipping category sync completed: created={}, updated={}, total={}", counters.created,
                counters.updated, counters.created + counters.updated);

        return CategorySyncResult.builder().created(counters.created).updated(counters.updated)
                .total(counters.created + counters.updated).build();
    }

    private void syncFirstLevel(CjCategoryFirstLevelDto firstLevel, SyncCounters counters) {
        String firstName = firstLevel.getCategoryFirstName();
        if (firstName == null || firstName.isBlank()) {
            return;
        }

        String firstLevelId = upsertAndCount(
                null, null, 1, firstName, () -> categoryRepository
                        .findCategoryIdByNameAndLocaleAndLevelAndParent(firstName, LOCALE_EN, 1, null).isPresent(),
                counters);

        if (firstLevel.getCategoryFirstList() == null) {
            return;
        }
        for (CjCategorySecondLevelDto secondLevel : firstLevel.getCategoryFirstList()) {
            syncSecondLevel(secondLevel, firstLevelId, counters);
        }
    }

    private void syncSecondLevel(CjCategorySecondLevelDto secondLevel, String firstLevelId, SyncCounters counters) {
        String secondName = secondLevel.getCategorySecondName();
        if (secondName == null || secondName.isBlank()) {
            return;
        }

        String secondLevelId = upsertAndCount(null, firstLevelId, 2, secondName, () -> categoryRepository
                .findCategoryIdByNameAndLocaleAndLevelAndParent(secondName, LOCALE_EN, 2, firstLevelId).isPresent(),
                counters);

        if (secondLevel.getCategorySecondList() == null) {
            return;
        }
        for (CjCategoryThirdLevelDto thirdLevel : secondLevel.getCategorySecondList()) {
            syncThirdLevel(thirdLevel, secondLevelId, counters);
        }
    }

    private void syncThirdLevel(CjCategoryThirdLevelDto thirdLevel, String secondLevelId, SyncCounters counters) {
        String thirdName = thirdLevel.getCategoryName();
        String thirdId = thirdLevel.getCategoryId();
        if (thirdName == null || thirdName.isBlank() || thirdId == null) {
            return;
        }
        upsertAndCount(thirdId, secondLevelId, 3, thirdName,
                () -> categoryRepository.findCategoryIdById(thirdId).isPresent(), counters);
    }

    private String upsertAndCount(String id, String parentId, int level, String name,
            java.util.function.BooleanSupplier existsCheck, SyncCounters counters) {
        boolean exists = existsCheck.getAsBoolean();
        String resultId = categoryRepository.upsertCategory(id, parentId, level, name, LOCALE_EN);
        if (exists) {
            counters.updated++;
        } else {
            counters.created++;
        }
        return resultId;
    }

    private static final class SyncCounters {
        int created;
        int updated;
    }
}
