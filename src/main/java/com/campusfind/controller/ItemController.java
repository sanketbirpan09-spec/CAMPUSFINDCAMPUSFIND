package com.campusfind.controller;

import com.campusfind.dto.ApiResponse;
import com.campusfind.dto.ItemRequest;
import com.campusfind.dto.ItemResponse;
import com.campusfind.dto.SystemStatsResponse;
import com.campusfind.exception.UnauthorizedException;
import com.campusfind.model.User;
import com.campusfind.service.ItemService;
import com.campusfind.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;

    public ItemController(ItemService itemService, UserService userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getRecentItems(@RequestParam(defaultValue = "6") int limit) {
        Long currentUserId = getCurrentUserId();
        List<ItemResponse> items = itemService.getRecentItems(Math.min(limit, 20), currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Recent items retrieved", items));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchItems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {

        int offset = Math.max(0, (page - 1) * size);
        Long currentUserId = getCurrentUserId();

        List<ItemResponse> items = itemService.searchAndFilter(query, type, category, location, status, offset, size, currentUserId);
        long totalCount = itemService.countSearchAndFilter(query, type, category, location, status);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("totalCount", totalCount);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);

        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        ItemResponse item = itemService.getItemById(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Item details retrieved", item));
    }

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<ItemResponse>> createReport(
            @Valid @ModelAttribute ItemRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("You must be logged in to report an item");
        }

        ItemResponse response = itemService.createItem(request, image, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Report submitted successfully!", response));
    }

    @GetMapping("/my-reports")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getMyReports() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("You must be logged in to view your reports");
        }

        List<ItemResponse> reports = itemService.getUserReports(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Your reports retrieved", reports));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<Void>> markResolved(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("You must be logged in to mark an item as resolved");
        }

        boolean isAdmin = isCurrentUserAdmin();
        itemService.markAsResolved(id, currentUserId, isAdmin);

        return ResponseEntity.ok(ApiResponse.success("Item marked as resolved!"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SystemStatsResponse>> getSystemStats() {
        SystemStatsResponse stats = itemService.getSystemStats();
        return ResponseEntity.ok(ApiResponse.success("System statistics", stats));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String email = auth.getName();
        return userService.findByEmail(email).map(User::getId).orElse(null);
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            }
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows your Vercel frontend to make requests
public class ItemController {
    // ...
}
        }
