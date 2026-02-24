package com.example.aimusicdispatcher.dispatcher;

import com.example.aimusicdispatcher.config.PermissionProperties;
import com.example.aimusicdispatcher.model.barrage.BarrageRequest;
import com.example.aimusicdispatcher.model.dy.CastGift;
import com.example.aimusicdispatcher.model.dy.CastMethod;
import com.example.aimusicdispatcher.model.dy.CastUser;
import com.example.aimusicdispatcher.model.dy.DyMessage;
import com.example.aimusicdispatcher.service.BarrageService;
import com.example.aimusicdispatcher.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageDispatcherTest {

    @Mock
    private PermissionService permissionService;

    @Mock
    private BarrageService barrageService;

    @Mock
    private BarrageFilterService barrageFilterService;

    @Mock
    private PermissionProperties permissionProperties;

    private MessageDispatcher messageDispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messageDispatcher = new MessageDispatcher(permissionService, barrageService, barrageFilterService, permissionProperties);
    }

    @Test
    void testDispatch_LikeMessage_GrantsPermission() {
        // Setup mock properties
        when(permissionProperties.isEnabled()).thenReturn(true);
        when(permissionProperties.getLikeMinutes()).thenReturn(5);

        // Create like message
        DyMessage message = new DyMessage();
        message.setMethod(CastMethod.LIKE);
        CastUser user = new CastUser();
        user.setId("user123");
        user.setName("TestUser");
        message.setUser(user);

        // Execute dispatch
        messageDispatcher.dispatch(message);

        // Verify grant was called with correct minutes
        verify(permissionService).grant(eq("user123"), eq(5));
    }

    @Test
    void testDispatch_GiftMessage_GrantsPermission() {
        // Setup mock properties
        when(permissionProperties.isEnabled()).thenReturn(true);
        when(permissionProperties.getGiftMinutes()).thenReturn(20);

        // Create gift message
        DyMessage message = new DyMessage();
        message.setMethod(CastMethod.GIFT);
        CastUser user = new CastUser();
        user.setId("user123");
        user.setName("TestUser");
        message.setUser(user);
        
        CastGift gift = new CastGift();
        gift.setName("TestGift");
        gift.setCount("1");
        message.setGift(gift);

        // Execute dispatch
        messageDispatcher.dispatch(message);

        // Verify grant was called with correct minutes
        verify(permissionService).grant(eq("user123"), eq(20));
    }

    @Test
    void testDispatch_ChatMessage_AuthorizedUser_ProcessesRequest() {
        // Setup mock properties
        when(permissionProperties.isEnabled()).thenReturn(true);
        when(permissionService.isAuthorized("user123")).thenReturn(true);
        when(permissionService.hasRequested("user123", "稻香")).thenReturn(false);
        when(barrageFilterService.extractSongName(any(BarrageRequest.class))).thenReturn(Optional.of("稻香"));

        // Create chat message
        DyMessage message = new DyMessage();
        message.setMethod(CastMethod.CHAT);
        message.setContent("点歌 稻香");
        CastUser user = new CastUser();
        user.setId("user123");
        user.setName("TestUser");
        message.setUser(user);

        // Execute dispatch
        messageDispatcher.dispatch(message);

        // Verify processBarrage was called
        verify(barrageService).processBarrage(any(BarrageRequest.class));
        verify(permissionService).isAuthorized("user123");
        verify(permissionService).hasRequested("user123", "稻香");
        verify(permissionService).recordRequest("user123", "稻香");
    }

    @Test
    void testDispatch_ChatMessage_DuplicateRequest_DeniesRequest() {
        // Setup mock properties
        when(permissionProperties.isEnabled()).thenReturn(true);
        when(permissionService.isAuthorized("user123")).thenReturn(true);
        when(permissionService.hasRequested("user123", "稻香")).thenReturn(true); // Already requested
        when(barrageFilterService.extractSongName(any(BarrageRequest.class))).thenReturn(Optional.of("稻香"));

        // Create chat message
        DyMessage message = new DyMessage();
        message.setMethod(CastMethod.CHAT);
        message.setContent("点歌 稻香");
        CastUser user = new CastUser();
        user.setId("user123");
        user.setName("TestUser");
        message.setUser(user);

        // Execute dispatch
        messageDispatcher.dispatch(message);

        // Verify processBarrage was NOT called
        verify(barrageService, never()).processBarrage(any(BarrageRequest.class));
        verify(permissionService).isAuthorized("user123");
        verify(permissionService).hasRequested("user123", "稻香");
        verify(permissionService, never()).recordRequest(any(), any());
    }

    @Test
    void testDispatch_ChatMessage_UnauthorizedUser_DeniesRequest() {
        // Setup mock properties
        when(permissionProperties.isEnabled()).thenReturn(true);
        when(permissionService.isAuthorized("user123")).thenReturn(false); // Unauthorized
        when(barrageFilterService.extractSongName(any(BarrageRequest.class))).thenReturn(Optional.of("回马枪"));

        // Create chat message
        DyMessage message = new DyMessage();
        message.setMethod(CastMethod.CHAT);
        message.setContent("点歌 回马枪");
        CastUser user = new CastUser();
        user.setId("user123");
        user.setName("TestUser");
        message.setUser(user);

        // Execute dispatch
        messageDispatcher.dispatch(message);

        // Verify processBarrage was NOT called
        verify(barrageService, never()).processBarrage(any(BarrageRequest.class));
        verify(permissionService).isAuthorized("user123");
    }

    @Test
    void testDispatch_ChatMessage_PermissionDisabled_AllowsRequest() {
        // Setup mock properties
        when(permissionProperties.isEnabled()).thenReturn(false); // Disabled
        when(barrageFilterService.extractSongName(any(BarrageRequest.class))).thenReturn(Optional.of("奢香夫人"));

        // Create chat message
        DyMessage message = new DyMessage();
        message.setMethod(CastMethod.CHAT);
        message.setContent("点歌 奢香夫人");
        CastUser user = new CastUser();
        user.setId("user123");
        user.setName("TestUser");
        message.setUser(user);

        // Execute dispatch
        messageDispatcher.dispatch(message);

        // Verify processBarrage was called without checking authorization
        verify(barrageService).processBarrage(any(BarrageRequest.class));
        verify(permissionService, never()).isAuthorized(any());
    }

    @Test
    void testDispatch_NonSongRequest_Ignored() {
        // Setup mock properties
        when(barrageFilterService.extractSongName(any(BarrageRequest.class))).thenReturn(Optional.empty());

        // Create chat message (not a song request)
        DyMessage message = new DyMessage();
        message.setMethod(CastMethod.CHAT);
        message.setContent("Hello World");
        CastUser user = new CastUser();
        user.setId("user123");
        user.setName("TestUser");
        message.setUser(user);

        // Execute dispatch
        messageDispatcher.dispatch(message);

        // Verify processBarrage was NOT called
        verify(barrageService, never()).processBarrage(any(BarrageRequest.class));
    }
    
    @Test
    void testDispatch_NullUser_Ignored() {
        DyMessage message = new DyMessage();
        message.setMethod(CastMethod.CHAT);
        message.setContent("点歌 SongName");
        message.setUser(null); // System message?

        messageDispatcher.dispatch(message);
        
        verify(permissionService, never()).grant(any(), anyInt());
        verify(barrageService, never()).processBarrage(any());
    }
}
