package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.request.CreateWalletRequest;
import com.example.cricket_app.dto.request.LoginRequest;
import com.example.cricket_app.dto.request.SignUpRequest;
import com.example.cricket_app.dto.response.*;
import com.example.cricket_app.entity.Bet;
import com.example.cricket_app.entity.Users;
import com.example.cricket_app.entity.Wallet;
import com.example.cricket_app.entity.WalletTransaction;
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
import com.example.cricket_app.service.UserService;
import com.example.cricket_app.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final SignUpMapper signUpMapper;
    private final WalletService walletService;
    private final WalletTransactionRepository walletTransactionRepository;
    private final BetRepository betRepository;
    private final WalletTransactionMapper walletTransactionMapper;
    private final BetMapper betMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, AuthenticationManager authenticationManager, JwtUtils jwtUtils, PasswordEncoder passwordEncoder, SignUpMapper signUpMapper, WalletService walletService, WalletTransactionRepository walletTransactionRepository, BetRepository betRepository, WalletTransactionMapper walletTransactionMapper, BetMapper betMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.signUpMapper = signUpMapper;
        this.walletService = walletService;
        this.walletTransactionRepository = walletTransactionRepository;
        this.betRepository = betRepository;
        this.walletTransactionMapper = walletTransactionMapper;
        this.betMapper = betMapper;
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        //getPrincipal means the authenticated user

        String jwt = jwtUtils.generateToken(
                userDetails.getUsername(),
                userDetails.getRole().name(),
                userDetails.getId()
        );

        JwtResponse response = new JwtResponse();
        response.setToken(jwt);
        response.setRole(userDetails.getRole().name());

        return response;
    }

    @Override
    public SignUpResponse registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new DuplicateEmailException("Email is already registered.");
        }

        Users user = new Users();
        user.setEmail(signUpRequest.getEmail());
        user.setFullName(signUpRequest.getFullName());
        user.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(UserRole.PLAYER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        walletService.initializeWallet(new CreateWalletRequest(user.getId()));
        return signUpMapper.toResponseDto(user);

    }


    @Override
    public SignUpResponse registerAdmin(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new DuplicateEmailException("Email is already registered.");
        }

        Users admin = new Users();
        admin.setEmail(signUpRequest.getEmail());
        admin.setFullName(signUpRequest.getFullName());
        admin.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));
        admin.setRole(UserRole.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        userRepository.save(admin);
        walletService.initializeWallet(new CreateWalletRequest(admin.getId()));//initializing a wallet by giving an id from here.
        return signUpMapper.toResponseDto(admin);
    }

    @Override
    public PagedUserResponse showUsers(int page, int size, String sortBy, String direction) {
        int pageNumber = Math.max(0, page - 1);
        //pageRequest is 0 indexing.but we want page=1 so to align we subtracted.
        int pageSize = Math.max(1, size);
        //page size should be at-least 1 that's why started from 1.
        //Direction is  enum{asc,desc}.

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        //fromString is case-insensitive it takes string as/desc and converts into enum constant.
        //Sort is class.Direction is static enum.fromString is a static method.
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));

        Page<Users> usersPage = userRepository.findByRole(UserRole.PLAYER, pageable);
        List<UserResponse> userDtos = usersPage.map(userMapper::toResponseDto).getContent();

        return new PagedUserResponse(
                userDtos,//object
                usersPage.getNumber() + 1, //for readability  of users.// convert back to 1-based
                usersPage.getTotalPages(),

                usersPage.getTotalElements()
        );
    }

    @Override
    public CompleteUserResponse getUserById(Long id, Pageable pageable) {

        Users user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        Page<WalletTransaction> transactions = walletTransactionRepository
                .findByWallet_User_IdOrderByCreatedAtDesc(id, pageable);

        List<WalletTransactionResponse> transactionDtos =
                transactions.map(walletTransactionMapper::toResponseDto).getContent();
        PagedWalletTransactionResponse pagedWalletTransactionResponse = new PagedWalletTransactionResponse(
                transactionDtos,
                transactions.getNumber() + 1,
                transactions.getTotalPages(),
                transactions.getTotalElements()
        );
        List<Bet> bets = betRepository.findByUser_IdOrderByIdDesc(id);
        CompleteUserResponse response = new CompleteUserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setBalance(wallet.getBalance());
        response.setPagedWalletTransactionResponse(pagedWalletTransactionResponse);
        response.setBets(bets.stream().map(betMapper::toResponse).toList());

        return response;
    }

}

