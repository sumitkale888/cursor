package com.cursor.backend.service;

import com.cursor.backend.dto.AddressRequest;
import com.cursor.backend.dto.AddressResponse;
import com.cursor.backend.entity.Address;
import com.cursor.backend.entity.User;
import com.cursor.backend.exception.ResourceNotFoundException;
import com.cursor.backend.mapper.EntityMapper;
import com.cursor.backend.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(String email) {
        User user = userService.getUserByEmail(email);
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId()).stream()
                .map(entityMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddressById(String email, Long addressId) {
        User user = userService.getUserByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        return entityMapper.toAddressResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(String email, AddressRequest request) {
        User user = userService.getUserByEmail(email);

        if (request.isDefault()) {
            clearDefaultAddress(user.getId());
        } else if (addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId()).isEmpty()) {
            request.setDefault(true);
        }

        Address address = buildAddress(user, request);
        Address saved = addressRepository.save(address);
        log.info("Created address id={} for user {}", saved.getId(), email);
        return entityMapper.toAddressResponse(saved);
    }

    @Transactional
    public AddressResponse updateAddress(String email, Long addressId, AddressRequest request) {
        User user = userService.getUserByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (request.isDefault()) {
            clearDefaultAddress(user.getId());
        }

        applyAddressRequest(address, request);
        Address saved = addressRepository.save(address);
        log.info("Updated address id={} for user {}", saved.getId(), email);
        return entityMapper.toAddressResponse(saved);
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = userService.getUserByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        addressRepository.delete(address);
        log.info("Deleted address id={} for user {}", addressId, email);
    }

    @Transactional(readOnly = true)
    public Address getUserAddress(String email, Long addressId) {
        User user = userService.getUserByEmail(email);
        return addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
    }

    private void clearDefaultAddress(Long userId) {
        addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                .filter(Address::isDefault)
                .forEach(address -> {
                    address.setDefault(false);
                    addressRepository.save(address);
                });
    }

    private Address buildAddress(User user, AddressRequest request) {
        return Address.builder()
                .user(user)
                .label(request.getLabel())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .isDefault(request.isDefault())
                .build();
    }

    private void applyAddressRequest(Address address, AddressRequest request) {
        address.setLabel(request.getLabel());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
        address.setDefault(request.isDefault());
    }
}
