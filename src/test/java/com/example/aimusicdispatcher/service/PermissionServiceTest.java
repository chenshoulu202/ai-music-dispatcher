package com.example.aimusicdispatcher.service;

import com.example.aimusicdispatcher.config.PermissionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PermissionServiceTest {

    private PermissionProperties permissionProperties;
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionProperties = Mockito.mock(PermissionProperties.class);
        // Default: enabled = true
        when(permissionProperties.isEnabled()).thenReturn(true);
        permissionService = new PermissionService(permissionProperties);
    }

    @Test
    void testGrantAndAuthorization() {
        String userId = "user123";
        assertFalse(permissionService.isAuthorized(userId), "User should not be authorized initially");

        permissionService.grant(userId, 5);
        assertTrue(permissionService.isAuthorized(userId), "User should be authorized after grant");
    }

    @Test
    void testRevoke() {
        String userId = "user123";
        permissionService.grant(userId, 5);
        assertTrue(permissionService.isAuthorized(userId));

        permissionService.revoke(userId);
        assertFalse(permissionService.isAuthorized(userId), "User should not be authorized after revoke");
    }

    @Test
    void testDisabledPermissionSystem() {
        // Disable permission system
        when(permissionProperties.isEnabled()).thenReturn(false);

        String userId = "user123";
        // Even without grant, should be authorized
        assertTrue(permissionService.isAuthorized(userId), "User should be authorized when permission system is disabled");

        // Grant should be ignored (log debug) but no error
        permissionService.grant(userId, 5);
    }

    @Test
    void testInvalidUserId() {
        assertFalse(permissionService.isAuthorized(null));
        assertFalse(permissionService.isAuthorized(""));
        
        permissionService.grant(null, 5); // Should not throw exception
        permissionService.grant("", 5); // Should not throw exception
    }
    
    @Test
    void testGetRemainingTime() {
        String userId = "user123";
        long remaining = permissionService.getRemainingTime(userId);
        assertEquals(-1, remaining);
        
        permissionService.grant(userId, 10);
        remaining = permissionService.getRemainingTime(userId);
        assertTrue(remaining > 0, "Remaining time should be positive");
    }

    @Test
    void testClearAll() {
        String userId1 = "user1";
        String userId2 = "user2";
        
        permissionService.grant(userId1, 5);
        permissionService.grant(userId2, 5);
        
        assertTrue(permissionService.isAuthorized(userId1));
        assertTrue(permissionService.isAuthorized(userId2));
        
        permissionService.clearAll();
        
        assertFalse(permissionService.isAuthorized(userId1));
        assertFalse(permissionService.isAuthorized(userId2));
    }

    @Test
    void testHasRequestedAndRecordRequest() {
        String userId = "user123";
        String songName = "Song A";
        
        assertFalse(permissionService.hasRequested(userId, songName));
        
        permissionService.grant(userId, 5);
        assertFalse(permissionService.hasRequested(userId, songName));
        
        permissionService.recordRequest(userId, songName);
        assertTrue(permissionService.hasRequested(userId, songName));
        
        // Test another song
        String songName2 = "Song B";
        assertFalse(permissionService.hasRequested(userId, songName2));
        permissionService.recordRequest(userId, songName2);
        assertTrue(permissionService.hasRequested(userId, songName2));
    }
    
    @Test
    void testSessionResetClearsHistory() {
        // This test is tricky because Caffeine relies on time. 
        // We can simulate a new session by not extending but letting it expire or just checking logic.
        // Actually, our logic says if expired, create new.
        
        // Since we can't easily mock time with System.currentTimeMillis() in this service without refactoring clock,
        // we can test that granting a new session (if logic allowed forcing new) would clear it.
        // But the current logic extends if valid.
        
        // Let's at least verify that the requested songs set is bound to the user.
        String userId = "user123";
        permissionService.grant(userId, 5);
        permissionService.recordRequest(userId, "Song A");
        assertTrue(permissionService.hasRequested(userId, "Song A"));
        
        permissionService.revoke(userId);
        assertFalse(permissionService.hasRequested(userId, "Song A"));
        
        permissionService.grant(userId, 5);
        assertFalse(permissionService.hasRequested(userId, "Song A"), "New session should have empty history");
    }
}
