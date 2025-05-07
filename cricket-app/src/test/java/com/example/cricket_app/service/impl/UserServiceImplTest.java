package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.request.CreateWalletRequest;
import com.example.cricket_app.dto.request.LoginRequest;
import com.example.cricket_app.dto.request.SignUpRequest;
import com.example.cricket_app.dto.response.*;
import com.example.cricket_app.entity.*;
import com.example.cricket_app.enums.Team;
import com.example.cricket_app.enums.TransactionType;
import com.example.cricket_app.enums.UserRole;
import com.example.cricket_app.exception.DuplicateEmailException;
import com.example.cricket_app.exception.UserNotFoundException;
import com.example.cricket_app.mapper.BetMapper;
import com.example.cricket_app.mapper.SignUpMapper;
import com.example.cricket_app.mapper.UserMapper;
import com.example.cricket_app.mapper.WalletTransactionMapper;
import com.example.cricket_app.repository.BetRepository;
import com.example.cricket_app.repository.UserRepository;
import com.example.cricket_app.repository.WalletTransactionRepository;
import com.example.cricket_app.security.CustomUserDetails;
import com.example.cricket_app.security.JwtUtils;
import com.example.cricket_app.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SignUpMapper signUpMapper;
    @Mock
    private WalletService walletService;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @Mock
    private BetRepository betRepository;
    @Mock
    private WalletTransactionMapper walletTransactionMapper;
    @Mock
    private BetMapper betMapper;

    @Test
    void authenticateUser() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password");


        CustomUserDetails customUserDetails = new CustomUserDetails();
        customUserDetails.setEmail("user@example.com");
        customUserDetails.setRole(UserRole.valueOf("PLAYER"));
        customUserDetails.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);


        String mockJwtToken = "mock.jwt.token";
        when(jwtUtils.generateToken(anyString(), anyString(), anyLong())).thenReturn(mockJwtToken);


        JwtResponse response = userService.authenticateUser(loginRequest);

        assertNotNull(response);
        assertEquals(mockJwtToken, response.getToken());
        assertEquals("PLAYER", response.getRole());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateToken("user@example.com", "PLAYER", 1L);
    }

    @Test
    void registerUser() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("user@example.com");
        request.setFullName("Test User");
        request.setPassword("pass");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashedPass");

        Users savedUser = new Users();
        savedUser.setId(1L);
        savedUser.setEmail("user@example.com");
        savedUser.setFullName("Test User");
        savedUser.setPasswordHash("hashedPass");
        savedUser.setRole(UserRole.PLAYER);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());

        SignUpResponse mockResponse = new SignUpResponse();
        mockResponse.setId(1L);
        mockResponse.setEmail("user@example.com");
        mockResponse.setFullName("Test User");
        mockResponse.setRole("PLAYER");

        when(signUpMapper.toResponseDto(any(Users.class))).thenReturn(mockResponse);

        SignUpResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("user@example.com", response.getEmail());
        verify(userRepository).save(any(Users.class));
        verify(walletService).initializeWallet(any(CreateWalletRequest.class));
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("user@example.com");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);//here we are just mocking the behaviour by->email exists-then throwing exception.

        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(request));
    }

    @Test
    void registerAdmin() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("admin@example.com");
        request.setFullName("Test Admin");
        request.setPassword("pass");

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashedPass");

        Users savedUser = new Users();
        savedUser.setId(1L);
        savedUser.setEmail("admin@example.com");
        savedUser.setFullName("Test Admin");
        savedUser.setPasswordHash("hashedPass");
        savedUser.setRole(UserRole.ADMIN);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());

        SignUpResponse mockResponse = new SignUpResponse();
        mockResponse.setId(1L);
        mockResponse.setEmail("admin@example.com");
        mockResponse.setFullName("Test Admin");
        mockResponse.setRole("ADMIN");

        when(signUpMapper.toResponseDto(any(Users.class))).thenReturn(mockResponse);

        SignUpResponse response = userService.registerAdmin(request);

        assertNotNull(response);
        assertEquals("admin@example.com", response.getEmail());
        verify(userRepository).save(any(Users.class));
        verify(walletService).initializeWallet(any(CreateWalletRequest.class));
    }

    @Test
    void showUsers() {
        int page = 1;
        int size = 2;
        String sortBy = "email";
        String direction = "asc";

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "email"));

        Users user1 = new Users();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setFullName("Test One");
        user1.setRole(UserRole.PLAYER);

        Users user2 = new Users();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFullName("Test Two");
        user2.setRole(UserRole.PLAYER);

        List<Users> users = List.of(user1, user2);
        Page<Users> mockPage = new PageImpl<>(users, pageable, 2);

        UserResponse userResponse1 = new UserResponse();
        userResponse1.setId(1L);
        userResponse1.setEmail("user1@example.com");
        userResponse1.setFullName("Test One");
        userResponse1.setRole(UserRole.PLAYER);
        userResponse1.setBalance(100.0);

        UserResponse userResponse2 = new UserResponse();
        userResponse2.setId(2L);
        userResponse2.setEmail("user2@example.com");
        userResponse2.setFullName("Test Two");
        userResponse2.setRole(UserRole.PLAYER);
        userResponse2.setBalance(200.0);

        when(userRepository.findByRole(UserRole.PLAYER, pageable)).thenReturn(mockPage);
        when(userMapper.toResponseDto(user1)).thenReturn(userResponse1);
        when(userMapper.toResponseDto(user2)).thenReturn(userResponse2);

        PagedUserResponse response = userService.showUsers(page, size, sortBy, direction);

        assertEquals(1, response.getCurrentPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getTotalUsers());

        List<UserResponse> userResponses = response.getUsers();
        assertEquals(2, userResponses.size());

        assertEquals("user1@example.com", userResponses.get(0).getEmail());
        assertEquals(100.0, userResponses.get(0).getBalance());

        assertEquals("user2@example.com", userResponses.get(1).getEmail());
        assertEquals(200.0, userResponses.get(1).getBalance());

        verify(userRepository).findByRole(UserRole.PLAYER, pageable);
        verify(userMapper, times(2)).toResponseDto(any(Users.class));
    }

    @Test
    void testGetUserById_userNotFound_throwsException() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId, pageable));
    }

    @Test
    void testGetUserById_validUser_returnsCompleteUserResponse() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Users user = new Users();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFullName("John Doe");
        user.setRole(UserRole.PLAYER);

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("1500.00"));
        wallet.setUser(user);
        user.setWallet(wallet);

        WalletTransaction transaction1 = new WalletTransaction();
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setTransactionType(TransactionType.ADMIN_CREDIT);
        transaction1.setDescription("Deposit transaction");
        transaction1.setCreatedAt(LocalDateTime.now().minusDays(1));
        transaction1.setWallet(wallet);

        WalletTransaction transaction2 = new WalletTransaction();
        transaction2.setAmount(new BigDecimal("50.00"));
        transaction2.setTransactionType(TransactionType.BET_PLACED);
        transaction2.setDescription("Withdrawal transaction");
        transaction2.setCreatedAt(LocalDateTime.now());
        transaction2.setWallet(wallet);


        Page<WalletTransaction> walletTransactions = new PageImpl<>(List.of(transaction1, transaction2));


        Bet bet = new Bet();
        bet.setId(1L);
        bet.setAmount(new BigDecimal("100.00"));
        bet.setTeamChosen(Team.AUSTRALIA);
        bet.setMatch(new Match());
        bet.setUser(user);

        List<Bet> bets = List.of(bet);


        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletTransactionRepository.findByWallet_User_IdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(walletTransactions);
        when(betRepository.findByUser_IdOrderByIdDesc(userId)).thenReturn(bets);
        when(walletTransactionMapper.toResponseDto(transaction1)).thenReturn(new WalletTransactionResponse());
        when(walletTransactionMapper.toResponseDto(transaction2)).thenReturn(new WalletTransactionResponse());
        when(betMapper.toResponse(bet)).thenReturn(new BetResponse());


        CompleteUserResponse response = userService.getUserById(userId, pageable);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John Doe", response.getFullName());
        assertEquals(UserRole.PLAYER, response.getRole());
        assertEquals(new BigDecimal("1500.00"), response.getBalance());
        assertEquals(2, response.getPagedWalletTransactionResponse().getWalletTransactionResponses().size());
        assertEquals(1, response.getBets().size());
    }

    @Test
    void testGetUserById_withEmptyTransactions_returnsEmptyTransactionList() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Users user = new Users();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFullName("John Doe");
        user.setRole(UserRole.PLAYER);
        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("1500.00"));
        user.setWallet(wallet);

        Page<WalletTransaction> walletTransactions = Page.empty();
        List<Bet> bets = List.of();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletTransactionRepository.findByWallet_User_IdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(walletTransactions);
        when(betRepository.findByUser_IdOrderByIdDesc(userId)).thenReturn(bets);

        CompleteUserResponse response = userService.getUserById(userId, pageable);

        assertNotNull(response);
        assertEquals(0, response.getPagedWalletTransactionResponse().getWalletTransactionResponses().size());
        assertEquals(0, response.getBets().size());
    }
}