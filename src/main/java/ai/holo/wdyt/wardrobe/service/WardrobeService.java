package ai.holo.wdyt.wardrobe.service;

import ai.holo.wdyt.common.exception.NotFoundException;
import ai.holo.wdyt.user.model.entity.User;
import ai.holo.wdyt.user.service.UserService;
import ai.holo.wdyt.wardrobe.model.dto.*;
import ai.holo.wdyt.wardrobe.model.entity.Category;
import ai.holo.wdyt.wardrobe.model.entity.ReportWardrobe;
import ai.holo.wdyt.wardrobe.model.entity.Wardrobe;
import ai.holo.wdyt.wardrobe.model.entity.WardrobeItem;
import ai.holo.wdyt.wardrobe.model.entity.Tags;
import ai.holo.wdyt.wardrobe.repository.ReportWardrobeRepository;
import ai.holo.wdyt.wardrobe.repository.WardrobeItemRepository;
import ai.holo.wdyt.wardrobe.repository.WardrobeRepository;
import ai.holo.wdyt.wardrobe.util.WardrobeItemSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WardrobeService {

    private final WardrobeItemRepository wardrobeItemRepository;
    private final WardrobeRepository wardrobeRepository;
    private final ReportWardrobeRepository reportWardrobeRepository;
    private final UserService userService;

    public WardrobeService(WardrobeItemRepository wardrobeItemRepository, WardrobeRepository wardrobeRepository, ReportWardrobeRepository reportWardrobeRepository, UserService userService) {
        this.wardrobeItemRepository = wardrobeItemRepository;
        this.wardrobeRepository = wardrobeRepository;
        this.reportWardrobeRepository = reportWardrobeRepository;
        this.userService = userService;
    }

    public Page<WardrobeItemDto> filter(WardrobeItemFilterRequest request, Pageable pageable) {
        Wardrobe wardrobe = getUserWardrobe();
        Specification<WardrobeItem> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("wardrobe").get("id"), wardrobe.getId())
        );
        if (request.colors() != null && !request.colors().isEmpty()) {
            spec = spec.and(WardrobeItemSpecifications.hasAnyColor(request.colors()));
        }
        if (request.seasons() != null && !request.seasons().isEmpty()) {
            spec = spec.and(WardrobeItemSpecifications.hasAnySeason(request.seasons()));
        }
        if (request.types() != null && !request.types().isEmpty()) {
            spec = spec.and(WardrobeItemSpecifications.hasAnyType(request.types()));
        }
        if (request.liked()) {
            spec = spec.and(WardrobeItemSpecifications.isLiked());
        }

        Page<WardrobeItem> resultPage = wardrobeItemRepository.findAll(spec, pageable);
        return resultPage.map(WardrobeItemDto::new);
    }

    public WardrobeItemDto createItem(CreateWardrobeItemDto dto) {
        Wardrobe wardrobe = getUserWardrobe();
        Tags tags = new Tags(
                dto.colors() != null ? dto.colors() : List.of(),
                dto.seasons() != null ? dto.seasons() : List.of(),
                dto.types() != null ? dto.types() : List.of(),
                dto.tags() != null ? dto.tags() : List.of()
        );

        WardrobeItem item = new WardrobeItem();
        item.setName(dto.name());
        item.setImagePath(dto.imagePath());
        item.setCategory(dto.category());
        item.setLiked(dto.liked());
        item.setTags(tags);
        item.setWardrobe(wardrobe);

        WardrobeItem savedItem = wardrobeItemRepository.save(item);
        return new WardrobeItemDto(savedItem);
    }

    public Page<WardrobeItemDto> listWardrobeItems(String category, Pageable pageable) {
        Wardrobe wardrobe = getUserWardrobe();
        Page<WardrobeItem> page;
        Category categoryEnum = category != null ? Category.valueOf(category.toUpperCase()) : null;
        if (categoryEnum != null) {
            page = wardrobeItemRepository.findByWardrobeIdAndCategory(wardrobe.getId(), categoryEnum, pageable);
        } else {
            page = wardrobeItemRepository.findByWardrobeId(wardrobe.getId(), pageable);
        }
        return page.map(WardrobeItemDto::new);
    }

    public WardrobeItemDto getItemById(Long id) {
        WardrobeItem item = wardrobeItemRepository.findById(id).orElseThrow(NotFoundException::new);
        return new WardrobeItemDto(item);
    }

    private Wardrobe getUserWardrobe() {
        User user = userService.getUser();
        return wardrobeRepository.findByUserId(user.getId()).orElseThrow(NotFoundException::new);
    }

    public void createWardrobeForUser(Long userId) {
        if (wardrobeRepository.existsByUserId(userId)) {
            return; // Wardrobe already exists for this user
        }
        Wardrobe wardrobe = new Wardrobe();
        wardrobe.setUserId(userId);
        wardrobeRepository.save(wardrobe);
        log.info("Wardrobe created for user with ID: {}", userId);
    }

    public void deleteWardrobeForUser(Long userId) {
        Wardrobe wardrobe = wardrobeRepository.findByUserId(userId).orElseThrow(NotFoundException::new);
        wardrobeItemRepository.deleteAllByWardrobeId(wardrobe.getId());
        wardrobeRepository.delete(wardrobe);
        log.info("Wardrobe deleted for user with ID: {}", userId);
    }

    public WardrobeItemDto likeItem(Long id, boolean like) {
        WardrobeItem item = wardrobeItemRepository.findById(id).orElseThrow(NotFoundException::new);
        item.setLiked(like);
        WardrobeItem updatedItem = wardrobeItemRepository.save(item);
        return new WardrobeItemDto(updatedItem);
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
        return new WardrobeItemDto(updatedItem);
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
}
