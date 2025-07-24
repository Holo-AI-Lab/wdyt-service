package ai.holo.wdyt.wardrobe.service;

import ai.holo.wdyt.common.S3Service;
import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import ai.holo.wdyt.wardrobe.model.dto.*;
import ai.holo.wdyt.wardrobe.model.entity.*;
import ai.holo.wdyt.wardrobe.repository.DraftWardrobeItemRepository;
import ai.holo.wdyt.wardrobe.repository.ReportWardrobeRepository;
import ai.holo.wdyt.wardrobe.repository.WardrobeItemRepository;
import ai.holo.wdyt.wardrobe.repository.WardrobeRepository;
import ai.holo.wdyt.wardrobe.util.WardrobeItemSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class WardrobeService {

    private final WardrobeItemRepository wardrobeItemRepository;
    private final WardrobeRepository wardrobeRepository;
    private final ReportWardrobeRepository reportWardrobeRepository;
    private final UserService userService;
    private final DraftWardrobeItemRepository draftWardrobeItemRepository;
    private final S3Service s3Service;

    public WardrobeService(WardrobeItemRepository wardrobeItemRepository, WardrobeRepository wardrobeRepository, ReportWardrobeRepository reportWardrobeRepository, UserService userService, DraftWardrobeItemRepository draftWardrobeItemRepository, S3Service s3Service) {
        this.wardrobeItemRepository = wardrobeItemRepository;
        this.wardrobeRepository = wardrobeRepository;
        this.reportWardrobeRepository = reportWardrobeRepository;
        this.userService = userService;
        this.draftWardrobeItemRepository = draftWardrobeItemRepository;
        this.s3Service = s3Service;
    }

    @Transactional(readOnly = true)
    public FilterDto filter() {
        Optional<Wardrobe> wardrobe = getUserWardrobe();
        if (wardrobe.isEmpty()) {
            return new FilterDto(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        List<Color> colors = getDistinctColors(wardrobe.get());
        List<String> seasons = wardrobeItemRepository.findDistinctSeasons(wardrobe.get().getId());
        List<String> types = wardrobeItemRepository.findDistinctTypes(wardrobe.get().getId());
        List<WardrobeItemCategory> categories = wardrobeItemRepository.findDistinctCategories(wardrobe.get().getId());
        return new FilterDto(categories ,colors, seasons, types);
    }

    private List<Color> getDistinctColors(Wardrobe wardrobe) {
        List<Object[]> results = wardrobeItemRepository.findDistinctColors(wardrobe.getId());
        List<Color> colors = new ArrayList<>();
        for (Object[] row : results) {
            String name = (String) row[0];
            String code = (String) row[1];
            colors.add(new Color(name, code));
        }
        return colors;
    }

    public List<WardrobeItemDto> createItem(CreateWardrobeItemsRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required.");
        }
        Wardrobe wardrobe = getUserWardrobe().orElseGet(this::createWardrobeForUser);

        List<WardrobeItem> items = request.items().stream()
                .map(dto -> {
                    Tags tags = new Tags(
                            dto.colors() != null ? dto.colors() : List.of(),
                            dto.seasons() != null ? dto.seasons() : List.of(),
                            dto.types() != null ? dto.types() : List.of(),
                            dto.tags() != null ? dto.tags() : List.of()
                    );
                    WardrobeItem item = new WardrobeItem();
                    item.setName(dto.name());
                    item.setImagePath(getImagePath(dto));
                    item.setCategory(dto.category());
                    item.setTags(tags);
                    item.setWardrobe(wardrobe);
                    return item;
                })
                .toList();

        List<WardrobeItem> savedItems = wardrobeItemRepository.saveAll(items);
        return savedItems.stream().map(wardrobeItem -> new WardrobeItemDto(wardrobeItem, getImageUrl(wardrobeItem.getImagePath()))).toList();
    }

    private String getImagePath(CreateWardrobeItemDto dto) {
        DraftWardrobeItem draftWardrobeItem = draftWardrobeItemRepository.findById(dto.draftItemId()).orElseThrow(NotFoundException::new);
        return draftWardrobeItem.getImagePath();
    }

    public Page<WardrobeItemDto> listWardrobeItems(WardrobeItemCategory category, Boolean liked, List<String> colors, List<String> seasons, List<String> types, Pageable pageable) {
        Optional<Wardrobe> wardrobe = getUserWardrobe();
        if (wardrobe.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        Specification<WardrobeItem> spec = Specification.where(WardrobeItemSpecifications.belongsToWardrobe(wardrobe.get().getId()));
        if (category != null) {
            spec = spec.and(WardrobeItemSpecifications.hasCategory(category));
        }
        if (liked != null) {
            spec = spec.and(WardrobeItemSpecifications.isLiked(liked));
        }
        if (colors != null && !colors.isEmpty()) {
            spec = spec.and(WardrobeItemSpecifications.hasAnyColor(colors));
        }
        if (seasons != null && !seasons.isEmpty()) {
            spec = spec.and(WardrobeItemSpecifications.hasAnySeason(seasons));
        }
        if (types != null && !types.isEmpty()) {
            spec = spec.and(WardrobeItemSpecifications.hasAnyType(types));
        }
        Page<WardrobeItem> page = wardrobeItemRepository.findAll(spec, pageable);
        return page.map(item -> new WardrobeItemDto(item, getImageUrl(item.getImagePath())));
    }

    private String getImageUrl(String imagePath) {
        return s3Service.getFileS3Url(imagePath);
    }

    public WardrobeItemDto getItemById(Long id) {
        WardrobeItem item = wardrobeItemRepository.findById(id).orElseThrow(NotFoundException::new);
        return new WardrobeItemDto(item, getImageUrl(item.getImagePath()));
    }

    private Optional<Wardrobe> getUserWardrobe() {
        User user = userService.getUser();
        return wardrobeRepository.findByUserId(user.getId());
    }

    private Wardrobe createWardrobeForUser() {
        Long userId = userService.getUser().getId();
        Wardrobe wardrobe = new Wardrobe(userId);
        log.info("Wardrobe created for user with ID: {}", userId);
        return wardrobeRepository.save(wardrobe);
    }

    public void deleteWardrobeForUser(Long userId) {
        Optional<Wardrobe> wardrobe = getUserWardrobe();
        if (wardrobe.isEmpty()) {
            return;
        }
        wardrobeItemRepository.deleteAllByWardrobeId(wardrobe.get().getId());
        draftWardrobeItemRepository.deleteByUserId(userId);
        wardrobeRepository.delete(wardrobe.get());
        log.info("Wardrobe deleted for user with ID: {}", userId);
    }

    public WardrobeItemDto likeItem(Long id, boolean like) {
        WardrobeItem item = wardrobeItemRepository.findById(id).orElseThrow(NotFoundException::new);
        item.setLiked(like);
        WardrobeItem updatedItem = wardrobeItemRepository.save(item);
        return new WardrobeItemDto(updatedItem, getImageUrl(updatedItem.getImagePath()));
    }

    public WardrobeItemDto updateItem(Long id, UpdateWardrobeItemDto dto) {
        WardrobeItem item = wardrobeItemRepository.findById(id).orElseThrow(NotFoundException::new);

        item.setName(dto.name());
        item.setCategory(dto.category());
        item.setLiked(dto.liked());
        item.setTags(new Tags(
                dto.colors() != null ? dto.colors() : List.of(),
                dto.seasons() != null ? dto.seasons() : List.of(),
                dto.types() != null ? dto.types() : List.of(),
                dto.tags() != null ? dto.tags() : List.of()
        ));

        WardrobeItem updatedItem = wardrobeItemRepository.save(item);
        return new WardrobeItemDto(updatedItem, getImageUrl(updatedItem.getImagePath()));
    }

    public void deleteItem(Long id) {
        WardrobeItem item = wardrobeItemRepository.findById(id).orElseThrow(NotFoundException::new);
        wardrobeItemRepository.delete(item);
    }

    public void reportItem(WardrobeReportRequest wardrobeReportRequest) {
        User user = userService.getUser();
        WardrobeItem item = wardrobeItemRepository.findById(wardrobeReportRequest.itemId()).orElseThrow(NotFoundException::new);
        ReportWardrobe reportWardrobe = new ReportWardrobe(user.getId(), item.getId(), wardrobeReportRequest.feedback());
        reportWardrobeRepository.save(reportWardrobe);
        log.info("Report created for item ID: {} by user ID: {}", wardrobeReportRequest.itemId(), user.getId());
    }

    public List<String> getSubcategories(String category, String search) {
        WardrobeItemCategory wardrobeItemCategory = WardrobeItemCategory.fromValue(category);
        return wardrobeItemCategory.filterSubcategories(search);
    }
}
