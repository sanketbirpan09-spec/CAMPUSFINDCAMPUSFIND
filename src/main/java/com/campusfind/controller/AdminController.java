package com.campusfind.controller;

import com.campusfind.dto.ApiResponse;
import com.campusfind.dto.ItemResponse;
import com.campusfind.dto.SystemStatsResponse;
import com.campusfind.exception.UnauthorizedException;
import com.campusfind.model.AdminAuditLog;
import com.campusfind.model.User;
import com.campusfind.repository.AdminAuditLogRepository;
import com.campusfind.service.ItemService;
import com.campusfind.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ItemService itemService;
    private final UserService userService;
    private final AdminAuditLogRepository auditLogRepository;

    public AdminController(ItemService itemService,
                           UserService userService,
                           AdminAuditLogRepository auditLogRepository) {
        this.itemService = itemService;
        this.userService = userService;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAllItems(@RequestParam(defaultValue = "100") int limit) {
        Long adminId = getAdminUserId();
        List<ItemResponse> items = itemService.getAllReportsForAdmin(limit, adminId);
        return ResponseEntity.ok(ApiResponse.success("Admin items retrieved", items));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload) {

        Long adminId = getAdminUserId();
        String reason = payload != null ? payload.get("reason") : "Deleted by administrator";

        itemService.deleteReportPermanently(id, adminId, reason);
        return ResponseEntity.ok(ApiResponse.success("Report permanently deleted"));
    }

    @PutMapping("/items/{id}/remove")
    public ResponseEntity<ApiResponse<Void>> removeInappropriate(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload) {

        Long adminId = getAdminUserId();
        String reason = payload != null ? payload.get("reason") : "Flagged as inappropriate";

        itemService.removeInappropriateReport(id, adminId, reason);
        return ResponseEntity.ok(ApiResponse.success("Report flagged and removed from public view"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SystemStatsResponse>> getAdminStats() {
        SystemStatsResponse stats = itemService.getSystemStats();
        return ResponseEntity.ok(ApiResponse.success("Admin stats", stats));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AdminAuditLog>>> getAuditLogs(@RequestParam(defaultValue = "50") int limit) {
        List<AdminAuditLog> logs = auditLogRepository.findRecent(limit);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", logs));
    }

    private Long getAdminUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Admin authentication required");
        }
        return userService.findByEmail(auth.getName()).map(User::getId)
                .orElseThrow(() -> new UnauthorizedException("Admin user not found"));
    }
}
