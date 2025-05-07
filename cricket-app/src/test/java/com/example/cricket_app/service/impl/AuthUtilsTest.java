package com.example.cricket_app.service.impl;

import com.example.cricket_app.security.AuthUtils;
import com.example.cricket_app.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Constructor;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

class AuthUtilsTest {


    @Test
    void privateConstructor_shouldThrowUnsupportedOperationException() throws Exception {
        Constructor<AuthUtils> constructor = AuthUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Exception exception = assertThrows(Exception.class, constructor::newInstance);
        Throwable cause = exception.getCause(); // unwrap the real exception

        assertNotNull(cause);
        assertTrue(cause instanceof UnsupportedOperationException);
        assertEquals("Utility class", cause.getMessage());
    }

    @Test
    void testGetLoggedInUserId_returnsUserId() {
        CustomUserDetails mockUserDetails = Mockito.mock(CustomUserDetails.class);
        Mockito.when(mockUserDetails.getId()).thenReturn(123L);

        Authentication mockAuthentication = Mockito.mock(Authentication.class);
        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(securityContext);

        Long userId = AuthUtils.getLoggedInUserId();
        assertEquals(123L, userId);
    }


}
