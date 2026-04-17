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

        int created = 0;
        int updated = 0;

        for (CjCategoryFirstLevelDto firstLevel : cjCategories) {
            String firstName = firstLevel.getCategoryFirstName();
            if (firstName == null || firstName.isBlank())
                continue;

            boolean firstExists = categoryRepository
                    .findCategoryIdByNameAndLocaleAndLevelAndParent(firstName, LOCALE_EN, 1, null).isPresent();

            String firstLevelId = categoryRepository.upsertCategory(null, null, 1, firstName, LOCALE_EN);

            if (firstExists) {
                updated++;
            } else {
                created++;
            }

            if (firstLevel.getCategoryFirstList() == null)
                continue;

            for (CjCategorySecondLevelDto secondLevel : firstLevel.getCategoryFirstList()) {
                String secondName = secondLevel.getCategorySecondName();
                if (secondName == null || secondName.isBlank())
                    continue;

                boolean secondExists = categoryRepository
                        .findCategoryIdByNameAndLocaleAndLevelAndParent(secondName, LOCALE_EN, 2, firstLevelId)
                        .isPresent();

                String secondLevelId = categoryRepository.upsertCategory(null, firstLevelId, 2, secondName, LOCALE_EN);

                if (secondExists) {
                    updated++;
                } else {
                    created++;
                }

                if (secondLevel.getCategorySecondList() == null)
                    continue;

                for (CjCategoryThirdLevelDto thirdLevel : secondLevel.getCategorySecondList()) {
                    String thirdName = thirdLevel.getCategoryName();
                    String thirdId = thirdLevel.getCategoryId();
                    if (thirdName == null || thirdName.isBlank() || thirdId == null)
                        continue;

                    boolean thirdExists = categoryRepository.findCategoryIdById(thirdId).isPresent();

                    categoryRepository.upsertCategory(thirdId, secondLevelId, 3, thirdName, LOCALE_EN);

                    if (thirdExists) {
                        updated++;
                    } else {
                        created++;
                    }
                }
            }
        }

        log.info("CJ Dropshipping category sync completed: created={}, updated={}, total={}", created, updated,
                created + updated);

        return CategorySyncResult.builder().created(created).updated(updated).total(created + updated).build();
    }
}
