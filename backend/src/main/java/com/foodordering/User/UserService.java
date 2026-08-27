package com.foodordering.User;

import com.foodordering.User.dto.*;
import com.foodordering.User.entity.SavedAddress;
import com.foodordering.User.entity.User;
import com.foodordering.User.repository.SavedAddressRepository;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.common.exception.ConflictException;
import com.foodordering.common.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SavedAddressRepository savedAddressRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            SavedAddressRepository savedAddressRepository,
            PasswordEncoder passwordEncoder,
        this.userRepository = userRepository;
        this.savedAddressRepository = savedAddressRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = findUserById(userId);
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        String newEmail = request.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail)) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new ConflictException("Email already in use by another account");
            }
            user.setEmail(newEmail);
        }

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setFullName(request.getFirstName().trim() + " " + request.getLastName().trim());
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);


    @Transactional(readOnly = true)
    public List<SavedAddressDto> getSavedAddresses(UUID userId) {
        return savedAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(SavedAddressDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public SavedAddressDto createSavedAddress(UUID userId, SavedAddressRequest request) {
        findUserById(userId); // ensure user exists

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            savedAddressRepository.clearDefaultAddressesForUser(userId);
        }

        // If it's the user's very first address, automatically make it default
        List<SavedAddress> existing = savedAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault()) || existing.isEmpty();

        SavedAddress address = new SavedAddress();
        address.setUserId(userId);
        address.setLabel(request.getLabel().trim());
        address.setAddress(request.getAddress().trim());
        address.setBuildingName(request.getBuildingName() != null ? request.getBuildingName().trim() : null);
        address.setApartmentNumber(request.getApartmentNumber() != null ? request.getApartmentNumber().trim() : null);
        address.setLandmarks(request.getLandmarks() != null ? request.getLandmarks().trim() : null);
        address.setDeliveryInstructions(request.getDeliveryInstructions() != null ? request.getDeliveryInstructions().trim() : null);
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setDefault(makeDefault);

        return new SavedAddressDto(savedAddressRepository.save(address));
    }

    @Transactional
    public SavedAddressDto updateSavedAddress(UUID userId, UUID addressId, SavedAddressRequest request) {
        SavedAddress address = savedAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved address not found"));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            savedAddressRepository.clearDefaultAddressesForUser(userId);
            address.setDefault(true);
        } else if (Boolean.FALSE.equals(request.getIsDefault())) {
            address.setDefault(false);
        }

        address.setLabel(request.getLabel().trim());
        address.setAddress(request.getAddress().trim());
        address.setBuildingName(request.getBuildingName() != null ? request.getBuildingName().trim() : null);
        address.setApartmentNumber(request.getApartmentNumber() != null ? request.getApartmentNumber().trim() : null);
        address.setLandmarks(request.getLandmarks() != null ? request.getLandmarks().trim() : null);
        address.setDeliveryInstructions(request.getDeliveryInstructions() != null ? request.getDeliveryInstructions().trim() : null);
        if (request.getLatitude() != null) address.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) address.setLongitude(request.getLongitude());

        return new SavedAddressDto(savedAddressRepository.save(address));
    }

    @Transactional
    public void deleteSavedAddress(UUID userId, UUID addressId) {
        SavedAddress address = savedAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved address not found"));

        savedAddressRepository.delete(address);
    }

    @Transactional
    public SavedAddressDto setDefaultAddress(UUID userId, UUID addressId) {
        SavedAddress address = savedAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved address not found"));

        savedAddressRepository.clearDefaultAddressesForUser(userId);
        address.setDefault(true);
        return new SavedAddressDto(savedAddressRepository.save(address));
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}

