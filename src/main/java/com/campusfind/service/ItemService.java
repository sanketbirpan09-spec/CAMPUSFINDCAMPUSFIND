package com.campusfind.service;

import com.campusfind.dto.ItemRequest;
import com.campusfind.dto.ItemResponse;
import com.campusfind.dto.SystemStatsResponse;
import com.campusfind.exception.BadRequestException;
import com.campusfind.exception.ResourceNotFoundException;
import com.campusfind.exception.UnauthorizedException;
import com.campusfind.model.Item;
import com.campusfind.model.User;
import com.campusfind.repository.AdminAuditLogRepository;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AdminAuditLogRepository auditLogRepository;

    public ItemService(ItemRepository itemRepository,
                       UserRepository userRepository,
                       FileStorageService fileStorageService,
                       AdminAuditLogRepository auditLogRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.auditLogRepository = auditLogRepository;
    }

    public ItemResponse createItem(ItemRequest request, MultipartFile imageFile, Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("User must be authenticated to submit a report");
        }

        // Store image if provided
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = fileStorageService.storeFile(imageFile);
        }

        Item item = new Item();
        item.setUserId(userId);
        item.setTitle(request.getTitle().trim());
        item.setType(request.getType().toUpperCase().trim());
        item.setCategory(request.getCategory().trim());
        item.setDescription(request.getDescription().trim());
        item.setItemDate(request.getItemDate());
        item.setLocation(request.getLocation().trim());
        item.setContactInfo(request.getContactInfo().trim());
        item.setImageUrl(imageUrl);
        item.setStatus("OPEN");

        Item saved = itemRepository.save(item);
        Item full = itemRepository.findById(saved.getId()).orElse(saved);
        return ItemResponse.fromItem(full, userId);
    }

    public ItemResponse getItemById(Long id, Long currentUserId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item report not found with id: " + id));
        return ItemResponse.fromItem(item, currentUserId);
    }

    public List<ItemResponse> getRecentItems(int limit, Long currentUserId) {
        return itemRepository.findRecent(limit).stream()
                .map(item -> ItemResponse.fromItem(item, currentUserId))
                .collect(Collectors.toList());
    }

    public List<ItemResponse> searchAndFilter(String query, String type, String category, String location, String status, int offset, int limit, Long currentUserId) {
        return itemRepository.searchAndFilter(query, type, category, location, status, offset, limit).stream()
                .map(item -> ItemResponse.fromItem(item, currentUserId))
                .collect(Collectors.toList());
    }

    public long countSearchAndFilter(String query, String type, String category, String location, String status) {
        return itemRepository.countSearchAndFilter(query, type, category, location, status);
    }

    public List<ItemResponse> getUserReports(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("Authentication required to view your reports");
        }
        return itemRepository.findByUserId(userId).stream()
                .map(item -> ItemResponse.fromItem(item, userId))
                .collect(Collectors.toList());
    }

    public void markAsResolved(Long itemId, Long currentUserId, boolean isAdmin) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item report not found with id: " + itemId));

        // Allow only the owner or an admin to mark as resolved
        if (!item.getUserId().equals(currentUserId) && !isAdmin) {
            throw new UnauthorizedException("You are only permitted to resolve your own reports");
        }

        itemRepository.updateStatus(itemId, "RESOLVED");

        if (isAdmin && !item.getUserId().equals(currentUserId)) {
            auditLogRepository.logAction(currentUserId, "RESOLVE_REPORT", itemId, "Resolved by administrator");
        }
    }

    public void removeInappropriateReport(Long itemId, Long adminId, String reason) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item report not found with id: " + itemId));

        // Update status to REMOVED_BY_ADMIN or delete
        itemRepository.updateStatus(itemId, "REMOVED_BY_ADMIN");

        // Log action in audit log
        auditLogRepository.logAction(adminId, "REMOVE_INAPPROPRIATE", itemId, reason != null ? reason : "Inappropriate content");
    }

    public void deleteReportPermanently(Long itemId, Long adminId, String reason) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item report not found with id: " + itemId));

        itemRepository.deleteById(itemId);
        auditLogRepository.logAction(adminId, "DELETE_PERMANENTLY", itemId, reason != null ? reason : "Deleted by administrator");
    }

    public List<ItemResponse> getAllReportsForAdmin(int limit, Long currentUserId) {
        return itemRepository.findAllForAdmin(limit).stream()
                .map(item -> ItemResponse.fromItem(item, currentUserId))
                .collect(Collectors.toList());
    }

    public SystemStatsResponse getSystemStats() {
        long total = itemRepository.countTotal();
        long lost = itemRepository.countByType("LOST");
        long found = itemRepository.countByType("FOUND");
        long resolved = itemRepository.countByStatus("RESOLVED");
        long users = userRepository.count();

        return new SystemStatsResponse(total, lost, found, resolved, users);
    }
}
